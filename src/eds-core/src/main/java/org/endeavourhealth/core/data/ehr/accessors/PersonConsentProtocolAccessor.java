package org.endeavourhealth.core.data.ehr.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.ehr.models.PersonConsentProtocol;

import java.util.UUID;

@Accessor
public interface PersonConsentProtocolAccessor {

    @Query("SELECT * FROM ehr.person_consent_protocol WHERE person_id = :person_id AND protocol_id = :protocol_id LIMIT 1")
    Result<PersonConsentProtocol> getMostRecent(@Param("person_id") UUID personId, @Param("protocol_id") UUID protocolId);

    @Query("SELECT * FROM ehr.person_consent_protocol WHERE person_id = :person_id AND protocol_id = :protocol_id")
    Result<PersonConsentProtocol> getHistory(@Param("person_id") UUID personId, @Param("protocol_id") UUID protocolId);

}
