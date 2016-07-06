package org.endeavourhealth.core.data.ehr;

import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonIdentifierByNhsNumberAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonIdentifierByNhsNumber;

import java.util.List;

public class PersonIdentifierByNhsNumberRepository extends Repository {

    public List<PersonIdentifierByNhsNumber> getForNhsNumber(String nhsNumber) {

        PersonIdentifierByNhsNumberAccessor accessor = getMappingManager().createAccessor(PersonIdentifierByNhsNumberAccessor.class);
        return Lists.newArrayList(accessor.getForNhsNumber(nhsNumber));
    }

}
