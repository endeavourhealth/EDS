package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.UserRepository;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.ui.framework.security.PasswordHash;
import org.endeavourhealth.ui.json.JsonEndUser;
import org.endeavourhealth.ui.json.JsonEndUserList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Endpoint for the functions related to managing person and organisation entities
 */
@Path("/admin")
public final class AdminEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(AdminEndpoint.class);



}
