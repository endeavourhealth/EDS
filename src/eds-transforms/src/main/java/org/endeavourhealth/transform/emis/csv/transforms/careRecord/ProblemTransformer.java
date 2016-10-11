package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.schema.ProblemRelationshipType;
import org.endeavourhealth.transform.fhir.schema.ProblemSignificance;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.Map;

public class ProblemTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvTransformer> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        Problem parser = (Problem)parsers.get(Problem.class);

        while (parser.nextRecord()) {

            try {
                createResource(parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void createResource(Problem problemParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        Condition fhirProblem = new Condition();
        fhirProblem.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROBLEM));

        String observationGuid = problemParser.getObservationGuid();
        String patientGuid = problemParser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirProblem, patientGuid, observationGuid);

        fhirProblem.setPatient(csvHelper.createPatientReference(patientGuid));

        String comments = problemParser.getComment();
        fhirProblem.setNotes(comments);

        Date endDate = problemParser.getEndDate();
        if (endDate != null) {
            String endDatePrecision = problemParser.getEndDatePrecision(); //NOTE; documentation refers to this as EffectiveDate, but this should be EndDate
            fhirProblem.setAbatement(EmisDateTimeHelper.createDateType(endDate, endDatePrecision));
        } else {

            //if there's no end date, the problem may still be ended, which is in the status description
            String problemStatus = problemParser.getProblemStatusDescription();
            if (problemStatus.equalsIgnoreCase("Past Problem")) {
                fhirProblem.setAbatement(new BooleanType(true));
            }
        }


        fhirProblem.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        Integer expectedDuration = problemParser.getExpectedDuration();
        if (expectedDuration != null) {
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_EXPECTED_DURATION, new IntegerType(expectedDuration.intValue())));
        }

        Date lastReviewDate = problemParser.getLastReviewDate();
        String lastReviewPrecision = problemParser.getLastReviewDatePrecision();
        DateType lastReviewDateType = EmisDateTimeHelper.createDateType(lastReviewDate, lastReviewPrecision);
        String lastReviewedByGuid = problemParser.getLastReviewUserInRoleGuid();
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

        ProblemSignificance fhirSignificance = convertSignificance(problemParser.getSignificanceDescription());
        CodeableConcept fhirConcept = CodeableConceptHelper.createCodeableConcept(fhirSignificance);
        fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_SIGNIFICANCE, fhirConcept));

        String parentProblemGuid = problemParser.getParentProblemObservationGuid();
        String parentRelationship = problemParser.getParentProblemRelationship();
        if (!Strings.isNullOrEmpty(parentProblemGuid)) {
            ProblemRelationshipType fhirRelationshipType = convertRelationshipType(parentRelationship);

            //this extension is composed of two separate extensions
            Extension typeExtension = ExtensionConverter.createExtension("type", new StringType(fhirRelationshipType.getCode()));
            Extension referenceExtension = ExtensionConverter.createExtension("target", csvHelper.createProblemReference(parentProblemGuid, patientGuid));
            fhirProblem.addExtension(ExtensionConverter.createCompoundExtension(FhirExtensionUri.PROBLEM_RELATED, typeExtension, referenceExtension));
        }

        //until the Observation, DrugIssue and DrugRecord files are completed, we can't
        //save the problem, so cache it in the helper to finish later
        csvHelper.cacheProblem(observationGuid, patientGuid, fhirProblem);
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
