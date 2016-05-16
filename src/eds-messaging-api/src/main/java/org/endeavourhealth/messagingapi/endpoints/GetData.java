package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.messagingapi.configuration.ConfigManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class GetData extends AbstractEndpoint {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/GetData")
	public Response GetData(@Context HttpHeaders headers) {
		Pipeline pipeline = ConfigManager.getInstance().getGetData().getPipeline();
		return Process(headers, null, pipeline);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/GetDataAsync")
	public Response GetDataAsync(@Context HttpHeaders headers) {
		Pipeline pipeline = ConfigManager.getInstance().getGetDataAsync().getPipeline();
		return Process(headers, null, pipeline);
	}
}
