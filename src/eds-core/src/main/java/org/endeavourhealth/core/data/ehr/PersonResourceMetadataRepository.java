package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonResourceMetadataAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonResourceMetadata;

import java.util.Date;
import java.util.UUID;

public class PersonResourceMetadataRepository extends Repository {
    public PersonResourceMetadata getByKey(UUID personId, String resourceType, Date effectiveDate, UUID serviceId, UUID systemInstanceId, String resourceId) {
        Mapper<PersonResourceMetadata> mapperPersonResource = getMappingManager().mapper(PersonResourceMetadata.class);
        return mapperPersonResource.get(personId, resourceType, effectiveDate, serviceId, systemInstanceId, resourceId);
    }

    public Iterable<PersonResourceMetadata> getByResourceType(UUID personId, String resourceType) {
        PersonResourceMetadataAccessor accessor = getMappingManager().createAccessor(PersonResourceMetadataAccessor.class);
        return accessor.getByResourceType(personId, resourceType);
    }

    public Iterable<PersonResourceMetadata> getByDateRange(UUID personId, String resourceType, Date startDate, Date endDate) {
        PersonResourceMetadataAccessor accessor = getMappingManager().createAccessor(PersonResourceMetadataAccessor.class);
        return accessor.getByDateRange(personId, resourceType, startDate, endDate);
    }
}
