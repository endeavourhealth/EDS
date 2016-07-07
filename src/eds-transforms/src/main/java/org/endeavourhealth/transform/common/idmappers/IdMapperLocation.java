package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperLocation extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Location location = (Location)resource;

        super.mapResourceId(location, serviceId, systemInstanceId);
        super.mapExtensions(location, serviceId, systemInstanceId);

        if (location.hasIdentifier()) {
            super.mapIdentifiers(location.getIdentifier(), serviceId, systemInstanceId);
        }
        if (location.hasManagingOrganization()) {
            super.mapReference(location.getManagingOrganization(), serviceId, systemInstanceId);
        }
        if (location.hasPartOf()) {
            super.mapReference(location.getPartOf(), serviceId, systemInstanceId);
        }
    }
}
