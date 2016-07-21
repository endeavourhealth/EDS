package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonConsentGlobalAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonConsentGlobal;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PersonConsentGlobalRepository extends Repository {

    public void insert(PersonConsentGlobal personConsent) {
        if (personConsent == null) {
            throw new IllegalArgumentException("personConsent is null");
        }

        Mapper<PersonConsentGlobal> mapper = getMappingManager().mapper(PersonConsentGlobal.class);
        mapper.save(personConsent);
    }

    public PersonConsentGlobal getMostRecent(UUID personId) {

        PersonConsentGlobalAccessor accessor = getMappingManager().createAccessor(PersonConsentGlobalAccessor.class);
        Iterator<PersonConsentGlobal> iterator = accessor.getMostRecent(personId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<PersonConsentGlobal> getHistory(UUID personId) {

        PersonConsentGlobalAccessor accessor = getMappingManager().createAccessor(PersonConsentGlobalAccessor.class);
        return Lists.newArrayList(accessor.getHistory(personId));
    }

}
