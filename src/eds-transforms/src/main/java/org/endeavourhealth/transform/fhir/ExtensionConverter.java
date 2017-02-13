package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.DomainResource;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Type;

public class ExtensionConverter {

    public static Extension createExtension(String uri, Type value) {
        return new Extension()
                .setUrl(uri)
                .setValue(value);
    }

    public static Extension createCompoundExtension(String uri, Extension... subExtensions) {
        Extension e = new Extension()
                .setUrl(uri);

        for (Extension subExtension: subExtensions) {
            e.addExtension(subExtension);
        }

        return e;
    }

    public static Extension findExtension(DomainResource resource, String extensionUrl) {
        if (resource.hasExtension()) {
            for (Extension extension: resource.getExtension()) {
                if (extension.getUrl().equals(extensionUrl)) {
                    return extension;
                }
            }
        }
        return null;
    }
}
