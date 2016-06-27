package org.endeavourhealth.core.data.ehr;

import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonConsentProtocolAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonConsentProtocol;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PersonConsentProtocolRepository extends Repository {

    public void insert(PersonConsentProtocol personConsent) {
        if (personConsent == null) {
            throw new IllegalArgumentException("personConsent is null");
        }

        super.insert(personConsent);
    }

    public PersonConsentProtocol getMostRecent(UUID personId, UUID protocolUuid) {

        PersonConsentProtocolAccessor accessor = getMappingManager().createAccessor(PersonConsentProtocolAccessor.class);
        Iterator<PersonConsentProtocol> iterator = accessor.getMostRecent(personId, protocolUuid).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<PersonConsentProtocol> getHistory(UUID personId, UUID protocolUuid) {

        PersonConsentProtocolAccessor accessor = getMappingManager().createAccessor(PersonConsentProtocolAccessor.class);
        return Lists.newArrayList(accessor.getHistory(personId, protocolUuid));
    }

}
