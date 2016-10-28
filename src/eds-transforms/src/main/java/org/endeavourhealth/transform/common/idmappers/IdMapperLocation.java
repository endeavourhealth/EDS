package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperLocation extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Location location = (Location)resource;

        super.mapCommonResourceFields(location, serviceId, systemId, mapResourceId);

        if (location.hasIdentifier()) {
            super.mapIdentifiers(location.getIdentifier(), resource, serviceId, systemId);
        }
        if (location.hasManagingOrganization()) {
            super.mapReference(location.getManagingOrganization(), resource, serviceId, systemId);
        }
        if (location.hasPartOf()) {
            super.mapReference(location.getPartOf(), resource, serviceId, systemId);
        }
    }
}
