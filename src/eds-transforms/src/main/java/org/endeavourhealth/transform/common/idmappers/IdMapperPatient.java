package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperPatient extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Patient patient = (Patient)resource;

        super.mapResourceId(patient, serviceId, systemInstanceId);
        super.mapExtensions(patient, serviceId, systemInstanceId);

        if (patient.hasIdentifier()) {
            super.mapIdentifiers(patient.getIdentifier(), serviceId, systemInstanceId);
        }
        if (patient.hasContact()) {
            for (Patient.ContactComponent contact: patient.getContact()) {
                if (contact.hasOrganization()) {
                    super.mapReference(contact.getOrganization(), serviceId, systemInstanceId);
                }
            }
        }
        if (patient.hasCareProvider()) {
            super.mapReferences(patient.getCareProvider(), serviceId, systemInstanceId);
        }
        if (patient.hasManagingOrganization()) {
            super.mapReference(patient.getManagingOrganization(), serviceId, systemInstanceId);
        }
        if (patient.hasLink()) {
            for (Patient.PatientLinkComponent link: patient.getLink()) {
                if (link.hasOther()) {
                    super.mapReference(link.getOther(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
