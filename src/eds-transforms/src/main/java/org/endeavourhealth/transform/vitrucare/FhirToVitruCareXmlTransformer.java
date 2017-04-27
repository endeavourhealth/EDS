package org.endeavourhealth.transform.vitrucare;

import OpenPseudonymiser.Crypto;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.CodeableConceptHelper;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.IdentifierHelper;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.HasResourceDataJson;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.data.transform.VitruCareRepository;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.FhirToXTransformerBase;
import org.endeavourhealth.transform.common.exceptions.TransformException;
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
    //private static final String PSEUDO_SALT_RESOURCE = "VitruCare - Leeds.EncryptedSalt";

    private static ResourceRepository resourceRepository = new ResourceRepository();
    private static VitruCareRepository vitruCareRepository = new VitruCareRepository();
    private static ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
    //private static byte[] saltBytes = null;
    private static Set<String> snomedCodeSet = null;
    private static Set<String> emisCodeSet = null;
    private static Map<String, byte[]> saltCacheMap = new HashMap<>();

    public static String transformFromFhir(UUID batchId,
                                           Map<ResourceType, List<UUID>> resourceIds, String configName) throws Exception {

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
            return createInitialPayload(edsPatientId, configName);

        } else if (containsDeletes) {
            return createReplacePayload(vitruCareId, edsPatientId, configName);

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

    private static String createReplacePayload(String vitruCareId, UUID edsPatientId, String configName) throws Exception {

        Payload payload = new Payload();
        if (!populateFullPayload(payload, edsPatientId, vitruCareId, configName)) {
            return null;
        }

        JAXBElement element = new ObjectFactory().createPatientReplace(payload);
        return XmlSerializer.serializeToString(element, XSD);
    }

    private static String createInitialPayload(UUID edsPatientId, String configName) throws Exception {

        //if we don't have a VitruCare ID, we need to get the full record from the DB to send a full payload
        Payload payload = new Payload();
        if (!populateFullPayload(payload, edsPatientId, null, configName)) {
            return null;
        }

        JAXBElement element = new ObjectFactory().createPatientCreate(payload);
        return XmlSerializer.serializeToString(element, XSD);
    }


    private static boolean populateFullPayload(Payload payload, UUID edsPatientId, String vitruCareId, String configName) throws Exception {

        ResourceHistory patientResourceWrapper = resourceRepository.getCurrentVersion(ResourceType.Patient.toString(), edsPatientId);
        if (patientResourceWrapper == null
            || patientResourceWrapper.getIsDeleted()) {
            return false;
        }

        UUID serviceId = patientResourceWrapper.getServiceId();
        UUID systemId = patientResourceWrapper.getSystemId();
        Patient fhirPatient = (Patient)FhirResourceHelper.deserialiseResouce(patientResourceWrapper);

        //if we don't have a VitruCare ID, generate one from our patient
        if (vitruCareId == null) {
            vitruCareId = createVitruCareId(fhirPatient, configName);
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

            if (resourceType == ResourceType.MedicationStatement) {
                org.endeavourhealth.transform.vitrucare.model.Medication medication = createMedication((MedicationStatement)resource);
                if (medication != null) {
                    payload.getMedication().add(medication);
                }

            } else if (resourceType == ResourceType.Observation) {
                ClinicalTerm clinicalTerm = createClinicalTerm((Observation)resource);
                if (clinicalTerm != null) {
                    payload.getClinicalTerm().add(clinicalTerm);
                }

            } else if (resourceType == ResourceType.Condition) {
                //I THINK that all the codes in the data set would fall into being observation codes,
                //but it can't hurt to handle the case if some end up as Conditions
                ClinicalTerm clinicalTerm = createClinicalTerm((Condition)resource);
                if (clinicalTerm != null) {
                    payload.getClinicalTerm().add(clinicalTerm);
                }

            } else {
                //VitruCare aren't interested in any other resource types
            }
        }
    }

    private static org.endeavourhealth.transform.vitrucare.model.Medication createMedication(MedicationStatement fhir) throws Exception {

        //they're only interested in repeat meds
        /*Extension extension = ExtensionConverter.findExtension(fhir, FhirExtensionUri.MEDICATION_AUTHORISATION_TYPE);
        if (extension != null) {
            Coding coding = (Coding)extension.getValue();
            String code = coding.getCode();
            if (code.equals(MedicationAuthorisationType.ACUTE.getCode())) {
                return null;
            }
        }*/

        CodeableConcept fhirCodeableConcept = fhir.getMedicationCodeableConcept();
        Long conceptId = CodeableConceptHelper.findSnomedConceptId(fhirCodeableConcept);

        if (conceptId == null) {
            LOG.warn("Failed to find snomed concept for " + fhir.getResourceType() + " " + fhir.getId());
            return null;
        }

        String productCode = null;
        String productName = null;
        String dose = null;
        String quantity = null;
        Date startDate = null;
        Date endDate = null;

        productCode = conceptId.toString();
        productName = fhirCodeableConcept.getText();

        if (fhir.hasDosage()) {
            if (fhir.getDosage().size() > 1) {
                throw new TransformException("Cannot support MedicationStatements with more than one dose " + fhir.getId());
            }

            MedicationStatement.MedicationStatementDosageComponent doseage = fhir.getDosage().get(0);
            dose = doseage.getText();
        }

        if (fhir.hasDateAsserted()) {
            startDate = fhir.getDateAsserted();
        }

        Extension quantityExtension = ExtensionConverter.findExtension(fhir, FhirExtensionUri.MEDICATION_AUTHORISATION_QUANTITY);
        if (quantityExtension != null) {
            Quantity q = (Quantity)quantityExtension.getValue();
            quantity = q.getValue() + " " + q.getUnit();
        }

        Extension cancellationExtension = ExtensionConverter.findExtension(fhir, FhirExtensionUri.MEDICATION_AUTHORISATION_CANCELLATION);
        if (cancellationExtension != null) {
            if (cancellationExtension.hasExtension()) {
                for (Extension innerExtension: cancellationExtension.getExtension()) {
                    if (innerExtension.getValue() instanceof DateType) {
                        DateType d = (DateType)innerExtension.getValue();
                        endDate = d.getValue();
                    }
                }
            }
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

    private static ClinicalTerm createClinicalTerm(Observation fhir) throws Exception {

        //find what code we should use to send this clinical item under
        String snomedOrEmisCode = findCodeToInclude(fhir.getCode());
        if (Strings.isNullOrEmpty(snomedOrEmisCode)) {
            return null;
        }

        String code = null;
        String term = null;
        BigDecimal value = null;
        String units = null;
        BigDecimal lowerBounds = null;
        BigDecimal upperBounds = null;
        Date startDate = null;
        Date endDate = null; //not assigned

        code = snomedOrEmisCode.toString();
        term = fhir.getCode().getText();

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

        //find what code we should use to send this clinical item under
        String snomedOrEmisCode = findCodeToInclude(fhir.getCode());
        if (Strings.isNullOrEmpty(snomedOrEmisCode)) {
            return null;
        }

        String code = null;
        String term = null;
        BigDecimal value = null; //not assigned
        String units = null; //not assigned
        BigDecimal lowerBounds = null; //not assigned
        BigDecimal upperBounds = null; //not assigned
        Date startDate = null;
        Date endDate = null;

        code = snomedOrEmisCode;
        term = fhir.getCode().getText();

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

    private static String createVitruCareId(Patient fhirPatient, String configName) throws Exception {

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
        crypto.SetEncryptedSalt(getEncryptedSalt(configName));
        return crypto.GetDigest(keys);
    }

    private static byte[] getEncryptedSalt(String configName) throws Exception {

        byte[] ret = saltCacheMap.get(configName);
        if (ret == null) {

            synchronized (saltCacheMap) {
                ret = saltCacheMap.get(configName);
                if (ret == null) {

                    JsonNode config = ConfigManager.getConfigurationAsJson(configName, "enterprise");
                    JsonNode saltNode = config.get("salt");
                    if (saltNode == null) {
                        throw new Exception("No 'Salt' element found in Enterprise config " + configName);
                    }
                    String base64Salt = saltNode.asText();
                    ret = Base64.getDecoder().decode(base64Salt);
                    saltCacheMap.put(configName, ret);
                }
            }
        }
        return ret;
    }
    /*private static byte[] getEncryptedSalt() throws Exception {
        if (saltBytes == null) {
            saltBytes = Resources.getResourceAsBytes(PSEUDO_SALT_RESOURCE);
        }
        return saltBytes;
    }*/

    private static String findCodeToInclude(CodeableConcept codeableConcept) {

        Long snomedConcept = CodeableConceptHelper.findSnomedConceptId(codeableConcept);
        if (snomedConcept != null) {
            String snomedConceptStr = snomedConcept.toString();
            if (getSnomedCodeSet().contains(snomedConceptStr)) {
                return snomedConceptStr;
            }
        }

        String originalCode = CodeableConceptHelper.findOriginalCode(codeableConcept);
        if (!Strings.isNullOrEmpty(originalCode)) {
            if (getEmisCodeSet().contains(originalCode)) {
                return originalCode;
            }
        }

        return null;
    }

    /*private static boolean shouldInclude(CodeableConcept codeableConcept) {
        Long snomedConcept = CodeableConceptHelper.findSnomedConceptId(codeableConcept);
        if (snomedConcept == null) {
            return false;
        }
        String snomedConceptStr = snomedConcept.toString();
        return getSnomedCodeSet().contains(snomedConceptStr);
    }*/

    private static Set<String> getEmisCodeSet() {
        if (emisCodeSet == null) {
            Set<String> set = new HashSet<>();
            
            set.add("EMISNQCY69"); //2184delA – mutation 1
            set.add("EMISNQCY71"); //278+5G>A – mutation 1
            set.add("EMISNQCY73"); //3120+G>A – mutation 1
            set.add("EMISNQCY75"); //2659delC – mutation 1
            set.add("EMISNQCY77"); //A455E– mutation 1
            set.add("EMISNQCY79"); //G551D – mutation 1
            set.add("EMISNQCY81"); //G542X – mutation 1
            set.add("EMISNQCY83"); //G85E– mutation 1
            set.add("EMISNQCY85"); //N1303K – mutation 1
            set.add("EMISNQCY87"); //R117H– mutation 1
            set.add("EMISNQCY89"); //R1162X– mutation 1
            set.add("EMISNQCY91"); //R347P– mutation 1
            set.add("EMISNQCY93"); //R334W– mutation 1
            set.add("EMISNQCY95"); //R3553X– mutation 1
            set.add("EMISNQCY97"); //R560T– mutation 1
            set.add("EMISNQCY99"); //S549N– mutation 1
            set.add("EMISNQCY101"); //1078delT– mutation 1
            set.add("EMISNQCY103"); //1717-1G>A– mutation 1
            set.add("EMISNQCY105"); //1898+1G>A– mutation 1
            set.add("EMISNQCY107"); //621+1G>T– mutation 1
            set.add("EMISNQCY109"); //711+1G>T T– mutation 1
            set.add("EMISNQCY111"); //V392G – mutation 1
            set.add("EMISNQCY113"); //V520F– mutation 1
            set.add("EMISNQCY115"); //W1282X– mutation 1
            set.add("EMISNQCY117"); //deltaI507– mutation 1
            set.add("EMISNQCY119"); //2183AA->G– mutation 1
            set.add("EMISNQCY121"); //3849+10kbC>T– mutation 1
            set.add("EMISNQCY123"); //3876delA– mutation 1
            set.add("EMISNQCY125"); //3905insT– mutation 1
            set.add("EMISNQCY127"); //394delTT– mutation 1
            set.add("EMISNQCY129"); //Q493X– mutation 1
            set.add("EMISNQCY131"); //S549R (A>C) – mutation 1
            set.add("EMISNQCY133"); //I148T– mutation 1
            set.add("EMISNQCY135"); //R347H– mutation 1
            set.add("EMISNQCY137"); //DeltaF508– mutation 1


            set.add("EMISNQCY70"); //2184delA – mutation 2
            set.add("EMISNQCY72"); //278+5G>A – mutation 2
            set.add("EMISNQCY74"); //3120+G>A – mutation 2
            set.add("EMISNQCY76"); //2659delC – mutation 2
            set.add("EMISNQCY78"); //A455E– mutation 2
            set.add("EMISNQCY80"); //G551D– mutation 2
            set.add("EMISNQCY82"); //G542X – mutation 2
            set.add("EMISNQCY84"); //G85E– mutation 2
            set.add("EMISNQCY86"); //N1303K – mutation 2
            set.add("EMISNQCY88"); //R117H– mutation 2
            set.add("EMISNQCY90"); //R1162X– mutation 2
            set.add("EMISNQCY92"); //R347P– mutation 2
            set.add("EMISNQCY94"); //R334W– mutation 2
            set.add("EMISNQCY96"); //R3553X– mutation 2
            set.add("EMISNQCY98"); //R560T– mutation 2
            set.add("EMISNQCY100"); //S549N– mutation 2
            set.add("EMISNQCY102"); //1078delT– mutation 2
            set.add("EMISNQCY104"); //1717-1G>A– mutation 2
            set.add("EMISNQCY106"); //1898+1G>A– mutation 2
            set.add("EMISNQCY108"); //621+1G>T– mutation 2
            set.add("EMISNQCY110"); //711+1G>T T– mutation 2
            set.add("EMISNQCY112"); //V392G– mutation 2
            set.add("EMISNQCY114"); //V520F– mutation 2
            set.add("EMISNQCY116"); //W1282X– mutation 2
            set.add("EMISNQCY118"); //deltaI507– mutation 2
            set.add("EMISNQCY120"); //2183AA->G– mutation 2
            set.add("EMISNQCY122"); //3849+10kbC>T– mutation 2
            set.add("EMISNQCY124"); //3876delA– mutation 2
            set.add("EMISNQCY126"); //3905insT– mutation 2
            set.add("EMISNQCY128"); //394delTT– mutation 2
            set.add("EMISNQCY130"); //Q493X– mutation 2
            set.add("EMISNQCY132"); //S549R (A>C)– mutation 2
            set.add("EMISNQCY134"); //I148T– mutation 2
            set.add("EMISNQCY136"); //R347H– mutation 2
            set.add("EMISNQCY138"); //DeltaF508– mutation 2

            set.add("EMISNQOT6"); //Linked with text

            set.add("EMISNQIN88"); //Intron 8 polythymidine sequence
            set.add("EMISNQ5T1"); //Intron 8 polythymidine sequence – 5T
            set.add("EMISNQ5T2"); //Intron 8 polythymidine sequence – 5T:5T
            set.add("EMISNQ5T3"); //Intron 8 polythymidine sequence – 5T:7T
            set.add("EMISNQ5T4"); //Intron 8 polythymidine sequence – 5T:9T
            set.add("EMISNQ7T1"); //Intron 8 polythymidine sequence – 7T
            set.add("EMISNQ7T2"); //Intron 8 polythymidine sequence – 7T:7T
            set.add("EMISNQ7T3"); //Intron 8 polythymidine sequence – 9T
            set.add("EMISNQ9T1"); //Intron 8 polythymidine sequence – 9T:9T
            set.add("EMISNQ9T2"); //Intron 8 polythymidine sequence – 7T:9T

            emisCodeSet = set;
        }
        return emisCodeSet;
    }

    private static Set<String> getSnomedCodeSet() {
        if (snomedCodeSet == null) {
            Set<String> set = new HashSet<>();

            set.add("401012008"); //Forced Expiratory Volume in 1 second
            set.add("50834005"); //Forced Vital Capacity
            set.add("445210000"); //% predicted Forced Vital Capacity

            set.add("162763007"); //weight
            set.add("162755006"); //height
            set.add("60621009"); //BMI

            set.add("1009081000000104"); // Sweat chloride

            set.add("1000661000000107"); //Serum sodium
            set.add("1017381000000106"); //Plasma sodium level
            set.add("1000651000000109"); //Serum potassium
            set.add("1017401000000106"); //Plasma potassium level
            set.add("1000951000000103"); //Serum urea level
            set.add("1000961000000100"); //Plasma urea level
            set.add("1000731000000107"); //Serum creatinine
            set.add("1000911000000102"); //Plasma creatine level
            set.add("1022431000000105"); //Haemoglobin estimation
            set.add("1022541000000102"); //Total white cell count
            set.add("1022551000000104"); //Neutrophil count
            set.add("1022651000000100"); //Platelet count
            set.add("1001371000000100"); //C Reactive Protein level
            set.add("1018251000000107"); //Serum ALT level

            set.add("1010671000000102"); //Plasma glucose level
            set.add("997671000000106"); //Random blood glucose
            set.add("1003671000000109"); //Haemaglobin A1c level-IFCC standardised

            set.add("1000791000000108"); //Serum vitamin A
            set.add("1000801000000107"); //Serum vitamin E
            set.add("1029801000000105"); //Total 25-hydroxyvitamin D level

            set.add("1006961000000102"); //Serum Tobramycin level
            set.add("1006871000000101"); //Plasma Tobramycin level
            set.add("1006901000000101"); //Serum amikacin level
            set.add("1004741000000108"); //Plasma amikacin level
            set.add("1028951000000109"); //Blood sirolimus level
            set.add("1003761000000107"); //Whole blood sirolimus concentration
            set.add("1026741000000105"); //Serum Tacrolimus
            set.add("1026281000000107"); //Plasma Tacrolimus
            set.add("1008501000000107"); //Whole blood tacrolimus
            set.add("1006721000000107"); //Serum ciclosporin

            /*set.add("457081010"); //Forced Expiratory Volume in 1 second
            set.add("7647410000006116"); //Forced Vital Capacity
            set.add("1141791000000119"); //% predicted Forced Vital Capacity

            set.add("253677014"); //weight
            set.add("253669010"); //height
            set.add("100716012"); //BMI

            set.add("507334018"); // Sweat chloride

            set.add("284446019"); //Serum sodium
            set.add("1484987016"); //Plasma sodium level
            set.add("1448210000006115"); //Serum potassium
            set.add("1484993-12"); //Plasma potassium level
            set.add("409688019"); //Serum urea level
            set.add("456207012"); //Plasma urea level
            set.add("380389013"); //Serum creatinine
            set.add("458156012"); //Plasma creatine level
            set.add("8135510000006113"); //Haemoglobin estimation
            set.add("984610000006116"); //Total white cell count
            set.add("51268016"); //Neutrophil count
            set.add("102928018"); //Platelet count
            set.add("216602012"); //C Reactive Protein level
            set.add("306221000000119"); //Serum ALT level

            set.add("134471000000110"); //Plasma glucose level
            set.add("1917610000006112"); //Random blood glucose
            set.add("17007110000006113"); //Haemaglobin A1c level-IFCC standardised

            set.add("1456910000006116"); //Serum vitamin A
            set.add("157610000006115"); //Serum vitamin E
            set.add("2108191000000115"); //Total 25-hydroxyvitamin D level

            set.add("457877014"); //Serum Tobramycin level
            set.add("458025019"); //Plasma Tobramycin level
            set.add("457476010"); //Serum amikacin level
            set.add("458000011"); //Plasma amikacin level
            set.add("2478462016"); //Blood sirolimus level
            set.add("2341000000118"); //Whole blood sirolimus concentration
            set.add("457522012"); //Serum Tacrolimus
            set.add("14853870118"); //Plasma tacrolimus
            set.add("2313241000000118"); //Whole blood tacrolimus
            set.add("258449018"); //Serum ciclosporin*/

            snomedCodeSet = set;
        }
        return snomedCodeSet;
    }

}
