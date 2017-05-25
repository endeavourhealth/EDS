package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.mySQLDatabase.MapType;
import org.endeavourhealth.core.mySQLDatabase.models.*;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.Document;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/documentation")
@Metrics(registry = "EdsRegistry")
public final class DocumentEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DocumentEndpoint.Get")
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Documents(s)",
                "Document Id", uuid);

        return getSingleDocument(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DocumentEndpoint.GetAssociatedDocuments")
    @Path("/associated")
    public Response getAssociatedDocuments(@Context SecurityContext sc, @QueryParam("parentUuid") String parentUuid, @QueryParam("parentType") Short parentType) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Documents(s)",
                "Parent Id", parentUuid,
                "Parent Type", parentType);

        return getAssociatedDocuments(parentUuid, parentType);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.CohortEndpoint.Delete")
    @Path("/")
    @RequiresAdmin
    public Response delete(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Cohort",
                "Cohort Id", uuid);

        DocumentationEntity.deleteDocument(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    private Response getSingleDocument(String uuid) throws Exception {
        DocumentationEntity documentationEntity = DocumentationEntity.getDocument(uuid);

        return Response
                .ok()
                .entity(documentationEntity)
                .build();

    }

    private Response getAssociatedDocuments(String parentUuid, Short parentType) throws Exception {

        List<String> documentUuids = MasterMappingEntity.getChildMappings(parentUuid, parentType, MapType.DOCUMENT.getMapType());
        List<DocumentationEntity> ret = new ArrayList<>();

        if (documentUuids.size() > 0)
            ret = DocumentationEntity.getDocumentsFromList(documentUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();

    }



}
