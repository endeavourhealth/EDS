package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.common.fhir.AnnotationHelper;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.QuantityHelper;
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

        //if the event is a problem then we MUST create a Condition resource for it, even if we also
        //create another resource type for it too
        boolean requiresCondition = eventType.getProblem() != null;

        //check for routing to FHIR based on the code first
        StringCodeType code = eventType.getCode();
        if (code != null) {
            String read2Code = code.getValue();
            if (Read2.isProcedure(read2Code)) {
                transformProcedure(eventType, results, patientUuid);

            } else if (Read2.isDisorder(read2Code)) {
                transformCondition(eventType, results, patientUuid);
                //if we've created a condition, set this to false, so we don't create another one
                requiresCondition = false;
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
                ImmunisationTransformer.transform(eventType, results, patientUuid);
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

        //if our event is a problem, and we didn't create a condition, create one now
        if (requiresCondition) {
            transformCondition(eventType, results, patientUuid);
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

                //TODO - use range operators
                //String highOperator = valueType.getMaximumRangeOperator();
                //String lowOperator = valueType.getMinRangeOperator();

                org.hl7.fhir.instance.model.Observation.ObservationReferenceRangeComponent fhirRange = fhirObservation.addReferenceRange();

                if (low != null && high != null) {
                    fhirRange.setLow(QuantityHelper.createSimpleQuantity(low, units, Quantity.QuantityComparator.GREATER_OR_EQUAL));
                    fhirRange.setHigh(QuantityHelper.createSimpleQuantity(high, units, Quantity.QuantityComparator.LESS_OR_EQUAL));
                } else if (low != null) {
                    fhirRange.setLow(QuantityHelper.createSimpleQuantity(low, units, Quantity.QuantityComparator.GREATER_THAN));
                } else {
                    fhirRange.setHigh(QuantityHelper.createSimpleQuantity(high, units, Quantity.QuantityComparator.LESS_THAN));
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

    private static void transformCondition(EventType eventType, List<Resource> results, String patientUuid) throws TransformException {

        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

        String eventGuid = eventType.getGUID();
        EmisOpenHelper.setUniqueId(fhirCondition, patientUuid, eventGuid);

        fhirCondition.setPatient(EmisOpenHelper.createPatientReference(patientUuid));

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

        //if the condition is also a problem, we need to change/add a few things
        ProblemType problem = eventType.getProblem();
        if (problem != null) {

            fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROBLEM));

            String endDateStr = problem.getEndDate();
            if (!Strings.isNullOrEmpty(endDateStr)) {
                fhirCondition.setAbatement(DateConverter.convertPartialDateToDateTimeType(endDateStr, null, problem.getEndDatePart()));

            } else {

                //if there's no end date, the problem may still be ended, which is in the status description
                /*String problemStatus = parser.getProblemStatusDescription();
                if (problemStatus.equalsIgnoreCase("Past Problem")) {
                    fhirProblem.setAbatement(new BooleanType(true));
                }*/
            }

            /*BigInteger duration = problem.getExpectedDuration();
            if (duration != null) {
                int durationInt = duration.intValue();
                if (durationInt > 0) {
                    fhirCondition.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_EXPECTED_DURATION, new IntegerType(durationInt)));
                }
            }

            Date lastReviewDate = parser.getLastReviewDate();
            String lastReviewPrecision = parser.getLastReviewDatePrecision();
            DateType lastReviewDateType = EmisDateTimeHelper.createDateType(lastReviewDate, lastReviewPrecision);
            String lastReviewedByGuid = parser.getLastReviewUserInRoleGuid();
            if (lastReviewDateType != null
                    || !Strings.isNullOrEmpty(lastReviewedByGuid)) {

                //the review extension is a compound extension, containing who and when
                Extension fhirExtension = ExtensionConverter.createCompoundExtension(FhirExtensionUri.PROBLEM_LAST_REVIEWED);

                if (lastReviewDateType != null) {
                    fhirExtension.addExtension(ExtensionConverter.createExtension(FhirExtensionUri._PROBLEM_LAST_REVIEWED__DATE, lastReviewDateType));
                }
                if (!Strings.isNullOrEmpty(lastReviewedByGuid)) {
                    fhirExtension.addExtension(ExtensionConverter.createExtension(FhirExtensionUri._PROBLEM_LAST_REVIEWED__PERFORMER, csvHelper.createPractitionerReference(lastReviewedByGuid)));
                }
                fhirProblem.addExtension(fhirExtension);
            }

            ProblemSignificance fhirSignificance = convertSignificance(parser.getSignificanceDescription());
            CodeableConcept fhirConcept = CodeableConceptHelper.createCodeableConcept(fhirSignificance);
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_SIGNIFICANCE, fhirConcept));

            String parentProblemGuid = parser.getParentProblemObservationGuid();
            String parentRelationship = parser.getParentProblemRelationship();
            if (!Strings.isNullOrEmpty(parentProblemGuid)) {
                ProblemRelationshipType fhirRelationshipType = convertRelationshipType(parentRelationship);

                //this extension is composed of two separate extensions
                Extension typeExtension = ExtensionConverter.createExtension("type", new StringType(fhirRelationshipType.getCode()));
                Extension referenceExtension = ExtensionConverter.createExtension("target", csvHelper.createProblemReference(parentProblemGuid, patientGuid));
                fhirProblem.addExtension(ExtensionConverter.createCompoundExtension(FhirExtensionUri.PROBLEM_RELATED, typeExtension, referenceExtension));
            }

            //carry over linked items from any previous instance of this problem
            List<Reference> previousReferences = findPreviousLinkedReferences(csvHelper, fhirResourceFiler, fhirProblem.getId());
            if (previousReferences != null && !previousReferences.isEmpty()) {
                csvHelper.addLinkedItemsToProblem(fhirProblem, previousReferences);
            }

            //apply any linked items from this extract
            List<String> linkedResources = csvHelper.getAndRemoveProblemRelationships(observationGuid, patientGuid);
            if (linkedResources != null) {
                List<Reference> references = ReferenceHelper.createReferences(linkedResources);
                csvHelper.addLinkedItemsToProblem(fhirProblem, references);
            }*/

            /**
             protected Byte problemStatus;
             protected Byte groupingStatus;
             protected Byte problemType;
             protected Byte significance;
             protected IdentType parentProblem;
             protected Byte owner;
             protected CareAimListType careAimList;
             protected CarePlanListType carePlanList;
             */
        }

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

        results.add(fhirProcedure);
    }

}
