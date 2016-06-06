package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Problem;
import org.endeavourhealth.transform.emis.csv.transforms.coding.Metadata;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProblemTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

        CareRecord_Problem parser = new CareRecord_Problem(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createProblem(parser, metadata, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createProblem(CareRecord_Problem problemParser, Metadata metadata, Map<String, List<Resource>> fhirResources) throws Exception {

        String patientGuid = problemParser.getPatientGuid();

        Condition fhirProblem = new Condition();
        fhirProblem.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_PROBLEM));
        Metadata.addToMap(patientGuid, fhirProblem, fhirResources);

        fhirProblem.setPatient(ReferenceHelper.createPatientReference(patientGuid.toString()));

        String comments = problemParser.getComment();
        fhirProblem.setNotes(comments);

        Date endDate = problemParser.getEndDate();
        String endDatePrecision = problemParser.getEffectiveDatePrecision(); //NOTE; documentation refers to this as EffectiveDate, but this should be EndDate
        fhirProblem.setAbatement(Metadata.createDateType(endDate, endDatePrecision));

        //some of the information we need is stored on our original Observation, so we
        //need to

        fhirProblem.setVerificationStatus(Condition.ConditionVerificationStatus.CONFIRMED);

        //to set:
        //Encounter
        //Asserter
        //DateRecorded
        //Code
        //Clinical Status
        //onset
        //signidicance
        //related
        //associated

        //TODO - need to set the ....partOfProblemEpisode extension on the Observation resource we're linked to
        //TODO - update the status field on the Observation resource we're linked to

        Integer expectedDuration = problemParser.getExpectedDuration();
        if (expectedDuration != null) {
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_EXPECTED_DURATION, new IntegerType(expectedDuration.intValue())));
        }

        Date lastReviewDate = problemParser.getLastReviewDate();
        String lastReviewPrecision = problemParser.getLastReviewDatePrecision();
        DateType lastReviewDateType = Metadata.createDateType(lastReviewDate, lastReviewPrecision);
        if (lastReviewDateType != null) {
            fhirProblem.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.PROBLEM_LAST_REVIEW_DATE, lastReviewDateType));
        }


/**
 * public UUID getObservationGuid() {
 return super.getUniqueIdentifier(0);
 }
 public UUID getParentProblemObservationGuid() {
 return super.getUniqueIdentifier(1);
 }
 public UUID getOrganisationGuid() {
 return super.getUniqueIdentifier(3);
 }
 public UUID getLastReviewUserInRoleGuid() {
 return super.getUniqueIdentifier(8);
 }
 public String getSignificanceDescription() {
 return super.getString(10);
 }
 public String getProblemStatusDescription() {
 return super.getString(11);
 }
 public String getParentProblemRelationship() {
 return super.getString(12);
 }

 */
    }


}
