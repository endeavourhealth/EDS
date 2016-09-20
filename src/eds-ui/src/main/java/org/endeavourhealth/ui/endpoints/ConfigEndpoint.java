package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/config")
public final class ConfigEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Config);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getConfig")
    public Response getConfig(@Context SecurityContext sc, @QueryParam("configurationId") String configurationId) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Configuration Id", configurationId);

        LOG.trace("getConfig for configurationId {}", configurationId);

				ConfigurationResource configurationResource = new ConfigurationRepository().getByKey(UUID.fromString(configurationId));

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(configurationResource)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/saveConfig")
    @RequiresAdmin
    public Response saveConfig(@Context SecurityContext sc, ConfigurationResource configurationResource) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
            "Configuration", configurationResource);

        LOG.trace("saveConfig {} Name {}", configurationResource.getConfigurationId(), configurationResource.getConfigurationData());

        new ConfigurationRepository().update(configurationResource);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(configurationResource)
                .build();
    }
}
