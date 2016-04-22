package org.endeavourhealth.messaging.exceptions;

public class ReceiverNotFoundException extends MsgxException
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
