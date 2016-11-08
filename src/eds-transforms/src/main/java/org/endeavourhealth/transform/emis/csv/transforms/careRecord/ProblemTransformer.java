package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.transform.ResourceIdMapRepository;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.exceptions.ResourceDeletedException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.ProblemRelationshipType;
import org.endeavourhealth.transform.fhir.schema.ProblemSignificance;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProblemTransformer {

    public static void transform(String version,
                                 Map<Class, List<AbstractCsvParser>> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        for (AbstractCsvParser parser: parsers.get(Problem.class)) {

            while (parser.nextRecord()) {

                try {
                    createResource((Problem)parser, csvProcessor, csvHelper, version);
                } catch (Exception ex) {
                    csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
                }
            }
        }

    }

    private static void createResource(Problem parser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper,
                                       String version) throws Exception {

        Condition fhirProblem = new Condition();
        fhirProblem.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROBLEM));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirProblem, patientGuid, observationGuid);

        fhirProblem.setPatient(csvHelper.createPatientReference(patientGuid));

        //the deleted fields isn't present in the test pack, so need to check the version first
        if (!version.equals(EmisCsvTransformer.VERSION_TEST_PACK)
            && parser.getDeleted()) {

            //if we have a row in the Problem file that's deleted but the row in the Observation file
            //isn't being deleted, then the Problem needs to be down-graded to just a Condition, so set
            //the profile URI accordingly
            fhirProblem.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_CONDITION));

            //the problem is actually saved in the ObservationTransformer, so just cache for later
            csvHelper.cacheProblem(observationGuid, patientGuid, fhirProblem);
            return;
        }

        String comments = parser.getComment();
        fhirProblem.setNotes(comments);

        Date endDate = parser.getEndDate();
        if (endDate != null) {
            String endDatePrecision = parser.getEndDatePrecision(); //NOTE; documentation refers to this as EffectiveDate, but this should be EndDate
            fhirProblem.setAbatement(EmisDateTimeHelper.createDateType(endDate, endDatePrecision));
        } else {

            //if there's no end date, the problem may still be ended, which is in the status description
            String problemStatus = parser.getProblemStatusDescription();
            if (problemStatus.equalsIgnoreCase("Past Problem")) {
                fhirProblem.setAbatement(new BooleanType(true));
            }
        }

        Integer expectedDuration = parser.getExpectedDuration();
        if (expectedDuration != null) {
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_EXPECTED_DURATION, new IntegerType(expectedDuration.intValue())));
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
        List<Reference> previousReferences = findPreviousLinkedReferences(csvHelper, csvProcessor, fhirProblem.getId());
        if (previousReferences != null && !previousReferences.isEmpty()) {
            csvHelper.addLinkedItemsToProblem(fhirProblem, previousReferences);
        }

        //apply any linked items from this extract
        List<String> linkedResources = csvHelper.getAndRemoveProblemRelationships(observationGuid, patientGuid);
        if (linkedResources != null) {
            List<Reference> references = ReferenceHelper.createReferences(linkedResources);
            csvHelper.addLinkedItemsToProblem(fhirProblem, references);
        }

        //the problem is actually saved in the ObservationTransformer, so just cache for later
        csvHelper.cacheProblem(observationGuid, patientGuid, fhirProblem);
    }

    private static List<Reference> findPreviousLinkedReferences(EmisCsvHelper csvHelper, CsvProcessor csvProcessor, String problemId) throws Exception {
        try {

            ResourceIdMapRepository repository = new ResourceIdMapRepository();
            List<Reference> ret = new ArrayList<>();

            Condition previousVersion = (Condition)csvHelper.retrieveResource(problemId, ResourceType.Condition, csvProcessor);

            if (previousVersion.hasContained()) {
                for (Resource contained: previousVersion.getContained()) {
                    if (contained instanceof List_) {
                        List_ list = (List_)contained;
                        for (List_.ListEntryComponent entry: list.getEntry()) {
                            Reference previousReference = entry.getItem();

                            //the reference we have has already been mapped to an EDS ID, so we need to un-map it
                            //back to the source ID, so the ID mapper can safely map it when we save the resource
                            Reference unmappedReference = IdHelper.convertEdsReferenceToLocallyUniqueReference(previousReference, csvProcessor);
                            ret.add(unmappedReference);
                        }
                    }
                }
            }

            return ret;

        } catch (ResourceNotFoundException|ResourceDeletedException ex) {
            //if this is the first time, then we'll get this exception raised
            return null;
        }
    }

    private static ProblemRelationshipType convertRelationshipType(String relationshipType) throws Exception {

        if (relationshipType.equalsIgnoreCase("grouped")) {
            return ProblemRelationshipType.GROUPED;
        } else if (relationshipType.equalsIgnoreCase("combined")) {
            return ProblemRelationshipType.COMBINED;
        } else if (relationshipType.equalsIgnoreCase("evolved")) {
            return ProblemRelationshipType.EVOLVED_FROM;
        } else if (relationshipType.equalsIgnoreCase("replaced")) {
            return ProblemRelationshipType.REPLACES;
        } else {
            throw new IllegalArgumentException("Unhanded problem relationship type " + relationshipType);
        }
    }

    private static ProblemSignificance convertSignificance(String significance) {

        if (significance.equalsIgnoreCase("Significant Problem")) {
            return ProblemSignificance.SIGNIFICANT;

        } else if (significance.equalsIgnoreCase("Minor Problem")) {
            return ProblemSignificance.NOT_SIGNIFICANT;

        } else {
            return ProblemSignificance.UNSPECIIED;
        }
    }
    /*private static ProblemSignificance convertSignificance(String significance) {
        significance = significance.toLowerCase();
        if (significance.indexOf("major") > -1) {
            return ProblemSignificance.SIGNIFICANT;
        } else if (significance.indexOf("minor") > -1) {
            return ProblemSignificance.NOT_SIGNIFICANT;
        } else {
            return ProblemSignificance.UNSPECIIED;
        }
    }*/


}
