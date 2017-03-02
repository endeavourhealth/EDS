package org.endeavourhealth.core.fhirStorage.exceptions;

public class UnprocessableEntityException extends FhirStorageException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
    public UnprocessableEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
