package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByLocalId;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByPatientId;

import java.util.UUID;

@Accessor
public interface PatientIdentifierAccessor {

    @Query("SELECT * FROM ehr.patient_identifier_by_local_id WHERE service_id = :service_id AND system_id = :system_id AND local_id = :local_id AND local_id_system = :local_id_system LIMIT 1")
    Result<PatientIdentifierByLocalId> getMostRecentForLocalId(@Param("service_id") UUID serviceId,
                                                     @Param("system_id") UUID systemId,
                                                     @Param("local_id") String localId,
                                                     @Param("local_id_system") String localIdSystem);

    @Query("SELECT * FROM ehr.patient_identifier_by_local_id WHERE service_id = :service_id AND system_id = :system_id AND local_id = :local_id")
    Result<PatientIdentifierByLocalId> getForLocalId(@Param("service_id") UUID serviceId,
                                                               @Param("system_id") UUID systemId,
                                                               @Param("local_id") String localId);

    @Query("SELECT * FROM ehr.patient_identifier_by_nhs_number WHERE nhs_number = :nhs_number")
    Result<PatientIdentifierByNhsNumber> getForNhsNumber(@Param("nhs_number") String nhsNumber);


    @Query("SELECT * FROM ehr.patient_identifier_by_patient_id WHERE patient_id = :patient_id LIMIT 1")
    Result<PatientIdentifierByPatientId> getMostRecentForPatientId(@Param("patient_id") UUID patientId);


    // temporary call while we build a proper patient find index
    @Query("SELECT * FROM ehr.patient_identifier_by_local_id WHERE service_id = :service_id AND system_id = :system_id AND nhs_number = :nhsNumber ALLOW FILTERING;")
    Result<PatientIdentifierByLocalId> getForNhsNumberTemporary(@Param("service_id") UUID serviceId,
                                                     @Param("system_id") UUID systemId,
                                                     @Param("nhsNumber") String nhsNumber);

    // temporary call while we build a proper patient find index
    @Query("SELECT * FROM ehr.patient_identifier_by_local_id WHERE service_id = :service_id AND system_id = :system_id AND surname = :surname ALLOW FILTERING;")
    Result<PatientIdentifierByLocalId> getForSurnameTemporary(@Param("service_id") UUID serviceId,
                                                     @Param("system_id") UUID systemId,
                                                     @Param("surname") String surname);

    // temporary call while we build a proper patient find index
    @Query("SELECT * FROM ehr.patient_identifier_by_local_id WHERE service_id = :service_id AND system_id = :system_id AND forenames = :forenames ALLOW FILTERING;")
    Result<PatientIdentifierByLocalId> getForForenamesTemporary(@Param("service_id") UUID serviceId,
                                                       @Param("system_id") UUID systemId,
                                                       @Param("forenames") String forenames);

    @Query("DELETE FROM ehr.patient_identifier_by_local_id WHERE service_id = :service_id AND system_id = :system_id")
    ResultSet hardDeleteForService(@Param("service_id") UUID serviceId,
                               @Param("system_id") UUID systemId);
}
