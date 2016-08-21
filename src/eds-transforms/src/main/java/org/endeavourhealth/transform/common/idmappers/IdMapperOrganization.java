package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperOrganization extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        Organization organization = (Organization)resource;

        super.mapResourceId(organization, serviceId, systemId);
        super.mapExtensions(organization, serviceId, systemId);

        if (organization.hasIdentifier()) {
            super.mapIdentifiers(organization.getIdentifier(), resource, serviceId, systemId);
        }
        if (organization.hasPartOf()) {
            super.mapReference(organization.getPartOf(), resource, serviceId, systemId);
        }
    }
}
