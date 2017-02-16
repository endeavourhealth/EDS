package org.endeavourhealth.patientexplorer.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.patientexplorer.database.CountReportProvider;
import org.endeavourhealth.patientexplorer.database.models.ConceptEntity;
import org.endeavourhealth.patientexplorer.models.JsonConcept;
import org.endeavourhealth.patientexplorer.models.JsonPractitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/practitioner")
public final class PractitionerEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(PractitionerEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsPatientExplorerModule.CountReport);
    private static final CountReportProvider countReportProvider = new CountReportProvider();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    public Response searchPractitioner(
        @Context SecurityContext sc,
        @QueryParam("searchData") String searchData,
        @QueryParam("organisationUuid") UUID organisationUuid) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), organisationUuid, AuditAction.Load, "Practitioner Search");
        LOG.debug("searchPractitioner");

        List<JsonPractitioner> ret = countReportProvider.searchPractitioner(searchData, organisationUuid);

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }
}