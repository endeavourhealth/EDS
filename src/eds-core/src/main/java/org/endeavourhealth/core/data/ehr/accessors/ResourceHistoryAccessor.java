package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;

import java.util.UUID;

@Accessor
public interface ResourceHistoryAccessor {
    @Query("SELECT * FROM ehr.resource_history WHERE resource_type = :resource_type AND resource_id = :resource_id LIMIT 1")
    ResourceHistory getCurrentVersion(@Param("resource_type") String resourceType, @Param("resource_id") UUID resourceId);

    @Query("SELECT * FROM ehr.resource_history WHERE resource_type = :resource_type AND resource_id = :resource_id")
    Result<ResourceHistory> getResourceHistory(@Param("resource_type") String resourceType, @Param("resource_id") UUID resourceId);

    @Query("SELECT resource_checksum FROM ehr.resource_history WHERE resource_type = :resource_type AND resource_id = :resource_id LIMIT 1")
    ResultSet getCurrentChecksum(@Param("resource_type") String resourceType, @Param("resource_id") UUID resourceId);

}
