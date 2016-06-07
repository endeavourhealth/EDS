package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.endeavourhealth.transform.emis.csv.transforms.coding.ClinicalCode;
import org.endeavourhealth.transform.emis.csv.transforms.coding.FhirObjectStore;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ObservationTransformer {

    //types derived from VocEventType in EMIS Open standard, minus the types known to be stored in other CSV files
    enum ObservationType {
        OBSERVATION("Observation"),
        TEST_REQUESTS("Test Request"),
        INVESTIGATION("Investigation"),
        VALUE("Value"),
        ATTACHMENT("Attachment"),
        ALLERGY("Allergy"),
        FAMILY_HISTORY("Family history"),
        IMMUNISATION("Immunisation"),
        REPORT("Report"),
        ORDER_HEADER("Order Header");

        private String value = null;

        ObservationType(String value) {
            this.value = value;
        }

        public static ObservationType fromValue(String v) {
            for (ObservationType c: ObservationType.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }


    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        CareRecord_Observation parser = new CareRecord_Observation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {

        //since we're not processing deltas, just ignore deleted obs
        if (observationParser.getDeleted()) {
            return;
        }

        //do not store confidential data in EDS
        if (observationParser.getIsConfidential()) {
            return;
        }

        String type = observationParser.getObservationType();
        ObservationType observationType = ObservationType.fromValue(type);
        switch (observationType) {
            case OBSERVATION:
                createObservation(observationParser, objectStore);
                break;
            case TEST_REQUESTS:

                break;
            case INVESTIGATION:

                break;
            case VALUE:

                break;
            case ATTACHMENT:

                break;
            case ALLERGY:
                createAllergy(observationParser, objectStore);
                break;
            case FAMILY_HISTORY:
                createFamilyMemberHistory(observationParser, objectStore);
                break;
            case IMMUNISATION:

                break;
            case REPORT:

                break;
            case ORDER_HEADER:

                break;
            default:
                throw new TransformException("Unhandled observationType " + observationType);
        }


        //TODO - get full Enum of possible EMIS observation types
        if (type.equals("Family History")) {

        } else if (type.equals("Observation")) {

        } else if (type.equals("Condition")) {
            createCondition(observationParser, objectStore);
        } else if (type.equals("Procedure")) {
            createProcedure(observationParser, objectStore);
        } else if (type.equals("Allergy")) {

        } else {
            throw new TransformException("Unexpected observation type " + type);
        }

        //TODO - transform test requests
        //TODO - handle "Attachment" ?
    }

    private static void createAllergy(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        String observationGuid = observationParser.getObservationGuid();
        fhirAllergy.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirAllergy);

        fhirAllergy.setPatient(objectStore.createPatientReference(patientGuid));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirAllergy.setRecorder(objectStore.createPractitionerReference(clinicianGuid, patientGuid));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirAllergy.setRecordedDate(enteredDate);

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirAllergy.setSubstance(clinicalCode.createCodeableConcept());
        //TODO - need to convert Snomed allergy code to FHIR substance

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirAllergy.setOnsetElement(FhirObjectStore.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirAllergy.setNote(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            Reference reference = objectStore.createEncounterReference(consultationGuid, patientGuid);
            fhirAllergy.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

        String problemGuid = observationParser.getProblemUGuid();
        objectStore.linkToProblem(fhirAllergy, problemGuid, patientGuid);
    }

    private static void createProcedure(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE));

        String observationGuid = observationParser.getObservationGuid();
        fhirProcedure.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirProcedure);

        fhirProcedure.setSubject(objectStore.createPatientReference(patientGuid));

        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirProcedure.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROCEDURED_RECORDED, new DateTimeType(enteredDate)));

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirProcedure.setCode(clinicalCode.createCodeableConcept());

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirProcedure.setPerformed(FhirObjectStore.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirProcedure.addNotes(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirProcedure.setEncounter(objectStore.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = observationParser.getProblemUGuid();
        objectStore.linkToProblem(fhirProcedure, problemGuid, patientGuid);

    }


    private static void createCondition(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

        String observationGuid = observationParser.getObservationGuid();
        fhirCondition.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirCondition);

        fhirCondition.setPatient(objectStore.createPatientReference(patientGuid));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirCondition.setAsserter(objectStore.createPractitionerReference(clinicianGuid, patientGuid));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirCondition.setDateRecorded(enteredDate);

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirCondition.setCode(clinicalCode.createCodeableConcept());

        fhirCondition.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed

        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirCondition.setOnset(FhirObjectStore.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirCondition.setNotes(associatedText);

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirCondition.setEncounter(objectStore.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = observationParser.getProblemUGuid();
        objectStore.linkToProblem(fhirCondition, problemGuid, patientGuid);
    }

    private static void createObservation(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        Observation fhirObservation = new Observation();
        fhirObservation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        String observationGuid = observationParser.getObservationGuid();
        fhirObservation.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirObservation);

        fhirObservation.setSubject(objectStore.createPatientReference(patientGuid));

        fhirObservation.setStatus(Observation.ObservationStatus.UNKNOWN);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirObservation.setEffective(FhirObjectStore.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirObservation.setIssued(enteredDate);

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirObservation.setCode(clinicalCode.createCodeableConcept());

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirObservation.addPerformer(objectStore.createPractitionerReference(clinicianGuid, patientGuid));

        String orgGuid = observationParser.getOrganisationGuid();
        fhirObservation.addPerformer(objectStore.createOrganisationReference(orgGuid, patientGuid));

        Double value = observationParser.getValue();
        String units = observationParser.getNumericUnit();
        fhirObservation.setValue(QuantityHelper.createQuantity(value, units));

        Double low = observationParser.getNumericRangeLow();
        Double high = observationParser.getNumericRangeHigh();

        if (low != null || high != null) {

            Observation.ObservationReferenceRangeComponent fhirRange = fhirObservation.addReferenceRange();
            if (low != null && high != null) {
                fhirRange.setLow(QuantityHelper.createSimpleQuantity(low, units, Quantity.QuantityComparator.GREATER_OR_EQUAL));
                fhirRange.setHigh(QuantityHelper.createSimpleQuantity(high, units, Quantity.QuantityComparator.LESS_OR_EQUAL));
            } else if (low != null) {
                fhirRange.setLow(QuantityHelper.createSimpleQuantity(low, units, Quantity.QuantityComparator.GREATER_THAN));
            } else {
                fhirRange.setHigh(QuantityHelper.createSimpleQuantity(high, units, Quantity.QuantityComparator.LESS_THAN));
            }
        }

        String associatedText = observationParser.getAssociatedText();
        fhirObservation.setComments(associatedText);

        String parentObservationGuid = observationParser.getParentObservationGuid();
        if (parentObservationGuid != null) {
            Observation fhirParentObservation = objectStore.findObservation(parentObservationGuid, patientGuid);
            Observation.ObservationRelatedComponent fhirRelation = fhirParentObservation.addRelated();
            fhirRelation.setType(Observation.ObservationRelationshipType.HASMEMBER);
            fhirRelation.setTarget(ReferenceHelper.createReference(fhirObservation));
        }

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirObservation.setEncounter(objectStore.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = observationParser.getProblemUGuid();
        objectStore.linkToProblem(fhirObservation, problemGuid, patientGuid);
    }

    private static void createFamilyMemberHistory(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {

        FamilyMemberHistory fhirFamilyHistory = new FamilyMemberHistory();
        fhirFamilyHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        String observationGuid = observationParser.getObservationGuid();
        fhirFamilyHistory.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        objectStore.addToMap(patientGuid, fhirFamilyHistory);

        fhirFamilyHistory.setPatient(objectStore.createPatientReference(patientGuid));

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirFamilyHistory.setDateElement(FhirObjectStore.createDateTimeType(effectiveDate, effectiveDatePrecision));

        fhirFamilyHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.HEALTHUNKNOWN);

        //TODO: need to set Relationship on FamilyMemberHistory resource

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent fhirCondition = fhirFamilyHistory.addCondition();

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirCondition.setCode(clinicalCode.createCodeableConcept());

        String associatedText = observationParser.getAssociatedText();
        fhirCondition.setNote(AnnotationHelper.createAnnotation(associatedText));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        Reference reference = objectStore.createPractitionerReference(clinicianGuid, patientGuid);
        fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.FAMILY_MEMBER_HISTORY_RECORDER, reference));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            reference = objectStore.createEncounterReference(consultationGuid, patientGuid);
            fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

        String problemGuid = observationParser.getProblemUGuid();
        objectStore.linkToProblem(fhirFamilyHistory, problemGuid, patientGuid);
    }


}
