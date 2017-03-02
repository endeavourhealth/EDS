package org.endeavourhealth.core.data.transform.accessors;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.transform.models.EnterpriseIdMap;
import org.endeavourhealth.core.data.transform.models.EnterpriseOrganisationIdMap;

import java.util.UUID;

@Accessor
public interface EnterpriseIdMapAccessor {

    @Query("SELECT * FROM transform.enterprise_organisation_id_map WHERE organisation_ods_code = :organisation_ods_code")
    Result<EnterpriseOrganisationIdMap> getEnterpriseIdMapping(@Param("organisation_ods_code") String odsCode);

    @Query("SELECT * FROM transform.enterprise_id_map WHERE enterprise_table_name = :enterprise_table_name AND resource_type = :resource_type AND resource_id = :resource_id")
    Result<EnterpriseIdMap> getEnterpriseIdMapping(@Param("enterprise_table_name") String enterpriseTableName,
                                                   @Param("resource_type") String resourceType,
                                                    @Param("resource_id") UUID resourceId);

    /*@Query("SELECT max(enterprise_id) FROM transform.enterprise_id_map WHERE resource_type = :resource_type")
    ResultSet getMaxEnterpriseId(@Param("resource_type") String resourceType);*/

    @Query("SELECT max_enterprise_id FROM transform.enterprise_id_max WHERE enterprise_table_name = :enterprise_table_name")
    ResultSet getMaxEnterpriseId(@Param("enterprise_table_name") String enterpriseTableName);

}