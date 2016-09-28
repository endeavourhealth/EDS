package org.endeavourhealth.transform.ui.helpers;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.StringType;

import java.util.List;

public class NameHelper {

    public static HumanName getUsualOrOfficialName(List<HumanName> names) {
        HumanName name = getNameByUse(names, HumanName.NameUse.USUAL);

        if (name == null)
            name = getNameByUse(names, HumanName.NameUse.OFFICIAL);

        return name;
    }

    public static String getNameForDisplay(HumanName name) {
        String surname = NameHelper.getFirst(name.getFamily());
        String forename = NameHelper.getFirst(name.getGiven());
        String title = NameHelper.getFirst(name.getPrefix());
        return NameHelper.getDisplayName(title, forename, surname);
    }

    public static String getUsualOrOfficialNameForDisplay(List<HumanName> names) {
        HumanName name = NameHelper.getUsualOrOfficialName(names);
        return getNameForDisplay(name);
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

    public static String getDisplayName(String title, String forename, String surname) {
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

    public static String getFirst(List<StringType> strings) {
        if (strings == null)
            return null;

        if (strings.size() == 0)
            return null;

        return strings.get(0).getValue();
    }
}
