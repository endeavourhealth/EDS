package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonResource;

import java.util.UUID;

@Accessor
public interface PersonResourceAccessor {
    @Query("SELECT * FROM ehr.person_resource WHERE person_id = :person_id AND resource_type = :resource_type")
    Result<PersonResource> getByResourceType(@Param("person_id") UUID personId, @Param("resource_type") String resourceType);

    @Query("SELECT * FROM ehr.person_resource WHERE person_id = :person_id AND resource_type = :resource_type AND service_id = :service_id")
    Result<PersonResource> getByService(@Param("person_id") UUID personId, @Param("resource_type") String resourceType, @Param("service_id") UUID serviceId);

    @Query("SELECT * FROM ehr.person_resource WHERE person_id = :person_id AND resource_type = :resource_type AND service_id = :service_id AND system_instance_id = :system_instance_id")
    Result<PersonResource> getBySystemInstance(@Param("person_id") UUID personId, @Param("resource_type") String resourceType, @Param("service_id") UUID serviceId, @Param("system_instance_id") UUID systemInstanceId);
}
