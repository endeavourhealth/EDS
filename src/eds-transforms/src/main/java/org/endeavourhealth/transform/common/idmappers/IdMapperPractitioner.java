package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperPractitioner extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        Practitioner practitioner = (Practitioner)resource;

        super.mapResourceId(practitioner, serviceId, systemId);
        super.mapExtensions(practitioner, serviceId, systemId);

        if (practitioner.hasIdentifier()) {
            super.mapIdentifiers(practitioner.getIdentifier(), resource, serviceId, systemId);
        }
        if (practitioner.hasPractitionerRole()) {
            for (Practitioner.PractitionerPractitionerRoleComponent role: practitioner.getPractitionerRole()) {
                if (role.hasManagingOrganization()) {
                    super.mapReference(role.getManagingOrganization(), resource, serviceId, systemId);
                }
                if (role.hasLocation()) {
                    super.mapReferences(role.getLocation(), resource, serviceId, systemId);
                }
                if (role.hasHealthcareService()) {
                    super.mapReferences(role.getHealthcareService(), resource, serviceId, systemId);
                }
            }
        }
    }
}
