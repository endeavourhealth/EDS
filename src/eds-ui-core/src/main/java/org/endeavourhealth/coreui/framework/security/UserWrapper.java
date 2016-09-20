package org.endeavourhealth.coreui.framework.security;

import java.util.UUID;

// TODO: replace
public class UserWrapper {
    private String nhsNumber = null;
    private UUID personId = null;

    public UserWrapper(String nhsNumber, UUID personId) {
        this.nhsNumber = nhsNumber;
        this.personId = personId;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public UUID getPersonId() {
        return personId;
    }
}