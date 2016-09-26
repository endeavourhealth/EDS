package org.endeavourhealth.ui.business.recordViewer;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.ui.business.recordViewer.models.JsonPatient;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RecordViewerBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerBusiness.class);

    public static JsonPatient getDemographics(UUID serviceId, UUID systemId, UUID patientId) throws Exception {

        Patient patient = getSingleResource(serviceId, systemId, patientId, Patient.class);

        return PatientTransform.transform(patient)
            .setServiceId(serviceId)
            .setSystemId(systemId)
            .setPatientId(patientId);
    }

    private static <T> T getSingleResource(UUID serviceId, UUID systemId, UUID patientId, Class<T> resourceType) throws Exception {
        List<T> resources = getResource(serviceId, systemId, patientId, resourceType);

        if ((resources == null) || (resources.size() == 0))
            throw new Exception(resourceType + " resource not found");

        if (resources.size() > 1)
            throw new Exception("Multiple " + resourceType + " resources found");

        return resources.get(0);
    }

    private static <T> List<T> getResource(UUID serviceId, UUID systemId, UUID patientId, Class<T> resourceType) throws Exception {
        ResourceRepository resourceRepository = new ResourceRepository();
        List<ResourceByPatient> resourceByPatientList = resourceRepository.getResourcesByPatient(serviceId, systemId, patientId, resourceType.getSimpleName());

        JsonParser jsonParser = new JsonParser();

        List<T> result = new ArrayList<>();

        for (ResourceByPatient resourceByPatient : resourceByPatientList)
            result.add((T)jsonParser.parse(resourceByPatient.getResourceData()));

        return result;
    }
}
