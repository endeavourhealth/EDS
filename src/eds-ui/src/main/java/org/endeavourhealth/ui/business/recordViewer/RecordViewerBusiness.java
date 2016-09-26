package org.endeavourhealth.ui.business.recordViewer;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceByService;
import org.endeavourhealth.ui.business.recordViewer.models.JsonEncounter;
import org.endeavourhealth.ui.business.recordViewer.models.JsonPatient;
import org.endeavourhealth.ui.business.recordViewer.transforms.JsonEncounterTransform;
import org.endeavourhealth.ui.business.recordViewer.transforms.JsonPatientTransform;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.EpisodeOfCare;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RecordViewerBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerBusiness.class);

    public static JsonPatient getPatient(UUID serviceId, UUID systemId, UUID patientId) throws Exception {

        Patient patient = RecordViewerData.getSingleResourceByPatient(serviceId, systemId, patientId, Patient.class);

        return JsonPatientTransform.transform(patient)
            .setServiceId(serviceId)
            .setSystemId(systemId)
            .setPatientId(patientId);
    }

    public static List<JsonEncounter> getEncounters(UUID serviceId, UUID systemId, UUID patientId) throws Exception {
        List<Encounter> encounterList = RecordViewerData.getResourceByPatient(serviceId, systemId, patientId, Encounter.class);

        List<UUID> practitionerIds = JsonEncounterTransform.getPractitionerIds(encounterList);

        List<Practitioner> practitioners = RecordViewerData.getResourcesByService(serviceId, systemId, practitionerIds, Practitioner.class);


        return new ArrayList<>();
    }


}
