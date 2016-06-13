package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Problem;
import org.endeavourhealth.transform.emis.csv.transforms.coding.FhirObjectStore;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.util.Date;

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

        boolean store = !objectStore.isObservationToDelete(patientGuid, observationGuid);
        objectStore.addResourceToSave(patientGuid, fhirProblem, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        String comments = problemParser.getComment();
        fhirProblem.setNotes(comments);

        Date endDate = problemParser.getEndDate();
        String endDatePrecision = problemParser.getEffectiveDatePrecision(); //NOTE; documentation refers to this as EffectiveDate, but this should be EndDate
        fhirProblem.setAbatement(FhirObjectStore.createDateType(endDate, endDatePrecision));

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
        DateType lastReviewDateType = FhirObjectStore.createDateType(lastReviewDate, lastReviewPrecision);
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
