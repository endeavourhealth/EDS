package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvTransformer;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.ObservationReferral;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.schema.ReferralRequestSendMode;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ReferralRequest;

import java.util.Map;

public class ObservationReferralTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvTransformer> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        ObservationReferral parser = (ObservationReferral)parsers.get(ObservationReferral.class);

        while (parser.nextRecord()) {

            try {
                createResource(parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void createResource(ObservationReferral observationParser,
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

            CodeableConcept codeableConcept = null;

            try {
                ReferralRequestSendMode fhirMode = ReferralRequestSendMode.fromDescription(mode);
                codeableConcept = CodeableConceptHelper.createCodeableConcept(fhirMode);
            } catch (IllegalArgumentException ex) {
                //if we couldn't map to a send mode from the value set, just save as a textual codeable concept
                codeableConcept = CodeableConceptHelper.createCodeableConcept(mode);
            }

            fhirReferral.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.REFERRAL_REQUEST_SEND_MODE, codeableConcept));
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
