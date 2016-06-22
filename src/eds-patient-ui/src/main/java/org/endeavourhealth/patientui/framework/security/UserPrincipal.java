package org.endeavourhealth.patientui.framework.security;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private String nhsNumber = null;

    public UserPrincipal(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    @Override
    public String getName() {
        return null;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }
}
