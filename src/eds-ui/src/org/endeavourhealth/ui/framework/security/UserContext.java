package org.endeavourhealth.ui.framework.security;

import javax.ws.rs.core.SecurityContext;
import java.util.Date;
import java.util.UUID;

public class UserContext {
    private UUID userUuid;
    private UUID organisationUuid;
    private boolean isAdmin;
    private Date tokenIssued;

    public UserContext(UUID userUuid, UUID organisationUuid, boolean isAdmin, Date tokenIssued) {
        this.userUuid = userUuid;
        this.organisationUuid = organisationUuid;
        this.isAdmin = isAdmin;
        this.tokenIssued = tokenIssued;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public UUID getOrganisationUuid() {
        return organisationUuid;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public Date getTokenIssued() {
        return tokenIssued;
    }

    public static UserContext fromSecurityContext(SecurityContext securityContext) {
        if (securityContext != null)
            if (securityContext.getUserPrincipal() != null)
                if (UserPrincipal.class.isInstance(securityContext.getUserPrincipal()))
                    return ((UserPrincipal) securityContext.getUserPrincipal()).getUserContext();

        return null;
    }
}
