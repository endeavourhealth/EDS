package org.endeavourhealth.ui.database.execution;

import org.endeavourhealth.ui.ExecutionStatus;
import org.endeavourhealth.ui.database.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DbJob extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbJob.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID jobUuid = null;
    @DatabaseColumn
    private ExecutionStatus statusId = ExecutionStatus.Executing;
    @DatabaseColumn
    private Instant startDateTime = null;
    @DatabaseColumn
    private Instant endDateTime = null;
    @DatabaseColumn
    private Integer patientsInDatabase = null;
    @DatabaseColumn
    private UUID baselineAuditUuid = null;

    public static List<DbJob> retrieveForJobReports(List<DbJobReport> jobReports) throws Exception {
        List<UUID> uuids = new ArrayList<>();
        for (DbJobReport jobReport: jobReports) {
            uuids.add(jobReport.getJobUuid());
        }
        return retrieveForUuids(uuids);
    }

    public static List<DbJob> retrieveForUuids(List<UUID> uuids) throws Exception {
        return DatabaseManager.db().retrieveJobsForUuids(uuids);
    }


    public static DbJob retrieveForUuid(UUID jobUuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbJob.class, jobUuid);
    }

    public static List<DbJob> retrieveRecent(int count) throws Exception {
        return DatabaseManager.db().retrieveRecentJobs(count);
    }

    public static List<DbJob> retrieveForStatus(ExecutionStatus status) throws Exception {
        return DatabaseManager.db().retrieveJobsForStatus(status);
    }

    public void markAsFinished(ExecutionStatus status) {
        setEndDateTime(Instant.now());
        setStatusId(status);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getJobUuid() {
        return jobUuid;
    }

    public void setJobUuid(UUID jobUuid) {
        this.jobUuid = jobUuid;
    }

    public Instant getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Instant endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Integer getPatientsInDatabase() {
        return patientsInDatabase;
    }

    public void setPatientsInDatabase(Integer patientsInDatabase) {
        this.patientsInDatabase = patientsInDatabase;
    }

    public Instant getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Instant startDateTime) {
        this.startDateTime = startDateTime;
    }

    public ExecutionStatus getStatusId() {
        return statusId;
    }

    public void setStatusId(ExecutionStatus statusId) {
        this.statusId = statusId;
    }

    public UUID getBaselineAuditUuid() {
        return baselineAuditUuid;
    }

    public void setBaselineAuditUuid(UUID baselineAuditUuid) {
        this.baselineAuditUuid = baselineAuditUuid;
    }
}
