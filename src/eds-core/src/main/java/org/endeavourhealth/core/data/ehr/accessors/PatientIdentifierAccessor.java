package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifier;

import java.util.UUID;

@Accessor
public interface PatientIdentifierAccessor {

    @Query("SELECT * FROM ehr.patient_identifier WHERE service_id = :service_id AND system_id = :system_id AND patient_id = :patient_id AND local_id_system = :local_id_system LIMIT 1")
    Result<PatientIdentifier> getMostRecent(@Param("service_id") UUID serviceId,
                                            @Param("system_id") UUID systemId,
                                            @Param("patient_id") UUID patientId,
                                            @Param("local_id_system") String localIdSystem);

}
