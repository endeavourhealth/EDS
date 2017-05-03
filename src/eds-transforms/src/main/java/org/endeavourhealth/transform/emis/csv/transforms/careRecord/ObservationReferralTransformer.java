package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.*;
import org.endeavourhealth.common.fhir.schema.ReferralPriority;
import org.endeavourhealth.common.fhir.schema.ReferralRequestSendMode;
import org.endeavourhealth.common.fhir.schema.ReferralType;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.ObservationReferral;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.ReferralRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ObservationReferralTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(ObservationReferralTransformer.class);

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        AbstractCsvParser parser = parsers.get(ObservationReferral.class);
        while (parser.nextRecord()) {

            try {
                createResource((ObservationReferral)parser, fhirResourceFiler, csvHelper);
            } catch (Exception ex) {
                fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
            }
        }
    }

    private static void createResource(ObservationReferral parser,
                                       FhirResourceFiler fhirResourceFiler,
                                       EmisCsvHelper csvHelper) throws Exception {

        ReferralRequest fhirReferral = new ReferralRequest();
        fhirReferral.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_REFERRAL_REQUEST));

        String observationGuid = parser.getObservationGuid();
        String patientGuid = parser.getPatientGuid();

        EmisCsvHelper.setUniqueId(fhirReferral, patientGuid, observationGuid);

        fhirReferral.setPatient(csvHelper.createPatientReference(patientGuid));

        String ubrn = parser.getReferralUBRN();
        fhirReferral.addIdentifier(IdentifierHelper.createUbrnIdentifier(ubrn));

        String urgency = parser.getReferralUrgency();
        if (!Strings.isNullOrEmpty(urgency)) {
            ReferralPriority fhirPriority = convertUrgency(urgency);
            if (fhirPriority != null) {
                fhirReferral.setPriority(CodeableConceptHelper.createCodeableConcept(fhirPriority));
            } else {
                //if the CSV urgency couldn't be mapped to a FHIR priority, then we can use free-text
                LOG.warn("Unmapped Emis referral priority {}", urgency);
                fhirReferral.setPriority(CodeableConceptHelper.createCodeableConcept(urgency));
            }
        }

        String serviceType = parser.getReferralServiceType();
        if (!Strings.isNullOrEmpty(serviceType)) {
            ReferralType type = convertTye(serviceType);
            if (type != null) {
                fhirReferral.setType(CodeableConceptHelper.createCodeableConcept(type));
            } else {
                LOG.warn("Unmapped Emis referral type {}", serviceType);
                fhirReferral.setType(CodeableConceptHelper.createCodeableConcept(serviceType));
            }
        }

        String mode = parser.getReferralMode();
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

        String recipientOrgGuid = parser.getReferalTargetOrganisationGuid();
        //the spec. states that this value will always be present, but there's some live data with a missing value
        if (!Strings.isNullOrEmpty(recipientOrgGuid)) {
            fhirReferral.addRecipient(csvHelper.createOrganisationReference(recipientOrgGuid));
        }

        //the below values are defined in the spec., but the spec also states that they'll be empty, so
        //none of the below will probably be used
        String sendingOrgGuid = parser.getReferralSourceOrganisationGuid();
        if (Strings.isNullOrEmpty(sendingOrgGuid)) {
            //in the absence of any data, treat the referral as though it was FROM this service so long as it wasn't TO this service
            if (!parser.getOrganisationGuid().equals(recipientOrgGuid)) {
                sendingOrgGuid = parser.getOrganisationGuid();
            }
        }

        if (!Strings.isNullOrEmpty(sendingOrgGuid)) {
            fhirReferral.setRequester(csvHelper.createOrganisationReference(sendingOrgGuid));
        }

        //although the columns exist in the CSV, the spec. states that they'll always be empty
        //ReferralReceivedDateTime
        //ReferralEndDate
        //ReferralSourceId - links to Coding_ClinicalCode
        //ReferralReasonCodeId - links to Coding_ClinicalCode
        //ReferringCareProfessionalStaffGroupCodeId - links to Coding_ClinicalCode
        //ReferralEpisodeRTTMeasurementTypeId - links to Coding_ClinicalCode
        //ReferralEpisodeClosureDate
        //ReferralEpisideDischargeLetterIssuedDate
        //ReferralClosureReasonCodeId - links to Coding_ClinicalCode

        //unlike other resources, we don't save the Referral immediately, as there's data we
        //require on the corresponding row in the Observation file. So cache in the helper
        //and we'll finish the job when we get to that.
        csvHelper.cacheReferral(observationGuid, patientGuid, fhirReferral);

    }

    private static ReferralType convertTye(String type) throws Exception {

        if (type.equalsIgnoreCase("Unknown")) {
            return ReferralType.UNKNOWN;

        } else if (type.equalsIgnoreCase("Assessment")) {
            return ReferralType.ASSESSMENT;

        } else if (type.equalsIgnoreCase("Investigation")) {
            return ReferralType.INVESTIGATION;

        } else if (type.equalsIgnoreCase("Management advice")) {
            return ReferralType.MANAGEMENT_ADVICE;

        } else if (type.equalsIgnoreCase("Patient reassurance")) {
            return ReferralType.PATIENT_REASSURANCE;

        } else if (type.equalsIgnoreCase("Self referral")) {
            return ReferralType.SELF_REFERRAL;

        } else if (type.equalsIgnoreCase("Treatment")) {
            return ReferralType.TREATMENT;

        } else if (type.equalsIgnoreCase("Outpatient")) {
            return ReferralType.OUTPATIENT;

        } else if (type.equalsIgnoreCase("Community Care")) {
            return ReferralType.COMMUNITY_CARE;

        } else if (type.equalsIgnoreCase("Performance of a procedure / operation")) {
            return ReferralType.PROCEDURE;

        } else if (type.equalsIgnoreCase("Admission")) {
            return ReferralType.ADMISSION;

        } else if (type.equalsIgnoreCase("Day Care")) {
            return ReferralType.DAY_CARE;

        } else if (type.equalsIgnoreCase("Assessment & Education")) {
            return ReferralType.ASSESSMENT_AND_EDUCATION;

        } else {
            return null;
        }
    }

    private static ReferralPriority convertUrgency(String urgency) throws Exception {

        //EMIS urgencies based on EMIS Open format (VocReferralUrgency)
        if (urgency.equalsIgnoreCase("Routine")) {
            return ReferralPriority.ROUTINE;

        } else if (urgency.equalsIgnoreCase("Urgent")) {
            return ReferralPriority.URGENT;

        } else if (urgency.equalsIgnoreCase("2 Week Wait")) {
            return ReferralPriority.TWO_WEEK_WAIT;

        } else if (urgency.equalsIgnoreCase("Soon")) {
            return ReferralPriority.SOON;

        } else {
            return null;
        }
    }

}
