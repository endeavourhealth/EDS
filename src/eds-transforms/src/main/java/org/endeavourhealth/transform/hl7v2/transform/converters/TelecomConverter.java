package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xad;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Xtn;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.ContactPoint;

public class TelecomConverter {

    public static ContactPoint convert(Xtn source) throws TransformException {

        ContactPoint target = new ContactPoint();

        if (StringUtils.isNotBlank(source.getEquipmentType()))
            target.setSystem(convertSystemType(source.getEquipmentType()));

        if (StringUtils.isNotBlank(source.getTelephoneNumber()))
            target.setValue(source.getTelephoneNumber());

        if (StringUtils.isNotBlank(source.getUseCode()))
            target.setUse(convertUseCode(source.getUseCode()));

        return target;
    }

    private static ContactPoint.ContactPointSystem convertSystemType(String systemType) throws TransformException {
        systemType = systemType.trim().toLowerCase();

        switch (systemType) {
            case "PH": return ContactPoint.ContactPointSystem.PHONE;
            case "FX": return ContactPoint.ContactPointSystem.FAX;
            case "Internet": return ContactPoint.ContactPointSystem.EMAIL;
            case "BP": return ContactPoint.ContactPointSystem.PAGER;
            default: throw new TransformException(systemType + " system type not recognised");
        }
    }

    private static ContactPoint.ContactPointUse convertUseCode(String useCode) throws TransformException {
        useCode = useCode.trim().toLowerCase();

        switch (useCode) {
            case "H": return ContactPoint.ContactPointUse.HOME;
            case "PRN": return ContactPoint.ContactPointUse.HOME;
            case "ORN": return ContactPoint.ContactPointUse.HOME;
            case "VHN": return ContactPoint.ContactPointUse.HOME;
            case "WP": return ContactPoint.ContactPointUse.WORK;
            case "WPN": return ContactPoint.ContactPointUse.WORK;
            case "TMP": return ContactPoint.ContactPointUse.TEMP;
            case "OLD": return ContactPoint.ContactPointUse.OLD;
            case "MC": return ContactPoint.ContactPointUse.MOBILE;
            case "PRS": return ContactPoint.ContactPointUse.MOBILE;
            default: throw new TransformException(useCode + " use code not recognised");
        }
    }
}
