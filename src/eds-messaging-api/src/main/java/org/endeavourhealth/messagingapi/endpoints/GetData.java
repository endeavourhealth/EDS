package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.messaging.exchange.Exchange;
import org.endeavourhealth.messagingapi.pipeline.GetAsyncPipeline;
import org.endeavourhealth.messagingapi.pipeline.GetSyncPipeline;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class GetData {

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/GetData")
	public Response GetData(@Context HttpHeaders headers, String body) {
		Exchange exchange = new Exchange();

		for (String key : headers.getRequestHeaders().keySet())
			exchange.headers.put(key, headers.getHeaderString(key));
		exchange.body = body;

		String async = headers.getRequestHeaders().getFirst("async");
		if (async != null && async.equals("true"))
			new GetAsyncPipeline().process(exchange);
		else
			new GetSyncPipeline().process(exchange);

		return Response
				.status(200)
				.entity(exchange.body)
				.build();
	}
}
