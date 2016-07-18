package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.transform.fhir.schema.*;
import org.endeavourhealth.transform.terminology.SnomedCode;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.DiagnosticOrder;
import org.hl7.fhir.instance.model.valuesets.RelationshipEnumFactory;


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

    public static Coding createCoding(String system, String term, String code) {
        return new Coding()
                .setSystem(system)
                .setDisplay(term)
                .setCode(code);
    }

}
