package org.endeavourhealth.patientexplorer.utility;

import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceByService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResourceFetcher {
    public static final ParserPool PARSER_POOL = new ParserPool();

    public static <T> List<T> getResourceByPatient(UUID serviceId, UUID systemId, UUID patientId, Class<T> resourceType) throws Exception {
        ResourceRepository resourceRepository = new ResourceRepository();
        List<ResourceByPatient> resourceByPatientList = resourceRepository.getResourcesByPatient(serviceId, systemId, patientId, resourceType.getSimpleName());

        List<String> resources = resourceByPatientList
                .stream()
                .map(t -> t.getResourceData())
                .collect(Collectors.toList());

        return parse(resources, resourceType);
    }

    public static <T> T getSingleResourceByPatient(UUID serviceId, UUID systemId, UUID patientId, Class<T> resourceType) throws Exception {
        List<T> resources = getResourceByPatient(serviceId, systemId, patientId, resourceType);

        return ensureSingleResource(resources, resourceType);
    }

    public static <T> List<T> getResourcesByService(UUID serviceId, UUID systemId, List<UUID> resourceIds, Class<T> resourceType) throws Exception {
        ResourceRepository resourceRepository = new ResourceRepository();
        List<ResourceByService> resourceByServiceList = resourceRepository.getResourcesByService(serviceId, systemId, resourceType.getSimpleName(), resourceIds);

        List<String> resources = resourceByServiceList
                .stream()
                .map(t -> t.getResourceData())
                .collect(Collectors.toList());

        return parse(resources, resourceType);
    }

    public static <T> T getSingleResourceByService(UUID serviceId, UUID systemId, List<UUID> resourceIds, Class<T> resourceType) throws Exception {
        List<T> resources = getResourcesByService(serviceId, systemId, resourceIds, resourceType);

        return ensureSingleResource(resources, resourceType);
    }

    private static <T> T ensureSingleResource(List<T> resources, Class<T> resourceType) throws Exception {
        if ((resources == null) || (resources.size() == 0))
            throw new Exception(resourceType.getSimpleName() + " resource not found");

        if (resources.size() > 1)
            throw new Exception("Multiple " + resourceType.getSimpleName() + " resources found");

        return resources.get(0);
    }

    private static <T> List<T> parse(List<String> resources, Class<T> resourceType) throws Exception {
        List<T> result = new ArrayList<>();

        for (String resource : resources)
            result.add((T)PARSER_POOL.parse(resource));

        return result;
    }
}
