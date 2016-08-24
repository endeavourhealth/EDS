package org.endeavourhealth.transform.common.exceptions;

public class FutureException extends Exception {

    public FutureException(Exception cause) {
        super("Exception processing task in threadpool", cause);
    }
}
