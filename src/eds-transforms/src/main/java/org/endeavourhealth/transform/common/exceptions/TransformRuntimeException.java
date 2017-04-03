package org.endeavourhealth.transform.common.exceptions;

public class TransformRuntimeException extends RuntimeException {

    public TransformRuntimeException(String message) {
        super(message);
    }
    public TransformRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    public TransformRuntimeException(Throwable cause) {
        super(cause);
    }
}
