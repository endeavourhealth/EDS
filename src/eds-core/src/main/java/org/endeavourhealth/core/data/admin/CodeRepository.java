package org.endeavourhealth.core.data.admin;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.admin.models.SnomedLookup;

public class CodeRepository extends Repository {

    public SnomedLookup getSnomedLookup(String conceptId) {
        Mapper<SnomedLookup> mapper = getMappingManager().mapper(SnomedLookup.class);
        return mapper.get(conceptId);
    }
}
