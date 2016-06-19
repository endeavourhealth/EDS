package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Type;

public class ExtensionConverter {

    public static Extension createExtension(String uri, Type value) {
        return new Extension()
                .setUrl(uri)
                .setValue(value);
    }

    public static Extension createExtension(String uri, Extension... subExtensions) {
        Extension e = new Extension()
                .setUrl(uri);

        for (Extension subExtension: subExtensions) {
            e.addExtension(subExtension);
        }

        return e;
    }
}
