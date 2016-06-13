package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_ObservationReferral;
import org.endeavourhealth.transform.emis.csv.transforms.coding.FhirObjectStore;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.*;

public class ObservationReferralTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, FhirObjectStore objectStore) throws Exception {

        CareRecord_ObservationReferral parser = new CareRecord_ObservationReferral(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, objectStore);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(CareRecord_ObservationReferral observationParser, FhirObjectStore objectStore) throws Exception {

        ReferralRequest fhirReferral = new ReferralRequest();
        fhirReferral.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_REFERRAL_REQUEST));

        String observationGuid = observationParser.getObservationGuid();
        fhirReferral.setId(observationGuid); //use the observation GUID as the problem GUID, since they only need to be unique per resource type

        String patientGuid = observationParser.getPatientGuid();
        fhirReferral.setPatient(objectStore.createPatientReference(patientGuid));

        boolean store = !objectStore.isObservationToDelete(patientGuid, observationGuid);
        objectStore.addResourceToSave(patientGuid, fhirReferral, store);

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (!store) {
            return;
        }

        String ubrn = observationParser.getReferralUBRN();
        fhirReferral.addIdentifier(IdentifierHelper.createUbrnIdentifier(ubrn));

        String sourceOrgGuid = observationParser.getReferralSourceOrganisationGuid();
        fhirReferral.setRequester(objectStore.createOrganisationReference(sourceOrgGuid, patientGuid));
        //TODO - referral source orgnisation is not provided in current EMIS extracts, so can't be populated in Mandatory resource field

        String recipientOrgGuid = observationParser.getReferalTargetOrganisationGuid();
        fhirReferral.addRecipient(objectStore.createOrganisationReference(recipientOrgGuid, patientGuid));

       // fhirReferral.setPriority()

        //TODO - set referral urgency, mode and service type in Resource
                //urgency
        //mode
        //service type


        //several of the Resource fields are simply carried over from the Observation the Referral is linked to
        Observation fhirObservation = objectStore.findObservation(observationGuid, patientGuid);

        fhirReferral.setDateElement(fhirObservation.getEffectiveDateTimeType());
        fhirReferral.setEncounter(fhirObservation.getEncounter());
        fhirReferral.setReason(fhirObservation.getCode());

/**

 public String getReferralUrgency() {
 return super.getString(4);
 }
 public String getReferralMode() {
 return super.getString(5);
 }
 public String getReferralServiceType() {
 return super.getString(6);
 }
 public Date getReferralReceivedDateTime() throws TransformException {
 return super.getDateTime(7, 8);
 }
 public Date getReferralEndDate() throws TransformException {
 return super.getDate(9);
 }
 public Long getReferralSourceId() {
 return super.getLong(10);
 }
  public Long getReferralReasonCodeId() {
 return super.getLong(13);
 }
 public Long getReferringCareProfessionalStaffGroupCodeId() {
 return super.getLong(14);
 }
 public Long getReferralEpisodeRTTMeasurmentTypeId() {
 return super.getLong(15);
 }
 public Date getReferralEpisodeClosureDate() throws TransformException {
 return super.getDate(16);
 }
 public Date getReferralEpisideDischargeLetterIssuedDate() throws TransformException {
 return super.getDate(17);
 }
 public Long getReferralClosureReasonCodeId() {
 return super.getLong(18);
 }

 */
    }
}
