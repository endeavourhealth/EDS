package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperOrganization extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        Organization organization = (Organization)resource;

        super.mapResourceId(organization, serviceId, systemInstanceId);
        super.mapExtensions(organization, serviceId, systemInstanceId);

        if (organization.hasIdentifier()) {
            super.mapIdentifiers(organization.getIdentifier(), serviceId, systemInstanceId);
        }
        if (organization.hasPartOf()) {
            super.mapReference(organization.getPartOf(), serviceId, systemInstanceId);
        }
    }
}
