package org.endeavourhealth.transform.fhir;

import com.google.common.base.Strings;
import org.hl7.fhir.instance.model.ContactPoint;

public class ContactPointHelper {


    public static ContactPoint createContactPointIfRequired(ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use,
                                                            String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return createContactPoint(system, use, value);
    }
    public static ContactPoint createContactPoint(ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use, String value) {

        return new ContactPoint()
                .setSystem(system)
                .setUse(use)
                .setValue(value);
    }
}
