package org.endeavourhealth.core.data.ehr;

import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PatientIdentifierByNhsNumberAccessor;
import org.endeavourhealth.core.data.ehr.models.PatientIdentifierByNhsNumber;

import java.util.List;

public class PatientIdentifierByNhsNumberRepository extends Repository {

    public List<PatientIdentifierByNhsNumber> getForNhsNumber(String nhsNumber) {

        PatientIdentifierByNhsNumberAccessor accessor = getMappingManager().createAccessor(PatientIdentifierByNhsNumberAccessor.class);
        return Lists.newArrayList(accessor.getForNhsNumber(nhsNumber));
    }

}
