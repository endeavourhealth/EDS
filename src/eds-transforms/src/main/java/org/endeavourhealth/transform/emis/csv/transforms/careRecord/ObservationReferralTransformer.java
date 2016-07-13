package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_ObservationReferral;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class ObservationReferralTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        CareRecord_ObservationReferral parser = new CareRecord_ObservationReferral(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createResource(parser, csvProcessor, csvHelper);
            }
        } finally {
            parser.close();
        }
    }

    private static void createResource(CareRecord_ObservationReferral observationParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper) throws Exception {

        ReferralRequest fhirReferral = new ReferralRequest();
        fhirReferral.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_REFERRAL_REQUEST));

        String observationGuid = observationParser.getObservationGuid();
        String patientGuid = observationParser.getPatientGuid();
        String organisationGuid = observationParser.getOrganisationGuid();

        EmisCsvHelper.setUniqueId(fhirReferral, patientGuid, observationGuid);

        fhirReferral.setPatient(csvHelper.createPatientReference(patientGuid));

        //if the Resource is to be deleted from the data store, then stop processing the CSV row
        if (csvHelper.isObservationToDelete(patientGuid, observationGuid)) {
            csvProcessor.deletePatientResource(fhirReferral, patientGuid);
            return;
        }

        String ubrn = observationParser.getReferralUBRN();
        fhirReferral.addIdentifier(IdentifierHelper.createUbrnIdentifier(ubrn));

        String recipientOrgGuid = observationParser.getReferalTargetOrganisationGuid();
        fhirReferral.addRecipient(csvHelper.createOrganisationReference(recipientOrgGuid));

        String urgency = observationParser.getReferralUrgency();
        if (!Strings.isNullOrEmpty(urgency)) {
            DiagnosticOrder.DiagnosticOrderPriority priority = convertUrgency(urgency);
            fhirReferral.setPriority(CodeableConceptHelper.createCodeableConcept(priority));
        }

        String serviceType = observationParser.getReferralServiceType();
        if (!Strings.isNullOrEmpty(serviceType)) {
            fhirReferral.setType(CodeableConceptHelper.createCodeableConcept(serviceType));
        }

        //several of the Resource fields are simply carried over from the Observation the Referral is linked to
        Observation fhirObservation = csvHelper.findObservation(observationGuid, patientGuid);

        fhirReferral.setDateElement(fhirObservation.getEffectiveDateTimeType());
        fhirReferral.setEncounter(fhirObservation.getEncounter());
        fhirReferral.setReason(fhirObservation.getCode());

        List<Reference> fhirPerformers = fhirObservation.getPerformer();
        Reference fhirPerformer = fhirPerformers.get(0);
        fhirReferral.setRequester(fhirPerformer);


        //although the columns exist in the CSV, the spec. states that they'll always be empty
        //ReferralReceivedDateTime
        //ReferralEndDate
        //ReferralSourceId
        //ReferralSourceOrganisationGuid
        //ReferralReasonCodeId
        //ReferringCareProfessionalStaffGroupCodeId
        //ReferralEpisodeRTTMeasurmentTypeId
        //ReferralEpisodeClosureDate
        //ReferralEpisideDischargeLetterIssuedDate
        //ReferralClosureReasonCodeId

        csvProcessor.savePatientResource(fhirReferral, patientGuid);

    }

    private static DiagnosticOrder.DiagnosticOrderPriority convertUrgency(String urgency) throws Exception {

        //EMIS urgencies based on EMIS Open format (VocReferralUrgency)
        if (urgency.equalsIgnoreCase("Routine")) {
            return DiagnosticOrder.DiagnosticOrderPriority.ROUTINE;

        } else if (urgency.equalsIgnoreCase("Soon")) {
            return DiagnosticOrder.DiagnosticOrderPriority.ASAP;

        } else if (urgency.equalsIgnoreCase("Urgent")) {
            return DiagnosticOrder.DiagnosticOrderPriority.URGENT;

        } else if (urgency.equalsIgnoreCase("Dated")) {
            //TODO - how to map EMIS Dated referral priority to FHIR
            return null;

        } else if (urgency.equalsIgnoreCase("2 week wait")) {
            //TODO - how to map EMIS 2 Week Wait referral priority to FHIR
            return null;

        } else {
            throw new TransformException("Unknown referral urgency " + urgency);
        }
    }

}
