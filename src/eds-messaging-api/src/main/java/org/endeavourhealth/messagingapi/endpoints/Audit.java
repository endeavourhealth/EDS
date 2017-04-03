package org.endeavourhealth.messagingapi.endpoints;

import org.endeavourhealth.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.UUID;

@Path("/")
public class Audit {

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/Audit")
	public Response postMessage(@Context SecurityContext sc, @Context HttpHeaders headers, String body) {
		String application = headers.getHeaderString("application");
		String level = headers.getHeaderString("level");
		Logger logger = LoggerFactory.getLogger(application);
		UUID userUuid = SecurityUtils.getCurrentUserId(sc);

		switch (level.toUpperCase()) {
			case "DEBUG":
				logger.debug(body, userUuid);
				break;
			case "ERROR":
				logger.error(body, userUuid);
				break;
			case "INFO":
				logger.info(body, userUuid);
				break;
			case "TRACE":
				logger.trace(body, userUuid);
				break;
			case "WARN":
				logger.warn(body, userUuid);
				break;
			default:
				logger.debug(body, userUuid);
		}

		return Response.ok().build();
	}
}
