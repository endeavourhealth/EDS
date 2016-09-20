package org.endeavourhealth.ui.business.recordViewer;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;

class RecordViewerData {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerData.class);
    private static final String RESOURCE_TYPE_PATIENT = "Patient";

    public static Patient getPatientResource(UUID serviceId, UUID systemId, UUID patientId) throws Exception {

        ResourceRepository resourceRepository = new ResourceRepository();
        List<ResourceByPatient> resourceByPatientList = resourceRepository.getResourcesByPatient(serviceId, systemId, patientId, RESOURCE_TYPE_PATIENT);

        if ((resourceByPatientList == null) || (resourceByPatientList.size() == 0))
            throw new NotFoundException("Patient resource not found");

        if (resourceByPatientList.size() > 1)
            throw new InternalServerErrorException("Multiple patient resources found");

        JsonParser jsonParser = new JsonParser();
        Patient patient = (Patient)jsonParser.parse(resourceByPatientList.get(0).getResourceData());

        return patient;
    }
}
