package org.endeavourhealth.core.transform.fhir;

import com.google.common.base.Strings;
import org.endeavourhealth.core.transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Fhir {


    public static Identifier createIdentifier(Identifier.IdentifierUse use, String value, String system) {

        return new Identifier()
                .setUse(use)
                .setValue(value)
                .setSystem(system);
    }

    public static HumanName createHumanName(HumanName.NameUse use, String title, String firstName, String middleNames, String surname) {
        HumanName ret = new HumanName();
        ret.setUse(use);

        //build up full name in standard format; SURNAME, firstname (title)
        StringBuilder displayName = new StringBuilder();

        if (!Strings.isNullOrEmpty(surname)) {
            List<String> v = split(surname);
            v.forEach(ret::addFamily);

            displayName.append(surname.toUpperCase());
        }

        if (!Strings.isNullOrEmpty(firstName)) {
            List<String> v = split(firstName);
            v.forEach(ret::addGiven);

            displayName.append(", ");
            displayName.append(firstName);
        }

        if (!Strings.isNullOrEmpty(middleNames)) {
            List<String> v = split(middleNames);
            v.forEach(ret::addGiven);
        }

        if (!Strings.isNullOrEmpty(title)) {
            List<String> v = split(title);
            v.forEach(ret::addPrefix);

            displayName.append(" (");
            displayName.append(title);
            displayName.append(")");
        }

        ret.setText(displayName.toString());

        return ret;
    }
    private static List<String> split(String s) {
        return Arrays.asList(s.split(" "));
    }

    public static CodeableConcept createCodeableConcept(String system, String term, String code) {
        return new CodeableConcept()
                .addCoding(new Coding()
                    .setSystem(system)
                    .setDisplay(term)
                    .setCode(code));
    }

    public static ContactPoint createContactPointIfRequired(ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use,
                                                    String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return createContactPoint(system, use, value);
    }
    public static ContactPoint createContactPoint(ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use, String value) {

        return new ContactPoint()
                .setSystem(system)
                .setUse(use)
                .setValue(value);
    }

    public static Reference createReference(ResourceType resourceType, String value) {
        if (Strings.isNullOrEmpty(value)) {
            throw new TransformException("Missing value when creating " + resourceType + "referece");
        }

        String reference = createResourceReference(resourceType, value);
        return new Reference().setReference(reference);
    }
    public static String createResourceReference(ResourceType resourceType, String id) {
        return resourceType.toString() + "/" + id;
    }
}
