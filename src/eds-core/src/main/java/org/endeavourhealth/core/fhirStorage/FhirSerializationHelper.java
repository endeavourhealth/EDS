package org.endeavourhealth.core.fhirStorage;

import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.core.fhirStorage.exceptions.SerializationException;
import org.hl7.fhir.instance.model.Resource;

public class FhirSerializationHelper {
    private static final ParserPool PARSER_POOL = new ParserPool();
    public static String serializeResource(Resource resource) throws SerializationException {
        try {
            return PARSER_POOL.composeString(resource);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public static Resource deserializeResource(String resourceAsJsonString) throws SerializationException {
        try {
            return PARSER_POOL.parse(resourceAsJsonString);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
