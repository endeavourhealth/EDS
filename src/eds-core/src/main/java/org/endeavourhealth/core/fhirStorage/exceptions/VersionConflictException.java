package org.endeavourhealth.core.fhirStorage.exceptions;

public class VersionConflictException extends FhirStorageException {
    private final String original;
    private final String replacement;

    public VersionConflictException(String original, String replacement) {
        super(String.format("The current resource on this server '%s' doesn't match the required version '%s'", original, replacement));
        this.original = original;
        this.replacement = replacement;
    }

    public String getOriginal() {
        return original;
    }

    public String getReplacement() {
        return replacement;
    }
}
