package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifier;

import java.util.UUID;

@Accessor
public interface PatientIdentifierAccessor {

    @Query("SELECT * FROM ehr.patient_identifier WHERE service_id = :service_id AND system_instance_id = :system_instance_id AND patient_id = :patient_id LIMIT 1")
    Result<PatientIdentifier> getMostRecent(@Param("service_id") UUID serviceId,
                                            @Param("system_instance_id") UUID systemInstanceId,
                                            @Param("patient_id") UUID patientId);

}
