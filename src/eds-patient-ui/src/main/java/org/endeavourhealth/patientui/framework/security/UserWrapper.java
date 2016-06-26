package org.endeavourhealth.patientui.framework.security;

import java.util.UUID;

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
