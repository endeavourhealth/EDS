package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Person;
import org.hl7.fhir.instance.model.HumanName;

import java.util.List;

public class NameConverter
{
    public static List<HumanName> convertName(OpenHR001Person sourcePerson)
    {
        return org.endeavourhealth.common.fhir.NameConverter.convert(
                sourcePerson.getTitle(),
                sourcePerson.getForenames(),
                sourcePerson.getSurname(),
                sourcePerson.getCallingName(),
                sourcePerson.getBirthSurname(),
                sourcePerson.getPreviousSurname());
    }
}
