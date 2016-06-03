package org.endeavourhealth.ui.framework.security;

import java.security.Principal;

public class UserSecurityContext implements javax.ws.rs.core.SecurityContext {
    private UserPrincipal userPrincipal;

    public UserSecurityContext(UserContext userContext) {
        userPrincipal = new UserPrincipal(userContext);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
