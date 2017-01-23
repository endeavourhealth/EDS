package org.endeavourhealth.patientexplorer.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.patientexplorer.database.CountReportProvider;
import org.endeavourhealth.patientexplorer.database.models.ConceptEntity;
import org.endeavourhealth.patientexplorer.models.JsonConcept;
import org.endeavourhealth.patientexplorer.models.JsonPractitioner;
import org.endeavourhealth.transform.enterprise.outputModels.Practitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.stream.Collectors;

@Path("/countReport")
public final class CountReportEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(CountReportEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsPatientExplorerModule.CountReport);
    private static final CountReportProvider countReportProvider = new CountReportProvider();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/runReport")
    public Response runReport(@Context SecurityContext sc, @QueryParam("reportUuid") UUID reportId, @QueryParam("organisationUuid") UUID organisationUuid, String reportParamsJson) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), organisationUuid, AuditAction.Run, "Report",
            "uuid", reportId,
            "params", reportParamsJson);
        LOG.debug("runReport");

        Map<String, String> reportParams = ObjectMapperPool.getInstance().readValue(reportParamsJson, new TypeReference<Map<String,String>>(){});
        LibraryItem ret = countReportProvider.runReport(reportId, organisationUuid, reportParams);

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/exportNHS")
    public Response exportNHSNumbers(@Context SecurityContext sc, @QueryParam("uuid") UUID reportId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Run, "Export NHS Numbers",
            "uuid", reportId);
        LOG.debug("exportNHS");

        List<String> data = countReportProvider.getNHSExport(reportId);
        String ret = StringUtils.join(data, "\n");

        return Response
            .ok(ret, MediaType.TEXT_PLAIN_TYPE)
            .build();
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/exportData")
    public Response exportData(@Context SecurityContext sc, @QueryParam("uuid") UUID reportId) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Run, "Export Data",
            "uuid", reportId);
        LOG.debug("exportData");

        List<String> data = countReportProvider.getDataExport(reportId);
        String ret = StringUtils.join(data, "\n");

        return Response
            .ok(ret, MediaType.TEXT_PLAIN_TYPE)
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/encounterType")
    public Response getEncounterTypes(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Encounter Types");
        LOG.debug("getEncounterTypes");

        List<ConceptEntity> data = countReportProvider.getEncounterTypes();
        List<JsonConcept> ret = data.stream().map(JsonConcept::new).collect(Collectors.toList());

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
    }
}