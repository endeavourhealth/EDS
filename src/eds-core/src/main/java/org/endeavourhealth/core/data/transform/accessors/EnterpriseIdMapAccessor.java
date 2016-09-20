package org.endeavourhealth.core.data.transform.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.transform.models.EnterpriseIdMap;

import java.util.UUID;

@Accessor
public interface EnterpriseIdMapAccessor {

    @Query("SELECT * FROM transform.enterprise_id_map WHERE resource_type = :resource_type AND resource_id = :resource_id")
    Result<EnterpriseIdMap> getEnterpriseIdMapping(@Param("resource_type") String resourceType,
                                                    @Param("resource_id") UUID resourceId);

}