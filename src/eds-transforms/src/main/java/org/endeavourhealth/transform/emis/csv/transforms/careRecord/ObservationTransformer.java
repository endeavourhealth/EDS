package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FieldNotEmptyException;
import org.endeavourhealth.transform.common.exceptions.FutureException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.ObservationType;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.FamilyMember;
import org.endeavourhealth.transform.fhir.schema.ImmunizationStatus;
import org.endeavourhealth.transform.terminology.Read2;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;

public class ObservationTransformer {


    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Observation parser = new Observation(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(version, parser, csvProcessor, csvHelper);
            }
        } catch (FutureException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }

    }


    private static void createResource(String version,
                                       Observation parser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        //the EMIS test pack has observation records that are completely invalid, missing almost every mandatory value,
        //so detect that and skip the rows
        if (version.equals(EmisCsvTransformer.VERSION_TEST_PACK)
                && parser.getCodeId() == null) {
            return;
        }

        ResourceType resourceType = getTargetResourceType(parser, csvProcessor, csvHelper);
        switch (resourceType) {
            case Observation:
                createObservation(parser, csvProcessor, csvHelper);
                break;
            case Condition:
                createCondition(parser, csvProcessor, csvHelper);
                break;
            case Procedure:
                createProcedure(parser, csvProcessor, csvHelper);
                break;
            case AllergyIntolerance:
                createAllergy(parser, csvProcessor, csvHelper);
                break;
            case FamilyMemberHistory:
                createFamilyMemberHistory(parser, csvProcessor, csvHelper);
                break;
            case Immunization:
                createImmunization(parser, csvProcessor, csvHelper);
                break;
            case DiagnosticOrder:
                createDiagnosticOrder(parser, csvProcessor, csvHelper);
                break;
            case ReferralRequest:
                createReferralRequest(parser, csvProcessor, csvHelper);
                break;
/*            case Specimen:
                createSpecimen(observationParser, csvProcessor, csvHelper);
                break;*/
            default:
                throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
        }

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        //the observation row may also be referenced by a row in the Problems file, which
        //we've already processed. Check this, and complete processing if required.
        Condition fhirProblem = csvHelper.findProblem(observationGuid, patientGuid);
        if (fhirProblem != null) {
            completeProblem(parser, fhirProblem, csvProcessor, csvHelper);
        }

        //remove any cached links of child observations that link to the row we just processed. If the row used
        //the links, they'll already have been removed. If not, then we can't use them anyway.
        csvHelper.getAndRemoveObservationParentRelationships(observationGuid, patientGuid);
    }

    /**
     * the FHIR resource type is roughly derived from the ObservationType, although the Value and ReadCode
     * are also used as it's not a perfect match.
     */
    public static ResourceType getTargetResourceType(Observation parser, CsvProcessor csvProcessor, EmisCsvHelper csvHelper) throws Exception {

        String observationTypeString = parser.getObservationType();
        ObservationType observationType = ObservationType.fromValue(observationTypeString);
        Double value = parser.getValue();
        Long codeId = parser.getCodeId();

        if (observationType == ObservationType.VALUE
                || observationType == ObservationType.INVESTIGATION
                || value != null) {
            //anything with a value, even if not labelled as a Value has to go into an Observation resource
            return ResourceType.Observation;
        } else if (observationType == ObservationType.ALLERGY) {
            return ResourceType.AllergyIntolerance;
        } else if (observationType == ObservationType.TEST_REQUEST) {
            return ResourceType.DiagnosticOrder;
        } else if (observationType == ObservationType.IMMUNISATION) {
            return ResourceType.Immunization;
        } else if (observationType == ObservationType.FAMILY_HISTORY) {
            return ResourceType.FamilyMemberHistory;
        } else if (observationType == ObservationType.REFERRAL) {
            return ResourceType.ReferralRequest;
        } else if (observationType == ObservationType.DOCUMENT) {
            return ResourceType.Observation;
        } else if (observationType == ObservationType.ANNOTATED_IMAGE) {
            return ResourceType.Observation;
        } else if (observationType == ObservationType.OBSERVATION) {
            if (isProcedure(codeId, csvProcessor, csvHelper)) {
                return ResourceType.Procedure;
            } else {
                return ResourceType.Condition;
            }
        } else {
            throw new IllegalArgumentException("Unhandled ObservationType " + observationType);
        }
    }


    private static void completeProblem(Observation parser,
                                         Condition fhirProblem,
                                         CsvProcessor csvProcessor,
                                         EmisCsvHelper csvHelper) throws Exception {

        String patientGuid = parser.getPatientGuid();

        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirProblem);
            return;
        }

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirProblem.setOnset(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        //the entered date and person is stored in an extension
        createRecordedByExtension(fhirProblem, parser, csvHelper);
        createRecordedDateExtension(fhirProblem, parser);

        String consultationGuid = parser.getConsultationGuid();
        fhirProblem.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));

        Long codeId = parser.getCodeId();
        fhirProblem.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirProblem.setAsserter(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = parser.getAssociatedText();
        fhirProblem.setNotes(associatedText);

        //problems are added to the processor for saving after all files are finished, so it doesn't need saving here
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

    /*private static void createSpecimen(CareRecord_Observation observationParser,
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
    }*/

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

    private static void createReferralRequest(Observation parser,
                                         CsvProcessor csvProcessor,
                                         EmisCsvHelper csvHelper) throws Exception {

        //we have already parsed the ObservationReferral file, and will have created ReferralRequest
        //resources for all records in that file. So, first find any pre-created ReferralRequest for our record
        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        //as well as processing the Observation row into a FHIR resource, we
        //may also have a row in the Referral file that we've previously processed into
        //a FHIR ReferralRequest that we need to complete
        ReferralRequest fhirReferral = csvHelper.findReferral(observationGuid, patientGuid);
        if (fhirReferral == null) {

            //if we didn't have a record in the ObservationReferral file, we need to create a new one
            fhirReferral = new ReferralRequest();
            fhirReferral.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_REFERRAL_REQUEST));

            EmisCsvHelper.setUniqueId(fhirReferral, patientGuid, observationGuid);

            fhirReferral.setPatient(csvHelper.createPatientReference(patientGuid));
        }

        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirReferral);
            return;
        }

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirReferral.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String consultationGuid = parser.getConsultationGuid();
        fhirReferral.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));

        Long codeId = parser.getCodeId();
        fhirReferral.setReason(csvHelper.findClinicalCode(codeId, csvProcessor));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirReferral.setRequester(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = parser.getAssociatedText();
        fhirReferral.setDescription(associatedText);

        //the entered date and person are stored in extensions
        createRecordedByExtension(fhirReferral, parser, csvHelper);
        createRecordedDateExtension(fhirReferral, parser);
        createDocumentExtension(fhirReferral, parser);

        //assert that these fields are empty, as we don't stored them in this resource type,
        assertValueEmpty(fhirReferral, parser);
        assertNumericUnitEmpty(fhirReferral, parser);
        assertNumericRangeLowEmpty(fhirReferral, parser);
        assertNumericRangeHighEmpty(fhirReferral, parser);


        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                                            patientGuid,
                                            parser.getObservationGuid(),
                                            fhirReferral.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirReferral);

    }


    private static void createDiagnosticOrder(Observation parser,
                                              CsvProcessor csvProcessor,
                                              EmisCsvHelper csvHelper) throws Exception {
        DiagnosticOrder fhirOrder = new DiagnosticOrder();
        fhirOrder.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_ORDER));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirOrder, patientGuid, observationGuid);

        fhirOrder.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirOrder);
            return;
        }

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirOrder.setOrderer(csvHelper.createPractitionerReference(clinicianGuid));

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirOrder.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        Long codeId = parser.getCodeId();
        DiagnosticOrder.DiagnosticOrderItemComponent diagnosticOrderItemComponent = fhirOrder.addItem();
        diagnosticOrderItemComponent.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String associatedText = parser.getAssociatedText();
        fhirOrder.addNote(AnnotationHelper.createAnnotation(associatedText));

        DiagnosticOrder.DiagnosticOrderEventComponent diagnosticOrderEventComponent = fhirOrder.addEvent();
        diagnosticOrderEventComponent.setStatus(DiagnosticOrder.DiagnosticOrderStatus.REQUESTED);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        diagnosticOrderEventComponent.setDateTimeElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        //the entered date and person are stored in extensions
        createRecordedByExtension(fhirOrder, parser, csvHelper);
        createRecordedDateExtension(fhirOrder, parser);
        createDocumentExtension(fhirOrder, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirOrder, parser);
        assertNumericUnitEmpty(fhirOrder, parser);
        assertNumericRangeLowEmpty(fhirOrder, parser);
        assertNumericRangeHighEmpty(fhirOrder, parser);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirOrder.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirOrder);

    }

    private static void createAllergy(Observation parser,
                                      CsvProcessor csvProcessor,
                                      EmisCsvHelper csvHelper) throws Exception {

        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirAllergy, patientGuid, observationGuid);

        fhirAllergy.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirAllergy);
            return;
        }

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirAllergy.setRecorder(csvHelper.createPractitionerReference(clinicianGuid));

        Date enteredDate = parser.getEnteredDateTime();
        fhirAllergy.setRecordedDate(enteredDate);

        createRecordedByExtension(fhirAllergy, parser, csvHelper);
        createDocumentExtension(fhirAllergy, parser);

        Long codeId = parser.getCodeId();
        fhirAllergy.setSubstance(csvHelper.findClinicalCode(codeId, csvProcessor));

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirAllergy.setOnsetElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = parser.getAssociatedText();
        fhirAllergy.setNote(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = parser.getConsultationGuid();
        if (consultationGuid != null) {
            Reference reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            fhirAllergy.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

        assertValueEmpty(fhirAllergy, parser);
        assertNumericUnitEmpty(fhirAllergy, parser);
        assertNumericRangeLowEmpty(fhirAllergy, parser);
        assertNumericRangeHighEmpty(fhirAllergy, parser);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirAllergy.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirAllergy);

    }

    private static void createProcedure(Observation parser,
                                        CsvProcessor csvProcessor,
                                        EmisCsvHelper csvHelper) throws Exception {

        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirProcedure, patientGuid, observationGuid);

        fhirProcedure.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirProcedure);
            return;
        }

        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        Date enteredDate = parser.getEnteredDateTime();
        fhirProcedure.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(enteredDate)));

        Long codeId = parser.getCodeId();
        fhirProcedure.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirProcedure.setPerformed(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        Procedure.ProcedurePerformerComponent fhirPerformer = fhirProcedure.addPerformer();
        fhirPerformer.setActor(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = parser.getAssociatedText();
        fhirProcedure.addNotes(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = parser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirProcedure.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        //the entered date and person are stored in extensions
        createRecordedByExtension(fhirProcedure, parser, csvHelper);
        createRecordedDateExtension(fhirProcedure, parser);
        createDocumentExtension(fhirProcedure, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirProcedure, parser);
        assertNumericUnitEmpty(fhirProcedure, parser);
        assertNumericRangeLowEmpty(fhirProcedure, parser);
        assertNumericRangeHighEmpty(fhirProcedure, parser);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirProcedure.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirProcedure);
    }


    private static void createCondition(Observation parser,
                                        CsvProcessor csvProcessor,
                                        EmisCsvHelper csvHelper) throws Exception {

        Condition fhirCondition = new Condition();
        fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirCondition, patientGuid, observationGuid);

        fhirCondition.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirCondition);
            return;
        }

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirCondition.setAsserter(csvHelper.createPractitionerReference(clinicianGuid));

        Date enteredDate = parser.getEnteredDateTime();
        fhirCondition.setDateRecorded(enteredDate);

        //the entered by is stored in an extension
        createRecordedByExtension(fhirCondition, parser, csvHelper);
        createDocumentExtension(fhirCondition, parser);

        Long codeId = parser.getCodeId();
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        fhirCondition.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed

        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirCondition.setOnset(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = parser.getAssociatedText();
        fhirCondition.setNotes(associatedText);

        String consultationGuid = parser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirCondition.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = parser.getProblemUGuid();
        if (!Strings.isNullOrEmpty(problemGuid)) {
            Reference problemReference = csvHelper.createProblemReference(problemGuid, patientGuid);
            fhirCondition.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.CONDITION_PART_OF_PROBLEM, problemReference));
        }

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirCondition, parser);
        assertNumericUnitEmpty(fhirCondition, parser);
        assertNumericRangeLowEmpty(fhirCondition, parser);
        assertNumericRangeHighEmpty(fhirCondition, parser);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirCondition.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirCondition);
    }

    private static void createObservation(Observation parser,
                                          CsvProcessor csvProcessor,
                                          EmisCsvHelper csvHelper) throws Exception {

        org.hl7.fhir.instance.model.Observation fhirObservation = new org.hl7.fhir.instance.model.Observation();
        fhirObservation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirObservation, patientGuid, observationGuid);

        fhirObservation.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirObservation);
            return;
        }

        fhirObservation.setStatus(org.hl7.fhir.instance.model.Observation.ObservationStatus.UNKNOWN);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirObservation.setEffective(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Date enteredDate = parser.getEnteredDateTime();
        fhirObservation.setIssued(enteredDate);

        Long codeId = parser.getCodeId();
        fhirObservation.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirObservation.addPerformer(csvHelper.createPractitionerReference(clinicianGuid));

        String orgGuid = parser.getOrganisationGuid();
        fhirObservation.addPerformer(csvHelper.createOrganisationReference(orgGuid));

        Double value = parser.getValue();
        String units = parser.getNumericUnit();
        fhirObservation.setValue(QuantityHelper.createQuantity(value, units));

        Double low = parser.getNumericRangeLow();
        Double high = parser.getNumericRangeHigh();

        if (low != null || high != null) {

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

        String associatedText = parser.getAssociatedText();
        fhirObservation.setComments(associatedText);

        String consultationGuid = parser.getConsultationGuid();
        if (consultationGuid != null) {
            fhirObservation.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        List<EmisCsvHelper.ResourceRelationship> childObservationIds = csvHelper.getAndRemoveObservationParentRelationships(observationGuid, patientGuid);
        linkChildObservations(csvHelper, fhirObservation, patientGuid, childObservationIds);

        //the entered date and person are stored in extensions
        createRecordedByExtension(fhirObservation, parser, csvHelper);
        createRecordedDateExtension(fhirObservation, parser);
        createDocumentExtension(fhirObservation, parser);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirObservation.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirObservation);
    }
    private static void linkChildObservations(EmisCsvHelper csvHelper,
                                              org.hl7.fhir.instance.model.Observation fhirObservation,
                                              String patientGuid,
                                              List<EmisCsvHelper.ResourceRelationship> resourceRelationships) throws Exception {
        if (resourceRelationships == null) {
            return;
        }

        for (EmisCsvHelper.ResourceRelationship resourceRelationship : resourceRelationships) {

            Reference reference = csvHelper.createObservationReference(resourceRelationship.getDependentResourceGuid(), patientGuid);

            //check if the parent observation doesn't already have our ob linked to it
            boolean alreadyLinked = false;
            for (org.hl7.fhir.instance.model.Observation.ObservationRelatedComponent related: fhirObservation.getRelated()) {
                if (related.getType() == org.hl7.fhir.instance.model.Observation.ObservationRelationshipType.HASMEMBER
                        && related.getTarget().equalsShallow(reference)) {
                    alreadyLinked = true;
                    break;
                }
            }

            if (!alreadyLinked) {
                org.hl7.fhir.instance.model.Observation.ObservationRelatedComponent fhirRelation = fhirObservation.addRelated();
                fhirRelation.setType(org.hl7.fhir.instance.model.Observation.ObservationRelationshipType.HASMEMBER);
                fhirRelation.setTarget(reference);
            }
        }

    }

    private static void createFamilyMemberHistory(Observation parser,
                                                  CsvProcessor csvProcessor,
                                                  EmisCsvHelper csvHelper) throws Exception {

        FamilyMemberHistory fhirFamilyHistory = new FamilyMemberHistory();
        fhirFamilyHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirFamilyHistory, patientGuid, observationGuid);

        fhirFamilyHistory.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirFamilyHistory);
            return;
        }

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirFamilyHistory.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        fhirFamilyHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.HEALTHUNKNOWN);

        //most of the codes are just "FH: xxx" so can't be mapped to a definite family member relationship,
        //so just use the generic family member term
        fhirFamilyHistory.setRelationship(CodeableConceptHelper.createCodeableConcept(FamilyMember.FAMILY_MEMBER));

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent fhirCondition = fhirFamilyHistory.addCondition();

        Long codeId = parser.getCodeId();
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String associatedText = parser.getAssociatedText();
        fhirCondition.setNote(AnnotationHelper.createAnnotation(associatedText));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        Reference reference = csvHelper.createPractitionerReference(clinicianGuid);
        fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));

        String consultationGuid = parser.getConsultationGuid();
        if (consultationGuid != null) {
            reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

        //the entered date and person are stored in extensions
        createRecordedByExtension(fhirFamilyHistory, parser, csvHelper);
        createRecordedDateExtension(fhirFamilyHistory, parser);
        createDocumentExtension(fhirFamilyHistory, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirFamilyHistory, parser);
        assertNumericUnitEmpty(fhirFamilyHistory, parser);
        assertNumericRangeLowEmpty(fhirFamilyHistory, parser);
        assertNumericRangeHighEmpty(fhirFamilyHistory, parser);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirFamilyHistory.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirFamilyHistory);
    }

    private static void createImmunization(Observation parser,
                                           CsvProcessor csvProcessor,
                                           EmisCsvHelper csvHelper) throws Exception {

        Immunization fhirImmunisation = new Immunization();
        fhirImmunisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirImmunisation, patientGuid, observationGuid);

        fhirImmunisation.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            csvProcessor.deletePatientResource(patientGuid, fhirImmunisation);
            return;
        }

        fhirImmunisation.setStatus(ImmunizationStatus.COMPLETED.getCode());
        fhirImmunisation.setWasNotGiven(false);
        fhirImmunisation.setReported(false);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirImmunisation.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Long codeId = parser.getCodeId();
        fhirImmunisation.setVaccineCode(csvHelper.findClinicalCode(codeId, csvProcessor));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        Reference reference = csvHelper.createPractitionerReference(clinicianGuid);
        fhirImmunisation.setPerformer(reference);

        String consultationGuid = parser.getConsultationGuid();
        if (consultationGuid != null) {
            reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            fhirImmunisation.setEncounter(reference);
        }

        String associatedText = parser.getAssociatedText();
        fhirImmunisation.addNote(AnnotationHelper.createAnnotation(associatedText));

        //the entered date and person are stored in extensions
        createRecordedByExtension(fhirImmunisation, parser, csvHelper);
        createRecordedDateExtension(fhirImmunisation, parser);
        createDocumentExtension(fhirImmunisation, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirImmunisation, parser);
        assertNumericUnitEmpty(fhirImmunisation, parser);
        assertNumericRangeLowEmpty(fhirImmunisation, parser);
        assertNumericRangeHighEmpty(fhirImmunisation, parser);

        //if this record is linked to a problem, store this relationship in the helper
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirImmunisation.getResourceType());

        csvProcessor.savePatientResource(patientGuid, fhirImmunisation);
    }

    private static void createDocumentExtension(DomainResource resource, Observation parser) {

        String documentGuid = parser.getDocumentGuid();
        if (Strings.isNullOrEmpty(documentGuid)) {
            return;
        }

        Identifier fhirIdentifier = IdentifierHelper.createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_EMIS_DOCUMENT_GUID, documentGuid);
        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.EXTERNAL_DOCUMENT, fhirIdentifier));
    }

    private static void createRecordedByExtension(DomainResource resource, Observation parser, EmisCsvHelper emisCsvHelper) throws Exception {
        String enteredByGuid = parser.getEnteredByUserInRoleGuid();
        if (Strings.isNullOrEmpty(enteredByGuid)) {
            return;
        }

        Reference reference = emisCsvHelper.createPractitionerReference(enteredByGuid);
        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
    }

    private static void createRecordedDateExtension(DomainResource resource, Observation parser) throws Exception {
        Date enteredDateTime = parser.getEnteredDateTime();
        if (enteredDateTime == null) {
            return;
        }

        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(enteredDateTime)));
    }

    private static void assertValueEmpty(Resource destinationResource, Observation parser) throws Exception {
        if (parser.getValue() != null) {
            throw new FieldNotEmptyException("Value", destinationResource);
        }
    }
    private static void assertNumericRangeLowEmpty(Resource destinationResource, Observation parser) throws Exception {
        if (parser.getNumericRangeLow() != null) {
            throw new FieldNotEmptyException("NumericRangeLow", destinationResource);
        }
    }
    private static void assertNumericRangeHighEmpty(Resource destinationResource, Observation parser) throws Exception {
        if (parser.getNumericRangeHigh() != null) {
            throw new FieldNotEmptyException("NumericRangeHigh", destinationResource);
        }
    }
    private static void assertNumericUnitEmpty(Resource destinationResource, Observation parser) throws Exception {
        if (!Strings.isNullOrEmpty(parser.getNumericUnit())) {
            throw new FieldNotEmptyException("NumericUnit", destinationResource);
        }
    }


    /**
     "EffectiveDate",
     "EffectiveDatePrecision",
     "EnteredDate",
     "EnteredTime",
     "ClinicianUserInRoleGuid",
     "EnteredByUserInRoleGuid",
     "ParentObservationGuid",
     "CodeId",
     "ProblemGuid",
     "AssociatedText",
     "ConsultationGuid",
            "Value",
            "NumericUnit",
            "ObservationType",
            "      NumericRangeLow",
            "NumericRangeHigh",
             "DocumentGuid",
            "Deleted",
                "IsConfidential",
     */
}
