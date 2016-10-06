package org.endeavourhealth.transform.ui.transforms.admin;

import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.AddressHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.IdentifierHelper;
import org.endeavourhealth.transform.ui.helpers.NameHelper;
import org.endeavourhealth.transform.ui.models.types.UIAddress;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.endeavourhealth.transform.ui.models.types.UIHumanName;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPatient;
import org.hl7.fhir.instance.model.*;

import java.util.List;

public class UIPatientTransform {

    public static UIPatient transform(Patient patient) {

        UIHumanName name = NameHelper.getUsualOrOfficialName(patient.getName());
        UIAddress homeAddress = AddressHelper.getHomeAddress(patient.getAddress());

        return new UIPatient()
                .setId(patient.getId())
                .setNhsNumber(getNhsNumber(patient.getIdentifier()))
                .setName(name)
                .setDateOfBirth(getBirthDate(patient))
                .setGender(patient.getGender().toCode())
                .setHomeAddress(homeAddress);
    }

    private static UIDate getBirthDate(Patient patient) {
        if (!patient.hasBirthDate())
            return null;

        return DateHelper.convert(patient.getBirthDateElement());
    }

    private static String getNhsNumber(List<Identifier> identifiers) {
        String result = IdentifierHelper.getIdentifierBySystem(identifiers, FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER);

        if (result != null)
            result = result.replaceAll(" ", "");

        return result;
    }
}
