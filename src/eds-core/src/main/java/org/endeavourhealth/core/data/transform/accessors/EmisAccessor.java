package org.endeavourhealth.core.data.transform.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.transform.models.EmisAdminResourceCache;
import org.endeavourhealth.core.data.transform.models.EmisCsvCodeMap;

@Accessor
public interface EmisAccessor {

    @Query("SELECT * FROM transform.emis_csv_code_map WHERE data_sharing_agreement_guid = :data_sharing_agreement_guid AND medication = :medication AND code_id = :code_id LIMIT 1")
    Result<EmisCsvCodeMap> getMostRecentCode(@Param("data_sharing_agreement_guid") String dataSharingAgreementGuid,
                                             @Param("medication") boolean medication,
                                             @Param("code_id") Long codeId);

    @Query("SELECT * FROM transform.emis_admin_resource_cache WHERE data_sharing_agreement_guid = :data_sharing_agreement_guid")
    Result<EmisAdminResourceCache> getCachedResources(@Param("data_sharing_agreement_guid") String dataSharingAgreementGuid);
}
