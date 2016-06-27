package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonConsentOrganisation;
import org.endeavourhealth.core.data.ehr.models.PersonIdentifier;

import java.util.UUID;

@Accessor
public interface PersonIdentifierAccessor {

    @Query("SELECT * FROM ehr.person_identifier WHERE organisation_id = :organisation_id AND local_id = :local_id LIMIT 1")
    Result<PersonIdentifier> getMostRecent(@Param("organisation_id") UUID organisationId,
                                           @Param("local_id") String localId);

}
