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

    @Query("SELECT * FROM ehr.person_identifier WHERE service_id = :service_id AND system_instance_id = :system_instance_id AND local_id = :local_id LIMIT 1")
    Result<PersonIdentifier> getMostRecent(@Param("service_id") UUID serviceId,
                                           @Param("system_instance_id") UUID systemInstanceId,
                                           @Param("local_id") String localId);

}
