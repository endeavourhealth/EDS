package org.endeavourhealth.messaging.exceptions;

public class EndpointException extends ResolutionException
{
    public EndpointException() {
        super();
    }
    public EndpointException(String message) { super(message); }
    public EndpointException(String message, Throwable cause) {
        super(message, cause);
    }
    public EndpointException(Throwable cause) {
        super(cause);
    }
}
