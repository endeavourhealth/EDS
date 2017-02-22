package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.common.fhir.schema.*;
import org.endeavourhealth.transform.terminology.SnomedCode;
import org.endeavourhealth.transform.terminology.TerminologyService;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.DiagnosticOrder;

import java.util.List;

public class CodeableConceptHelper {

    public static CodeableConcept createCodeableConcept(String system, String term, String code) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(system, term, code));
    }

    public static CodeableConcept createCodeableConcept(OrganisationType organisationType) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(organisationType));
    }

    public static CodeableConcept createCodeableConcept(ContactRelationship contactRelationship) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(contactRelationship));
    }

    public static CodeableConcept createCodeableConcept(DiagnosticOrder.DiagnosticOrderPriority priority) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(priority));
    }

    public static CodeableConcept createCodeableConcept(ProblemSignificance significance) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(significance));
    }

    public static CodeableConcept createCodeableConcept(FamilyMember familyMember) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(familyMember));
    }

    public static CodeableConcept createCodeableConcept(EncounterParticipantType participantType) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(participantType));
    }

    public static CodeableConcept createCodeableConcept(NhsNumberVerificationStatus nhsNumberVerificationStatus) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(nhsNumberVerificationStatus));
    }

    public static CodeableConcept createCodeableConcept(ReferralRequestSendMode referralRequestSendMode) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(referralRequestSendMode));
    }

    public static CodeableConcept createCodeableConcept(EthnicCategory ethnicCategory) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(ethnicCategory));
    }

    public static CodeableConcept createCodeableConcept(MaritalStatus maritalStatus) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(maritalStatus));
    }

    public static CodeableConcept createCodeableConcept(ReferralPriority referralPriority) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(referralPriority));
    }

    public static CodeableConcept createCodeableConcept(ReferralType referralType) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(referralType));
    }


    public static CodeableConcept createCodeableConcept(String text) {
        return new CodeableConcept().setText(text);
    }

    /**
     * checks the first Coding element in the CodeableConcept and adds a second Coding if it
     * needs to be mapped to SNOMED CT
     */
    public static void translateToSnomed(CodeableConcept codeableConcept) throws TransformException {
        List<Coding> codingList = codeableConcept.getCoding();
        if (codingList.isEmpty()) {
            return;
        }

        Coding coding = codingList.get(0);
        String system = coding.getSystem();
        if (system.equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
            //no mapping required
        } else if (system.equals(FhirUri.CODE_SYSTEM_CTV3)) {
            SnomedCode mapping = TerminologyService.translateCtv3ToSnomed(coding.getCode());
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else if (system.equals(FhirUri.CODE_SYSTEM_READ2)) {
            SnomedCode mapping = TerminologyService.translateRead2ToSnomed(coding.getCode());
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else if (system.equals(FhirUri.CODE_SYSTEM_EMISPREPARATION)) {
            SnomedCode mapping = TerminologyService.translateEmisPreparationToSnomed(coding.getCode());
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else if (system.equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
            SnomedCode mapping = TerminologyService.translateEmisSnomedToSnomed(coding.getCode());
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else {
            throw new TransformException("Unexpected coding system [" + system + "]");
        }
    }


    public static Long findSnomedConceptId(CodeableConcept code) {
        for (Coding coding: code.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
                return Long.parseLong(coding.getCode());
            }
        }

        return null;
    }

    public static String findSnomedConceptText(CodeableConcept code) {
        for (Coding coding: code.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
                return coding.getDisplay();
            }
        }

        return null;
    }

    public static String findOriginalCode(CodeableConcept code) {
        for (Coding coding: code.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_READ2)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMIS_CODE)) {
                return coding.getCode();
            }
        }

        return null;
    }
}
