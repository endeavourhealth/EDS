package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_ObservationReferral;
import org.endeavourhealth.transform.fhir.CodeableConceptHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.IdentifierHelper;
import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ReferralRequest;

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
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
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

        EmisCsvHelper.setUniqueId(fhirReferral, patientGuid, observationGuid);

        fhirReferral.setPatient(csvHelper.createPatientReference(patientGuid));

        String ubrn = observationParser.getReferralUBRN();
        fhirReferral.addIdentifier(IdentifierHelper.createUbrnIdentifier(ubrn));

        String recipientOrgGuid = observationParser.getReferalTargetOrganisationGuid();
        fhirReferral.addRecipient(csvHelper.createOrganisationReference(recipientOrgGuid));

        String urgency = observationParser.getReferralUrgency();
        if (!Strings.isNullOrEmpty(urgency)) {
            DiagnosticOrder.DiagnosticOrderPriority fhirPriority = convertUrgency(urgency);
            if (fhirPriority != null) {
                fhirReferral.setPriority(CodeableConceptHelper.createCodeableConcept(fhirPriority));
            } else {
                //if the CSV urgency couldn't be mapped to a FHIR priority, then we can use free-text
                fhirReferral.setPriority(CodeableConceptHelper.createCodeableConcept(urgency));
            }
        }

        String serviceType = observationParser.getReferralServiceType();
        if (!Strings.isNullOrEmpty(serviceType)) {
            fhirReferral.addServiceRequested(CodeableConceptHelper.createCodeableConcept(serviceType));
        }

        String mode = observationParser.getReferralMode();
        if (!Strings.isNullOrEmpty(mode)) {
            fhirReferral.setType(CodeableConceptHelper.createCodeableConcept(mode));
        }

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

        //unlike other resources, we don't save the Referral immediately, as there's data we
        //require on the corresponding row in the Observation file. So cache in the helper
        //and we'll finish the job when we get to that.
        csvHelper.cacheReferral(observationGuid, patientGuid, fhirReferral);

    }

    private static DiagnosticOrder.DiagnosticOrderPriority convertUrgency(String urgency) throws Exception {

        //EMIS urgencies based on EMIS Open format (VocReferralUrgency)
        if (urgency.equalsIgnoreCase("Routine")) {
            return DiagnosticOrder.DiagnosticOrderPriority.ROUTINE;

        } else if (urgency.equalsIgnoreCase("Soon")) {
            return DiagnosticOrder.DiagnosticOrderPriority.ASAP;

        } else if (urgency.equalsIgnoreCase("Urgent")) {
            return DiagnosticOrder.DiagnosticOrderPriority.URGENT;

        } else {
            return null;
        }
    }

}
