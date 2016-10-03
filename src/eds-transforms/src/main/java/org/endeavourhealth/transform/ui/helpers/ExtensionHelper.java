package org.endeavourhealth.transform.ui.helpers;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DomainResource;
import org.hl7.fhir.instance.model.Extension;

public class ExtensionHelper {

    public static Extension getExtension(DomainResource resource, String name) {
        if (StringUtils.isEmpty(name) || resource == null)
            return null;

        for (Extension e : resource.getModifierExtension())
            if (name.equals(e.getUrl()))
                return e;

        for (Extension e : resource.getExtension())
            if (name.equals(e.getUrl()))
                return e;

        return null;
    }

    public static <T extends Object> T getExtensionValue(DomainResource resource, String name, Class<T> type) {
        Extension extension = getExtension(resource, name);

        if (extension == null)
            return null;

        if (type.equals(extension.getValue().getClass()))
            return (T)extension.getValue();

        throw new TransformRuntimeException("Extension value is not a " + type.getSimpleName());
    }
}
