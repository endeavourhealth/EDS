package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.fhir.schema.ImmunizationStatus;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.transform.terminology.Read2;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class EventTransformer extends ClinicalTransformerBase {



    private static final Logger LOG = LoggerFactory.getLogger(EventTransformer.class);

    //Internal use 0 = text, 1= Observation; 2= Problems; 5=Values; 7=Attachments; 8=Referrals;
    //        10=Alerts; 11=Allergies; 12=Family History; 13=Immunisations; 14=Problem Ratings
    enum ObservationType
    {
        TEXT(0),
        OBSERVATION(1),
        PROBLEM(2),
        VALUE(5),
        ATTACHMENT(7),
        REFERRAL(8),
        ALERT(10),
        ALLERGY(11),
        FAMILYHISTORY(12),
        IMMUNISATION(13),
        PROBLEMRATING(14);

        private final int value;

        ObservationType(final int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return this.value;
        }

        public static ObservationType fromValue(int value)
        {
            for (ObservationType observationType : ObservationType.values())
                if (observationType.getValue() == value)
                    return observationType;

            throw new IllegalArgumentException(Integer.toString(value));
        }
    }

    public static void transform(MedicalRecordType medicalRecordType, List<Resource> results, String patientUuid) throws TransformException {

        //got records that have null event lists
        EventListType eventList = medicalRecordType.getEventList();
        if (eventList == null) {
            return;
        }

        for (EventType eventType : eventList.getEvent()) {
            transform(eventType, results, patientUuid);
        }
    }

    public static void transform(EventType eventType, List<Resource> results, String patientUuid) throws TransformException {

        //check for routing to FHIR based on the code first
        StringCodeType code = eventType.getCode();
        if (code != null) {
            String read2Code = code.getValue();
            if (Read2.isProcedure(read2Code)) {
                transformProcedure(eventType, results, patientUuid);
                return;

            } else if (Read2.isDisorder(read2Code)) {
                transformCondition(eventType, results, patientUuid);
                return;
            }
        }

        switch (ObservationType.fromValue(eventType.getEventType().intValue())) {

            case TEXT:
            case OBSERVATION:
            case PROBLEM:
            case VALUE:
                transformObservation(eventType, results, patientUuid);
                break;
            case ALLERGY:
                AllergyTransformer.transform(eventType, results, patientUuid);
                break;
            case FAMILYHISTORY:
                FamilyHistoryTransformer.transform(eventType, results, patientUuid);
                break;
            case IMMUNISATION:
                transformImmunisation(eventType, results, patientUuid);
                break;
            case REFERRAL:
                ReferralTransformer.transform(eventType, results, patientUuid);
                break;
            case ALERT:
            case ATTACHMENT:
            case PROBLEMRATING:
                break;
            default:
                throw new TransformException("Unhandled event type " + eventType);
        }
    }
    /*public static Resource transform(EventType eventType, String patientUuid) throws TransformException {

        switch (ObservationType.fromValue(eventType.getEventType().intValue())) {

            case TEXT:
            case OBSERVATION:
                return ObservationTransformer.transform(eventType, patientUuid);
            case PROBLEM:
            case VALUE:
                return ObservationTransformer.transform(eventType, patientUuid);
            case ATTACHMENT:
            case REFERRAL:
            case ALERT:
                return null;
            case ALLERGY:
                return AllergyTransformer.transform(eventType, patientUuid);
            case FAMILYHISTORY:
                return FamilyHistoryTransformer.transform(eventType, patientUuid);
            case IMMUNISATION:
                return ImmunisationTransformer.transform(eventType, patientUuid);
            case PROBLEMRATING:
                return null;
            default:
                throw new TransformException("Unhandled event type " + eventType);
        }
    }*/

    public static void transformImmunisation(EventType eventType, List<Resource> results, String patientGuid) throws TransformException
    {
        Immunization fhirImmunization = new Immunization();
        fhirImmunization.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        EmisOpenHelper.setUniqueId(fhirImmunization, patientGuid, eventType.getGUID());

        fhirImmunization.setPatient(EmisOpenHelper.createPatientReference(patientGuid));
        fhirImmunization.setStatus(ImmunizationStatus.COMPLETED.getCode());
        fhirImmunization.setWasNotGiven(false);
        fhirImmunization.setReported(false);

        IdentType author = eventType.getAuthorID();
        if (author != null) {
            fhirImmunization.setPerformer(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        fhirImmunization.setDateElement(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        fhirImmunization.setVaccineCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirImmunization.addNote(AnnotationHelper.createAnnotation(text));
        }

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        addRecordedDateExtension(fhirImmunization, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirImmunization, recordedByGuid);

        List<String> batchNumbers = findQualifierTerms(eventType, EmisOpenHelper.QUALIFIER_GROUP_TERM_BATCH_NUMBER);
        if (!batchNumbers.isEmpty()) {
            String batchNumberStr = String.join(", ", batchNumbers);
            fhirImmunization.setLotNumber(batchNumberStr);
        }

        List<String> sites = findQualifierTerms(eventType, EmisOpenHelper.QUALIFIER_GROUP_TERM_INJECTION_SITE);
        if (!sites.isEmpty()) {
            String siteStr = String.join(", ", sites);
            fhirImmunization.setSite(CodeableConceptHelper.createCodeableConcept(siteStr));
        }

        List<String> expiryDates = findQualifierTerms(eventType, EmisOpenHelper.QUALIFIER_GROUP_TERM_EXPIRY_DATE);
        if (!expiryDates.isEmpty()) {
            if (expiryDates.size() > 1) {
                throw new TransformException("Vaccination " + fhirImmunization.getId() + " has multiple expiry dates");
            }
            Date expiryDate = DateConverter.getDate(expiryDates.get(0));
            fhirImmunization.setExpirationDate(expiryDate);
        }

        List<String> manufacturers = findQualifierTerms(eventType, EmisOpenHelper.QUALIFIER_GROUP_TERM_MANUFACTURER);
        if (!manufacturers.isEmpty()) {
            String manufacturerStr = String.join(", ", manufacturers);

            //the manufacturer is supposed to be a reference to an organisation, so I'm just going to use a contained resource
            String containedOrgId = "Manufacturer";
            fhirImmunization.setManufacturer(ReferenceHelper.createInternalReference(containedOrgId));

            Organization fhirOrg = new Organization();
            fhirOrg.setName(manufacturerStr);
            fhirOrg.setId(containedOrgId);
            fhirImmunization.getContained().add(fhirOrg);
        }


        List<String> gmsStatuses = findQualifierTerms(eventType, EmisOpenHelper.QUALIFIER_GROUP_TERM_GMS);
        if (!gmsStatuses.isEmpty()) {
            String gmsStatus = String.join(", ", gmsStatuses);

            //this should ideally be a new extension to the Immunization resource, but if we're moving away from FHIR, I'm storing here
            fhirImmunization.addNote(AnnotationHelper.createAnnotation("GMS Status: " + gmsStatus));
        }

        linkToProblem(eventType, patientGuid, fhirImmunization, results);

        results.add(fhirImmunization);
    }

    private static void transformObservation(EventType eventType, List<Resource> results, String patientGuid) throws TransformException {

        Observation fhirObservation = new Observation();
        fhirObservation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        String eventGuid = eventType.getGUID();
        EmisOpenHelper.setUniqueId(fhirObservation, patientGuid, eventGuid);

        fhirObservation.setSubject(EmisOpenHelper.createPatientReference(patientGuid));

        fhirObservation.setStatus(org.hl7.fhir.instance.model.Observation.ObservationStatus.UNKNOWN);

        fhirObservation.setEffective(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        fhirObservation.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        IdentType author = eventType.getAuthorID();
        if (author != null) {
            fhirObservation.addPerformer(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        NumericValueType valueType = eventType.getNumericValue();
        if (valueType != null) {
            Double value = valueType.getValue();
            String units = valueType.getUnits();
            fhirObservation.setValue(QuantityHelper.createQuantity(value, units));

            Double low = valueType.getNumericMinimum();
            Double high = valueType.getNumericMaximum();

            if (low != null || high != null) {

                String lowOperator = valueType.getMinRangeOperator();
                String highOperator = valueType.getMaximumRangeOperator();

                Quantity.QuantityComparator lowComparator = null;
                Quantity.QuantityComparator highComparator = null;

                if (Strings.isNullOrEmpty(lowOperator)) {
                    //when we have both min and max value (i.e. between), we don't get any operators, so treat between as inclusive of bounds
                    lowComparator = Quantity.QuantityComparator.GREATER_OR_EQUAL;
                } else if (lowOperator.equals(">")) {
                    lowComparator = Quantity.QuantityComparator.GREATER_THAN;
                } else if (lowOperator.equals(">=")) {
                    lowComparator = Quantity.QuantityComparator.GREATER_OR_EQUAL;
                } else {
                    throw new TransformException("Unsupported min range operator " + lowOperator);
                }

                if (Strings.isNullOrEmpty(highOperator)) {
                    //when we have both min and max value (i.e. between), we don't get any operators, so treat between as inclusive of bounds
                    highComparator = Quantity.QuantityComparator.LESS_OR_EQUAL;
                } else if (highOperator.equals("<")) {
                    highComparator = Quantity.QuantityComparator.LESS_THAN;
                } else if (highOperator.equals("<=")) {
                    highComparator = Quantity.QuantityComparator.LESS_OR_EQUAL;
                } else {
                    throw new TransformException("Unsupported max range operator " + highOperator);
                }

                org.hl7.fhir.instance.model.Observation.ObservationReferenceRangeComponent fhirRange = fhirObservation.addReferenceRange();
                if (low != null) {
                    fhirRange.setLow(QuantityHelper.createSimpleQuantity(low, units, lowComparator));
                }
                if (high != null) {
                    fhirRange.setHigh(QuantityHelper.createSimpleQuantity(high, units, highComparator));
                }
            }
        }

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirObservation.setComments(text);
        }

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        addRecordedDateExtension(fhirObservation, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirObservation, recordedByGuid);

        linkToProblem(eventType, patientGuid, fhirObservation, results);

        results.add(fhirObservation);
    }

    /*public static Observation transform(EventType eventType, String patientGuid) throws TransformException {

        Observation observation = new Observation();
        observation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        EmisOpenHelper.setUniqueId(observation, patientGuid, eventType.getGUID());

        observation.setSubject(EmisOpenHelper.createPatientReference(patientGuid));
        observation.addPerformer(EmisOpenHelper.createPractitionerReference(eventType.getOriginalAuthor().getUser().getGUID()));

        observation.setEffective(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        observation.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        if (eventType.getNumericValue() != null)
        {
            SimpleQuantity simpleQuantity = new SimpleQuantity();
            simpleQuantity.setValue(BigDecimal.valueOf(eventType.getNumericValue().getValue()));
            simpleQuantity.setUnit(eventType.getNumericValue().getUnits());
            observation.setValue(simpleQuantity);
        }

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            observation.setComments(text);
        }

        return observation;
    }*/

    private static void transformCondition(EventType eventType, List<Resource> results, String patientGuid) throws TransformException {

        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

        String eventGuid = eventType.getGUID();
        EmisOpenHelper.setUniqueId(fhirCondition, patientGuid, eventGuid);

        //don't create a duplicate condition if we've already created a problem for this event
        for (Resource resource: results) {
            if (resource instanceof Condition
                    && resource.getId().equals(fhirCondition.getId())) {
                return;
            }
        }

        fhirCondition.setPatient(EmisOpenHelper.createPatientReference(patientGuid));

        IdentType author = eventType.getAuthorID();
        if (author != null) {
            fhirCondition.setAsserter(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        if (dateRecorded != null) {
            fhirCondition.setDateRecorded(dateRecorded);
        }

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirCondition, recordedByGuid);

        fhirCondition.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        fhirCondition.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed
        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        fhirCondition.setOnset(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirCondition.setNotes(text);
        }

        linkToProblem(eventType, patientGuid, fhirCondition, results);

        results.add(fhirCondition);
    }

    private static void transformProcedure(EventType eventType, List<Resource> results, String patientGuid) throws TransformException {
        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE));

        String eventGuid = eventType.getGUID();
        EmisOpenHelper.setUniqueId(fhirProcedure, patientGuid, eventGuid);

        fhirProcedure.setSubject(EmisOpenHelper.createPatientReference(patientGuid));
        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        fhirProcedure.setCode(CodeConverter.convert(eventType.getCode(), eventType.getDisplayTerm()));

        fhirProcedure.setPerformed(DateConverter.convertPartialDateToDateTimeType(eventType.getAssignedDate(), eventType.getAssignedTime(), eventType.getDatePart()));

        String text = eventType.getDescriptiveText();
        if (!Strings.isNullOrEmpty(text)) {
            fhirProcedure.addNotes(AnnotationHelper.createAnnotation(text));
        }

        IdentType author = eventType.getAuthorID();
        if (author != null) {
            Procedure.ProcedurePerformerComponent fhirPerformer = fhirProcedure.addPerformer();
            fhirPerformer.setActor(EmisOpenHelper.createPractitionerReference(author.getGUID()));
        }

        Date dateRecorded = findRecordedDate(eventType.getOriginalAuthor());
        addRecordedDateExtension(fhirProcedure, dateRecorded);

        String recordedByGuid = findRecordedUserGuid(eventType.getOriginalAuthor());
        addRecordedByExtension(fhirProcedure, recordedByGuid);

        linkToProblem(eventType, patientGuid, fhirProcedure, results);

        results.add(fhirProcedure);
    }

}
