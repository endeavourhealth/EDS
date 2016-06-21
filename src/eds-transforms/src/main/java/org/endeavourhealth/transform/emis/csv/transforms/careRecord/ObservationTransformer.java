package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.endeavourhealth.transform.emis.csv.ClinicalCode;
import org.endeavourhealth.transform.emis.csv.FhirObjectStore;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.ImmunizationStatus;
import org.endeavourhealth.transform.terminology.Snomed;
import org.endeavourhealth.transform.terminology.TerminologyService;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

public class ObservationTransformer {

    //types derived from VocEventType in EMIS Open standard, minus the types known to be stored in other CSV files
    //TODO - verify ObservationType options in EMIS CSV
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

        String type = observationParser.getObservationType();
        ObservationType observationType = ObservationType.fromValue(type);
        switch (observationType) {
            case OBSERVATION:
                //following same pattern as OpenHR transform, where EMIS obs go to FHIR conditions or procedures
                if (isProcedure(observationParser, objectStore)) {
                    createProcedure(observationParser, objectStore);
                } else {
                    createCondition(observationParser, objectStore);
                }
                break;
            case TEST_REQUESTS:
                createDiagnosticOrder(observationParser, objectStore);
                break;
            case INVESTIGATION:
                createObservation(observationParser, objectStore);
                break;
            case VALUE:
                createObservation(observationParser, objectStore);
                break;
            case ATTACHMENT:
                //openHR transform turns these into Observations, so following same pattern
                createObservation(observationParser, objectStore);
                break;
            case ALLERGY:
                createAllergy(observationParser, objectStore);
                break;
            case FAMILY_HISTORY:
                createFamilyMemberHistory(observationParser, objectStore);
                break;
            case IMMUNISATION:
                createImmunization(observationParser, objectStore);
                break;
            case REPORT:
                createDiagnosticReport(observationParser, objectStore);
                break;
            case ORDER_HEADER:
                createDiagnosticOrder(observationParser, objectStore);
                break;
            default:
                throw new TransformException("Unhandled observationType " + observationType);
        }
    }

    private static boolean isProcedure(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        ClinicalCode clinicalCode = objectStore.findClinicalCode(observationParser.getCodeId());
        return Snomed.isProcedureCode(clinicalCode.getSnomedConceptId());
    }

    private static void createDiagnosticReport(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        DiagnosticReport fhirReport = new DiagnosticReport();
        fhirReport.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_REPORT));

        String observationGuid = observationParser.getObservationGuid();
        fhirReport.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        fhirReport.setSubject(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirReport, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        fhirReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirReport.setCode(clinicalCode.createCodeableConcept());

        String consultationGuid = observationParser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirReport.setEncounter(objectStore.createEncounterReference(consultationGuid, patientGuid));
        }

        /**
         OrganisationGuid
         EffectiveDate
         EffectiveDatePrecision
         EnteredDate
         EnteredTime
         ClinicianUserInRoleGuid
         EnteredByUserInRoleGuid
         ParentObservationGuid
         ProblemGuid
         AssociatedText
         Value
         NumericUnit
         ObservationType
         NumericRangeLow
         NumericRangeHigh
         DocumentGuid

         */
    }

    private static void createDiagnosticOrder(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        DiagnosticOrder fhirOrder = new DiagnosticOrder();
        fhirOrder.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_ORDER));

        String observationGuid = observationParser.getObservationGuid();
        fhirOrder.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        fhirOrder.setSubject(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirOrder, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirOrder.setOrderer(objectStore.createPractitionerReference(clinicianGuid, patientGuid));

        String consultationGuid = observationParser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirOrder.setEncounter(objectStore.createEncounterReference(consultationGuid, patientGuid));
        }

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        DiagnosticOrder.DiagnosticOrderItemComponent diagnosticOrderItemComponent = fhirOrder.addItem();
        diagnosticOrderItemComponent.setCode(clinicalCode.createCodeableConcept());

        String associatedText = observationParser.getAssociatedText();
        fhirOrder.addNote(AnnotationHelper.createAnnotation(associatedText));

        DiagnosticOrder.DiagnosticOrderEventComponent diagnosticOrderEventComponent = fhirOrder.addEvent();
        diagnosticOrderEventComponent.setStatus(DiagnosticOrder.DiagnosticOrderStatus.REQUESTED);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        diagnosticOrderEventComponent.setDateTimeElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String problemGuid = observationParser.getProblemUGuid();
        objectStore.linkToProblem(fhirOrder, problemGuid, patientGuid);
    }

    private static void createAllergy(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {
        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        String observationGuid = observationParser.getObservationGuid();
        fhirAllergy.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        fhirAllergy.setPatient(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirAllergy, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

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
        fhirAllergy.setOnsetElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

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
        fhirProcedure.setSubject(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirProcedure, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirProcedure.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROCEDURED_RECORDED, new DateTimeType(enteredDate)));

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirProcedure.setCode(clinicalCode.createCodeableConcept());

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirProcedure.setPerformed(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

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
        fhirCondition.setPatient(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirCondition, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

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
        fhirCondition.setOnset(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

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
        fhirObservation.setSubject(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirObservation, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        fhirObservation.setStatus(Observation.ObservationStatus.UNKNOWN);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirObservation.setEffective(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

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
            //the parent observation may have been converted to one of several different FHIR resource types
            Observation fhirParentObservation = objectStore.findObservation(parentObservationGuid, patientGuid);
            if (fhirParentObservation != null) {
                Observation.ObservationRelatedComponent fhirRelation = fhirParentObservation.addRelated();
                fhirRelation.setType(Observation.ObservationRelationshipType.HASMEMBER);
                fhirRelation.setTarget(ReferenceHelper.createReference(fhirObservation));
            } else {
                DiagnosticReport fhirDiagnosticReport = objectStore.findDiagnosticReport(parentObservationGuid, patientGuid);
                if (fhirDiagnosticReport != null) {
                    fhirDiagnosticReport.addResult(ReferenceHelper.createReference(fhirObservation));
                } else {
                    throw new TransformException("Failed to find parent observation for GUID " + parentObservationGuid);
                }
            }
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
        fhirFamilyHistory.setPatient(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirFamilyHistory, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirFamilyHistory.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

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

    private static void createImmunization(CareRecord_Observation observationParser, FhirObjectStore objectStore) throws Exception {

        Immunization fhirImmunisation = new Immunization();
        fhirImmunisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        String observationGuid = observationParser.getObservationGuid();
        fhirImmunisation.setId(observationGuid);

        String patientGuid = observationParser.getPatientGuid();
        fhirImmunisation.setPatient(objectStore.createPatientReference(patientGuid));

        String organisationGuid = observationParser.getOrganisationGuid();
        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirImmunisation, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        fhirImmunisation.setStatus(ImmunizationStatus.COMPLETED.getCode());
        fhirImmunisation.setWasNotGiven(false);
        fhirImmunisation.setReported(false);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirImmunisation.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Long codeId = observationParser.getCodeId();
        ClinicalCode clinicalCode = objectStore.findClinicalCode(codeId);
        fhirImmunisation.setVaccineCode(clinicalCode.createCodeableConcept());

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        Reference reference = objectStore.createPractitionerReference(clinicianGuid, patientGuid);
        fhirImmunisation.setPerformer(reference);

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            reference = objectStore.createEncounterReference(consultationGuid, patientGuid);
            fhirImmunisation.setEncounter(reference);
        }

        String associatedText = observationParser.getAssociatedText();
        fhirImmunisation.addNote(AnnotationHelper.createAnnotation(associatedText));

        String problemGuid = observationParser.getProblemUGuid();
        objectStore.linkToProblem(fhirImmunisation, problemGuid, patientGuid);
    }

}
