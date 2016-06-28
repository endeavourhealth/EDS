package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonConsentGlobalAccessor;
import org.endeavourhealth.core.data.ehr.accessors.PersonIdentifierAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonConsentGlobal;
import org.endeavourhealth.core.data.ehr.models.PersonIdentifier;

import java.util.Iterator;
import java.util.UUID;

public class PersonIdentifierRepository extends Repository {

    public void insert(PersonIdentifier personIdentifier) {
        if (personIdentifier == null) {
            throw new IllegalArgumentException("personIdentifier is null");
        }

        Mapper<PersonIdentifier> mapper = getMappingManager().mapper(PersonIdentifier.class);
        mapper.save(personIdentifier);
    }

    public PersonIdentifier getMostRecent(UUID organisationId, String localId) {

        PersonIdentifierAccessor accessor = getMappingManager().createAccessor(PersonIdentifierAccessor.class);
        Iterator<PersonIdentifier> iterator = accessor.getMostRecent(organisationId, localId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }


}
