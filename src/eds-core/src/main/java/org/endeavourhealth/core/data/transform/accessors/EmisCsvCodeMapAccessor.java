package org.endeavourhealth.core.data.transform.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.transform.models.EmisCsvCodeMap;

@Accessor
public interface EmisCsvCodeMapAccessor {

    @Query("SELECT * FROM transform.emis_csv_code_map WHERE medication = :medication AND code_id = :code_id LIMIT 1")
    Result<EmisCsvCodeMap> getMostRecent(@Param("medication") boolean medication,
                                         @Param("code_id") Long codeId);

}
