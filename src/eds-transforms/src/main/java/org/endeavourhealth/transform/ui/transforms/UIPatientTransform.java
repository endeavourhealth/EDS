package org.endeavourhealth.transform.ui.transforms;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.AddressHelper;
import org.endeavourhealth.transform.ui.helpers.NameHelper;
import org.endeavourhealth.transform.ui.models.UIAddress;
import org.endeavourhealth.transform.ui.models.UIHumanName;
import org.endeavourhealth.transform.ui.models.UIPatient;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class UIPatientTransform {

    public static UIPatient transform(Patient patient) {

        UIHumanName name = NameHelper.getUsualOrOfficialName(patient.getName());
        UIAddress homeAddress = AddressHelper.getHomeAddress(patient.getAddress());

        UIPatient UIPatient = new UIPatient()
                .setNhsNumber(getNhsNumber(patient.getIdentifier()))
                .setName(name)
                .setDateOfBirth(patient.getBirthDate())
                .setGenderFormatted(getGender(patient.getGender()))
                .setHomeAddress(homeAddress);

        return UIPatient;
    }

    private static String getGender(Enumerations.AdministrativeGender gender) {
        if (gender == null)
            return null;

        return gender.getDisplay();
    }

    private static String getNhsNumber(List<Identifier> identifiers) {
        String result = null;

        if (identifiers != null)
            for (Identifier identifier : identifiers)
                if (identifier.getSystem() != null)
                    if (identifier.getSystem().equals(FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER))
                        return identifier.getValue();

        if (result != null)
            result = result.replaceAll(" ", "");

        return result;
    }
}
