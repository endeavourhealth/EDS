package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperOrganization extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Organization organization = (Organization)resource;

        super.mapCommonResourceFields(organization, serviceId, systemId, mapResourceId);

        if (organization.hasIdentifier()) {
            super.mapIdentifiers(organization.getIdentifier(), resource, serviceId, systemId);
        }
        if (organization.hasPartOf()) {
            super.mapReference(organization.getPartOf(), resource, serviceId, systemId);
        }
    }
}
