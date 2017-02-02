package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperPatient extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Patient patient = (Patient)resource;

        if (patient.hasIdentifier()) {
            super.mapIdentifiers(patient.getIdentifier(), resource, serviceId, systemId);
        }
        if (patient.hasContact()) {
            for (Patient.ContactComponent contact: patient.getContact()) {
                if (contact.hasOrganization()) {
                    super.mapReference(contact.getOrganization(), resource, serviceId, systemId);
                }
            }
        }
        if (patient.hasCareProvider()) {
            super.mapReferences(patient.getCareProvider(), resource, serviceId, systemId);
        }
        if (patient.hasManagingOrganization()) {
            super.mapReference(patient.getManagingOrganization(), resource, serviceId, systemId);
        }
        if (patient.hasLink()) {
            for (Patient.PatientLinkComponent link: patient.getLink()) {
                if (link.hasOther()) {
                    super.mapReference(link.getOther(), resource, serviceId, systemId);
                }
            }
        }

        return super.mapCommonResourceFields(patient, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        Patient patient = (Patient)resource;
        return patient.getId();
    }
}
