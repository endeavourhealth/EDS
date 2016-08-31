package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.models.StatsEvent;
import org.endeavourhealth.core.data.admin.models.StatsPatient;
import org.endeavourhealth.core.security.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/stats")
public final class StatsEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(StatsEndpoint.class);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getStatsPatients")
    public Response getStatsPatients(@Context SecurityContext sc, @QueryParam("serviceId") String serviceId) throws Exception {

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        UUID orgUuid = getOrganisationUuidFromToken(sc);

        LOG.trace("getStatsPatient {}", serviceId);

        List<StatsPatient> patient = new ArrayList<>();

        StatsPatient stats = new StatsPatient();
        stats.setOrganisation("Chrisp Street Health Centre");
        stats.setRegular("10000");
        stats.setLeft("2000");
        stats.setDead("500");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("New Road Practice");
        stats.setRegular("12000");
        stats.setLeft("3000");
        stats.setDead("700");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("Tredegar Practice");
        stats.setRegular("7000");
        stats.setLeft("1000");
        stats.setDead("200");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("Thompson Practice");
        stats.setRegular("9000");
        stats.setLeft("3000");
        stats.setDead("700");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("Outwood Health Centre");
        stats.setRegular("19000");
        stats.setLeft("1000");
        stats.setDead("200");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("Bretton Practice");
        stats.setRegular("11000");
        stats.setLeft("2000");
        stats.setDead("700");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("Toulson Practice");
        stats.setRegular("4000");
        stats.setLeft("1000");
        stats.setDead("600");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("Lewis Street Practice");
        stats.setRegular("15000");
        stats.setLeft("5000");
        stats.setDead("600");
        patient.add(stats);

        stats = new StatsPatient();
        stats.setOrganisation("Trafford Practice");
        stats.setRegular("3000");
        stats.setLeft("200");
        stats.setDead("200");
        patient.add(stats);

        return Response
                .ok()
                .entity(patient)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getStatsEvents")
    public Response getStatsEvents(@Context SecurityContext sc, @QueryParam("serviceId") String serviceId) throws Exception {

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        UUID orgUuid = getOrganisationUuidFromToken(sc);

        LOG.trace("getStatsEvents {}", serviceId);

        List<StatsEvent> event = new ArrayList<>();

        StatsEvent stats = new StatsEvent();
        stats.setOrganisation("Chrisp Street Health Centre");
        stats.setObservation("50000");
        stats.setMedication("200000");
        stats.setCondition("5000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("New Road Practice");
        stats.setObservation("120000");
        stats.setMedication("30000");
        stats.setCondition("7000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("Tredegar Practice");
        stats.setObservation("70000");
        stats.setMedication("10000");
        stats.setCondition("2000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("Thompson Practice");
        stats.setObservation("90000");
        stats.setMedication("30000");
        stats.setCondition("7000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("Outwood Health Centre");
        stats.setObservation("190000");
        stats.setMedication("10000");
        stats.setCondition("2000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("Bretton Practice");
        stats.setObservation("110000");
        stats.setMedication("20000");
        stats.setCondition("7000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("Toulson Practice");
        stats.setObservation("40000");
        stats.setMedication("10000");
        stats.setCondition("6000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("Lewis Street Practice");
        stats.setObservation("150000");
        stats.setMedication("50000");
        stats.setCondition("6000");
        event.add(stats);

        stats = new StatsEvent();
        stats.setOrganisation("Trafford Practice");
        stats.setObservation("30000");
        stats.setMedication("2000");
        stats.setCondition("2000");
        event.add(stats);

        return Response
                .ok()
                .entity(event)
                .build();
    }

}
