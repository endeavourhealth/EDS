package org.endeavourhealth.patientui.endpoints;

import org.endeavourhealth.patientui.framework.security.UserPrincipal;
import org.endeavourhealth.patientui.framework.security.UserWrapper;
import org.slf4j.MDC;

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
        UserWrapper userWrapper = up.getUserWrapper();
        return userWrapper.getNhsNumber();
    }

    protected UUID getPersonIdFromSession(SecurityContext sc) throws Exception {
        UserPrincipal up = (UserPrincipal)sc.getUserPrincipal();
        if (up == null) {
            return null;
        }
        UserWrapper userWrapper = up.getUserWrapper();
        return userWrapper.getPersonId();
    }

/*    protected String convertToJsonArray(List<String> list) {
        return "[" + list.t  "]";
    }*/

}
