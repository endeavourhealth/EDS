package org.endeavourhealth.ui.database.execution;

import org.endeavourhealth.ui.database.*;

import java.util.UUID;

public final class DbJobReportItemOrganisation extends DbAbstractTable {
    private static final TableAdapter adapter = new TableAdapter(DbJobReportItemOrganisation.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID jobReportItemUuid = null;
    @DatabaseColumn
    @PrimaryKeyColumn
    private String organisationOdsCode = null;
    @DatabaseColumn
    private Integer resultCount = null;

    public static DbJobReportItemOrganisation retrieveForJobReportItemAndOdsCode(DbJobReportItem jobReportItem, String odsCode) throws Exception {
        return retrieveForJobReportItemAndOdsCode(jobReportItem.getJobReportItemUuid(), odsCode);
    }
    public static DbJobReportItemOrganisation retrieveForJobReportItemAndOdsCode(UUID jobReportItemUUid, String odsCode) throws Exception {
        return DatabaseManager.db().retrieveJobReportItemOrganisationForJobReportItemAndOdsCode(jobReportItemUUid, odsCode);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getJobReportItemUuid() {
        return jobReportItemUuid;
    }

    public void setJobReportItemUuid(UUID jobReportItemUuid) {
        this.jobReportItemUuid = jobReportItemUuid;
    }

    public String getOrganisationOdsCode() {
        return organisationOdsCode;
    }

    public void setOrganisationOdsCode(String organisationOdsCode) {
        this.organisationOdsCode = organisationOdsCode;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }


}
