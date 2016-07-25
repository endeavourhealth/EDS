package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.endeavourhealth.transform.emis.csv.schema.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.FamilyMember;
import org.endeavourhealth.transform.fhir.schema.ImmunizationStatus;
import org.endeavourhealth.transform.terminology.Read2;
import org.hl7.fhir.instance.model.*;

import java.util.*;

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
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }

    }

    private static void createResource(CareRecord_Observation observationParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        Long codeId = observationParser.getCodeId();
        ResourceType resourceType = getTargetResourceType(codeId, csvProcessor, csvHelper);
        switch (resourceType) {
            case Observation:
                createObservation(observationParser, csvProcessor, csvHelper);
                break;
            case Condition:
                createCondition(observationParser, csvProcessor, csvHelper);
                break;
            case Procedure:
                createProcedure(observationParser, csvProcessor, csvHelper);
                break;
            case AllergyIntolerance:
                createAllergy(observationParser, csvProcessor, csvHelper);
                break;
            case FamilyMemberHistory:
                createFamilyMemberHistory(observationParser, csvProcessor, csvHelper);
                break;
            case Immunization:
                createImmunization(observationParser, csvProcessor, csvHelper);
                break;
            case DiagnosticOrder:
                createDiagnosticOrder(observationParser, csvProcessor, csvHelper);
                break;
            case Specimen:
                createSpecimen(observationParser, csvProcessor, csvHelper);
                break;
            default:
                throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
        }

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();

        //as well as processing the Observation row into a FHIR resource, we
        //may also have a row in the Referral file that we've previously processed into
        //a FHIR ReferralRequest that we need to complete
        ReferralRequest fhirReferral = csvHelper.findReferral(observationGuid, patientGuid);
        if (fhirReferral != null) {
            completeReferral(observationParser, fhirReferral, csvProcessor, csvHelper);
        }

        //the observation row may also be referenced by a row in the Problems file, which
        //we've already processed. Check this, and complete processing if required.
        Condition fhirProblem = csvHelper.findProblem(observationGuid, patientGuid);
        if (fhirProblem != null) {
            completeProblem(observationParser, fhirProblem, csvProcessor, csvHelper);
        }

        //remove any cached links of child observations that link to the row we just processed. If the row used
        //the links, they'll already have been removed. If not, then we can't use them anyway.
        csvHelper.getAndRemoveObservationParentRelationships(observationGuid, patientGuid);
    }

    public static ResourceType getTargetResourceType(Long codeId, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        ClinicalCodeType codeType = csvHelper.findClinicalCodeType(codeId, csvProcessor);

        switch (codeType) {
            case Conditions_Operations_Procedures:

                if (isProcedure(codeId, csvProcessor, csvHelper)) {
                    return ResourceType.Procedure;
                } else {
                    return ResourceType.Condition;
                }
            case Dental_Disorder:
            case Symptoms_Findings:
                return ResourceType.Condition;
            case Dental_Procedure:
            case Procedure:
                return ResourceType.Procedure;
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
            case Referral:
            case Referral_Activity:
            case Referral_Rejected:
            case Referral_Withdrawn:
            case Regiment:
            case Religion:
            case Trade_Branch:
            case Unset:
                return ResourceType.Observation;
            case Allergy_Adverse_Drug_Reations:
            case Allergy_Adverse_Reations:
                return ResourceType.AllergyIntolerance;
            case Family_History:
                return ResourceType.FamilyMemberHistory;
            case Immunisations:
                return ResourceType.Immunization;
            case Diagnostics:
            case Investigation_Requests:
                return ResourceType.DiagnosticOrder;
            case Pathology_Specimen:
                return ResourceType.Specimen;
            default:
                throw new IllegalArgumentException("Unhandled observationType " + codeType);
        }
    }


    private static void completeProblem(CareRecord_Observation observationParser,
                                         Condition fhirProblem,
                                         CsvProcessor csvProcessor,
                                         EmisCsvHelper csvHelper) throws Exception {

        String patientGuid = observationParser.getPatientGuid();

        if (observationParser.getDeleted()) {
            csvProcessor.deletePatientResource(fhirProblem, patientGuid);
            return;
        }

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirProblem.setDateRecordedElement(EmisDateTimeHelper.createDateType(effectiveDate, effectiveDatePrecision));

        String consultationGuid = observationParser.getConsultationGuid();
        fhirProblem.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));

        Long codeId = observationParser.getCodeId();
        fhirProblem.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirProblem.setAsserter(csvHelper.createPractitionerReference(clinicianGuid));

        //problems are added to the processor after all files are finished, so it doesn't need saving here
    }

    private static void completeReferral(CareRecord_Observation observationParser,
                                         ReferralRequest fhirReferral,
                                         CsvProcessor csvProcessor,
                                         EmisCsvHelper csvHelper) throws Exception {

        String patientGuid = observationParser.getPatientGuid();

        if (observationParser.getDeleted()) {
            csvProcessor.deletePatientResource(fhirReferral, patientGuid);
            return;
        }

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirReferral.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String consultationGuid = observationParser.getConsultationGuid();
        fhirReferral.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));

        Long codeId = observationParser.getCodeId();
        fhirReferral.setReason(csvHelper.findClinicalCode(codeId, csvProcessor));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirReferral.setRequester(csvHelper.createPractitionerReference(clinicianGuid));

        csvProcessor.savePatientResource(fhirReferral, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                                            patientGuid,
                                            observationParser.getObservationGuid(),
                                            fhirReferral.getResourceType());
    }

    private static boolean isProcedure(Long codeId,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {
        CodeableConcept fhirConcept = csvHelper.findClinicalCode(codeId, csvProcessor);
        for (Coding coding: fhirConcept.getCoding()) {

            //would prefer to check for procedures using Snomed, but this Read2 is simple and works
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_READ2)) {
                return Read2.isProcedure(coding.getCode());
            }

            //the above testing of Read2 works, but this Snomed testing hasn't been implemented yet
            /*if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
                String code = coding.getCode();
                return Snomed.isProcedureCode(Long.parseLong(code));
            }*/
        }

        throw new TransformException("Failed to determine if CodeableConcept is procedure or not");
    }

    /*private static void createReferral(CareRecord_Observation observationParser,
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
        fhirSpecimen.setType(csvHelper.findClinicalCode(codeId, csvProcessor));

        Specimen.SpecimenCollectionComponent fhirCollection = new Specimen.SpecimenCollectionComponent();
        fhirSpecimen.setCollection(fhirCollection);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEndDatePrecision();
        fhirCollection.setCollected(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirCollection.setCollector(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = observationParser.getAssociatedText();
        fhirCollection.addComment(associatedText);

        String problemGuid = observationParser.getProblemUGuid();
        csvHelper.linkToProblem(fhirSpecimen, problemGuid, patientGuid, csvProcessor);

        csvProcessor.savePatientResource(fhirSpecimen, patientGuid);
    }*/

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
        fhirSpecimen.setType(csvHelper.findClinicalCode(codeId, csvProcessor));

        Specimen.SpecimenCollectionComponent fhirCollection = new Specimen.SpecimenCollectionComponent();
        fhirSpecimen.setCollection(fhirCollection);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirCollection.setCollected(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String clinicianGuid = observationParser.getClinicianUserInRoleGuid();
        fhirCollection.setCollector(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = observationParser.getAssociatedText();
        fhirCollection.addComment(associatedText);

        csvProcessor.savePatientResource(fhirSpecimen, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirSpecimen.getResourceType());
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
        diagnosticOrderItemComponent.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String associatedText = observationParser.getAssociatedText();
        fhirOrder.addNote(AnnotationHelper.createAnnotation(associatedText));

        DiagnosticOrder.DiagnosticOrderEventComponent diagnosticOrderEventComponent = fhirOrder.addEvent();
        diagnosticOrderEventComponent.setStatus(DiagnosticOrder.DiagnosticOrderStatus.REQUESTED);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        diagnosticOrderEventComponent.setDateTimeElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        csvProcessor.savePatientResource(fhirOrder, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirOrder.getResourceType());
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
        fhirAllergy.setSubstance(csvHelper.findClinicalCode(codeId, csvProcessor));

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

        csvProcessor.savePatientResource(fhirAllergy, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirAllergy.getResourceType());
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
        fhirProcedure.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirProcedure.setPerformed(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = observationParser.getAssociatedText();
        fhirProcedure.addNotes(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirProcedure.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        csvProcessor.savePatientResource(fhirProcedure, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirProcedure.getResourceType());
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
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

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

        csvProcessor.savePatientResource(fhirCondition, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirCondition.getResourceType());
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
            return;
        }

        fhirObservation.setStatus(Observation.ObservationStatus.UNKNOWN);

        Date effectiveDate = observationParser.getEffectiveDate();
        String effectiveDatePrecision = observationParser.getEffectiveDatePrecision();
        fhirObservation.setEffective(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Date enteredDate = observationParser.getEnteredDateTime();
        fhirObservation.setIssued(enteredDate);

        Long codeId = observationParser.getCodeId();
        fhirObservation.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

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

        String consultationGuid = observationParser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirObservation.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        List<EmisCsvHelper.ResourceRelationship> childObservationIds = csvHelper.getAndRemoveObservationParentRelationships(observationGuid, patientGuid);
        linkChildObservations(csvHelper, fhirObservation, patientGuid, childObservationIds);

        csvProcessor.savePatientResource(fhirObservation, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirObservation.getResourceType());
    }
    private static void linkChildObservations(EmisCsvHelper csvHelper,
                                              Observation fhirObservation,
                                              String patientGuid,
                                              List<EmisCsvHelper.ResourceRelationship> resourceRelationships) throws Exception {
        if (resourceRelationships == null) {
            return;
        }

        for (EmisCsvHelper.ResourceRelationship resourceRelationship : resourceRelationships) {

            Reference reference = csvHelper.createObservationReference(resourceRelationship.getDependentResourceGuid(), patientGuid);

            //check if the parent observation doesn't already have our ob linked to it
            boolean alreadyLinked = false;
            for (Observation.ObservationRelatedComponent related: fhirObservation.getRelated()) {
                if (related.getType() == Observation.ObservationRelationshipType.HASMEMBER
                        && related.getTarget().equalsShallow(reference)) {
                    alreadyLinked = true;
                    break;
                }
            }

            if (!alreadyLinked) {
                Observation.ObservationRelatedComponent fhirRelation = fhirObservation.addRelated();
                fhirRelation.setType(Observation.ObservationRelationshipType.HASMEMBER);
                fhirRelation.setTarget(reference);
            }
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

        //most of the codes are just "FH: xxx" so can't be mapped to a definite family member relationship,
        //so just use the generic family member term
        fhirFamilyHistory.setRelationship(CodeableConceptHelper.createCodeableConcept(FamilyMember.FAMILY_MEMBER));

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent fhirCondition = fhirFamilyHistory.addCondition();

        Long codeId = observationParser.getCodeId();
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

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

        csvProcessor.savePatientResource(fhirFamilyHistory, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirFamilyHistory.getResourceType());
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
        fhirImmunisation.setVaccineCode(csvHelper.findClinicalCode(codeId, csvProcessor));

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

        csvProcessor.savePatientResource(fhirImmunisation, patientGuid);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(observationParser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirImmunisation.getResourceType());
    }

}
