package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xpn;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.HumanName;

public class NameConverter {
    public static HumanName convert(Xpn source) throws TransformException {
        HumanName humanName = new HumanName();

        if (StringUtils.isNotBlank(source.getFamilyName()))
            humanName.addFamily(source.getFamilyName());

        if (StringUtils.isNotBlank(source.getGivenName()))
            humanName.addGiven(source.getGivenName());

        if (StringUtils.isNotBlank(source.getMiddleName()))
            humanName.addGiven(source.getMiddleName());

        if (StringUtils.isNotBlank(source.getPrefix()))
            humanName.addPrefix(source.getPrefix());

        if (StringUtils.isNotBlank(source.getSuffix()))
            humanName.addSuffix(source.getSuffix());

        humanName.setUse(convertNameTypeCode(source.getNameTypeCode()));

        return humanName;
    }

    private static HumanName.NameUse convertNameTypeCode(String nameTypeCode) throws TransformException {
        Validate.notNull(nameTypeCode);

        nameTypeCode = nameTypeCode.trim().toLowerCase();

        switch (nameTypeCode) {

            // HL7v2 table 0200
            case "a": return HumanName.NameUse.NICKNAME;        // alias
            case "l": return HumanName.NameUse.OFFICIAL;        // legal
            case "d": return HumanName.NameUse.USUAL;           // display
            case "m": return HumanName.NameUse.MAIDEN;          // maiden
            case "c": return HumanName.NameUse.OLD;             // adopted
            case "o": return HumanName.NameUse.TEMP;            // other

            // Cerner Millenium
            case "adopted": return HumanName.NameUse.OLD;
            case "alternate": return HumanName.NameUse.NICKNAME;
            case "current": return HumanName.NameUse.USUAL;
            case "legal": return HumanName.NameUse.OFFICIAL;
            case "maiden": return HumanName.NameUse.MAIDEN;
            case "other": return HumanName.NameUse.TEMP;
            case "previous": return HumanName.NameUse.OLD;
            case "preferred": return HumanName.NameUse.USUAL;

            default: throw new TransformException(nameTypeCode + " name type code not recognised");
        }
    }
}
