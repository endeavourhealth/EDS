package org.endeavourhealth.messaging.exceptions;

public class ResolutionException extends Exception
{
    public ResolutionException() {
        super();
    }
    public ResolutionException(String message) { super(message); }
    public ResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
    public ResolutionException(Throwable cause) {
        super(cause);
    }
}
