package org.endeavourhealth.core.fhirStorage.exceptions;

public abstract class FhirStorageException extends Exception {
    // Base fhir storage exception exception class for handling storage error states
    public FhirStorageException(String message) {
        super(message);
    }
    public FhirStorageException(Throwable cause) {
        super(cause);
    }
    public FhirStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
