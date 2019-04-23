package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.core.configuration.ConfigWrapper;
import org.endeavourhealth.core.configuration.Pipeline;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class GetData extends AbstractEndpoint {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/GetData")
	@RolesAllowed({"eds_messaging_get"})
	public Response getData(@Context HttpHeaders headers) {
		MetricsHelper.recordEvent("get-data");

		Pipeline pipeline = ConfigWrapper.getInstance().getGetData().getPipeline();
		//super-class process function is for posting data, so commenting this out until properly implemented
		return null;
		//return process(headers, null, pipeline);
	}

}
