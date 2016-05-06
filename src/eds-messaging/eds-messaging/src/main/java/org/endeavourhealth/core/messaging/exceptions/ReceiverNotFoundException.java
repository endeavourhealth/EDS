package org.endeavourhealth.core.messaging.exceptions;

public class ReceiverNotFoundException extends ResolutionException
{
    public ReceiverNotFoundException() {
        super();
    }
    public ReceiverNotFoundException(String message) { super(message); }
    public ReceiverNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public ReceiverNotFoundException(Throwable cause) {
        super(cause);
    }
}
