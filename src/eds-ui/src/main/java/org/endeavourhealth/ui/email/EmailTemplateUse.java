package org.endeavourhealth.ui.email;

public enum EmailTemplateUse {
    INVITATION("invitation"),
    PASSWORD_RESET("passwordReset"),
    NEW_ORGANISATION("newOrganisation");

    private final String value;

    EmailTemplateUse(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
