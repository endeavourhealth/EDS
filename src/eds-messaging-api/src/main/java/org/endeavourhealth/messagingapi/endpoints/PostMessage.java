package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.messagingapi.configuration.ConfigManager;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
	@RolesAllowed({"eds_messaging_post"})
	public Response postMessage(@Context HttpHeaders headers, String body) {
		Pipeline pipeline = ConfigManager.getInstance().getPostMessage().getPipeline();
		return Process(headers, body, pipeline);
	}

	@POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	@Path("/PostMessageAsync")
	@RolesAllowed({"eds_messaging_post"})
	public Response postMessageAsync(@Context HttpHeaders headers, String body) {
		Pipeline pipeline = ConfigManager.getInstance().getPostMessageAsync().getPipeline();
		return Process(headers, body, pipeline);
	}
}
