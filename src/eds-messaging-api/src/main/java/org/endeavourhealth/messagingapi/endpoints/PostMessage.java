package org.endeavourhealth.messagingapi.endpoints;

import org.apache.http.HttpStatus;
import org.endeavourhealth.core.configuration.ConfigWrapper;
import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeGeneralErrorDalI;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/")
public class PostMessage extends AbstractEndpoint {

    private static final ExchangeGeneralErrorDalI errorDal = DalProvider.factoryExchangeGeneralErrorDal();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostMessage")
    @RolesAllowed({"eds_messaging_post"})
    public Response postMessage(@Context HttpHeaders headers, String body) throws Throwable {
        UUID exchangeId = UUID.randomUUID();

        Pipeline pipeline = ConfigWrapper.getInstance().getPostMessage().getPipeline();

        Response response = null;
        try {
            response = process(headers, body, pipeline, exchangeId);

            if (response.getStatus() != HttpStatus.SC_OK
                    && response.getStatus() != HttpStatus.SC_ACCEPTED) {

                errorDal.save(exchangeId, response.getEntity().toString());
            }
        } catch (Throwable throwable) {
            // save the error message in the DB
            errorDal.save(exchangeId, throwable.getMessage());
            throw throwable;
        }

        return response;
    }

    @POST
    // @Consumes(MediaType.APPLICATION_JSON)
    // @Produces(MediaType.APPLICATION_JSON)
    @Path("/PostMessageAsync")
    @RolesAllowed({"eds_messaging_post"})
    public Response postMessageAsync(@Context HttpHeaders headers, String body) throws Throwable {
        UUID exchangeId = UUID.randomUUID();

        Pipeline pipeline = ConfigWrapper.getInstance().getPostMessageAsync().getPipeline();

        Response response = null;
        try {
            response = process(headers, body, pipeline, exchangeId);

            if (response.getStatus() != HttpStatus.SC_OK
                    && response.getStatus() != HttpStatus.SC_ACCEPTED) {

                errorDal.save(exchangeId, response.getEntity().toString());
            }
        } catch (Throwable throwable) {
            // save the error message in the DB
            errorDal.save(exchangeId, throwable.getMessage());
            throw throwable;
        }

        return response;
    }
}
