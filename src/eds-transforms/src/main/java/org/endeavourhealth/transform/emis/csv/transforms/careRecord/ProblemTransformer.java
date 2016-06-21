package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisDateTimeHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Problem;
import org.endeavourhealth.transform.emis.csv.FhirObjectStore;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.ProblemRelationshipType;
import org.endeavourhealth.transform.fhir.schema.ProblemSignificance;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;

public class ProblemTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        CareRecord_Problem parser = new CareRecord_Problem(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createProblem(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createProblem(CareRecord_Problem problemParser, FhirObjectStore objectStore) throws Exception {

        Condition fhirProblem = new Condition();
        fhirProblem.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROBLEM));

        String observationGuid = problemParser.getObservationGuid();
        fhirProblem.setId(observationGuid); //use the observation GUID as the problem GUID, since they only need to be unique per resource type

        String patientGuid = problemParser.getPatientGuid();
        fhirProblem.setPatient(objectStore.createPatientReference(patientGuid));

        String organisationGuid = problemParser.getOrganisationGuid();
        boolean store = !objectStore.isObservationToDelete(patientGuid, observationGuid);
        objectStore.addResourceToSave(patientGuid, organisationGuid, fhirProblem, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        String comments = problemParser.getComment();
        fhirProblem.setNotes(comments);

        Date endDate = problemParser.getEndDate();
        String endDatePrecision = problemParser.getEffectiveDatePrecision(); //NOTE; documentation refers to this as EffectiveDate, but this should be EndDate
        fhirProblem.setAbatement(EmisDateTimeHelper.createDateType(endDate, endDatePrecision));

        fhirProblem.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        Integer expectedDuration = problemParser.getExpectedDuration();
        if (expectedDuration != null) {
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_EXPECTED_DURATION, new IntegerType(expectedDuration.intValue())));
        }

        Date lastReviewDate = problemParser.getLastReviewDate();
        String lastReviewPrecision = problemParser.getLastReviewDatePrecision();
        DateType lastReviewDateType = EmisDateTimeHelper.createDateType(lastReviewDate, lastReviewPrecision);
        if (lastReviewDateType != null) {
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_LAST_REVIEW_DATE, lastReviewDateType));
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
            Extension referenceExtension = ExtensionConverter.createExtension("target", objectStore.createProblemReference(parentProblemGuid, patientGuid));
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_RELATED, typeExtension, referenceExtension));
        }

        //TODO - need to set ClinicalStatus on Problem FHIR resource

        //several of the Resource fields are simply carried over from the Observation the Problem is linked to
        Observation fhirObservation = objectStore.findObservation(observationGuid, patientGuid);

        DateTimeType fhirDateTime = fhirObservation.getEffectiveDateTimeType();
        Date date = fhirDateTime.getValue();
        TemporalPrecisionEnum precision = fhirDateTime.getPrecision();
        //have to convert dateTimeType to just dateType
        if (precision == TemporalPrecisionEnum.MILLI
                || precision == TemporalPrecisionEnum.MINUTE
                || precision == TemporalPrecisionEnum.SECOND) {
            precision = TemporalPrecisionEnum.DAY;
        }

        Reference asserter = null;
        List<Reference> performers = fhirObservation.getPerformer();
        if (performers.size() > 0) {
            asserter = performers.get(0);
        }

        fhirProblem.setDateRecordedElement(new DateType(date, precision));
        fhirProblem.setEncounter(fhirObservation.getEncounter());
        fhirProblem.setCode(fhirObservation.getCode());
        fhirProblem.setAsserter(asserter);
    }

    private static ProblemRelationshipType convertRelationshipType(String relationshipType) throws Exception {

        //TODO - validate possible problem relationship types from EMIS CSV
        if (relationshipType.equalsIgnoreCase("grouped")) {
            return ProblemRelationshipType.GROUPED;
        } else if (relationshipType.equalsIgnoreCase("combined")) {
            return ProblemRelationshipType.COMBINED;
        } else if (relationshipType.equalsIgnoreCase("evolved from")) {
            return ProblemRelationshipType.EVOLVED_FROM;
        } else if (relationshipType.equalsIgnoreCase("replaced")) {
            return ProblemRelationshipType.REPLACED;
        } else {
            throw new TransformException("Unhanded problem relationship type " + relationshipType);
        }
    }

    private static ProblemSignificance convertSignificance(String significance) {
        significance = significance.toLowerCase();
        if (significance.indexOf("major") > -1) {
            return ProblemSignificance.SIGNIFICANT;
        } else if (significance.indexOf("minor") > -1) {
            return ProblemSignificance.NOT_SIGNIFICANT;
        } else {
            return ProblemSignificance.UNSPECIIED;
        }
    }


}
