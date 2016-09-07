package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.UserRepository;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.security.RoleUtils;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.ui.framework.exceptions.BadRequestException;
import org.slf4j.MDC;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

public abstract class AbstractEndpoint {

    private static final String MDC_MARKER_UUID = "UserUuid";

    @Context
    protected SecurityContext securityContext;

    /**
     * used to set LogBack to include the user UUID in all logging
     */
    protected void setLogbackMarkers(SecurityContext sc) {
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        if (userUuid != null) {
            MDC.put(MDC_MARKER_UUID, userUuid.toString());
        }
    }
    public static void clearLogbackMarkers() {
        MDC.remove(MDC_MARKER_UUID);
    }


    protected EndUser getEndUserFromSession(SecurityContext sc) throws Exception {
        UserRepository userRepository = new UserRepository();

        UUID uuid = SecurityUtils.getCurrentUserId(sc);
        return userRepository.getById(uuid);
    }

    protected Organisation getOrganisationFromSession(SecurityContext sc) throws Exception {
        UUID uuid = getOrganisationUuidFromToken(sc);
        OrganisationRepository organisationRepository = new OrganisationRepository();

        return organisationRepository.getById(uuid);
    }

    protected UUID getOrganisationUuidFromToken(SecurityContext sc) throws Exception {
        //an authenticated user MUST have a EndUser UUID, but they may not have an organisation selected yet
        //TODO - need to work out ORG UUID using keycloak??
        UUID orgUuid = UUID.fromString("b6ff900d-8fcd-43d8-af37-5db3a87a6ef6");

        if (orgUuid == null) {
            throw new BadRequestException("Organisation must be selected before performing any actions");
        }
        return orgUuid;
    }

    protected boolean isAdminFromSession(SecurityContext sc) throws Exception {
        return RoleUtils.isAdmin(sc);       // TODO: should this be ADMIN or SUPERUSER??
    }

}
