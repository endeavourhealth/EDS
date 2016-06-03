package org.endeavourhealth.ui.database.execution;

import org.endeavourhealth.ui.database.*;

import java.util.UUID;

public final class DbJobReportOrganisation extends DbAbstractTable {
    private static final TableAdapter adapter = new TableAdapter(DbJobReportOrganisation.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID jobReportUuid = null;
    @DatabaseColumn
    @PrimaryKeyColumn
    private String organisationOdsCode = null;
    @DatabaseColumn
    private Integer populationCount = null;

    public static DbJobReportOrganisation retrieveForJobReportAndOdsCode(DbJobReport jobReport, String odsCode) throws Exception {
        return retrieveForJobReportAndOdsCode(jobReport.getJobReportUuid(), odsCode);
    }
    public static DbJobReportOrganisation retrieveForJobReportAndOdsCode(UUID jobReportUuid, String odsCode) throws Exception {
        return DatabaseManager.db().retrieveJobReportOrganisationForJobReportAndOdsCode(jobReportUuid, odsCode);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getJobReportUuid() {
        return jobReportUuid;
    }

    public void setJobReportUuid(UUID jobReportUuid) {
        this.jobReportUuid = jobReportUuid;
    }

    public String getOrganisationOdsCode() {
        return organisationOdsCode;
    }

    public void setOrganisationOdsCode(String organisationOdsCode) {
        this.organisationOdsCode = organisationOdsCode;
    }

    public Integer getPopulationCount() {
        return populationCount;
    }

    public void setPopulationCount(Integer populationCount) {
        this.populationCount = populationCount;
    }
}
