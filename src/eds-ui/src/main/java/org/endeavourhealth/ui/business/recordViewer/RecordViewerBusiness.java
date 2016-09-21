package org.endeavourhealth.ui.business.recordViewer;

import org.endeavourhealth.ui.business.recordViewer.models.JsonPatient;
import org.hl7.fhir.instance.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class RecordViewerBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(RecordViewerBusiness.class);

    public static JsonPatient getDemographics(UUID serviceId, UUID systemId, UUID patientId) throws Exception {

        Patient patient = RecordViewerData.getPatientResource(serviceId, systemId, patientId);

        return PatientTransform.transform(patient);
    }
}
