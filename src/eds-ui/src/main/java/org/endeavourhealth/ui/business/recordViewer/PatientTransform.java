package org.endeavourhealth.ui.business.recordViewer;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.ui.business.recordViewer.models.JsonPatient;
import org.hl7.fhir.instance.model.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class PatientTransform {

    public static JsonPatient transform(Patient patient) {

        HumanName name = getUsualOrOfficialName(patient.getName());

        String surname = getFirst(name.getFamily());
        String forename = getFirst(name.getGiven());
        String title = getFirst(name.getPrefix());
        String displayName = getDisplayName(title, forename, surname);
        String birthDateFormatted = getBirthDateFormatted(patient.getBirthDate());
        String nhsNumber = getNhsNumber(patient.getIdentifier());
        String nhsNumberFormatted = formatNhsNumber(nhsNumber);
        String gender = getGender(patient.getGender());
        String singleLineAddress = getSingleLineAddress(patient);

        JsonPatient jsonPatient = new JsonPatient()
                .setNhsNumber(nhsNumber)
                .setNhsNumberFormatted(nhsNumberFormatted)
                .setSurname(surname)
                .setForename(forename)
                .setTitle(title)
                .setDisplayName(displayName)
                .setDateOfBirthFormatted(birthDateFormatted)
                .setGenderFormatted(gender)
                .setSingleLineAddress(singleLineAddress);

        return jsonPatient;
    }

    private static String getSingleLineAddress(Patient patient) {

        if (patient.getAddress() == null)
            return null;

        if (patient.getAddress().size() == 0)
            return null;

        Address address = patient.getAddress().get(0);

        List<String> lines = address
                .getLine()
                .stream()
                .map(t -> t.getValue())
                .collect(Collectors.toList());

        lines.add(address.getCity());
        lines.add(address.getDistrict());
        lines.add(formatPostcode(address.getPostalCode()));
        lines.add(address.getCountry());

        lines = lines
                .stream()
                .filter(t -> (!StringUtils.isEmpty(t)))
                .collect(Collectors.toList());

        return StringUtils.join(lines, ", ");
    }

    private static String formatPostcode(String postcode) {
        if (postcode == null)
            return null;

        postcode = postcode.replace(" ", "").toUpperCase().trim();

        String matchString = "^(?<Primary>([A-Z]{1,2}[0-9]{1,2}[A-Z]?))(?<Secondary>([0-9]{1}[A-Z]{2}))$";

        Pattern pattern = Pattern.compile(matchString);

        Matcher matcher = pattern.matcher(postcode);

        if (!matcher.find())
            return postcode;

        return matcher.group("Primary") + " " + matcher.group("Secondary");
    }

    private static String getBirthDateFormatted(Date birthDate) {
        LocalDate localDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        java.time.Period timespan = localDate.until(LocalDate.now());
        return localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + " (" + Integer.toString(timespan.getYears()) + "y " + Integer.toString(timespan.getMonths()) + "m)";
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
