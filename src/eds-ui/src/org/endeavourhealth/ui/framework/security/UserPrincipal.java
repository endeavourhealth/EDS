package org.endeavourhealth.ui.framework.security;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private UserContext userContext;

    public UserPrincipal(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public String getName() {
        return null;
    }

    public UserContext getUserContext() {
        return userContext;
    }
}
