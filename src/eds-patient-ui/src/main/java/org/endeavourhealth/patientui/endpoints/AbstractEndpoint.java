package org.endeavourhealth.patientui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.UserRepository;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.patientui.framework.security.UserPrincipal;
import org.slf4j.MDC;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

public abstract class AbstractEndpoint {

    private static final String MDC_MARKER_UUID = "nhsNumber";

    @Context
    protected SecurityContext securityContext;

    /**
     * used to set LogBack to include the user UUID in all logging
     */
    protected void setLogbackMarkers(SecurityContext sc) throws Exception {
        String nhsNumber = getNhsNumberFromSession(sc);
        if (nhsNumber != null) {
            MDC.put(MDC_MARKER_UUID, nhsNumber);
        }
    }
    public static void clearLogbackMarkers() {
        MDC.remove(MDC_MARKER_UUID);
    }

    protected String getNhsNumberFromSession(SecurityContext sc) throws Exception {
        UserPrincipal up = (UserPrincipal)sc.getUserPrincipal();
        if (up == null) {
            return null;
        }
        return up.getNhsNumber();
    }

}
