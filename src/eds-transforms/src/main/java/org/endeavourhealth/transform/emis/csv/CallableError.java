package org.endeavourhealth.transform.emis.csv;

import java.util.concurrent.Callable;

public class CallableError {
    private Callable callable = null;
    private Exception exception = null;

    public CallableError(Callable callable, Exception exception) {
        this.callable = callable;
        this.exception = exception;
    }

    public Callable getCallable() {
        return callable;
    }

    public Exception getException() {
        return exception;
    }
}
