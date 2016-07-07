package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperPractitioner extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Practitioner practitioner = (Practitioner)resource;

        super.mapResourceId(practitioner, serviceId, systemInstanceId);
        super.mapExtensions(practitioner, serviceId, systemInstanceId);

        if (practitioner.hasIdentifier()) {
            super.mapIdentifiers(practitioner.getIdentifier(), serviceId, systemInstanceId);
        }
        if (practitioner.hasPractitionerRole()) {
            for (Practitioner.PractitionerPractitionerRoleComponent role: practitioner.getPractitionerRole()) {
                if (role.hasManagingOrganization()) {
                    super.mapReference(role.getManagingOrganization(), serviceId, systemInstanceId);
                }
                if (role.hasLocation()) {
                    super.mapReferences(role.getLocation(), serviceId, systemInstanceId);
                }
                if (role.hasHealthcareService()) {
                    super.mapReferences(role.getHealthcareService(), serviceId, systemInstanceId);
                }
            }
        }
    }
}
