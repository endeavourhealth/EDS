package org.endeavourhealth.core.fhirStorage.exceptions;

public class SerializationException extends FhirStorageException  {
    public SerializationException(Throwable cause) {
        super(cause);
    }
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
