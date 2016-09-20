package org.endeavourhealth.coreui.framework.exceptions;

import javax.ws.rs.core.Response;

public final class BadRequestException extends MappedException {

    //specifically not providing a default constructor, so a message must always be supplied
    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
