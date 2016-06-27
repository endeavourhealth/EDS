package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonConsentGlobal;

import java.util.UUID;

@Accessor
public interface PersonConsentGlobalAccessor {

    @Query("SELECT * FROM ehr.person_consent_global WHERE person_id = :person_id LIMIT 1")
    Result<PersonConsentGlobal> getMostRecent(@Param("person_id") UUID personId);

    @Query("SELECT * FROM ehr.person_consent_global WHERE person_id = :person_id")
    Result<PersonConsentGlobal> getHistory(@Param("person_id") UUID personId);

}
