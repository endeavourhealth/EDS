package org.endeavourhealth.ui.business.recordViewer;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.ui.json.JsonPatient;
import org.hl7.fhir.instance.model.*;

import java.util.List;

class PatientTransform {

    public static JsonPatient transform(Patient patient) {

        HumanName name = getUsualOrOfficialName(patient.getName());

        String surname = getFirst(name.getFamily());
        String forename = getFirst(name.getGiven());
        String title = getFirst(name.getPrefix());
        String displayName = getDisplayName(title, forename, surname);

        String nhsNumber = getNhsNumber(patient.getIdentifier());
        String nhsNumberFormatted = formatNhsNumber(nhsNumber);
        String gender = getGender(patient.getGender());

        JsonPatient jsonPatient = new JsonPatient()
                .setNhsNumber(nhsNumber)
                .setNhsNumberFormatted(nhsNumberFormatted)
                .setSurname(surname)
                .setForename(forename)
                .setTitle(title)
                .setDisplayName(displayName)
                .setDateOfBirthFormatted("12-Feb-2006 (2y 5m)")
                .setGenderFormatted(gender);

        return jsonPatient;
    }

    private static String getGender(Enumerations.AdministrativeGender gender) {
        if (gender == null)
            return null;

        return gender.getDisplay();
    }

    private static String getNhsNumber(List<Identifier> identifiers) {
        if (identifiers == null)
            return null;

        for (Identifier identifier : identifiers)
            if (identifier.getSystem() != null)
                if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER))
                    return identifier.getValue();

        return null;
    }

    private static String formatNhsNumber(String nhsNumber) {
        if (nhsNumber == null)
            return null;

        nhsNumber = nhsNumber.replace(" ", "");

        if (nhsNumber.length() != 10)
            return nhsNumber;

        return nhsNumber.substring(0, 2) + " " + nhsNumber.substring(3, 5) + " " + nhsNumber.substring(6, 9);
    }

    private static String getDisplayName(String title, String forename, String surname) {
        if (title == null)
            title = "";

        title = StringUtils.capitalize(title.trim());

        if (forename == null)
            forename = "";

        forename = StringUtils.capitalize(forename.trim());

        if (StringUtils.isEmpty(surname))
            surname = "UNKNOWN";

        surname = surname.trim().toUpperCase();

        String result = surname;

        if (StringUtils.isNotEmpty(forename))
            result += ", " + forename;

        if (StringUtils.isNotEmpty(title))
            result += " (" + title + ")";

        return result;
    }

    private static HumanName getUsualOrOfficialName(List<HumanName> names) {
        HumanName name = getNameByUse(names, HumanName.NameUse.USUAL);

        if (name == null)
            name = getNameByUse(names, HumanName.NameUse.OFFICIAL);

        return name;
    }

    private static HumanName getNameByUse(List<HumanName> names, HumanName.NameUse nameUse) {
        if (names == null)
            return null;

        for (HumanName name : names)
            if (name.getUse() != null)
                if (name.getUse() == nameUse)
                    return name;

        return null;
    }

    private static String getFirst(List<StringType> strings)
    {
        if (strings == null)
            return null;

        if (strings.get(0) == null)
            return null;

        return strings.get(0).getValue();
    }
}
