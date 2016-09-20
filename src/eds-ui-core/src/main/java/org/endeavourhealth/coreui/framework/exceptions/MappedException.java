package org.endeavourhealth.coreui.framework.exceptions;

import javax.ws.rs.core.Response;

public abstract class MappedException extends Exception {
    public MappedException() {
        super();
    }

    public MappedException(String message) {
        super(message);
    }

    public abstract Response.Status getResponseStatus();
}
