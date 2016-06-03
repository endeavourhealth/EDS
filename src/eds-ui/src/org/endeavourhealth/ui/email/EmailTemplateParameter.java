package org.endeavourhealth.ui.email;

public enum EmailTemplateParameter {
    EMAIL_TO("[EmailTo]"),
    TOKEN("[Token]"),
    TITLE("[Title]"),
    FORENAME("[Forename]"),
    SURNAME("[Surname]"),
    ORGANISATION_NAME("[OrganisationName]"),
    ORGANISATION_ID("[OrganisationId]");

    private final String value;

    EmailTemplateParameter(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}