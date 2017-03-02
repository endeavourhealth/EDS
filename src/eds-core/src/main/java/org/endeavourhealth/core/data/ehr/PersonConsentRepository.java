package org.endeavourhealth.core.data.ehr;

import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.ehr.accessors.PersonConsentAccessor;
import org.endeavourhealth.core.data.ehr.models.PersonConsentGlobal;
import org.endeavourhealth.core.data.ehr.models.PersonConsentOrganisation;
import org.endeavourhealth.core.data.ehr.models.PersonConsentProtocol;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PersonConsentRepository extends Repository {


    /**
     * get the explicit patient consent for the given protocol and organisation
     * returns:
     *      TRUE for explicit consent
     *      FALSE for explicit dissent
     *      NULL if no explicit consent or dissent
     */
    public static Boolean getPatientConsent(UUID personId, UUID protocolId, UUID organisationId) {

        PersonConsentRepository repository = new PersonConsentRepository();

        //check global consent first. If it's an explicit dissent, then just return out
        PersonConsentGlobal globalConsent = repository.getMostRecentGlobalConsent(personId);
        if (isExplicitConsentOrDissent(globalConsent, false)) {
            return Boolean.FALSE;
        }

        //check protocol consent second. If it's an explicit dissent, return out
        PersonConsentProtocol protocolConsent = repository.getMostRecentProtocolConsent(personId, protocolId);
        if (isExplicitConsentOrDissent(protocolConsent, false)) {
            return Boolean.FALSE;
        }

        //check organisation consent for explicit dissent last
        PersonConsentOrganisation organisationConsent = repository.getMostRecentOrganisationConsent(personId, protocolId, organisationId);
        if (isExplicitConsentOrDissent(organisationConsent, false)) {
            return Boolean.FALSE;
        }

        //having ruled out any dissent, work back through the objects to see if an explicit consent has been recorded
        if (isExplicitConsentOrDissent(organisationConsent, true)) {
            return Boolean.TRUE;
        }

        if (isExplicitConsentOrDissent(protocolConsent, true)) {
            return Boolean.TRUE;
        }

        if (isExplicitConsentOrDissent(globalConsent, true)) {
            return Boolean.TRUE;
        }

        //if we make it here, no explicit consent or dissent has been granted
        return null;
    }
    private static boolean isExplicitConsentOrDissent(PersonConsentGlobal personConsent, boolean consent) {
        return personConsent != null
                && personConsent.getConsentGiven() != null
                && personConsent.getConsentGiven().booleanValue() == consent;
    }
    private static boolean isExplicitConsentOrDissent(PersonConsentProtocol personConsent, boolean consent) {
        return personConsent != null
                && personConsent.getConsentGiven() != null
                && personConsent.getConsentGiven().booleanValue() == consent;
    }
    private static boolean isExplicitConsentOrDissent(PersonConsentOrganisation personConsent, boolean consent) {
        return personConsent != null
                && personConsent.getConsentGiven() != null
                && personConsent.getConsentGiven().booleanValue() == consent;
    }


    public void insert(PersonConsentGlobal personConsent) {
        if (personConsent == null) {
            throw new IllegalArgumentException("personConsent is null");
        }

        Mapper<PersonConsentGlobal> mapper = getMappingManager().mapper(PersonConsentGlobal.class);
        mapper.save(personConsent);
    }

    public PersonConsentGlobal getMostRecentGlobalConsent(UUID personId) {

        PersonConsentAccessor accessor = getMappingManager().createAccessor(PersonConsentAccessor.class);
        Iterator<PersonConsentGlobal> iterator = accessor.getMostRecent(personId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<PersonConsentGlobal> getGlobalConsentHistory(UUID personId) {

        PersonConsentAccessor accessor = getMappingManager().createAccessor(PersonConsentAccessor.class);
        return Lists.newArrayList(accessor.getHistory(personId));
    }

    public void insert(PersonConsentOrganisation personConsent) {
        if (personConsent == null) {
            throw new IllegalArgumentException("personConsent is null");
        }

        Mapper<PersonConsentOrganisation> mapper = getMappingManager().mapper(PersonConsentOrganisation.class);
        mapper.save(personConsent);
    }

    public PersonConsentOrganisation getMostRecentOrganisationConsent(UUID personId, UUID protocolUuid, UUID organisationUuid) {

        PersonConsentAccessor accessor = getMappingManager().createAccessor(PersonConsentAccessor.class);
        Iterator<PersonConsentOrganisation> iterator = accessor.getMostRecent(personId, protocolUuid, organisationUuid).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<PersonConsentOrganisation> getOrganisationConsentHistory(UUID personId, UUID protocolUuid, UUID organisationUuid) {

        PersonConsentAccessor accessor = getMappingManager().createAccessor(PersonConsentAccessor.class);
        return Lists.newArrayList(accessor.getHistory(personId, protocolUuid, organisationUuid));
    }


    public void insert(PersonConsentProtocol personConsent) {
        if (personConsent == null) {
            throw new IllegalArgumentException("personConsent is null");
        }

        Mapper<PersonConsentProtocol> mapper = getMappingManager().mapper(PersonConsentProtocol.class);
        mapper.save(personConsent);
    }

    public PersonConsentProtocol getMostRecentProtocolConsent(UUID personId, UUID protocolUuid) {

        PersonConsentAccessor accessor = getMappingManager().createAccessor(PersonConsentAccessor.class);
        Iterator<PersonConsentProtocol> iterator = accessor.getMostRecent(personId, protocolUuid).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<PersonConsentProtocol> getProtocolConsentlHistory(UUID personId, UUID protocolUuid) {

        PersonConsentAccessor accessor = getMappingManager().createAccessor(PersonConsentAccessor.class);
        return Lists.newArrayList(accessor.getHistory(personId, protocolUuid));
    }
}
