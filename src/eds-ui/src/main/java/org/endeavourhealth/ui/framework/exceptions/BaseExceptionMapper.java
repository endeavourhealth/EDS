package org.endeavourhealth.ui.framework.exceptions;

import org.endeavourhealth.ui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.JsonServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public final class BaseExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = LoggerFactory.getLogger(BaseExceptionMapper.class);

    public BaseExceptionMapper() {
        LOG.debug("Exception mapper created");
    }

    @Override
    public Response toResponse(Exception exception) {
        Response.ResponseBuilder r = null;

        //if the exception is one of our own exception objects
        if (exception instanceof MappedException) {
            MappedException me = (MappedException) exception;
            r = Response.status(me.getResponseStatus());
        }
        //if the exception is a web service application
        else if (exception instanceof WebApplicationException) {
            WebApplicationException we = (WebApplicationException) exception;
            r = Response.status(we.getResponse().getStatus());
        }
        //if it's some other kind of exception (e.g. SQLException) that's got this far
        else {
            //log on the server too, since these are unexpected
            LOG.error(exception.getMessage(), exception);

            r = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }

        //if our exception has a message, then send this in the response too
        //so even if there's an error, we can give some feedback as to what it is
        String message = exception.getMessage();
        if (message != null) {
            JsonServerException wrapper = new JsonServerException(message);
            r = r.entity(wrapper);
        }

        //because we've thrown an exception, we won't have completed the endPoint functions properly
        //so we need to remove the MDC markers here, so our thread is no longer associated with the user
        AbstractEndpoint.clearLogbackMarkers();

        return r.build();
    }


}
