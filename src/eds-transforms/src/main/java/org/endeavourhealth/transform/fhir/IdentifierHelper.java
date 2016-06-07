package org.endeavourhealth.transform.fhir;

import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.Identifier;

public class IdentifierHelper {

    public static Identifier createNhsNumberIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, value, FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER);
    }
    public static Identifier createOdsOrganisationIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, value, FhirUri.IDENTIFIER_SYSTEM_ODS_CODE);
    }
    public static Identifier createUbrnIdentifier(String value) {
        return createIdentifier(Identifier.IdentifierUse.OFFICIAL, value, FhirUri.IDENTIFIER_SYSTEM_UBRN);
    }

    public static Identifier createIdentifier(Identifier.IdentifierUse use, String value, String system) {

        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return new Identifier()
                .setUse(use)
                .setValue(value)
                .setSystem(system);
    }
}
