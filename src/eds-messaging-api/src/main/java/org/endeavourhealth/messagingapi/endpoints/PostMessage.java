package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.messagingapi.configuration.ConfigManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class PostMessage extends AbstractEndpoint {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/PostMessage")
	public Response postMessage(@Context HttpHeaders headers, String body) {
		Pipeline pipeline = ConfigManager.getInstance().getPostMessage().getPipeline();
		return Process(headers, body, pipeline);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/PostMessageAsync")
	public Response postMessageAsync(@Context HttpHeaders headers, String body) {
		Pipeline pipeline = ConfigManager.getInstance().getPostMessageAsync().getPipeline();
		return Process(headers, body, pipeline);
	}
}
