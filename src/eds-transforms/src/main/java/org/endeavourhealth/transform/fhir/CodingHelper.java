package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.transform.fhir.schema.*;
import org.endeavourhealth.transform.terminology.SnomedCode;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.DiagnosticOrder;


public class CodingHelper {

    public static Coding createCoding(SnomedCode snomedCode) {
        return new Coding()
                .setSystem(snomedCode.getSystem())
                .setDisplay(snomedCode.getTerm())
                .setCode(snomedCode.getConceptCode());
    }

    public static Coding createCoding(RegistrationType registrationType) {
        return new Coding()
                .setSystem(registrationType.getSystem())
                .setDisplay(registrationType.getDescription())
                .setCode(registrationType.getCode());
    }

    public static Coding createCoding(OrganisationType organizationType) {
        return new Coding()
                .setSystem(organizationType.getSystem())
                .setDisplay(organizationType.getDescription())
                .setCode(organizationType.getCode());
    }

    public static Coding createCoding(ContactRelationship carerRelationship) {
        return new Coding()
                .setSystem(carerRelationship.getSystem())
                .setDisplay(carerRelationship.getDescription())
                .setCode(carerRelationship.getCode());
    }

    public static Coding createCoding(DiagnosticOrder.DiagnosticOrderPriority priority) {
        return new Coding()
                .setSystem(priority.getSystem())
                .setDisplay(priority.getDisplay())
                .setCode(priority.toCode());
    }

    public static Coding createCoding(ProblemSignificance significance) {
        return new Coding()
                .setSystem(significance.getSystem())
                .setDisplay(significance.getDescription())
                .setCode(significance.getCode());
    }

    public static Coding createCoding(FamilyMember familyMember) {
        return new Coding()
                .setSystem(familyMember.getSystem())
                .setDisplay(familyMember.getDescription())
                .setCode(familyMember.getCode());
    }

    public static Coding createCoding(EncounterParticipantType participantType) {
        return new Coding()
                .setSystem(participantType.getSystem())
                .setDisplay(participantType.getDescription())
                .setCode(participantType.getCode());
    }

    public static Coding createCoding(NhsNumberVerificationStatus nhsNumberVerificationStatus) {
        return new Coding()
                .setSystem(nhsNumberVerificationStatus.getSystem())
                .setDisplay(nhsNumberVerificationStatus.getDescription())
                .setCode(nhsNumberVerificationStatus.getCode());
    }

    public static Coding createCoding(MedicationAuthorisationType medicationAuthorisationType) {
        return new Coding()
                .setSystem(medicationAuthorisationType.getSystem())
                .setDisplay(medicationAuthorisationType.getDescription())
                .setCode(medicationAuthorisationType.getCode());
    }

    public static Coding createCoding(ReferralRequestSendMode referralRequestSendMode) {
        return new Coding()
                .setSystem(referralRequestSendMode.getSystem())
                .setDisplay(referralRequestSendMode.getDescription())
                .setCode(referralRequestSendMode.getCode());
    }

    public static Coding createCoding(EthnicCategory ethnicCategory) {
        return new Coding()
                .setSystem(ethnicCategory.getSystem())
                .setDisplay(ethnicCategory.getDescription())
                .setCode(ethnicCategory.getCode());
    }

    public static Coding createCoding(MaritalStatus maritalStatus) {
        return new Coding()
                .setSystem(maritalStatus.getSystem())
                .setDisplay(maritalStatus.getDescription())
                .setCode(maritalStatus.getCode());
    }

    public static Coding createCoding(String system, String term, String code) {
        return new Coding()
                .setSystem(system)
                .setDisplay(term)
                .setCode(code);
    }

}
