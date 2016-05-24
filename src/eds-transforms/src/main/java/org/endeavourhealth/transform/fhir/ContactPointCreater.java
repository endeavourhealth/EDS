package org.endeavourhealth.transform.fhir;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.PersonType;
import org.hl7.fhir.instance.model.ContactPoint;

import java.util.ArrayList;
import java.util.List;

public class ContactPointCreater
{
    public static ContactPoint create(String value, ContactPoint.ContactPointSystem contactPointSystem, ContactPoint.ContactPointUse contactPointUse)
    {
        return new ContactPoint()
                .setSystem(contactPointSystem)
                .setUse(contactPointUse)
                .setValue(value);
    }

    public static List<ContactPoint> createWorkContactPoints(String telephone1, String telephone2, String fax, String email)
    {
        List<ContactPoint> contactPoints = new ArrayList<>();

        if (StringUtils.isNotBlank(telephone1))
            contactPoints.add(ContactPointCreater.create(telephone1, ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK));

        if (StringUtils.isNotBlank(telephone2))
            contactPoints.add(ContactPointCreater.create(telephone2, ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK));

        if (StringUtils.isNotBlank(email))
            contactPoints.add(ContactPointCreater.create(email, ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.WORK));

        if (StringUtils.isNotBlank(fax))
            contactPoints.add(ContactPointCreater.create(fax, ContactPoint.ContactPointSystem.FAX, ContactPoint.ContactPointUse.WORK));

        return contactPoints;
    }
}
