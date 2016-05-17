package org.endeavourhealth.transform.emis.openhr.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.emis.openhr.schema.OpenHR001Person;
import org.hl7.fhir.instance.model.HumanName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NameConverter
{
    public static List<HumanName> convertName(OpenHR001Person sourcePerson)
    {
        return NameConverter.convert(
                sourcePerson.getTitle(),
                sourcePerson.getForenames(),
                sourcePerson.getSurname(),
                sourcePerson.getCallingName(),
                sourcePerson.getBirthSurname(),
                sourcePerson.getPreviousSurname());
    }

    public static List<HumanName> convert(String title, String forenames, String surname, String callingName, String birthSurname, String previousSurname)
    {
        return convertCleaned(
                StringUtils.trimToNull(forenames),
                StringUtils.trimToNull(callingName),
                StringUtils.trimToNull(surname),
                StringUtils.trimToNull(birthSurname),
                StringUtils.trimToNull(previousSurname),
                StringUtils.trimToNull(title));
    }

    public static HumanName convert(String forenames, String surname, String title)
    {
        List<HumanName> results = convertCleaned(
                StringUtils.trimToNull(forenames),
                null,
                StringUtils.trimToNull(surname),
                null,
                null,
                StringUtils.trimToNull(title));

        if (results.size() != 1)
            throw new IllegalStateException("Expected list of size 1");
        else
            return results.get(0);
    }

    private static List<HumanName> convertCleaned(String forenames, String callingName, String surname, String birthSurname, String previousSurname, String title)
    {
        List<HumanName> list = new ArrayList<>();

        //scope to ensure that the usualName variable is not accidentally reused during any copy and paste
        list.add(createName(HumanName.NameUse.OFFICIAL, title, forenames, surname));

        if (birthSurname != null && !birthSurname.equalsIgnoreCase(surname))
            list.add(createName(HumanName.NameUse.OLD, title, forenames, birthSurname));

        if (previousSurname != null && !previousSurname.equalsIgnoreCase(surname))
            list.add(createName(HumanName.NameUse.OLD, title, forenames, previousSurname));

        if (callingName != null && !callingName.equalsIgnoreCase(forenames))
            list.add(createName(HumanName.NameUse.USUAL, title, callingName, surname));

        return list;
    }

    private static HumanName createName(HumanName.NameUse use, String title, String forenames, String surname)
    {
        HumanName name = new HumanName()
                .setUse(use)
                .setText(buildDisplayFormat(title, forenames, surname));

        List<String> titleList = split(title);

        if (titleList != null)
            titleList.forEach(name::addPrefix);

        List<String> forenameList = split(forenames);

        if (forenameList != null)
            forenameList.forEach(name::addGiven);

        List<String> surnameList = split(surname);

        if (surnameList != null)
            surnameList.forEach(name::addFamily);


        return name;
    }

    private static List<String> split(String input)
    {
        if (input == null)
            return null;

        return new ArrayList<String>(Arrays.asList(input.split(" ")));
    }

    private static String buildDisplayFormat(String title, String forenames, String surname)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(surname.toUpperCase());

        List<String> forenameList = split(forenames);

        if (forenameList != null && forenameList.size() > 0)
        {
            sb.append(", ");
            sb.append(forenameList.get(0));
        }

        if (StringUtils.isNotBlank(title))
        {
            sb.append(" (");
            sb.append(title);
            sb.append(")");
        }

        return sb.toString();
    }
}

