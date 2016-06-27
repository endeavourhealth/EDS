package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonIdentifierByNhsNumber;

@Accessor
public interface PersonIdentifierByNhsNumberAccessor {

    @Query("SELECT * FROM ehr.person_identifier_by_nhs_number WHERE nhs_number = :nhs_number")
    Result<PersonIdentifierByNhsNumber> getForNhsNumber(@Param("nhs_number") String nhsNumber);

}
