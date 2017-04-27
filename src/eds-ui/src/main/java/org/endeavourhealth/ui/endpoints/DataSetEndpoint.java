package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.mySQLDatabase.MapType;
import org.endeavourhealth.core.mySQLDatabase.models.DataProcessingAgreementEntity;
import org.endeavourhealth.core.mySQLDatabase.models.DatasetEntity;
import org.endeavourhealth.core.mySQLDatabase.models.MasterMappingEntity;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonDataSet;
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

@Path("/dataSet")
public final class DataSetEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data Set(s)",
                "Data Set Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDataSet - list");

            return getDataSetList();
        } else if (uuid != null){
            LOG.trace("getDataSet - single - " + uuid);
            return getSingleDataSet(uuid);
        } else {
            LOG.trace("Search DataSet - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonDataSet dataset) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data Set",
                "Data Set", dataset);

        if (dataset.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(dataset.getUuid());
            DatasetEntity.updateDataSet(dataset);
        } else {
            dataset.setUuid(UUID.randomUUID().toString());
            DatasetEntity.saveDataSet(dataset);
        }

        MasterMappingEntity.saveDataSetMappings(dataset);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response delete(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Data Set",
                "Data Set Id", uuid);

        DatasetEntity.deleteDataSet(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dpas")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "Data set Id", uuid);

        return getLinkedDpas(uuid);
    }

    private Response getDataSetList() throws Exception {

        List<DatasetEntity> datasets = DatasetEntity.getAllDataSets();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(datasets)
                .build();
    }

    private Response getSingleDataSet(String uuid) throws Exception {
        DatasetEntity dataSetEntity = DatasetEntity.getDataSet(uuid);

        return Response
                .ok()
                .entity(dataSetEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DatasetEntity> datasets = DatasetEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(datasets)
                .build();
    }

    private Response getLinkedDpas(String dataSetUuid) throws Exception {

        List<String> dpaUuids = MasterMappingEntity.getParentMappings(dataSetUuid, MapType.DATASET.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());

        List<DataProcessingAgreementEntity> ret = new ArrayList<>();

        if (dpaUuids.size() > 0)
            ret = DataProcessingAgreementEntity.getDPAsFromList(dpaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
