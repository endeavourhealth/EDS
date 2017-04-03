package org.endeavourhealth.core.data.transform.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.transform.models.ResourceIdMap;
import org.endeavourhealth.core.data.transform.models.ResourceIdMapByEdsId;

import java.util.UUID;

@Accessor
public interface ResourceIdMapAccessor {

    @Query("SELECT * FROM transform.resource_id_map WHERE service_id = :service_id AND system_id = :system_id AND resource_type = :resource_type AND source_id = :source_id LIMIT 1")
    Result<ResourceIdMap> getResourceIdMap(@Param("service_id") UUID serviceId,
                                           @Param("system_id") UUID systemId,
                                           @Param("resource_type") String resourceType,
                                           @Param("source_id") String sourceId);

    @Query("SELECT * FROM transform.resource_id_map_by_eds_id WHERE resource_type = :resource_type AND eds_id = :eds_id LIMIT 1")
    Result<ResourceIdMapByEdsId> getResourceIdMapByEdsId(@Param("resource_type") String resourceType,
                                                         @Param("eds_id") UUID edsId);

}
