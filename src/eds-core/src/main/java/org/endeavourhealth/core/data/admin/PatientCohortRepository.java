package org.endeavourhealth.core.data.admin;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.admin.accessors.PatientCohortAccessor;

import java.util.UUID;

public class PatientCohortRepository extends Repository {

    public boolean isInCohort(UUID protocolId, UUID serviceId, String nhsNumber) {
        PatientCohortAccessor accessor = getMappingManager().createAccessor(PatientCohortAccessor.class);
        ResultSet resultSet = accessor.getLatestCohortStatus(protocolId, serviceId, nhsNumber);
        Row row = resultSet.one();
        if (row != null) {
            return row.getBool(0);
        } else {
            //if there's no row in the result set, the patient has never been in the cohort
            return false;
        }
    }
}
