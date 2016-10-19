package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperLocation extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        Location location = (Location)resource;

        super.mapResourceId(location, serviceId, systemId);
        super.mapExtensions(location, serviceId, systemId);

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
