package org.endeavourhealth.messaging.exceptions;

public class MsgxException extends Exception
{
    public MsgxException() {
        super();
    }
    public MsgxException(String message) { super(message); }
    public MsgxException(String message, Throwable cause) {
        super(message, cause);
    }
    public MsgxException(Throwable cause) {
        super(cause);
    }
}
