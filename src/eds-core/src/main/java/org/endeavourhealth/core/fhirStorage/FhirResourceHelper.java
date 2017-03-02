package org.endeavourhealth.core.fhirStorage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.endeavourhealth.core.data.ehr.HasResourceDataJson;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.PrimitiveType;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FhirResourceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FhirResourceHelper.class);

    public static UUID getResourceId(Resource resource) {
        return UUID.fromString(resource.getId());
    }

    public static String getResourceType(Resource resource) {
        return resource.getResourceType().toString();
    }

    public static Date getLastUpdated(Resource resource) {
        Meta meta = resource.getMeta();
        return meta.getLastUpdated();
    }

    public static List<String> getProfiles(Resource resource) {
        Meta meta = resource.getMeta();

        if (!meta.hasProfile()) {
            return null;
        }

        return meta.getProfile()
                .stream()
                .map(PrimitiveType::toString)
                .collect(Collectors.toList());
    }

    public static void updateMetaTags(Resource resource, UUID version, Date createdAt) {
        Meta meta = resource.getMeta();
        meta.setVersionId(version.toString());
        meta.setLastUpdated(createdAt);
    }

    public static Resource deserialiseResouce(HasResourceDataJson resourceWrapper) throws Exception {
        String json = resourceWrapper.getResourceData();
        return deserialiseResouce(json);
    }

    public static Resource deserialiseResouce(String json) throws Exception {

        try {
            return new JsonParser().parse(json);

        } catch (Exception ex) {
            LOG.error("Error deserialising resource", ex);
            LOG.error(json);
            throw ex;
        }
    }
}
