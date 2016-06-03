package org.endeavourhealth.ui.database.execution;

import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.database.*;
import org.endeavourhealth.ui.database.definition.DbActiveItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DbRequest extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbRequest.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID requestUuid = null;
    @DatabaseColumn
    private UUID reportUuid = null;
    @DatabaseColumn
    private UUID organisationUuid = null;
    @DatabaseColumn
    private UUID endUserUuid = null;
    @DatabaseColumn
    private Instant timeStamp = null;
    @DatabaseColumn
    private String parameters = null;
    @DatabaseColumn
    private UUID jobReportUuid = null;

    public static DbRequest retrieveForUuid(UUID requestUuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbRequest.class, requestUuid);
    }
    public static List<DbRequest> retrievePendingForActiveItems(UUID organisationUuid, List<DbActiveItem> activeItems) throws Exception {

        //filter activeItems to find UUIDs of just reports
        List<UUID> itemUuids = new ArrayList<>();
        for (DbActiveItem activeItem: activeItems) {
            if (activeItem.getItemTypeId() == DefinitionItemType.Report) {
                itemUuids.add(activeItem.getItemUuid());
            }
        }
        return retrievePendingForItemUuids(organisationUuid, itemUuids);
    }
    public static List<DbRequest> retrievePendingForItemUuids(UUID organisationUuid, List<UUID> itemUuids) throws Exception {
        return DatabaseManager.db().retrievePendingRequestsForItems(organisationUuid, itemUuids);
    }
    public static List<DbRequest> retrieveAllPending() throws Exception {
        return DatabaseManager.db().retrievePendingRequests();
    }
    public static List<DbRequest> retrieveForItem(UUID organisationUuid, UUID itemUuid, int count) throws Exception {
        return DatabaseManager.db().retrieveRequestsForItem(organisationUuid, itemUuid, count);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getRequestUuid() {
        return requestUuid;
    }

    public void setRequestUuid(UUID requestUuid) {
        this.requestUuid = requestUuid;
    }

    public UUID getEndUserUuid() {
        return endUserUuid;
    }

    public void setEndUserUuid(UUID endUserUuid) {
        this.endUserUuid = endUserUuid;
    }

    public UUID getOrganisationUuid() {
        return organisationUuid;
    }

    public void setOrganisationUuid(UUID organisationUuid) {
        this.organisationUuid = organisationUuid;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public UUID getReportUuid() {
        return reportUuid;
    }

    public void setReportUuid(UUID reportUuid) {
        this.reportUuid = reportUuid;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public UUID getJobReportUuid() {
        return jobReportUuid;
    }

    public void setJobReportUuid(UUID jobReportUuid) {
        this.jobReportUuid = jobReportUuid;
    }
}
