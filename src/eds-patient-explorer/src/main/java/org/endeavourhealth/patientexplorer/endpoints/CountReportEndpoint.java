package org.endeavourhealth.patientexplorer.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.patientexplorer.database.CountReportProvider;
import org.endeavourhealth.patientexplorer.database.SqlUtils;
import org.endeavourhealth.patientexplorer.database.models.ConceptEntity;
import org.endeavourhealth.patientexplorer.models.JsonConcept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API for count reports.  Provides all methods on the path "/countReport"
 */
@Path("/countReport")
public final class CountReportEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(CountReportEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsPatientExplorerModule.CountReport);
    private static final CountReportProvider countReportProvider = new CountReportProvider();

    /**
     * Run a predefined count report
     * @param sc                Security context (provided)
     * @param reportUuid          UUID of the report to run
     * @param organisationUuid  UUID of the service/organisation to restrict the report to
     * @param reportParamsJson  Appropriate report filtering parameters
     * @return  LibraryItem definition of the report that ran
     * @throws Exception
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/runReport")
    public Response runReport(@Context SecurityContext sc, @QueryParam("reportUuid") UUID reportUuid, String reportParamsJson) throws Exception {
        try {
            UUID userUuid = SecurityUtils.getCurrentUserId(sc);
            userAudit.save(userUuid, getOrganisationUuidFromToken(sc), AuditAction.Run, "Report",
                "uuid", reportUuid,
                "params", reportParamsJson);
            LOG.debug("runReport");

            Map<String, String> reportParams = ObjectMapperPool.getInstance().readValue(reportParamsJson, new TypeReference<Map<String, String>>() {
            });
            LibraryItem ret = countReportProvider.runReport(userUuid, reportUuid, reportParams);

            return Response
                .ok(ret, MediaType.APPLICATION_JSON_TYPE)
                .build();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Export a list of NHS Numbers resulting from the most recent run of a report
     * @param sc        Security context (provided)
     * @param uuid  UUID of the report to export
     * @return  Line-break separated list of NHS Numbers
     * @throws Exception
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/exportNHS")
    public Response exportNHSNumbers(@Context SecurityContext sc, @QueryParam("uuid") UUID uuid) throws Exception {
        try {
            UUID userUuid = SecurityUtils.getCurrentUserId(sc);
            userAudit.save(userUuid, getOrganisationUuidFromToken(sc), AuditAction.Run, "Export NHS Numbers",
                "uuid", uuid);
            LOG.debug("exportNHS");

            List<List<String>> data = countReportProvider.getNHSExport(userUuid, uuid);

            String ret = SqlUtils.getCSVAsString(data);

            return Response
                .ok(ret, MediaType.TEXT_PLAIN_TYPE)
                .build();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }

    }

    /**
     * Export the data resulting from the most recent run of a report
     * @param sc        Security context (provided)
     * @param uuid  UUID of the report to export
     * @return  CSV export of the report data
     * @throws Exception
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/exportData")
    public Response exportData(@Context SecurityContext sc, @QueryParam("uuid") UUID uuid) throws Exception {
        try {
            UUID userUuid = SecurityUtils.getCurrentUserId(sc);
            userAudit.save(userUuid, getOrganisationUuidFromToken(sc), AuditAction.Run, "Export Data",
                "uuid", uuid);
            LOG.debug("exportData");

            List<List<String>> data = countReportProvider.getDataExport(userUuid, uuid);
            String ret = SqlUtils.getCSVAsString(data);

            return Response
                .ok(ret, MediaType.TEXT_PLAIN_TYPE)
                .build();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }

    }

    /**
     * Retrieve a distinct list of encounter types (based on code AND term)
     * @param sc Security context (provided)
     * @return  A JSON array of Concepts
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/encounterType")
    public Response getEncounterTypes(@Context SecurityContext sc) throws Exception {
        try {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Encounter Types");
        LOG.debug("getEncounterTypes");

        List<ConceptEntity> data = countReportProvider.getEncounterTypes();
        List<JsonConcept> ret = data.stream().map(JsonConcept::new).collect(Collectors.toList());

        return Response
            .ok(ret, MediaType.APPLICATION_JSON_TYPE)
            .build();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve a distinct list of referral types
     * @param sc Security context (provided)
     * @return  A JSON array of Concepts
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/referralTypes")
    public Response getReferralTypes(@Context SecurityContext sc) throws Exception {
        try {
            userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Referral Types");
            LOG.debug("getReferralTypes");

            List<ConceptEntity> data = countReportProvider.getReferralTypes();
            List<JsonConcept> ret = data.stream().map(JsonConcept::new).collect(Collectors.toList());

            return Response
                .ok(ret, MediaType.APPLICATION_JSON_TYPE)
                .build();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve a distinct list of referral priorities
     * @param sc Security context (provided)
     * @return  A JSON array of Concepts
     * @throws Exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/referralPriorities")
    public Response getReferralPriorities(@Context SecurityContext sc) throws Exception {
        try {
            userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Referral Priorities");
            LOG.debug("getReferralPriorities");

            List<ConceptEntity> data = countReportProvider.getReferralPriorities();
            List<JsonConcept> ret = data.stream().map(JsonConcept::new).collect(Collectors.toList());

            return Response
                .ok(ret, MediaType.APPLICATION_JSON_TYPE)
                .build();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }
}