package org.endeavourhealth.core.messaging.exceptions;

public class MessageNotFoundException extends ResolutionException
{
    public MessageNotFoundException() {
        super();
    }
    public MessageNotFoundException(String message) { super(message); }
    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public MessageNotFoundException(Throwable cause) {
        super(cause);
    }
}
