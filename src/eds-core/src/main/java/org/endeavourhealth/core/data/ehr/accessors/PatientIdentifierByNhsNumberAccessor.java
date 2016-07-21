package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;

@Accessor
public interface PatientIdentifierByNhsNumberAccessor {

    @Query("SELECT * FROM ehr.oatient_identifier_by_nhs_number WHERE nhs_number = :nhs_number")
    Result<PatientIdentifierByNhsNumber> getForNhsNumber(@Param("nhs_number") String nhsNumber);

}
