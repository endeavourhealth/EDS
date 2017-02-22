package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.transform.ResourceIdMapRepository;
import org.endeavourhealth.core.data.transform.models.ResourceIdMap;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.exceptions.FieldNotEmptyException;
import org.endeavourhealth.transform.emis.EmisCsvToFhirTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation;
import org.endeavourhealth.transform.emis.csv.schema.coding.ClinicalCodeType;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.common.fhir.schema.FamilyMember;
import org.endeavourhealth.common.fhir.schema.ImmunizationStatus;
import org.endeavourhealth.transform.terminology.Read2;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ObservationTransformer {

    private static ResourceIdMapRepository idMapRepository = new ResourceIdMapRepository();

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        AbstractCsvParser parser = parsers.get(Observation.class);
        while (parser.nextRecord()) {

            try {

                //depending whether deleting or saving, we go through a different path to find what
                //the target resource type should be
                Observation observationParser = (Observation)parser;
                if (observationParser.getDeleted() || observationParser.getIsConfidential()) {
                    deleteResource(version, observationParser, fhirResourceFiler, csvHelper);
                } else {
                    createResource(version, observationParser, fhirResourceFiler, csvHelper);
                }
            } catch (Exception ex) {
                fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
            }
        }
    }

    private static void deleteResource(String version,
                                       Observation parser,
                                       FhirResourceFiler fhirResourceFiler,
                                       EmisCsvHelper csvHelper) throws Exception {

        ResourceType resourceType = findOriginalTargetResourceType(fhirResourceFiler, csvHelper, parser);
        if (resourceType != null) {
            switch (resourceType) {
                case Observation:
                    createOrDeleteObservation(parser, fhirResourceFiler, csvHelper);
                    break;
                //checked below, as this is a special case
                /*case Condition:
                    createOrDeleteCondition(parser, csvProcessor, csvHelper);
                    break;*/
                case Procedure:
                    createOrDeleteProcedure(parser, fhirResourceFiler, csvHelper);
                    break;
                case AllergyIntolerance:
                    createOrDeleteAllergy(parser, fhirResourceFiler, csvHelper);
                    break;
                case FamilyMemberHistory:
                    createOrDeleteFamilyMemberHistory(parser, fhirResourceFiler, csvHelper);
                    break;
                case Immunization:
                    createOrDeleteImmunization(parser, fhirResourceFiler, csvHelper);
                    break;
                case DiagnosticOrder:
                    createOrDeleteDiagnosticOrder(parser, fhirResourceFiler, csvHelper);
                    break;
                case DiagnosticReport:
                    createOrDeleteDiagnosticReport(parser, fhirResourceFiler, csvHelper);
                    break;
                case Specimen:
                    createOrDeleteSpecimen(parser, fhirResourceFiler, csvHelper);
                    break;
                case ReferralRequest:
                    createOrDeleteReferralRequest(parser, fhirResourceFiler, csvHelper);
                    break;
    /*            case Specimen:
                    createOrDeleteSpecimen(observationParser, csvProcessor, csvHelper);
                    break;*/
                default:
                    throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
            }
        }

        //if EMIS has a non-Condition code (e.g. family history) that's flagged as a problem, we'll create
        //a FHIR Condition (for the problem) as well as the FHIR FamilyMemberHistory. The above code will
        //sort out deleting the FamilyMemberHistory, so we also need to see if the same EMIS observation
        //was saved as a condition too
        if (wasSavedAsResourceType(fhirResourceFiler, csvHelper, parser, ResourceType.Condition)) {
            createOrDeleteCondition(parser, fhirResourceFiler, csvHelper, true);
        }
    }


    /**
     * finds out what resource type an EMIS observation was previously saved as
     */
    private static ResourceType findOriginalTargetResourceType(FhirResourceFiler fhirResourceFiler, EmisCsvHelper csvHelper, Observation parser) {

        List<ResourceType> potentialResourceTypes = new ArrayList<>();
        potentialResourceTypes.add(ResourceType.Observation);
        //potentialResourceTypes.add(ResourceType.Condition); //don't check this here - as conditions are handled differently
        potentialResourceTypes.add(ResourceType.Procedure);
        potentialResourceTypes.add(ResourceType.AllergyIntolerance);
        potentialResourceTypes.add(ResourceType.FamilyMemberHistory);
        potentialResourceTypes.add(ResourceType.Immunization);
        potentialResourceTypes.add(ResourceType.DiagnosticOrder);
        potentialResourceTypes.add(ResourceType.Specimen);
        potentialResourceTypes.add(ResourceType.DiagnosticReport);
        potentialResourceTypes.add(ResourceType.ReferralRequest);
        
        for (ResourceType resourceType: potentialResourceTypes) {
            if (wasSavedAsResourceType(fhirResourceFiler, csvHelper, parser, resourceType)) {
                return resourceType;
            }
        }
        return null;
    }

    private static boolean wasSavedAsResourceType(FhirResourceFiler fhirResourceFiler, EmisCsvHelper csvHelper, Observation parser, ResourceType resourceType) {
        String uniqueId = csvHelper.createUniqueId(parser.getPatientGuid(), parser.getObservationGuid());
        ResourceIdMap mapping = idMapRepository.getResourceIdMap(fhirResourceFiler.getServiceId(), fhirResourceFiler.getSystemId(), resourceType.toString(), uniqueId);
        return mapping != null;
    }
    

    private static void createResource(String version,
                                       Observation parser,
                                       FhirResourceFiler fhirResourceFiler,
                                       EmisCsvHelper csvHelper) throws Exception {

        //the code ID should NEVER be null, but the test data has nulls, so adding this to handle those rows gracefully
        if ((version.equalsIgnoreCase(EmisCsvToFhirTransformer.VERSION_5_0)
                || version.equalsIgnoreCase(EmisCsvToFhirTransformer.VERSION_5_1))
                && parser.getCodeId() == null) {
            return;
        }

        ResourceType resourceType = getTargetResourceType(parser, fhirResourceFiler, csvHelper);
        switch (resourceType) {
            case Observation:
                createOrDeleteObservation(parser, fhirResourceFiler, csvHelper);
                break;
            case Condition:
                createOrDeleteCondition(parser, fhirResourceFiler, csvHelper, true);
                break;
            case Procedure:
                createOrDeleteProcedure(parser, fhirResourceFiler, csvHelper);
                break;
            case AllergyIntolerance:
                createOrDeleteAllergy(parser, fhirResourceFiler, csvHelper);
                break;
            case FamilyMemberHistory:
                createOrDeleteFamilyMemberHistory(parser, fhirResourceFiler, csvHelper);
                break;
            case Immunization:
                createOrDeleteImmunization(parser, fhirResourceFiler, csvHelper);
                break;
            case DiagnosticOrder:
                createOrDeleteDiagnosticOrder(parser, fhirResourceFiler, csvHelper);
                break;
            case DiagnosticReport:
                createOrDeleteDiagnosticReport(parser, fhirResourceFiler, csvHelper);
                break;
            case Specimen:
                createOrDeleteSpecimen(parser, fhirResourceFiler, csvHelper);
                break;
            case ReferralRequest:
                createOrDeleteReferralRequest(parser, fhirResourceFiler, csvHelper);
                break;
/*            case Specimen:
                createOrDeleteSpecimen(observationParser, csvProcessor, csvHelper);
                break;*/
            default:
                throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
        }

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        //if we didn't transform our record into a Condition, but the Problem CSV had a row for
        //it, then we'll also have part-created a Condition resource for it, which we need to finish populating
        if (resourceType != ResourceType.Condition) {
            Condition fhirProblem = csvHelper.findProblem(observationGuid, patientGuid);
            if (fhirProblem != null) {
                createOrDeleteCondition(parser, fhirResourceFiler, csvHelper, false);
            }
        }

        //remove any cached links of child observations that link to the row we just processed. If the row used
        //the links, they'll already have been removed. If not, then we can't use them anyway.
        csvHelper.getAndRemoveObservationParentRelationships(observationGuid, patientGuid);
    }

    /**
     * the FHIR resource type is roughly derived from the code category primarily, although the Value and ReadCode
     * are also used as it's not a perfect match.
     */
    public static ResourceType getTargetResourceType(Observation parser,
                                                     FhirResourceFiler fhirResourceFiler,
                                                     EmisCsvHelper csvHelper) throws Exception {

        Long codeId = parser.getCodeId();
        ClinicalCodeType codeType = csvHelper.findClinicalCodeType(codeId);
        Double value = parser.getValue();

        if (value != null
            || codeType == ClinicalCodeType.Biochemistry
            || codeType == ClinicalCodeType.Biological_Values
            || codeType == ClinicalCodeType.Cyology_Histology
            || codeType == ClinicalCodeType.Haematology
            || codeType == ClinicalCodeType.Health_Management
            || codeType == ClinicalCodeType.Immunology
            || codeType == ClinicalCodeType.Microbiology
            || codeType == ClinicalCodeType.Radiology
            || codeType == ClinicalCodeType.Symptoms_Findings
            || codeType == ClinicalCodeType.Procedure //note, the codes is this category aren't actually "procedures"
            || codeType == ClinicalCodeType.Adminisation_Documents_Attachments
            || codeType == ClinicalCodeType.Body_Structure //dental structures
            || codeType == ClinicalCodeType.Care_Episode_Outcome
            || codeType == ClinicalCodeType.Dental_Finding
            || codeType == ClinicalCodeType.Diagnostics
            || codeType == ClinicalCodeType.Discharged_From_Service
            || codeType == ClinicalCodeType.EMIS_Qualifier //not really suited to any FHIR resource, but they have to go somewhere
            || codeType == ClinicalCodeType.Ethnicity
            || codeType == ClinicalCodeType.HMP //not necessarily suited to FHIR observations, but seems the best match
            || codeType == ClinicalCodeType.Intervention_Category
            || codeType == ClinicalCodeType.Intervention_Target
            || codeType == ClinicalCodeType.KC60 //looks like these codes are a mix of procedures, conditions and vaccinations, but no way to distinguish them apart
            || codeType == ClinicalCodeType.Marital_Status
            || codeType == ClinicalCodeType.Nationality
            || codeType == ClinicalCodeType.Nursing_Problem
            || codeType == ClinicalCodeType.Nursing_Problem_Domain
            || codeType == ClinicalCodeType.Obsteterics_Birth
            || codeType == ClinicalCodeType.Person_Health_Social
            || codeType == ClinicalCodeType.Planned_Dental
            || codeType == ClinicalCodeType.Problem_Rating_Scale
            || codeType == ClinicalCodeType.Reason_For_Care
            || codeType == ClinicalCodeType.Referral_Activity
            || codeType == ClinicalCodeType.Referral_Rejected
            || codeType == ClinicalCodeType.Referral_Withdrawn
            || codeType == ClinicalCodeType.Regiment
            || codeType == ClinicalCodeType.Religion
            || codeType == ClinicalCodeType.Trade_Branch
            || codeType == ClinicalCodeType.Unset) {

            if (isDiagnosticReport(parser, fhirResourceFiler, csvHelper)) {
                return ResourceType.DiagnosticReport;
            } else {
                return ResourceType.Observation;
            }

        } else if (codeType == ClinicalCodeType.Conditions_Operations_Procedures) {

            if (isProcedure(codeId, fhirResourceFiler, csvHelper)) {
                return ResourceType.Procedure;
            } else if (isDisorder(codeId, fhirResourceFiler, csvHelper)) {
                return ResourceType.Condition;
            } else {
                return ResourceType.Observation;
            }

        } else if (codeType == ClinicalCodeType.Allergy_Adverse_Drug_Reations
            || codeType == ClinicalCodeType.Allergy_Adverse_Reations) {

            return ResourceType.AllergyIntolerance;

        } else if (codeType == ClinicalCodeType.Dental_Disorder) {

            return ResourceType.Condition;

        } else if (codeType == ClinicalCodeType.Dental_Procedure) {

            return ResourceType.Condition;

        } else if (codeType == ClinicalCodeType.Family_History) {

            return ResourceType.FamilyMemberHistory;

        } else if (codeType == ClinicalCodeType.Immunisations) {

            return ResourceType.Immunization;

        } else if (codeType == ClinicalCodeType.Investigation_Requests) {

            return ResourceType.DiagnosticOrder;

        } else if (codeType == ClinicalCodeType.Pathology_Specimen) {

            return ResourceType.Specimen;

        } else if (codeType == ClinicalCodeType.Referral) {

            return ResourceType.ReferralRequest;

        } else {
            throw new IllegalArgumentException("Unhandled codeType " + codeType);
        }
    }

    private static boolean isDisorder(Long codeId, FhirResourceFiler fhirResourceFiler, EmisCsvHelper csvHelper) throws Exception {

        CodeableConcept fhirConcept = csvHelper.findClinicalCode(codeId);
        for (Coding coding: fhirConcept.getCoding()) {

            //would prefer to check for procedures using Snomed, but this Read2 is simple and works
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_READ2)) {
                return Read2.isDisorder(coding.getCode());
            }

        }

        return false;
    }

    /*public static ResourceType getTargetResourceType(Observation parser,
                                                     CsvProcessor csvProcessor,
                                                     EmisCsvHelper csvHelper) throws Exception {

        String observationTypeString = parser.getObservationType();
        ObservationType observationType = ObservationType.fromValue(observationTypeString);
        Double value = parser.getValue();

        if (observationType == ObservationType.VALUE
                || observationType == ObservationType.INVESTIGATION
                || value != null) { //anything with a value, even if not labelled as a Value has to go into an Observation resource
            if (isDiagnosticReport(parser, csvProcessor, csvHelper)) {
                return ResourceType.DiagnosticReport;
            } else {
                return ResourceType.Observation;
            }

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
            if (isProcedure(parser, csvProcessor, csvHelper)) {
                return ResourceType.Procedure;
            } else {
                return ResourceType.Condition;
            }

        } else {
            throw new IllegalArgumentException("Unhandled ObservationType " + observationType);
        }
    }
*/
    private static boolean isDiagnosticReport(Observation parser,
                                              FhirResourceFiler fhirResourceFiler,
                                              EmisCsvHelper csvHelper) throws Exception {

        //if it's got a value, it's not a diagnostic report, as it'll be an investigation within a report
        if (parser.getValue() != null) {
            return false;
        }

        //if it doesn't have any child observations linking to it, then don't store as a report
        if (!csvHelper.hasChildObservations(parser.getObservationGuid(), parser.getPatientGuid())) {
            return false;
        }

        //if we pass the above checks, then check what kind of code it is. If one of the below types, then store as a report.
        Long codeId = parser.getCodeId();
        ClinicalCodeType codeType = csvHelper.findClinicalCodeType(codeId);
        return codeType == ClinicalCodeType.Biochemistry
            || codeType == ClinicalCodeType.Cyology_Histology
            || codeType == ClinicalCodeType.Haematology
            || codeType == ClinicalCodeType.Immunology
            || codeType == ClinicalCodeType.Microbiology
            || codeType == ClinicalCodeType.Radiology
            || codeType == ClinicalCodeType.Health_Management;
    }

    private static boolean isProcedure(Long codeId,
                                       FhirResourceFiler fhirResourceFiler,
                                       EmisCsvHelper csvHelper) throws Exception {

        CodeableConcept fhirConcept = csvHelper.findClinicalCode(codeId);
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

        return false;
    }

    private static void createOrDeleteReferralRequest(Observation parser,
                                                      FhirResourceFiler fhirResourceFiler,
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
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirReferral);
            return;
        }

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirReferral.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirReferral.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        Long codeId = parser.getCodeId();
        //after discussion, the observation code should go into the service requested field
        fhirReferral.addServiceRequested(csvHelper.findClinicalCode(codeId));
        //fhirReferral.setType(csvHelper.findClinicalCode(codeId));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();

        Reference practitionerReference = csvHelper.createPractitionerReference(clinicianGuid);
        if (!fhirReferral.hasRequester()) {
            fhirReferral.setRequester(practitionerReference);
        } else {
            //in the referral file transform we set the requester to a reference to an organisation
            Reference requesterReference = fhirReferral.getRequester();
            Reference ourOrgReference = csvHelper.createOrganisationReference(parser.getOrganisationGuid());

            //if that requester is OUR organisation, then replace the requester with the specific clinician we have
            if (requesterReference.equalsShallow(ourOrgReference)) {
                fhirReferral.setRequester(practitionerReference);
            } else {
                //if the referral didn't come FROM our organisation, then our clinician should be the recipient
                fhirReferral.addRecipient(practitionerReference);
            }
        }

        String associatedText = parser.getAssociatedText();
        fhirReferral.setDescription(associatedText);

        //the entered date and person are stored in extensions
        addRecordedByExtension(fhirReferral, parser, csvHelper);
        addRecordedDateExtension(fhirReferral, parser);
        addDocumentExtension(fhirReferral, parser);

        //assert that these fields are empty, as we don't stored them in this resource type,
        assertValueEmpty(fhirReferral, parser);
        assertNumericUnitEmpty(fhirReferral, parser);
        assertNumericRangeLowEmpty(fhirReferral, parser);
        assertNumericRangeHighEmpty(fhirReferral, parser);


        //if this record is linked to a problem, store this relationship in the helper
