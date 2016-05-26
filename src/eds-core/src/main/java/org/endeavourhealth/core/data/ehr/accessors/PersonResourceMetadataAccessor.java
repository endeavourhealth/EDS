package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.core.data.ehr.models.PersonResourceMetadata;

import java.util.Date;
import java.util.UUID;

@Accessor
public interface PersonResourceMetadataAccessor {
    @Query("SELECT * FROM ehr.person_resource_metadata_by_date WHERE person_id = :person_id AND resource_type = :resource_type")
    Result<PersonResourceMetadata> getByResourceType(@Param("person_id") UUID personId, @Param("resource_type") String resourceType);

    @Query("SELECT * FROM ehr.person_resource_metadata_by_date WHERE person_id = :person_id AND resource_type = :resource_type AND effective_date >= :effective_date_start AND effective_date <= effective_date_end")
    Result<PersonResourceMetadata> getByDateRange(@Param("person_id") UUID personId, @Param("resource_type") String resourceType, @Param("effective_date_start") Date startDate, @Param("effective_date_end") Date endDate);
}
