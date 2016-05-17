package org.endeavourhealth.transform.emis.openhr.transforms.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtContact;
import org.endeavourhealth.transform.emis.openhr.schema.VocContactType;
import org.endeavourhealth.transform.emis.openhr.transforms.helpers.OpenHRHelper;
import org.hl7.fhir.instance.model.ContactPoint;

import java.util.ArrayList;
import java.util.List;

public class ContactPointConverter
{
    public static List<ContactPoint> convert(List<DtContact> sourceList) throws TransformException
    {
        if (sourceList == null)
            return null;

        List<ContactPoint> targetList = new ArrayList<>();

        for (DtContact source: sourceList)
        {
            ContactPoint target = convertDtContact(source);

            if (target != null)
                targetList.add(target);
        }

        return targetList;
    }

    private static ContactPoint convertDtContact(DtContact source) throws TransformException
    {
        String value = StringUtils.trimToNull(source.getValue());

        if (value == null)
            return null;

        OpenHRHelper.ensureDboNotDelete(source);

        ContactPoint contact = new ContactPoint();
        contact.setSystem(convertContactSystem(source.getContactType()));
        contact.setUse(convertContactInfoUse(source.getContactType()));
        contact.setValue(value);

        return contact;
    }

    private static ContactPoint.ContactPointSystem convertContactSystem(VocContactType openHRType) throws TransformException
    {
        switch (openHRType)
        {
            case H:
            case W:
            case M:
                return ContactPoint.ContactPointSystem.PHONE;
            case FX:
                return ContactPoint.ContactPointSystem.FAX;
            case EM:
                return ContactPoint.ContactPointSystem.EMAIL;
            default:
                throw new TransformException("VocContactType not supported: " + openHRType.toString());
        }
    }

    private static ContactPoint.ContactPointUse convertContactInfoUse(VocContactType openHRType) throws TransformException
    {
        switch (openHRType)
        {
            case H:
            case EM:
                return ContactPoint.ContactPointUse.HOME;
            case W:
            case FX:
                return ContactPoint.ContactPointUse.WORK;
            case M:
                return ContactPoint.ContactPointUse.MOBILE;
            default:
                throw new TransformException("VocContactType not supported: " + openHRType.toString());
        }
    }

}