/*
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                                            patientGuid,
                                            parser.getObservationGuid(),
                                            fhirReferral.getResourceType());
*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirReferral);

    }


    private static void createOrDeleteDiagnosticOrder(Observation parser,
                                                      FhirResourceFiler fhirResourceFiler,
                                                      EmisCsvHelper csvHelper) throws Exception {
        DiagnosticOrder fhirOrder = new DiagnosticOrder();
        fhirOrder.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_ORDER));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirOrder, patientGuid, observationGuid);

        fhirOrder.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirOrder);
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
        diagnosticOrderItemComponent.setCode(csvHelper.findClinicalCode(codeId));

        String associatedText = parser.getAssociatedText();
        fhirOrder.addNote(AnnotationHelper.createAnnotation(associatedText));

        DiagnosticOrder.DiagnosticOrderEventComponent diagnosticOrderEventComponent = fhirOrder.addEvent();
        diagnosticOrderEventComponent.setStatus(DiagnosticOrder.DiagnosticOrderStatus.REQUESTED);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        diagnosticOrderEventComponent.setDateTimeElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        //the entered date and person are stored in extensions
        addRecordedByExtension(fhirOrder, parser, csvHelper);
        addRecordedDateExtension(fhirOrder, parser);
        addDocumentExtension(fhirOrder, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirOrder, parser);
        assertNumericUnitEmpty(fhirOrder, parser);
        assertNumericRangeLowEmpty(fhirOrder, parser);
        assertNumericRangeHighEmpty(fhirOrder, parser);

        //if this record is linked to a problem, store this relationship in the helper
/*
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirOrder.getResourceType());
*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirOrder);

    }

    private static void createOrDeleteSpecimen(Observation parser, FhirResourceFiler fhirResourceFiler, EmisCsvHelper csvHelper) throws Exception {

        Specimen fhirSpecimen = new Specimen();
        fhirSpecimen.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SPECIMIN));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirSpecimen, patientGuid, observationGuid);

        fhirSpecimen.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirSpecimen);
            return;
        }

        Specimen.SpecimenCollectionComponent fhirCollection = new Specimen.SpecimenCollectionComponent();
        fhirSpecimen.setCollection(fhirCollection);

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirCollection.setCollector(csvHelper.createPractitionerReference(clinicianGuid));

        Long codeId = parser.getCodeId();
        fhirSpecimen.setType(csvHelper.findClinicalCode(codeId));

        String associatedText = parser.getAssociatedText();
        fhirCollection.addComment(associatedText);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirCollection.setCollected(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        //the entered date and person are stored in extensions
        addEncounterExtension(fhirSpecimen, parser, csvHelper, patientGuid);
        addRecordedByExtension(fhirSpecimen, parser, csvHelper);
        addRecordedDateExtension(fhirSpecimen, parser);
        addDocumentExtension(fhirSpecimen, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirSpecimen, parser);
        assertNumericUnitEmpty(fhirSpecimen, parser);
        assertNumericRangeLowEmpty(fhirSpecimen, parser);
        assertNumericRangeHighEmpty(fhirSpecimen, parser);

        //if this record is linked to a problem, store this relationship in the helper
/*
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirOrder.getResourceType());
*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirSpecimen);

    }


    private static void createOrDeleteAllergy(Observation parser,
                                              FhirResourceFiler fhirResourceFiler,
                                              EmisCsvHelper csvHelper) throws Exception {

        AllergyIntolerance fhirAllergy = new AllergyIntolerance();
        fhirAllergy.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ALLERGY_INTOLERANCE));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirAllergy, patientGuid, observationGuid);

        fhirAllergy.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirAllergy);
            return;
        }

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirAllergy.setRecorder(csvHelper.createPractitionerReference(clinicianGuid));

        Date enteredDate = parser.getEnteredDateTime();
        fhirAllergy.setRecordedDate(enteredDate);

        addRecordedByExtension(fhirAllergy, parser, csvHelper);
        addDocumentExtension(fhirAllergy, parser);

        Long codeId = parser.getCodeId();
        fhirAllergy.setSubstance(csvHelper.findClinicalCode(codeId));

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirAllergy.setOnsetElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = parser.getAssociatedText();
        fhirAllergy.setNote(AnnotationHelper.createAnnotation(associatedText));

        addEncounterExtension(fhirAllergy, parser, csvHelper, patientGuid);

        assertValueEmpty(fhirAllergy, parser);
        assertNumericUnitEmpty(fhirAllergy, parser);
        assertNumericRangeLowEmpty(fhirAllergy, parser);
        assertNumericRangeHighEmpty(fhirAllergy, parser);

        //if this record is linked to a problem, store this relationship in the helper
/*
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirAllergy.getResourceType());
*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirAllergy);

    }

    private static void createOrDeleteDiagnosticReport(Observation parser,
                                                      FhirResourceFiler fhirResourceFiler,
                                                      EmisCsvHelper csvHelper) throws Exception {
        DiagnosticReport fhirReport = new DiagnosticReport();
        fhirReport.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_REPORT));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirReport, patientGuid, observationGuid);

        fhirReport.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirReport);
            return;
        }

        fhirReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        if (!Strings.isNullOrEmpty(clinicianGuid)) {
            Reference reference = csvHelper.createPractitionerReference(clinicianGuid);
            fhirReport.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.DIAGNOSTIC_REPORT_FILED_BY, reference));
        }

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirReport.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        Long codeId = parser.getCodeId();
        fhirReport.setCode(csvHelper.findClinicalCode(codeId));

        String associatedText = parser.getAssociatedText();
        if (!Strings.isNullOrEmpty(associatedText)) {
            fhirReport.setConclusion(associatedText);
        }

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirReport.setEffective(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        List<String> childObservations = csvHelper.getAndRemoveObservationParentRelationships(observationGuid, patientGuid);
        if (childObservations != null) {
            List<Reference> references = ReferenceHelper.createReferences(childObservations);
            for (Reference reference : references) {
                fhirReport.getResult().add(reference);
            }
        }

        //the entered date and person are stored in extensions
        addRecordedByExtension(fhirReport, parser, csvHelper);
        addRecordedDateExtension(fhirReport, parser);
        addDocumentExtension(fhirReport, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirReport, parser);
        assertNumericUnitEmpty(fhirReport, parser);
        assertNumericRangeLowEmpty(fhirReport, parser);
        assertNumericRangeHighEmpty(fhirReport, parser);

        //if this record is linked to a problem, store this relationship in the helper
        /*csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirReport.getResourceType());*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirReport);

    }

    private static void createOrDeleteProcedure(Observation parser,
                                                FhirResourceFiler fhirResourceFiler,
                                                EmisCsvHelper csvHelper) throws Exception {

        Procedure fhirProcedure = new Procedure();
        fhirProcedure.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROCEDURE));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirProcedure, patientGuid, observationGuid);

        fhirProcedure.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirProcedure);
            return;
        }

        fhirProcedure.setStatus(Procedure.ProcedureStatus.COMPLETED);

        Long codeId = parser.getCodeId();
        fhirProcedure.setCode(csvHelper.findClinicalCode(codeId));

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirProcedure.setPerformed(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        Procedure.ProcedurePerformerComponent fhirPerformer = fhirProcedure.addPerformer();
        fhirPerformer.setActor(csvHelper.createPractitionerReference(clinicianGuid));

        String associatedText = parser.getAssociatedText();
        fhirProcedure.addNotes(AnnotationHelper.createAnnotation(associatedText));

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirProcedure.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        //the entered date and person are stored in extensions
        addRecordedByExtension(fhirProcedure, parser, csvHelper);
        addRecordedDateExtension(fhirProcedure, parser);
        addDocumentExtension(fhirProcedure, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirProcedure, parser);
        assertNumericUnitEmpty(fhirProcedure, parser);
        assertNumericRangeLowEmpty(fhirProcedure, parser);
        assertNumericRangeHighEmpty(fhirProcedure, parser);

        //if this record is linked to a problem, store this relationship in the helper
/*
        csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirProcedure.getResourceType());
*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirProcedure);
    }


    private static void createOrDeleteCondition(Observation parser,
                                                FhirResourceFiler fhirResourceFiler,
                                                EmisCsvHelper csvHelper,
                                                boolean validateUnusedFields) throws Exception {

        //we have already parsed the Problem file, and will have created Condition
        //resources for all records in that file. So, first find any pre-created Condition for our record
        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        //as well as processing the Observation row into a FHIR resource, we
        //may also have a row in the Problem file that we've previously processed into
        //a FHIR Condition that we need to complete
        Condition fhirCondition = csvHelper.findProblem(observationGuid, patientGuid);

        //if we didn't find a Condtion from the problem map, then it's not a problem and should be
        //treated just as a standalone condition resource
        if (fhirCondition == null) {

            //if we didn't have a record in the Problem file, we need to create a new one
            fhirCondition = new Condition();
            fhirCondition.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

            EmisCsvHelper.setUniqueId(fhirCondition, patientGuid, observationGuid);

            fhirCondition.setPatient(csvHelper.createPatientReference(patientGuid));
        }

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirCondition);
            return;
        }

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirCondition.setAsserter(csvHelper.createPractitionerReference(clinicianGuid));

        Date enteredDate = parser.getEnteredDateTime();
        fhirCondition.setDateRecorded(enteredDate);

        //the entered by is stored in an extension
        addRecordedByExtension(fhirCondition, parser, csvHelper);
        addDocumentExtension(fhirCondition, parser);

        Long codeId = parser.getCodeId();
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId));

        //we don't have enough information to set this accurately, so taking out
        //fhirCondition.setClinicalStatus("active"); //if we have a Problem record for this condition, this status may be changed

        fhirCondition.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirCondition.setOnset(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        String associatedText = parser.getAssociatedText();
        //if the condition is a problem, there may already be text in the note variable, from the Comment field on the problem table
        if (fhirCondition.hasNotes()) {
            fhirCondition.setNotes(associatedText + "\n" + fhirCondition.getNotes());
        } else {
            fhirCondition.setNotes(associatedText);
        }

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirCondition.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        String problemGuid = parser.getProblemUGuid();
        if (!Strings.isNullOrEmpty(problemGuid)) {
            Reference problemReference = csvHelper.createProblemReference(problemGuid, patientGuid);
            fhirCondition.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.CONDITION_PART_OF_PROBLEM, problemReference));
        }

        //assert that these cells are empty, as we don't stored them in this resource type
        //but only if we've passed in the boolean to say so - if this is false, we've already processed this
        //row in the CSV into a different resource type, so will have used the values and don't need to worry we're ignoring them now
        if (validateUnusedFields) {
            assertValueEmpty(fhirCondition, parser);
            assertNumericUnitEmpty(fhirCondition, parser);
            assertNumericRangeLowEmpty(fhirCondition, parser);
            assertNumericRangeHighEmpty(fhirCondition, parser);
        }

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirCondition);
    }

    private static void createOrDeleteObservation(Observation parser,
                                                  FhirResourceFiler fhirResourceFiler,
                                                  EmisCsvHelper csvHelper) throws Exception {

        org.hl7.fhir.instance.model.Observation fhirObservation = new org.hl7.fhir.instance.model.Observation();
        fhirObservation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirObservation, patientGuid, observationGuid);

        fhirObservation.setSubject(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirObservation);
            return;
        }

        fhirObservation.setStatus(org.hl7.fhir.instance.model.Observation.ObservationStatus.UNKNOWN);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirObservation.setEffective(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Long codeId = parser.getCodeId();
        fhirObservation.setCode(csvHelper.findClinicalCode(codeId));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        fhirObservation.addPerformer(csvHelper.createPractitionerReference(clinicianGuid));

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
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            fhirObservation.setEncounter(csvHelper.createEncounterReference(consultationGuid, patientGuid));
        }

        List<String> childObservations = csvHelper.getAndRemoveObservationParentRelationships(observationGuid, patientGuid);
        if (childObservations != null) {
            List<Reference> references = ReferenceHelper.createReferences(childObservations);
            for (Reference reference : references) {
                org.hl7.fhir.instance.model.Observation.ObservationRelatedComponent fhirRelation = fhirObservation.addRelated();
                fhirRelation.setType(org.hl7.fhir.instance.model.Observation.ObservationRelationshipType.HASMEMBER);
                fhirRelation.setTarget(reference);
            }
        }

        //if we have BP readings from child observations, include them in the components for this observation too
        List<org.hl7.fhir.instance.model.Observation.ObservationComponentComponent> observationComponents = csvHelper.findBpComponents(observationGuid, patientGuid);
        if (observationComponents != null) {
            for (org.hl7.fhir.instance.model.Observation.ObservationComponentComponent component: observationComponents) {
                fhirObservation.getComponent().add(component);
            }
        }

        //the entered date and person are stored in extensions
        addRecordedByExtension(fhirObservation, parser, csvHelper);
        addRecordedDateExtension(fhirObservation, parser);
        addDocumentExtension(fhirObservation, parser);

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirObservation);
    }

    private static void createOrDeleteFamilyMemberHistory(Observation parser,
                                                          FhirResourceFiler fhirResourceFiler,
                                                          EmisCsvHelper csvHelper) throws Exception {

        FamilyMemberHistory fhirFamilyHistory = new FamilyMemberHistory();
        fhirFamilyHistory.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_FAMILY_MEMBER_HISTORY));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirFamilyHistory, patientGuid, observationGuid);

        fhirFamilyHistory.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirFamilyHistory);
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
        fhirCondition.setCode(csvHelper.findClinicalCode(codeId));

        String associatedText = parser.getAssociatedText();
        fhirCondition.setNote(AnnotationHelper.createAnnotation(associatedText));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        Reference reference = csvHelper.createPractitionerReference(clinicianGuid);
        fhirFamilyHistory.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.FAMILY_MEMBER_HISTORY_REPORTED_BY, reference));

        //the entered date and person are stored in extensions
        addEncounterExtension(fhirFamilyHistory, parser, csvHelper, patientGuid);
        addRecordedByExtension(fhirFamilyHistory, parser, csvHelper);
        addRecordedDateExtension(fhirFamilyHistory, parser);
        addDocumentExtension(fhirFamilyHistory, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirFamilyHistory, parser);
        assertNumericUnitEmpty(fhirFamilyHistory, parser);
        assertNumericRangeLowEmpty(fhirFamilyHistory, parser);
        assertNumericRangeHighEmpty(fhirFamilyHistory, parser);

        //if this record is linked to a problem, store this relationship in the helper
        /*csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirFamilyHistory.getResourceType());*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirFamilyHistory);
    }

    private static void createOrDeleteImmunization(Observation parser,
                                                   FhirResourceFiler fhirResourceFiler,
                                                   EmisCsvHelper csvHelper) throws Exception {

        Immunization fhirImmunisation = new Immunization();
        fhirImmunisation.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_IMMUNIZATION));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirImmunisation, patientGuid, observationGuid);

        fhirImmunisation.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (parser.getDeleted() || parser.getIsConfidential()) {
            fhirResourceFiler.deletePatientResource(parser.getCurrentState(), patientGuid, fhirImmunisation);
            return;
        }

        fhirImmunisation.setStatus(ImmunizationStatus.COMPLETED.getCode());
        fhirImmunisation.setWasNotGiven(false);
        fhirImmunisation.setReported(false);

        Date effectiveDate = parser.getEffectiveDate();
        String effectiveDatePrecision = parser.getEffectiveDatePrecision();
        fhirImmunisation.setDateElement(EmisDateTimeHelper.createDateTimeType(effectiveDate, effectiveDatePrecision));

        Long codeId = parser.getCodeId();
        fhirImmunisation.setVaccineCode(csvHelper.findClinicalCode(codeId));

        String clinicianGuid = parser.getClinicianUserInRoleGuid();
        Reference reference = csvHelper.createPractitionerReference(clinicianGuid);
        fhirImmunisation.setPerformer(reference);

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {
            reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            fhirImmunisation.setEncounter(reference);
        }

        String associatedText = parser.getAssociatedText();
        fhirImmunisation.addNote(AnnotationHelper.createAnnotation(associatedText));

        //the entered date and person are stored in extensions
        addRecordedByExtension(fhirImmunisation, parser, csvHelper);
        addRecordedDateExtension(fhirImmunisation, parser);
        addDocumentExtension(fhirImmunisation, parser);

        //assert that these cells are empty, as we don't stored them in this resource type
        assertValueEmpty(fhirImmunisation, parser);
        assertNumericUnitEmpty(fhirImmunisation, parser);
        assertNumericRangeLowEmpty(fhirImmunisation, parser);
        assertNumericRangeHighEmpty(fhirImmunisation, parser);

        //if this record is linked to a problem, store this relationship in the helper
        /*csvHelper.cacheProblemRelationship(parser.getProblemUGuid(),
                patientGuid,
                observationGuid,
                fhirImmunisation.getResourceType());*/

        fhirResourceFiler.savePatientResource(parser.getCurrentState(), patientGuid, fhirImmunisation);
    }

    private static void addDocumentExtension(DomainResource resource, Observation parser) {

        String documentGuid = parser.getDocumentGuid();
        if (Strings.isNullOrEmpty(documentGuid)) {
            return;
        }

        Identifier fhirIdentifier = IdentifierHelper.createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_EMIS_DOCUMENT_GUID, documentGuid);
        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.EXTERNAL_DOCUMENT, fhirIdentifier));
    }

    private static void addRecordedByExtension(DomainResource resource, Observation parser, EmisCsvHelper emisCsvHelper) throws Exception {
        String enteredByGuid = parser.getEnteredByUserInRoleGuid();
        if (Strings.isNullOrEmpty(enteredByGuid)) {
            return;
        }

        Reference reference = emisCsvHelper.createPractitionerReference(enteredByGuid);
        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_BY, reference));
    }

    private static void addRecordedDateExtension(DomainResource resource, Observation parser) throws Exception {
        Date enteredDateTime = parser.getEnteredDateTime();
        if (enteredDateTime == null) {
            return;
        }

        resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.RECORDED_DATE, new DateTimeType(enteredDateTime)));
    }

    private static void addEncounterExtension(DomainResource resource, Observation parser, EmisCsvHelper csvHelper, String patientGuid) throws Exception {

        String consultationGuid = parser.getConsultationGuid();
        if (!Strings.isNullOrEmpty(consultationGuid)) {

            Reference reference = csvHelper.createEncounterReference(consultationGuid, patientGuid);
            resource.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ASSOCIATED_ENCOUNTER, reference));
        }

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
