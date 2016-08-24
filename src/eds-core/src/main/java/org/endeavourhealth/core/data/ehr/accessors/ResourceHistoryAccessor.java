package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.ResourceByPatient;
import org.endeavourhealth.core.data.ehr.models.ResourceHistory;
import org.endeavourhealth.core.data.ehr.models.ResourceTypesUsed;

import java.util.UUID;

@Accessor
public interface ResourceHistoryAccessor {
    @Query("SELECT * FROM ehr.resource_history WHERE resource_type = :resource_type AND resource_id = :resource_id LIMIT 1")
    ResourceHistory getCurrentVersion(@Param("resource_type") String resourceType, @Param("resource_id") UUID resourceId);

    @Query("SELECT * FROM ehr.resource_history WHERE resource_type = :resource_type AND resource_id = :resource_id")
    Result<ResourceHistory> getResourceHistory(@Param("resource_type") String resourceType, @Param("resource_id") UUID resourceId);

    @Query("SELECT * FROM ehr.resource_by_patient WHERE service_id = :service_id AND system_id = :system_id AND patient_id = :patient_id")
    Result<ResourceByPatient> getResourcesByPatient(@Param("service_id") UUID serviceId,
                                                    @Param("system_id") UUID systemId,
                                                    @Param("patient_id") UUID patientId);

    @Query("SELECT * FROM ehr.resource_by_patient WHERE service_id = :service_id AND system_id = :system_id AND patient_id = :patient_id AND resource_type = :resource_type")
    Result<ResourceByPatient> getResourcesByPatient(@Param("service_id") UUID serviceId,
                                                  @Param("system_id") UUID systemId,
                                                  @Param("patient_id") UUID patientId,
                                                  @Param("resource_type") String resourceType);

    @Query("SELECT * FROM ehr.resource_types_used WHERE service_id = :service_id AND system_id = :system_id")
    Result<ResourceTypesUsed> getResourceTypesUsed(@Param("service_id") UUID serviceId,
                                                   @Param("system_id") UUID systemId);

}
