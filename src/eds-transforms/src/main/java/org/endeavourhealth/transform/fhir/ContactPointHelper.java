package org.endeavourhealth.transform.fhir;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.ContactPoint;

import java.util.ArrayList;
import java.util.List;

public class ContactPointHelper {


    public static ContactPoint create(ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use, String value) {

        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return new ContactPoint()
                .setSystem(system)
                .setUse(use)
                .setValue(value);
    }

    public static List<ContactPoint> createWorkContactPoints(String telephone1, String telephone2, String fax, String email)
    {
        List<ContactPoint> contactPoints = new ArrayList<>();

        if (StringUtils.isNotBlank(telephone1))
            contactPoints.add(create(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK, telephone1));

        if (StringUtils.isNotBlank(telephone2))
            contactPoints.add(create(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK, telephone2));

        if (StringUtils.isNotBlank(email))
            contactPoints.add(create(ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.WORK, email));

        if (StringUtils.isNotBlank(fax))
            contactPoints.add(create(ContactPoint.ContactPointSystem.FAX, ContactPoint.ContactPointUse.WORK, fax));

        return contactPoints;
    }
}

