package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.transform.fhir.schema.ContactRelationship;
import org.endeavourhealth.transform.fhir.schema.OrganisationType;
import org.endeavourhealth.transform.fhir.schema.RegistrationType;
import org.endeavourhealth.transform.terminology.SnomedCode;
import org.hl7.fhir.instance.model.Coding;


public class CodingHelper {

    public static Coding createCoding(SnomedCode snomedCode) {
        return new Coding()
                .setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT)
                .setDisplay(snomedCode.getTerm())
                .setCode(snomedCode.getConceptCode());
    }

    public static Coding createCoding(RegistrationType registrationType) {
        return new Coding()
                .setSystem(FhirUri.VALUE_SET_REGISTRATION_TYPE)
                .setDisplay(registrationType.getValue())
                .setCode(registrationType.toString());
    }

    public static Coding createCoding(OrganisationType organizationType) {
        return new Coding()
                .setSystem(FhirUri.VALUE_SET_ORGANISATION_TYPE)
                .setDisplay(organizationType.getValue())
                .setCode(organizationType.toString());
    }

    public static Coding createCoding(ContactRelationship carerRelationship) {
        return new Coding()
                .setSystem(FhirUri.VALUE_SET_CONTACT_RELATIOSHIP)
                .setDisplay(carerRelationship.getValue())
                .setCode(carerRelationship.toString());
    }




    public static Coding createCoding(String system, String term, String code) {
        return new Coding()
                .setSystem(system)
                .setDisplay(term)
                .setCode(code);
    }

}
