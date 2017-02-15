package org.endeavourhealth.transform.common;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.endeavourhealth.core.data.transform.ResourceIdMapRepository;
import org.endeavourhealth.core.data.transform.models.ResourceIdMap;
import org.endeavourhealth.core.data.transform.models.ResourceIdMapByEdsId;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.common.idmappers.BaseIdMapper;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            //by default the Java Caching System has a load of logging enabled, which is really slow, so turn it off
            org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.apache.jcs");
            logger.setLevel(org.apache.log4j.Level.OFF);

            cache = JCS.getInstance("ResourceIdentifiers");
        } catch (CacheException ex) {
            throw new RuntimeException("Error initialising cache", ex);
        }
    }

    private static String createCacheKey(UUID serviceId, UUID systemId, ResourceType resourceType, String sourceId) {
        return serviceId + "/" + systemId + "/" + resourceType + "/" + sourceId;
    }

    public static String getOrCreateEdsResourceIdString(UUID serviceId, UUID systemId, ResourceType resourceType, String sourceId) {
        return getOrCreateEdsResourceId(serviceId, systemId, resourceType, sourceId).toString();
    }

    public static UUID getOrCreateEdsResourceId(UUID serviceId, UUID systemId, ResourceType resourceType, String sourceId) {
        String key = createCacheKey(serviceId, systemId, resourceType, sourceId);

        UUID edsId = (UUID)cache.get(key);
        if (edsId == null) {
            ResourceIdMap mapping = repository.getResourceIdMap(serviceId, systemId, resourceType.toString(), sourceId);
            if (mapping == null) {
                mapping = new ResourceIdMap();
                mapping.setServiceId(serviceId);
                mapping.setSystemId(systemId);
                mapping.setResourceType(resourceType.toString());
                mapping.setSourceId(sourceId);
                mapping.setEdsId(UUID.randomUUID());
                repository.insert(mapping);

                //logging to trace problem with EmisOpen duplicate patients
                if (resourceType == ResourceType.Patient) {
                    LOG.trace("Creating new EDS ID " + mapping.getEdsId() + " for patient " + sourceId + " service " + serviceId + " system " + systemId);
                }
            }

            edsId = mapping.getEdsId();
            try {
                cache.put(key, edsId);
            } catch (Exception ex) {
                LOG.error("Error adding key ["+key+"] value ["+edsId+"] to ID map cache", ex);
            }
        }
        return edsId;
    }

    public static UUID getEdsResourceId(UUID serviceId, UUID systemId, ResourceType resourceType, String sourceId) {
        String key = createCacheKey(serviceId, systemId, resourceType, sourceId);

        UUID edsId = (UUID)cache.get(key);
        if (edsId == null) {
            ResourceIdMap mapping = repository.getResourceIdMap(serviceId, systemId, resourceType.toString(), sourceId);
            if (mapping == null) {
                return null;
            }

            edsId = mapping.getEdsId();
            try {
                cache.put(key, edsId);
            } catch (Exception ex) {
                LOG.error("Error adding key ["+key+"] value ["+edsId+"] to ID map cache", ex);
            }
        }
        return edsId;
    }

    public static boolean mapIds(UUID serviceId, UUID systemId, Resource resource) throws Exception {
        return mapIds(serviceId, systemId, resource, true);
    }

    /**
     * maps the ID and all IDs within references in a FHIR resource to unique ones in the EDS space
     * returns true to indicate the resource is new to us, false otherwise
     */
    public static boolean mapIds(UUID serviceId, UUID systemId, Resource resource, boolean mapResourceId) throws Exception {
        return getIdMapper(resource).mapIds(resource, serviceId, systemId, mapResourceId);
    }

    /**
     * returns the patient ID of the resource or null if it doesn't have one. If called with
     * a resource that doesn't support a patient ID, an exception is thrown
     */
    public static String getPatientId(Resource resource) throws Exception {
        return getIdMapper(resource).getPatientId(resource);
    }

    private static BaseIdMapper getIdMapper(Resource resource) throws Exception {

        BaseIdMapper mapper = idMappers.get(resource.getClass());
        if (mapper == null) {
            String clsName = "org.endeavourhealth.transform.common.idmappers.IdMapper" + resource.getClass().getSimpleName();
            try {
                Class cls = Class.forName(clsName);
                mapper = (BaseIdMapper)cls.newInstance();
                idMappers.put(resource.getClass(), mapper);
            } catch (Exception ex) {
                throw new TransformException("Exception creating ID Mapper for " + clsName, ex);
            }
        }
        return mapper;
    }

    public static Reference convertLocallyUniqueReferenceToEdsReference(Reference localReference, FhirResourceFiler fhirResourceFiler) {
        ReferenceComponents components = ReferenceHelper.getReferenceComponents(localReference);
        String locallyUniqueId = components.getId();
        ResourceType resourceType = components.getResourceType();

        String globallyUniqueId = getOrCreateEdsResourceIdString(fhirResourceFiler.getServiceId(),
                fhirResourceFiler.getSystemId(),
                resourceType,
                locallyUniqueId);

        return ReferenceHelper.createReference(resourceType, globallyUniqueId);
    }

    public static Reference convertEdsReferenceToLocallyUniqueReference(Reference edsReference, FhirResourceFiler fhirResourceFiler) {
        ReferenceComponents components = ReferenceHelper.getReferenceComponents(edsReference);
        ResourceType resourceType = components.getResourceType();
        ResourceIdMapByEdsId mapping = repository.getResourceIdMapByEdsId(resourceType.toString(), components.getId());
        String emisId = mapping.getSourceId();

        return ReferenceHelper.createReference(resourceType, emisId);
    }

}
