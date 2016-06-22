package org.endeavourhealth.patientui.framework.security;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class UserSecurityContext implements SecurityContext {

    private UserPrincipal userPrincipal = null;

    public UserSecurityContext(String nhsNumber) {
        userPrincipal = new UserPrincipal(nhsNumber);
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
        //TODO - change when using HTTPS
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
