package org.endeavourhealth.transform.ui.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.NameHelper;
import org.endeavourhealth.transform.ui.models.UIPatient;
import org.hl7.fhir.instance.model.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class UIPatientTransform {

    public static UIPatient transform(Patient patient) {

        String displayName = NameHelper.getUsualOrOfficialNameForDisplay(patient.getName());
        String birthDateFormatted = DateHelper.getCuiDob(patient.getBirthDate());
        String nhsNumber = getNhsNumber(patient.getIdentifier());
        String nhsNumberFormatted = formatNhsNumber(nhsNumber);
        String gender = getGender(patient.getGender());
        String singleLineAddress = getSingleLineAddress(patient);

        UIPatient UIPatient = new UIPatient()
                .setNhsNumber(nhsNumber)
                .setNhsNumberFormatted(nhsNumberFormatted)
                .setDisplayName(displayName)
                .setDateOfBirthFormatted(birthDateFormatted)
                .setGenderFormatted(gender)
                .setSingleLineAddress(singleLineAddress);

        return UIPatient;
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
}
