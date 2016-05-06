package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.messaging.exchange.Exchange;
import org.endeavourhealth.messagingapi.pipeline.PostAsyncPipeline;
import org.endeavourhealth.messagingapi.pipeline.PostSyncPipeline;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class PostMessage {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/PostMessage")
	public Response postMessage(@Context HttpHeaders headers, String body) {
		Exchange exchange = new Exchange();

		for (String key : headers.getRequestHeaders().keySet())
			exchange.headers.put(key, headers.getHeaderString(key));
		exchange.body = body;

		new PostSyncPipeline().process(exchange);

		return Response
				.status(200)
				.entity(exchange.body)
				.build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/PostMessageAsync")
	public Response postMessageAsync(@Context HttpHeaders headers, String body) {
		Exchange exchange = new Exchange();

		for (String key : headers.getRequestHeaders().keySet())
			exchange.headers.put(key, headers.getHeaderString(key));
		exchange.body = body;


		new PostAsyncPipeline().process(exchange);

		return Response
				.status(200)
				.entity(exchange.body)
				.build();
	}}
