package org.endeavourhealth.transform.ui.helpers;

import org.hl7.fhir.instance.model.Identifier;

import java.util.List;

public class IdentifierHelper {
    public static String getIdentifierBySystem(List<Identifier> identifiers, String system) {
        String result = null;

        if (identifiers != null)
            for (Identifier identifier : identifiers)
                if (identifier.getSystem() != null)
                    if (identifier.getSystem().equals(system))
                        return identifier.getValue();

        if (result != null)
            result = result.replaceAll(" ", "");

        return result;
    }
}
