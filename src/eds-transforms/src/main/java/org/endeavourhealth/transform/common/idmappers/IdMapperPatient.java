package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperPatient extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Patient patient = (Patient)resource;

        super.mapResourceId(patient, serviceId, systemId);
        super.mapExtensions(patient, serviceId, systemId);

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
    }
}
