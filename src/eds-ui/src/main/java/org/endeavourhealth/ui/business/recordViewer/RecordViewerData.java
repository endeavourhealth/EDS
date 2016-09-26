package org.endeavourhealth.ui.business.recordViewer;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceByService;
import org.hl7.fhir.instance.formats.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class RecordViewerData {
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

        if ((resources == null) || (resources.size() == 0))
            throw new Exception(resourceType + " resource not found");

        if (resources.size() > 1)
            throw new Exception("Multiple " + resourceType + " resources found");

        return resources.get(0);
    }

    public static <T> List<T> getResourcesByService(UUID serviceId, UUID systemId, UUID resourceId, Class<T> resourceType) throws Exception {
        ResourceRepository resourceRepository = new ResourceRepository();
        List<ResourceByService> resourceByServiceList = resourceRepository.getResourcesByService(serviceId, systemId, resourceType.getSimpleName(), resourceId);

        List<String> resources = resourceByServiceList
                .stream()
                .map(t -> t.getResourceData())
                .collect(Collectors.toList());

        return parse(resources, resourceType);
    }

    public static <T> T getSingleResourceByService(UUID serviceId, UUID systemId, UUID resourceId, Class<T> resourceType) throws Exception {
        List<T> resources = getResourcesByService(serviceId, systemId, resourceId, resourceType);

        if ((resources == null) || (resources.size() == 0))
            throw new Exception(resourceType + " resource not found");

        if (resources.size() > 1)
            throw new Exception("Multiple " + resourceType + " resources found");

        return resources.get(0);
    }

    private static <T> List<T> parse(List<String> resources, Class<T> resourceType) throws Exception {
        JsonParser jsonParser = new JsonParser();

        List<T> result = new ArrayList<>();

        for (String resource : resources)
            result.add((T)jsonParser.parse(resource));

        return result;
    }
}
