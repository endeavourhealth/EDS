package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.endeavourhealth.transform.emis.csv.transforms.coding.ClinicalCode;
import org.endeavourhealth.transform.emis.csv.transforms.coding.Metadata;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ObservationTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

        CareRecord_Observation parser = new CareRecord_Observation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, metadata, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(CareRecord_Observation observationParser, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

        //since we're not processing deltas, just ignore deleted obs
        if (observationParser.getDeleted()) {
            return;
        }

        //do not store confidential data in EDS
        if (observationParser.getIsConfidential()) {
            return;
        }

        String type = observationParser.getObservationType();
        //TODO - get full Enum of possible EMIS observation types
        if (type.equals("Family History")) {
            createFamilyMemberHistory(observationParser, metadata, fhirResources);
        } else if (type.equals("Observation")) {
            createObservation(observationParser, metadata, fhirResources);
        } else if (type.equals("Condition")) {
            createCondition(observationParser, metadata, fhirResources);
        } else if (type.equals("Procedure")) {
            createProcedure(observationParser, metadata, fhirResources);
        }

        /**
         *
         public UUID getObservationGuid() {
         return super.getUniqueIdentifier(0);
         }
         public UUID getParentOvercastionGuid() {
         return super.getUniqueIdentifier(1);
         }
         public UUID getPatientGuid() {
         return super.getUniqueIdentifier(2);
         }
         public UUID getOrganisationGuid() {
         return super.getUniqueIdentifier(3);
         }
         public Date getEffectiveDate() throws TransformException {
         return super.getDate(4);
         }
         public String getEffectiveDatePrecision() {
         return super.getString(5);
         }
         public Date getEnteredDateTime() throws TransformException {
         return super.getDateTime(6, 7);
         }
         public UUID getClinicianUserInRoleGuid() {
         return super.getUniqueIdentifier(8);
         }
         public UUID getEnteredByUserInRoleGuid() {
         return super.getUniqueIdentifier(9);
         }
         public Long getCodeId() {
         return super.getLong(10);
         }
         public UUID getProblemUGuid() {
         return super.getUniqueIdentifier(11);
         }
         public UUID getConsultationGuid() {
         return super.getUniqueIdentifier(12);
         }
         public Double getValue() {
         return super.getDouble(13);
         }
         public Double getNumericRangeLow() {
         return super.getDouble(14);
         }
         public Double getNumericRangeHigh() {
         return super.getDouble(15);
         }
         public String getNumericUnit() {
         return super.getString(16);
         }
         public String getAssociatedText() {
         return super.getString(18);
         }
         public UUID getDocumentGuid() {
         return super.getUniqueIdentifier(21);
         }
         */
    }

    private static void createProcedure(CareRecord_Observation observationParser, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {
        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE));

        String observationGuid = observationParser.getObservationGuid();
        fhirProcedure.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        Metadata.addToMap(patientGuid, fhirProcedure, fhirResources);

        fhirProcedure.setSubject(ReferenceHelper.createPatientReference(patientGuid));

        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirProcedure.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROCEDURED_RECORDED, new DateTimeType(enteredDate)));

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = metadata.findClinicalCode(codeId);
        fhirProcedure.setCode(clinicalCode.createCodeableConcept());

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirProcedure.setPerformed(Metadata.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirProcedure.addNotes(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirProcedure.setEncounter(metadata.createEncounterReference(consultationGuid, patientGuid, fhirResources));
        }

        String problemGuid = observationParser.getProblemUGuid();
        linkToProblem(fhirProcedure, problemGuid, patientGuid, fhirResources);

    }


    private static void createCondition(CareRecord_Observation observationParser, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {
        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

        String observationGuid = observationParser.getObservationGuid();
        fhirCondition.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        Metadata.addToMap(patientGuid, fhirCondition, fhirResources);

        fhirCondition.setPatient(ReferenceHelper.createPatientReference(patientGuid));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirCondition.setAsserter(metadata.createPractitionerReference(clinicianGuid, patientGuid, fhirResources));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirCondition.setDateRecorded(enteredDate);

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = metadata.findClinicalCode(codeId);
        fhirCondition.setCode(clinicalCode.createCodeableConcept());

        fhirCondition.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed

        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirCondition.setOnset(Metadata.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirCondition.setNotes(associatedText);

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirCondition.setEncounter(metadata.createEncounterReference(consultationGuid, patientGuid, fhirResources));
        }

        String problemGuid = observationParser.getProblemUGuid();
        linkToProblem(fhirCondition, problemGuid, patientGuid, fhirResources);
    }

    private static void createObservation(CareRecord_Observation observationParser, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {
        Observation fhirObservation = new Observation();
        fhirObservation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        String observationGuid = observationParser.getObservationGuid();
        fhirObservation.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        Metadata.addToMap(patientGuid, fhirObservation, fhirResources);

        fhirObservation.setSubject(ReferenceHelper.createPatientReference(patientGuid));

        fhirObservation.setStatus(Observation.ObservationStatus.UNKNOWN);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirObservation.setEffective(Metadata.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirObservation.setIssued(enteredDate);

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = metadata.findClinicalCode(codeId);
        fhirObservation.setCode(clinicalCode.createCodeableConcept());

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirObservation.addPerformer(metadata.createPractitionerReference(clinicianGuid, patientGuid, fhirResources));

        String orgGuid = observationParser.getOrganisationGuid();
        fhirObservation.addPerformer(metadata.createOrganisationReference(orgGuid, patientGuid, fhirResources));

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
            Observation fhirParentObservation = Metadata.findObservation(parentObservationGuid, patientGuid, fhirResources);
            Observation.ObservationRelatedComponent fhirRelation = fhirParentObservation.addRelated();
            fhirRelation.setType(Observation.ObservationRelationshipType.HASMEMBER);
            fhirRelation.setTarget(ReferenceHelper.createReference(fhirObservation));
        }

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirObservation.setEncounter(metadata.createEncounterReference(consultationGuid, patientGuid, fhirResources));
        }

        String problemGuid = observationParser.getProblemUGuid();
        linkToProblem(fhirObservation, problemGuid, patientGuid, fhirResources);
    }

    private static void createFamilyMemberHistory(CareRecord_Observation observationParser, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

        FamilyMemberHistory fhirFamilyHistory = new FamilyMemberHistory();
        fhirFamilyHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        String observationGuid = observationParser.getObservationGuid();
        fhirFamilyHistory.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        Metadata.addToMap(patientGuid, fhirFamilyHistory, fhirResources);

        fhirFamilyHistory.setPatient(ReferenceHelper.createPatientReference(patientGuid));

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirFamilyHistory.setDateElement(Metadata.createDateTimeType(effectiveDate, effectiveDatePrecision));

        fhirFamilyHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.HEALTHUNKNOWN);

        //TODO: need to set Relationship on FamilyMemberHistory resource

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent fhirCondition = fhirFamilyHistory.addCondition();

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = metadata.findClinicalCode(codeId);
        fhirCondition.setCode(clinicalCode.createCodeableConcept());

        String associatedText = observationParser.getAssociatedText();
        fhirCondition.setNote(AnnotationHelper.createAnnotation(associatedText));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        Reference reference = metadata.createPractitionerReference(clinicianGuid, patientGuid, fhirResources);
        fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.FAMILY_MEMBER_HISTORY_RECORDER, reference));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            reference = metadata.createEncounterReference(consultationGuid, patientGuid, fhirResources);
            fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

        String problemGuid = observationParser.getProblemUGuid();
        linkToProblem(fhirFamilyHistory, problemGuid, patientGuid, fhirResources);
    }

    private static void linkToProblem(Resource resource, String problemGuid, String patientGuid, Map<String, List<Resource>> fhirResources) throws Exception {
        if (problemGuid == null) {
            return;
        }

        Reference reference = ReferenceHelper.createReference(resource);
        Condition fhirProblem = Metadata.findProblem(problemGuid, patientGuid, fhirResources);
        fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_ASSOCIATED_RESOURCE, reference));
    }

 /*   private static AllergyIntolerance createAllergyIntolerace(CareRecord_Observation observationParser) throws Exception {

    }

    private static Immunization createImmunuzation(CareRecord_Observation observationParser) throws Exception {

    }*/
}
