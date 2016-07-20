package org.endeavourhealth.core.fhirStorage;

import org.endeavourhealth.core.fhirStorage.exceptions.SerializationException;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;

public class JsonSerializationHelper {
    public static String serializeResource(Resource resource) throws SerializationException {
        try {
            return new JsonParser().composeString(resource);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public static Resource deserializeResource(String resourceAsJsonString) throws SerializationException {
        try {
            return new JsonParser().parse(resourceAsJsonString);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
