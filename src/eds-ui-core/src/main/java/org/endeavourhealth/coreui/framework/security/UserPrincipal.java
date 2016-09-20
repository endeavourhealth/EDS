package org.endeavourhealth.coreui.framework.security;

import java.security.Principal;

// TODO: replace
public class UserPrincipal implements Principal {
    private UserWrapper userWrapper = null;

    public UserPrincipal(UserWrapper userWrapper) {
        this.userWrapper = userWrapper;
    }

    @Override
    public String getName() {
        return null;
    }

    public UserWrapper getUserWrapper() {
        return userWrapper;
    }
}