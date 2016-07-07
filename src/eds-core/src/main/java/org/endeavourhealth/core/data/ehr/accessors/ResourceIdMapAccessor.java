package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonConsentGlobal;
import org.endeavourhealth.core.data.ehr.models.ResourceIdMap;

import java.util.UUID;

@Accessor
public interface ResourceIdMapAccessor {

    @Query("SELECT * FROM ehr.resource_id_map WHERE service_id = :service_id AND system_instance_id = :system_instance_id AND resource_type = :resource_type AND source_id = :source_id LIMIT 1")
    Result<ResourceIdMap> getResourceIdMap(@Param("service_id") UUID serviceId,
                                           @Param("system_instance_id") UUID systemInstanceId,
                                           @Param("resource_type") String resourceType,
                                           @Param("source_id") String sourceId);

}
