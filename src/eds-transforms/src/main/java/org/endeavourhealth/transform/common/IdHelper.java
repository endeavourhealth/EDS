package org.endeavourhealth.transform.common;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.endeavourhealth.core.data.ehr.ResourceIdMapRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceIdMap;
import org.endeavourhealth.transform.common.idmappers.BaseIdMapper;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IdHelper {
    private static final Logger LOG = LoggerFactory.getLogger(IdHelper.class);

    private static JCS cache = null;
    private static Map<Class, BaseIdMapper> idMappers = new ConcurrentHashMap<>();
    private static ResourceIdMapRepository repository = new ResourceIdMapRepository();

    static {
        try {
            cache = JCS.getInstance("ResourceIdentifiers");
        } catch (CacheException ex) {
            throw new RuntimeException("Error initialising cache", ex);
        }
    }

    private static String createCacheKey(UUID serviceId, UUID systemInstanceId, ResourceType resourceType, String sourceId) {
        return serviceId + "/" + systemInstanceId + "/" + resourceType + "/" + sourceId;
    }

    public static String getEdsResourceId(UUID serviceId, UUID systemInstanceId, ResourceType resourceType, String sourceId) {
        String key = createCacheKey(serviceId, systemInstanceId, resourceType, sourceId);
        String edsId = (String)cache.get(key);
        if (edsId == null) {
            ResourceIdMap mapping = repository.getResourceIdMap(serviceId, systemInstanceId, resourceType.toString(), sourceId);
            if (mapping == null) {
                mapping = new ResourceIdMap();
                mapping.setServiceId(serviceId);
                mapping.setSystemInstanceId(systemInstanceId);
                mapping.setResourceType(resourceType.toString());
                mapping.setSourceId(sourceId);
                mapping.setEdsId(UUID.randomUUID());
                repository.insert(mapping);
            }

            edsId = mapping.getEdsId().toString();
            try {
                cache.put(key, edsId);
            } catch (Exception ex) {
                LOG.error("Error adding key [" + key + "] value [" + edsId + "] to ID map cache", ex);
            }
        }
        return edsId;
    }

    public static void mapIds(UUID serviceId, UUID systemInstanceId, List<Resource> resources) {
        resources
                .parallelStream()
                .forEach((resource) -> { mapIds(serviceId, systemInstanceId, resource); });
    }
    public static void mapIds(UUID serviceId, UUID systemInstanceId, Resource resource) {

        BaseIdMapper mapper = idMappers.get(resource.getClass());
        if (mapper == null) {
            String clsName = "org.endeavourhealth.transform.common.idmappers.IdMapper" + resource.getClass().getSimpleName();
            try {
                Class cls = Class.forName(clsName);
                mapper = (BaseIdMapper)cls.newInstance();
                idMappers.put(resource.getClass(), mapper);
            } catch (Exception ex) {
                throw new RuntimeException("Exception creating ID Mapper for " + clsName, ex);
            }

        }

        mapper.mapIds(resource, serviceId, systemInstanceId);
    }
}
