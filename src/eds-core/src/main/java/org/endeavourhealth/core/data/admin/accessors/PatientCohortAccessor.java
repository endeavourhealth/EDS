package org.endeavourhealth.core.data.admin.accessors;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.admin.models.Service;

import java.util.UUID;

@Accessor
public interface PatientCohortAccessor {

    @Query("SELECT in_cohort FROM admin.patient_cohort WHERE protocol_id = :protocol_id AND service_id = :service_id AND nhs_number = :nhs_number LIMIT 1;")
    ResultSet getLatestCohortStatus(@Param("protocol_id") UUID protocolId,
                                   @Param("service_id") UUID serviceId,
                                   @Param("nhs_number") String nhsNumber);
}
