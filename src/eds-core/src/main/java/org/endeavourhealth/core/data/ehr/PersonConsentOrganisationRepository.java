package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonConsentOrganisationAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonConsentOrganisation;
import org.endeavourhealth.core.data.ehr.models.PersonConsentProtocol;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PersonConsentOrganisationRepository extends Repository {

    public void insert(PersonConsentOrganisation personConsent) {
        if (personConsent == null) {
            throw new IllegalArgumentException("personConsent is null");
        }

        Mapper<PersonConsentOrganisation> mapper = getMappingManager().mapper(PersonConsentOrganisation.class);
        mapper.save(personConsent);
    }

    public PersonConsentOrganisation getMostRecent(UUID personId, UUID protocolUuid, UUID organisationUuid) {

        PersonConsentOrganisationAccessor accessor = getMappingManager().createAccessor(PersonConsentOrganisationAccessor.class);
        Iterator<PersonConsentOrganisation> iterator = accessor.getMostRecent(personId, protocolUuid, organisationUuid).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<PersonConsentOrganisation> getHistory(UUID personId, UUID protocolUuid, UUID organisationUuid) {

        PersonConsentOrganisationAccessor accessor = getMappingManager().createAccessor(PersonConsentOrganisationAccessor.class);
        return Lists.newArrayList(accessor.getHistory(personId, protocolUuid, organisationUuid));
    }

}
