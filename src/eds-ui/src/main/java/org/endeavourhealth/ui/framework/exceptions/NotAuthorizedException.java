package org.endeavourhealth.ui.framework.exceptions;

import org.endeavourhealth.coreui.framework.exceptions.MappedException;

import javax.ws.rs.core.Response;

public class NotAuthorizedException extends MappedException {
    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.UNAUTHORIZED;
    }
}
