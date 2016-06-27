package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonConsentOrganisation;

import java.util.UUID;

@Accessor
public interface PersonConsentOrganisationAccessor {

    @Query("SELECT * FROM ehr.person_consent_organisation WHERE person_id = :person_id AND protocol_id = :protocol_id AND organisation_id = :organisation_id LIMIT 1")
    Result<PersonConsentOrganisation> getMostRecent(@Param("person_id") UUID personId,
                                                    @Param("protocol_id") UUID protocolId,
                                                    @Param("organisation_id") UUID organisationId);

    @Query("SELECT * FROM ehr.person_consent_organisation WHERE person_id = :person_id AND protocol_id = :protocol_id AND organisation_id = :organisation_id")
    Result<PersonConsentOrganisation> getHistory(@Param("person_id") UUID personId,
                                                 @Param("protocol_id") UUID protocolId,
                                                 @Param("organisation_id") UUID organisationId);

}
