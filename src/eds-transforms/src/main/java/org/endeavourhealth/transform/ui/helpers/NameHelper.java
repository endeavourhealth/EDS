package org.endeavourhealth.transform.ui.helpers;

import org.endeavourhealth.transform.ui.models.types.UIHumanName;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.StringType;

import java.util.List;
import java.util.stream.Collectors;

public class NameHelper {

    public static UIHumanName getUsualOrOfficialName(List<HumanName> names) {
        HumanName name = getNameByUse(names, HumanName.NameUse.USUAL);

        if (name == null)
            name = getNameByUse(names, HumanName.NameUse.OFFICIAL);

        return transform(name);
    }

    public static UIHumanName transform(HumanName name) {
        return new UIHumanName()
                .setFamilyName(NameHelper.getFirst(name.getFamily()))
                .setGivenNames(NameHelper.getAll(name.getGiven()))
                .setPrefix(NameHelper.getFirst(name.getPrefix()));
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

    public static String getFirst(List<StringType> strings) {
        if (strings == null)
            return null;

        if (strings.size() == 0)
            return null;

        return strings.get(0).getValue();
    }

    public static List<String> getAll(List<StringType> strings) {
        if (strings == null)
            return null;

        return strings
                .stream()
                .map(t -> t.getValueNotNull())
                .collect(Collectors.toList());
    }
}
