package org.endeavourhealth.core.data.ehr;

import org.endeavourhealth.core.data.ehr.models.PersonConsentGlobal;
import org.endeavourhealth.core.data.ehr.models.PersonConsentOrganisation;
import org.endeavourhealth.core.data.ehr.models.PersonConsentProtocol;

import java.util.UUID;

public abstract class PersonConsentHelper {

    private static PersonConsentGlobalRepository globalRepository = new PersonConsentGlobalRepository();
    private static PersonConsentProtocolRepository protocolRepository = new PersonConsentProtocolRepository();
    private static PersonConsentOrganisationRepository organisationRepository = new PersonConsentOrganisationRepository();

    /**
     * get the explicit patient consent for the given protocol and organisation
     * returns:
     *      TRUE for explicit consent
     *      FALSE for explicit dissent
     *      NULL if no explicit consent or dissent
     */
    public static Boolean getPatientConsent(UUID personId, UUID protocolId, UUID organisationId) {

        //check global consent first. If it's an explicit dissent, then just return out
        PersonConsentGlobal globalConsent = globalRepository.getMostRecent(personId);
        if (isExplicitConsentOrDissent(globalConsent, false)) {
            return Boolean.FALSE;
        }

        //check protocol consent second. If it's an explicit dissent, return out
        PersonConsentProtocol protocolConsent = protocolRepository.getMostRecent(personId, protocolId);
        if (isExplicitConsentOrDissent(protocolConsent, false)) {
            return Boolean.FALSE;
        }

        //check organisation consent for explicit dissent last
        PersonConsentOrganisation organisationConsent = organisationRepository.getMostRecent(personId, protocolId, organisationId);
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
}
