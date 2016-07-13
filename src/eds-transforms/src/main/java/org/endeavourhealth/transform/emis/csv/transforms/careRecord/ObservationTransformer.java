package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.endeavourhealth.transform.emis.csv.schema.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.ImmunizationStatus;
import org.endeavourhealth.transform.terminology.Snomed;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

public class ObservationTransformer {


    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        CareRecord_Observation parser = new CareRecord_Observation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, csvProcessor, csvHelper);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(CareRecord_Observation observationParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        Long codeId = observationParser.getCodeId();
        ClinicalCodeType codeType = csvHelper.findClinicalCodeType(codeId);
        switch (codeType) {
            case Conditiions_Operations_Procedures:

                if (isProcedure(codeId, csvHelper)) {
                    createProcedure(observationParser, csvProcessor, csvHelper);
                } else {
                    createCondition(observationParser, csvProcessor, csvHelper);
                }
                break;

            case Dental_Disorder:
            case Symptoms_Findings:
                createCondition(observationParser, csvProcessor, csvHelper);
                break;

            case Dental_Procedure:
            case Procedure:
                createProcedure(observationParser, csvProcessor, csvHelper);
                break;

            case Adminisation_Documents_Attachments:
            case Biochemistry:
            case Biological_Values:
            case Body_Structure:
            case Care_Episode_Outcome:
            case Cyology_Histology:
            case Dental_Finding:
            case Discharged_From_Service:
            case EMIS_Qualifier:
            case Ethnicity:
            case Haematology:
            case Health_Management:
            case HMP:
            case Intervention_Category:
            case Intervention_Target:
            case Immunology:
            case KC60:
            case Marital_Status:
            case Microbiology:
            case Nationality:
            case Nursing_Problem:
            case Nursing_Problem_Domain:
            case Obsteterics_Birth:
            case Person_Health_Social:
            case Planned_Dental:
            case Problem_Rating_Scale:
            case Radiology:
            case Reason_For_Care:
            case Referral_Activity:
            case Referral_Rejected:
            case Referral_Withdrawn:
            case Regiment:
            case Religion:
            case Trade_Branch:
            case Unset:
                createObservation(observationParser, csvProcessor, csvHelper);
                break;
            case Allergy_Adverse_Drug_Reations:
            case Allergy_Adverse_Reations:
                createAllergy(observationParser, csvProcessor, csvHelper);
                break;
            case Family_History:
                createFamilyMemberHistory(observationParser, csvProcessor, csvHelper);
                break;
            case Immunisations:
                createImmunization(observationParser, csvProcessor, csvHelper);
                break;
            case Diagnostics:
            case Investigation_Requests:
                createDiagnosticOrder(observationParser, csvProcessor, csvHelper);
                break;
            case Pathology_Specimen:
                createSpecimen(observationParser, csvProcessor, csvHelper);
            case Referral:
                createReferral(observationParser, csvProcessor, csvHelper);
            default:
                throw new TransformException("Unhandled observationType " + codeType);
        }
    }

    private static boolean isProcedure(Long codeId, EmisCsvHelper csvHelper) throws Exception {
        CodeableConcept fhirConcept = csvHelper.findClinicalCode(codeId);
        for (Coding coding: fhirConcept.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
                String code = coding.getCode();
                return Snomed.isProcedureCode(Long.parseLong(code));
            }
        }

        throw new TransformException("Failed to determine if CodeableConcept is procedure or not");
    }

    private static void createReferral(CareRecord_Observation observationParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {
//TODO - cache referral code and date WITHOUT creating observation>>>>???


        Specimen fhirSpecimen = new Specimen();
        fhirSpecimen.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SPECIMIN));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirSpecimen, patientGuid, observationGuid);

        fhirSpecimen.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirSpecimen, patientGuid);
            return;
        }

        Long codeId = observationParser.getCodeId();
        fhirSpecimen.setType(csvHelper.findClinicalCode(codeId));

        Specimen.SpecimenCollectionComponent fhirCollection = new Specimen.SpecimenCollectionComponent();
        fhirSpecimen.setCollection(fhirCollection);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirCollection.setCollected(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirCollection.setCollector(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = observationParser.getAssociatedText();
        fhirCollection.addComment(associatedText);

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirSpecimen, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirSpecimen, patientGuid);
    }

    private static void createSpecimen(CareRecord_Observation observationParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        Specimen fhirSpecimen = new Specimen();
        fhirSpecimen.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SPECIMIN));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirSpecimen, patientGuid, observationGuid);

        fhirSpecimen.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirSpecimen, patientGuid);
            return;
        }

        Long codeId = observationParser.getCodeId();
        fhirSpecimen.setType(csvHelper.findClinicalCode(codeId));

        Specimen.SpecimenCollectionComponent fhirCollection = new Specimen.SpecimenCollectionComponent();
        fhirSpecimen.setCollection(fhirCollection);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirCollection.setCollected(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirCollection.setCollector(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = observationParser.getAssociatedText();
        fhirCollection.addComment(associatedText);

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirSpecimen, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirSpecimen, patientGuid);
    }

    /*private static void createDiagnosticReport(CareRecord_Observation observationParser, EmisCsvHelper csvHelper) throws Exception {
        DiagnosticReport fhirReport = new DiagnosticReport();
        fhirReport.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_REPORT));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirReport, patientGuid, observationGuid);

        fhirReport.setSubject(objectStore.createPatientReference(patientGuid));

        boolean store = !observationParser.getDeleted() && !observationParser.getIsConfidential();
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirReport, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            csvHelper.cacheDiagnosticReport(fhirReport);

            return;
        }

        fhirReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

        Long codeId = observationParser.getCodeId();
        fhirReport.setCode(objectStore.findClinicalCode(codeId));

        String consultationGuid = observationParser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirReport.setEncounter(objectStore.createEncounterReference(consultationGuid, patientGuid));
        }

        csvHelper.cacheDiagnosticReport(fhirReport);
    }*/

    private static void createDiagnosticOrder(CareRecord_Observation observationParser,
                                              CsvProcessor csvProcessor,
                                              EmisCsvHelper csvHelper) throws Exception {
        DiagnosticOrder fhirOrder = new DiagnosticOrder();
        fhirOrder.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_ORDER));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirOrder, patientGuid, observationGuid);

        fhirOrder.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirOrder, patientGuid);
            return;
        }

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirOrder.setOrderer(csvHelper.createPractitionerReference(clinicianGuid));

        String consultationGuid = observationParser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirOrder.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        Long codeId = observationParser.getCodeId();
        DiagnosticOrder.DiagnosticOrderItemComponent diagnosticOrderItemComponent = fhirOrder.addItem();
        diagnosticOrderItemComponent.setCode(csvHelper.findClinicalCode(codeId));

        String associatedText = observationParser.getAssociatedText();
        fhirOrder.addNote(AnnotationHelper.createAnnotation(associatedText));

        DiagnosticOrder.DiagnosticOrderEventComponent diagnosticOrderEventComponent = fhirOrder.addEvent();
        diagnosticOrderEventComponent.setStatus(DiagnosticOrder.DiagnosticOrderStatus.REQUESTED);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        diagnosticOrderEventComponent.setDateTimeElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirOrder, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirOrder, patientGuid);
    }

    private static void createAllergy(CareRecord_Observation observationParser,
                                      CsvProcessor csvProcessor,
                                      EmisCsvHelper csvHelper) throws Exception {

        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirAllergy, patientGuid, observationGuid);

        fhirAllergy.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {

            csvProcessor.deletePatientResource(fhirAllergy, patientGuid);
            return;
        }

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirAllergy.setRecorder(csvHelper.createPractitionerReference(clinicianGuid));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirAllergy.setRecordedDate(enteredDate);

        Long codeId = observationParser.getCodeId();
        fhirAllergy.setSubstance(csvHelper.findClinicalCode(codeId));
        //TODO - need to convert Snomed allergy code to FHIR substance

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirAllergy.setOnsetElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirAllergy.setNote(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            Reference reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            fhirAllergy.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirAllergy, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirAllergy, patientGuid);
    }

    private static void createProcedure(CareRecord_Observation observationParser,
                                        CsvProcessor csvProcessor,
                                        EmisCsvHelper csvHelper) throws Exception {

        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirProcedure, patientGuid, observationGuid);

        fhirProcedure.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirProcedure, patientGuid);
            return;
        }

        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirProcedure.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROCEDURED_RECORDED, new DateTimeType(enteredDate)));

        Long codeId = observationParser.getCodeId();
        fhirProcedure.setCode(csvHelper.findClinicalCode(codeId));

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirProcedure.setPerformed(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirProcedure.addNotes(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirProcedure.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirProcedure, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirProcedure, patientGuid);
    }


    private static void createCondition(CareRecord_Observation observationParser,
                                        CsvProcessor csvProcessor,
                                        EmisCsvHelper csvHelper) throws Exception {

        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirCondition, patientGuid, observationGuid);

        fhirCondition.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirCondition, patientGuid);
            return;
        }

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirCondition.setAsserter(csvHelper.createPractitionerReference(clinicianGuid));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirCondition.setDateRecorded(enteredDate);

        Long codeId = observationParser.getCodeId();
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId));

        fhirCondition.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed

        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirCondition.setOnset(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirCondition.setNotes(associatedText);

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirCondition.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirCondition, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirCondition, patientGuid);
    }

    private static void createObservation(CareRecord_Observation observationParser,
                                          CsvProcessor csvProcessor,
                                          EmisCsvHelper csvHelper) throws Exception {

        Observation fhirObservation = new Observation();
        fhirObservation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirObservation, patientGuid, observationGuid);

        fhirObservation.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirObservation, patientGuid);
            csvHelper.cacheObservation(fhirObservation);

            return;
        }

        fhirObservation.setStatus(Observation.ObservationStatus.UNKNOWN);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirObservation.setEffective(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirObservation.setIssued(enteredDate);

        Long codeId = observationParser.getCodeId();
        fhirObservation.setCode(csvHelper.findClinicalCode(codeId));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirObservation.addPerformer(csvHelper.createPractitionerReference(clinicianGuid));

        String orgGuid = observationParser.getOrganisationGuid();
        fhirObservation.addPerformer(csvHelper.createOrganisationReference(orgGuid));

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

        Observation fhirParentObservation = null;

        String parentObservationGuid = observationParser.getParentObservationGuid();
        if (parentObservationGuid != null) {
            Reference reference = csvHelper.createObservationReference(observationGuid, patientGuid);

            //the parent observation may have been converted to one of several different FHIR resource types
            fhirParentObservation = csvHelper.findObservation(parentObservationGuid, patientGuid);
            if (fhirParentObservation == null) {
                throw new TransformException("Failed to find parent observation for GUID " + parentObservationGuid);
            }

            //check if the parent observation doesn't already have our ob linked to it
            boolean alreadyLinked = false;
            for (Observation.ObservationRelatedComponent related: fhirParentObservation.getRelated()) {
                if (related.getType() == Observation.ObservationRelationshipType.HASMEMBER
                        && related.getTarget().equalsShallow(reference)) {
                    alreadyLinked = true;
                    break;
                }
            }

            if (!alreadyLinked) {
                Observation.ObservationRelatedComponent fhirRelation = fhirParentObservation.addRelated();
                fhirRelation.setType(Observation.ObservationRelationshipType.HASMEMBER);
                fhirRelation.setTarget(reference);
            }
        }

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirObservation.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirObservation, problemGuid, patientGuid);

        csvHelper.cacheObservation(fhirObservation);

        csvProcessor.savePatientResource(fhirObservation, patientGuid);
        if (fhirParentObservation != null) {
            csvProcessor.savePatientResource(fhirParentObservation, patientGuid);
        }
    }

    private static void createFamilyMemberHistory(CareRecord_Observation observationParser,
                                                  CsvProcessor csvProcessor,
                                                  EmisCsvHelper csvHelper) throws Exception {

        FamilyMemberHistory fhirFamilyHistory = new FamilyMemberHistory();
        fhirFamilyHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirFamilyHistory, patientGuid, observationGuid);

        fhirFamilyHistory.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirFamilyHistory, patientGuid);
            return;
        }

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirFamilyHistory.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        fhirFamilyHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.HEALTHUNKNOWN);

        //TODO: need to set Relationship on FamilyMemberHistory resource

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent fhirCondition = fhirFamilyHistory.addCondition();

        Long codeId = observationParser.getCodeId();
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId));

        String associatedText = observationParser.getAssociatedText();
        fhirCondition.setNote(AnnotationHelper.createAnnotation(associatedText));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        Reference reference = csvHelper.createPractitionerReference(clinicianGuid);
        fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.FAMILY_MEMBER_HISTORY_RECORDER, reference));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirFamilyHistory, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirFamilyHistory, patientGuid);
    }

    private static void createImmunization(CareRecord_Observation observationParser,
                                           CsvProcessor csvProcessor,
                                           EmisCsvHelper csvHelper) throws Exception {

        Immunization fhirImmunisation = new Immunization();
        fhirImmunisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirImmunisation, patientGuid, observationGuid);

        fhirImmunisation.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
            csvProcessor.deletePatientResource(fhirImmunisation, patientGuid);
            return;
        }

        fhirImmunisation.setStatus(ImmunizationStatus.COMPLETED.getCode());
        fhirImmunisation.setWasNotGiven(false);
        fhirImmunisation.setReported(false);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirImmunisation.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Long codeId = observationParser.getCodeId();
        fhirImmunisation.setVaccineCode(csvHelper.findClinicalCode(codeId));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        Reference reference = csvHelper.createPractitionerReference(clinicianGuid);
        fhirImmunisation.setPerformer(reference);

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            fhirImmunisation.setEncounter(reference);
        }

        String associatedText = observationParser.getAssociatedText();
        fhirImmunisation.addNote(AnnotationHelper.createAnnotation(associatedText));

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirImmunisation, problemGuid, patientGuid);

        csvProcessor.savePatientResource(fhirImmunisation, patientGuid);
    }

}
