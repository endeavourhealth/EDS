package org.endeavourhealth.messaging.exceptions;

public class ReceiverMethodNotSupportedException extends ResolutionException
{
    public ReceiverMethodNotSupportedException() {
        super();
    }
    public ReceiverMethodNotSupportedException(String message) { super(message); }
    public ReceiverMethodNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
    public ReceiverMethodNotSupportedException(Throwable cause) {
        super(cause);
    }
}
