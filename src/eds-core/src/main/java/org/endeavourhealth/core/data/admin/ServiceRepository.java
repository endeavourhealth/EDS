package org.endeavourhealth.core.data.admin;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.admin.accessors.ServiceAccessor;
import org.endeavourhealth.core.data.admin.models.OrganisationServiceLink;
import org.endeavourhealth.core.data.admin.models.Service;

import java.util.UUID;

public class ServiceRepository extends Repository {
    public Service getById(UUID id) {
        Mapper<Service> mapper = getMappingManager().mapper(Service.class);
        return mapper.get(id);
    }

    public Iterable<Service> getAll() {
        ServiceAccessor accessor = getMappingManager().createAccessor(ServiceAccessor.class);
        return accessor.getAll();
    }

    public Iterable<OrganisationServiceLink> getOrganisations(UUID serviceId) {
        ServiceAccessor accessor = getMappingManager().createAccessor(ServiceAccessor.class);
        return accessor.getOrganisationServiceLinkByServiceId(serviceId);
    }

}

