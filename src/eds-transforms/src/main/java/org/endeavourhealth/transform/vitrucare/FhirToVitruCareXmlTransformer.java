package org.endeavourhealth.transform.vitrucare;

import OpenPseudonymiser.Crypto;
import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.HasResourceDataJson;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.data.transform.VitruCareRepository;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.core.utility.Resources;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.FhirToXTransformerBase;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.endeavourhealth.transform.vitrucare.model.ClinicalTerm;
import org.endeavourhealth.transform.vitrucare.model.ObjectFactory;
import org.endeavourhealth.transform.vitrucare.model.Payload;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class FhirToVitruCareXmlTransformer extends FhirToXTransformerBase {

    private static final Logger LOG = LoggerFactory.getLogger(FhirToVitruCareXmlTransformer.class);

    private static final String XSD = "VitruCare.xsd";
    private static final String PSEUDO_KEY_NHS_NUMBER = "NHSNumber";
    private static final String PSEUDO_KEY_DATE_OF_BIRTH = "DOB";
    private static final String PSEUDO_SALT_RESOURCE = "VitruCare - Leeds.EncryptedSalt";

    private static ResourceRepository resourceRepository = new ResourceRepository();
    private static VitruCareRepository vitruCareRepository = new VitruCareRepository();
    private static ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
    private static byte[] saltBytes = null;

    public static String transformFromFhir(UUID batchId,
                                           Map<ResourceType, List<UUID>> resourceIds) throws Exception {

        //retrieve our resources
        List<ResourceByExchangeBatch> filteredResources = getResources(batchId, resourceIds);
        if (filteredResources.isEmpty()) {
            return null;
        }

        //deserialise any patient-facing resources
        List<ResourceByExchangeBatch> patientResourceWrappers = new ArrayList<>();
        boolean containsDeletes = false;
        UUID exchangeId = null;

        for (ResourceByExchangeBatch resourceBatchEntry: filteredResources) {
            String typeString = resourceBatchEntry.getResourceType();
            ResourceType type = ResourceType.valueOf(typeString);
            if (!FhirResourceFiler.isPatientResource(type)) {
                continue;
            }

            patientResourceWrappers.add(resourceBatchEntry);

            if (resourceBatchEntry.getIsDeleted()) {
                containsDeletes = true;
            }

            //just get this off any of them, since it'll be the same for all
            if (exchangeId == null) {
                exchangeId = resourceBatchEntry.getExchangeId();
            }
        }

        //if there's no patient resources, just return null since there's nothing to send on
        if (patientResourceWrappers.isEmpty()) {
            return null;
        }

        //find a patient ID from something we've received
        UUID edsPatientId = findPatientId(batchId, exchangeId);
        //String edsPatientId = findEdsPatientIdFromResources(patientResourceWrappers);
        if (edsPatientId == null) {
            //if there's no EDS patient ID for this data, just return out
            return null;
        }

        //see if we've sent data to vitrucare before or not
        String vitruCareId = findVitruCareId(edsPatientId);

        if (Strings.isNullOrEmpty(vitruCareId)) {
            return createInitialPayload(edsPatientId);

        } else if (containsDeletes) {
            return createReplacePayload(vitruCareId, edsPatientId);

        } else {
            return createUpdatePayload(vitruCareId, patientResourceWrappers);
        }
    }

    private static UUID findPatientId(UUID batchId, UUID exchangeId) throws Exception {

        ExchangeBatch exchangeBatch = exchangeBatchRepository.getForExchangeAndBatchId(exchangeId, batchId);
        if (exchangeBatch == null) {
            return null;

        } else {
            return exchangeBatch.getEdsPatientId();
        }
    }
    /*private static String findEdsPatientIdFromResources(List<ResourceByExchangeBatch> patientResourceWrappers) throws Exception {

        //TODO - need better way to find patient ID from a batch ID

        //go through what we've received and see if we can find a patient ID from there
        for (ResourceByExchangeBatch batchEntry: patientResourceWrappers) {
            if (!batchEntry.getIsDeleted()) {
                Resource fhir = FhirResourceHelper.deserialiseResouce(batchEntry);
                return IdHelper.getPatientId(fhir);
            }
        }

        //if everything in our batch is deleted, we need to look at past instances of the same resources we received
        for (ResourceByExchangeBatch batchEntry: patientResourceWrappers) {

            String resourceType = batchEntry.getResourceType();
            UUID resourceId = batchEntry.getResourceId();
            List<ResourceHistory> history = resourceRepository.getResourceHistory(resourceType, resourceId);

            //work back through the history to find a non-deleted instance, which will allow us to find the EDS patient ID
            for (int i=history.size()-1; i>=0; i--) {
                ResourceHistory historyEntry = history.get(i);
                if (historyEntry.getIsDeleted()) {
                    continue;
                }

                Resource fhir = FhirResourceHelper.deserialiseResouce(historyEntry);
                return IdHelper.getPatientId(fhir);
            }
        }

        //we've got some data that's only ever deleted, so we never had a non-deleted instance to look back on,
        //in which case just return null to not send anything out
        return null;
        //throw new TransformException("Failed to find EDS patient ID for batch");
    }*/

    private static String createUpdatePayload(String vitruCareId, List<ResourceByExchangeBatch> patientResources) throws Exception {

        Payload payload = new Payload();
        payload.setPatientGUID(vitruCareId);

        populatePayloadClinicals(payload, patientResources);

        //if we've not actually added anything, just return null as there's nothing to send
        if (payload.getMedication().isEmpty()
                && payload.getClinicalTerm().isEmpty()) {
            return null;
        }

        JAXBElement element = new ObjectFactory().createPatientUpdate(payload);
        return XmlSerializer.serializeToString(element, XSD);
    }

    private static String createReplacePayload(String vitruCareId, UUID edsPatientId) throws Exception {

        Payload payload = new Payload();
        if (!populateFullPayload(payload, edsPatientId, vitruCareId)) {
            return null;
        }

        JAXBElement element = new ObjectFactory().createPatientReplace(payload);
        return XmlSerializer.serializeToString(element, XSD);
    }

    private static String createInitialPayload(UUID edsPatientId) throws Exception {

        //if we don't have a VitruCare ID, we need to get the full record from the DB to send a full payload
        Payload payload = new Payload();
        if (!populateFullPayload(payload, edsPatientId, null)) {
            return null;
        }

        JAXBElement element = new ObjectFactory().createPatientCreate(payload);
        return XmlSerializer.serializeToString(element, XSD);
    }


    private static boolean populateFullPayload(Payload payload, UUID edsPatientId, String vitruCareId) throws Exception {

        ResourceHistory patientResourceWrapper = resourceRepository.getCurrentVersion(ResourceType.Patient.toString(), edsPatientId);
        if (patientResourceWrapper.getIsDeleted()) {
            return false;
        }

        UUID serviceId = patientResourceWrapper.getServiceId();
        UUID systemId = patientResourceWrapper.getSystemId();
        Patient fhirPatient = (Patient)FhirResourceHelper.deserialiseResouce(patientResourceWrapper);

        //if we don't have a VitruCare ID, generate one from our patient
        if (vitruCareId == null) {
            vitruCareId = createVitruCareId(fhirPatient);
            vitruCareRepository.saveVitruCareIdMapping(edsPatientId, serviceId, systemId, vitruCareId);
        }
        payload.setPatientGUID(vitruCareId);

        if (fhirPatient.hasBirthDate()) {
            Date dob = fhirPatient.getBirthDate();
            payload.setDateofbirth(convertDate(dob));
        }

        if (fhirPatient.hasGender()) {
            Enumerations.AdministrativeGender gender = fhirPatient.getGender();
            payload.setGender(convertGender(gender));
        }

        List<ResourceByPatient> resourceByPatients = resourceRepository.getResourcesByPatient(serviceId, systemId, edsPatientId);
        populatePayloadClinicals(payload, resourceByPatients);

        return true;
    }

    private static <T extends HasResourceDataJson> void populatePayloadClinicals(Payload payload, List<T> resourceWrappers) throws Exception {

        for (HasResourceDataJson resourceWrapper: resourceWrappers) {

            Resource resource = FhirResourceHelper.deserialiseResouce(resourceWrapper);
            ResourceType resourceType = resource.getResourceType();

            if (resourceType == ResourceType.MedicationOrder) {
                org.endeavourhealth.transform.vitrucare.model.Medication medication = createMedication((MedicationOrder)resource);
                payload.getMedication().add(medication);

            } else if (resourceType == ResourceType.Observation) {
                ClinicalTerm clinicalTerm = createClinicalTerm((Observation)resource);
                payload.getClinicalTerm().add(clinicalTerm);

            } else if (resourceType == ResourceType.Condition) {
                ClinicalTerm clinicalTerm = createClinicalTerm((Condition) resource);
                payload.getClinicalTerm().add(clinicalTerm);

            } else {
                //VitruCare aren't interested in any other resource types
            }
        }
    }

    private static org.endeavourhealth.transform.vitrucare.model.Medication createMedication(MedicationOrder fhir) throws Exception {

        String productCode = null;
        String productName = null;
        String dose = null;
        String quantity = null;
        Date startDate = null;
        Date endDate = null;

        CodeableConcept fhirCodeableConcept = fhir.getMedicationCodeableConcept();
        Long conceptId = findSnomedConceptId(fhirCodeableConcept);

        if (conceptId != null) {
            productCode = conceptId.toString();
        }

        productName = fhirCodeableConcept.getText();

        if (fhir.hasDosageInstruction()) {
            if (fhir.getDosageInstruction().size() > 1) {
                throw new TransformException("Cannot support MedicationStatements with more than one dose " + fhir.getId());
            }

            MedicationOrder.MedicationOrderDosageInstructionComponent doseage = fhir.getDosageInstruction().get(0);
            dose = doseage.getText();
        }

        if (fhir.hasDateWrittenElement()) {
            DateTimeType dt = fhir.getDateWrittenElement();
            startDate = dt.getValue();
        }

        if (fhir.hasDispenseRequest()) {
            MedicationOrder.MedicationOrderDispenseRequestComponent dispenseRequestComponent = fhir.getDispenseRequest();
            Quantity q = dispenseRequestComponent.getQuantity();
            quantity = q.getValue() + " " + q.getUnit();

            Duration duration = dispenseRequestComponent.getExpectedSupplyDuration();
            if (!duration.getUnit().equalsIgnoreCase("days")) {
                throw new TransformException("Unsupported medication order duration type [" + duration.getUnit() + "] for " + fhir.getId());
            }
            int days = duration.getValue().intValue();
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            c.add(Calendar.DAY_OF_YEAR, days);
            endDate = c.getTime();
        }

        org.endeavourhealth.transform.vitrucare.model.Medication ret = new org.endeavourhealth.transform.vitrucare.model.Medication();
        ret.setProductId(productCode);
        ret.setProductName(productName);
        ret.setDose(dose);
        ret.setQuanity(quantity);
        ret.setStartDate(convertDate(startDate));
        ret.setEndDate(convertDate(endDate));

        return ret;
    }

    protected static Long findSnomedConceptId(CodeableConcept code) {
        for (Coding coding: code.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
                return Long.parseLong(coding.getCode());
            }
        }

        return null;
    }

    private static ClinicalTerm createClinicalTerm(Observation fhir) throws Exception {

        String code = null;
        String term = null;
        BigDecimal value = null;
        String units = null;
        BigDecimal lowerBounds = null;
        BigDecimal upperBounds = null;
        Date startDate = null;
        Date endDate = null; //not assigned

        CodeableConcept fhirCodeableConcept = fhir.getCode();
        code = findSnomedConceptId(fhirCodeableConcept).toString();
        term = fhirCodeableConcept.getText();

        if (fhir.hasEffectiveDateTimeType()) {
            DateTimeType dt = fhir.getEffectiveDateTimeType();
            startDate = dt.getValue();
        }

        if (fhir.hasValue()) {
            Quantity quantity = fhir.getValueQuantity();
            value = quantity.getValue();
            units = quantity.getUnit();
        }

        if (fhir.hasReferenceRange()) {
            if (fhir.getReferenceRange().size() > 1) {
                throw new TransformException("Cannot support Observations with more than one reference range " + fhir.getId());
            }

            Observation.ObservationReferenceRangeComponent fhirReferenceRange = fhir.getReferenceRange().get(0);
            if (fhirReferenceRange.hasLow()) {
                Quantity quantity = fhirReferenceRange.getLow();
                lowerBounds = quantity.getValue();
            }
            if (fhirReferenceRange.hasHigh()) {
                Quantity quantity = fhirReferenceRange.getHigh();
                upperBounds = quantity.getValue();
            }
        }

        ClinicalTerm ret = new ClinicalTerm();
        ret.setCode(code);
        ret.setDescription(term);
        ret.setValue(value);
        ret.setUnits(units);
        ret.setLowerRecommendedBound(lowerBounds);
        ret.setUpperRecommendedBound(upperBounds);
        ret.setStartDate(convertDate(startDate));
        ret.setEndDate(convertDate(endDate));
        return ret;
    }

    private static ClinicalTerm createClinicalTerm(Condition fhir) throws Exception {

        String code = null;
        String term = null;
        BigDecimal value = null; //not assigned
        String units = null; //not assigned
        BigDecimal lowerBounds = null; //not assigned
        BigDecimal upperBounds = null; //not assigned
        Date startDate = null;
        Date endDate = null;

        CodeableConcept fhirCodeableConcept = fhir.getCode();
        code = findSnomedConceptId(fhirCodeableConcept).toString();
        term = fhirCodeableConcept.getText();

        if (fhir.hasOnsetDateTimeType()) {
            DateTimeType dt = fhir.getOnsetDateTimeType();
            startDate = dt.getValue();
        }

        if (fhir.hasAbatementDateTimeType()) {
            DateTimeType dt = fhir.getAbatementDateTimeType();
            endDate = dt.getValue();
        }

        ClinicalTerm ret = new ClinicalTerm();
        ret.setCode(code);
        ret.setDescription(term);
        ret.setValue(value);
        ret.setUnits(units);
        ret.setLowerRecommendedBound(lowerBounds);
        ret.setUpperRecommendedBound(upperBounds);
        ret.setStartDate(convertDate(startDate));
        ret.setEndDate(convertDate(endDate));
        return ret;
    }


    private static String convertGender(Enumerations.AdministrativeGender fhirGender) throws Exception {
        if (fhirGender == Enumerations.AdministrativeGender.FEMALE) {
            return "F";
        } else if (fhirGender == Enumerations.AdministrativeGender.MALE) {
            return "M";
        } else if (fhirGender == Enumerations.AdministrativeGender.UNKNOWN) {
            return "U";
        } else if (fhirGender == Enumerations.AdministrativeGender.OTHER) {
            return "I";
        } else {
            throw new TransformException("Unhandled FHIR gender " + fhirGender);
        }
    }

    private static XMLGregorianCalendar convertDate(Date date) throws Exception {
        if (date == null) {
            return null;
        }
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }


    private static String findVitruCareId(UUID edsPatientId) {
        return vitruCareRepository.getVitruCareId(edsPatientId);
    }

    private static String createVitruCareId(Patient fhirPatient) throws Exception {

        String nhsNumber = IdentifierHelper.findNhsNumber(fhirPatient);

        String dob = null;
        if (fhirPatient.hasBirthDate()) {
            Date d = fhirPatient.getBirthDate();
            dob = new SimpleDateFormat("dd-MM-yyyy").format(d);
        }

        //if we don't have either of these values, we can't generate a pseudo ID
        if (Strings.isNullOrEmpty(nhsNumber)
                || Strings.isNullOrEmpty(dob)) {
            return "";
        }

        TreeMap keys = new TreeMap();
        keys.put(PSEUDO_KEY_DATE_OF_BIRTH, dob);
        keys.put(PSEUDO_KEY_NHS_NUMBER, nhsNumber);

        Crypto crypto = new Crypto();
        crypto.SetEncryptedSalt(getEncryptedSalt());
        return crypto.GetDigest(keys);
    }

    private static byte[] getEncryptedSalt() throws Exception {
        if (saltBytes == null) {
            saltBytes = Resources.getResourceAsBytes(PSEUDO_SALT_RESOURCE);
        }
        return saltBytes;
    }

}
