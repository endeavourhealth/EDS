package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperOrganization extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        Organization organization = (Organization)resource;

        if (organization.hasIdentifier()) {
            super.mapIdentifiers(organization.getIdentifier(), resource, serviceId, systemId);
        }
        if (organization.hasPartOf()) {
            super.mapReference(organization.getPartOf(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(organization, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {
        throw new PatientResourceException(resource, true);
    }
}
