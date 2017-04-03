package org.endeavourhealth.core.data.transform.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.transform.models.EmisAdminResourceCache;
import org.endeavourhealth.core.data.transform.models.VitruCarePatientIdMap;

import java.util.UUID;

@Accessor
public interface VitruCareAccessor {

    @Query("SELECT * FROM transform.vitrucare_patient_id_map WHERE eds_patient_id = :eds_patient_id")
    Result<VitruCarePatientIdMap> getVitruCareIdMapping(@Param("eds_patient_id") UUID edsPatientId);
}
