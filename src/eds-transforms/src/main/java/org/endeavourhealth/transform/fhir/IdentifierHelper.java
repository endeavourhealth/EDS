package org.endeavourhealth.transform.fhir;

import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;

import java.util.List;

public class IdentifierHelper {

    public static Identifier createNhsNumberIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER, value);
    }
    public static Identifier createOdsOrganisationIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_ODS_CODE, value);
    }
    public static Identifier createUbrnIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_UBRN, value);
    }
    public static Identifier createGmcIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, FhirUri.IDENTIFIER_SYSTEM_GMC_NUMBER, value);
    }


    public static Identifier createIdentifier(Identifier.IdentifierUse use, String system, String value) {

        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return new Identifier()
                .setUse(use)
                .setValue(value)
                .setSystem(system);
    }

    public static String findIdentifierValue(List<Identifier> identifiers, String system) {
        for (Identifier id: identifiers) {
            if (id.getSystem().equals(system)) {
                return id.getValue();
            }
        }

        return null;
    }

    public static String findNhsNumber(Patient fhirPatient) {
        if (fhirPatient.hasIdentifier()) {
            return findIdentifierValue(fhirPatient.getIdentifier(), FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER);
        }
        return null;
    }

}
