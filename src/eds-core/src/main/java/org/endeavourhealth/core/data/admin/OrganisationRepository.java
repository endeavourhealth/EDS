package org.endeavourhealth.core.data.admin;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.admin.accessors.OrganisationAccessor;
import org.endeavourhealth.core.data.admin.models.*;

import java.util.UUID;

public class OrganisationRepository extends Repository {
    public Organisation getById(UUID id) {
        Mapper<Organisation> mapper = getMappingManager().mapper(Organisation.class);
        return mapper.get(id);
    }

    public Iterable<Organisation> getAll() {
        OrganisationAccessor accessor = getMappingManager().createAccessor(OrganisationAccessor.class);
        return accessor.getAll();
    }

    public Iterable<OrganisationEndUserLink> getByUserId(UUID userId) {
        OrganisationAccessor accessor = getMappingManager().createAccessor(OrganisationAccessor.class);
        return accessor.getOrganisationEndUserLinkByEndUserId(userId);
    }

    public Iterable<OrganisationServiceLink> getServices(UUID organisationId) {
        OrganisationAccessor accessor = getMappingManager().createAccessor(OrganisationAccessor.class);
        return accessor.getOrganisationServiceLinkByOrganisationId(organisationId);
    }

}

