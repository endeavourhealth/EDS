package org.endeavourhealth.ui.database;

import ch.qos.logback.core.db.dialect.SQLDialectCode;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.ExecutionStatus;
import org.endeavourhealth.ui.database.administration.*;
import org.endeavourhealth.ui.database.definition.DbActiveItem;
import org.endeavourhealth.ui.database.definition.DbAudit;
import org.endeavourhealth.ui.database.definition.DbItem;
import org.endeavourhealth.ui.database.definition.DbItemDependency;
import org.endeavourhealth.ui.database.execution.*;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisation;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisationSet;

import java.util.List;
import java.util.UUID;

public interface DatabaseI {

    //used for setting up logging to db
    public SQLDialectCode getLogbackDbDialectCode();

    //generic read/write functions
    public <T extends DbAbstractTable> void writeEntity(T entity) throws Exception;

    public <T extends DbAbstractTable> void writeEntities(List<T> entities) throws Exception;

    public <T extends DbAbstractTable> T retrieveForPrimaryKeys(Class<T> type, Object... keys) throws Exception;

    //specific functions
    public DbEndUser retrieveEndUserForEmail(String email) throws Exception;

    public List<DbEndUser> retrieveSuperUsers() throws Exception;

    public List<DbEndUser> retrieveEndUsersForUuids(List<UUID> uuids) throws Exception;

    public DbEndUserPwd retrieveEndUserPwdForUserNotExpired(UUID endUserUuid) throws Exception;

    public List<DbEndUserEmailInvite> retrieveEndUserEmailInviteForUserNotCompleted(UUID userUuid) throws Exception;

    public DbEndUserEmailInvite retrieveEndUserEmailInviteForToken(String token) throws Exception;

    public List<DbOrganisationEndUserLink> retrieveOrganisationEndUserLinksForOrganisationNotExpired(UUID organisationUuid) throws Exception;

    public List<DbOrganisationEndUserLink> retrieveOrganisationEndUserLinksForUserNotExpired(UUID endUserUuid) throws Exception;

    public DbOrganisationEndUserLink retrieveOrganisationEndUserLinksForOrganisationEndUserNotExpired(UUID organisationUuid, UUID endUserUuid) throws Exception;

    public DbItem retrieveLatestItemForUuid(UUID itemUuid) throws Exception;

    public List<DbItem> retrieveDependentItems(UUID itemUuid, DependencyType dependencyType) throws Exception;

    public List<DbItem> retrieveNonDependentItems(UUID organisationUuid, DependencyType dependencyType, DefinitionItemType itemType) throws Exception;

    public List<DbItem> retrieveItemsForActiveItems(List<DbActiveItem> activeItems) throws Exception;

    public List<DbItem> retrieveItemsForJob(UUID jobUuid) throws Exception;

    public List<DbItem> retrieveLatestItemsForUuids(List<UUID> itemUuids) throws Exception;

    public DbActiveItem retrieveActiveItemForItemUuid(UUID itemUuid) throws Exception;

    public List<DbActiveItem> retrieveActiveItemDependentItems(UUID organisationUuid, UUID itemUuid, DependencyType dependencyType) throws Exception;

    public List<DbActiveItem> retrieveActiveItemRecentItems(UUID userUuid, UUID organisationUuid, int count) throws Exception;

    public int retrieveCountDependencies(UUID itemUuid, DependencyType dependencyType) throws Exception;

    public List<DbItemDependency> retrieveItemDependenciesForItem(UUID itemUuid, UUID auditUuid) throws Exception;

    public List<DbItemDependency> retrieveItemDependenciesForItemType(UUID itemUuid, UUID auditUuid, DependencyType dependencyType) throws Exception;

    public List<DbItemDependency> retrieveItemDependenciesForDependentItem(UUID dependentItemUuid) throws Exception;

    public List<DbItemDependency> retrieveItemDependenciesForDependentItemType(UUID dependentItemUuid, DependencyType dependencyType) throws Exception;

    public List<DbRequest> retrievePendingRequestsForItems(UUID organisationUuid, List<UUID> itemUuids) throws Exception;

    public List<DbRequest> retrievePendingRequests() throws Exception;

    public List<DbRequest> retrieveRequestsForItem(UUID organisationUuid, UUID itemUuid, int count) throws Exception;

    public List<DbJob> retrieveRecentJobs(int count) throws Exception;

    public List<DbJob> retrieveJobsForStatus(ExecutionStatus status) throws Exception;

    public List<DbJob> retrieveJobsForUuids(List<UUID> uuids) throws Exception;

    public List<DbJob> retrieveJobsForJobReportUuids(List<UUID> uuids) throws Exception;

    public List<DbJobReport> retrieveJobReports(UUID organisationUuid, int count) throws Exception;

    public List<DbJobReport> retrieveJobReportsForJob(UUID jobUuid) throws Exception;

    public List<DbJobReport> retrieveLatestJobReportsForItemUuids(UUID organisationUuid, List<UUID> itemUuids) throws Exception;

    public DbJobReport retrieveJobReportForJobAndReportAndParameters(UUID jobUuid, UUID reportUuid, String parameters) throws Exception;

    public List<DbJobReport> retrieveJobReportsForUuids(List<UUID> uuids) throws Exception;

    public List<DbJobReportItem> retrieveJobReportItemsForJobReport(UUID jobReportUuid) throws Exception;

    public List<DbAudit> retrieveAuditsForUuids(List<UUID> uuids) throws Exception;

    public DbAudit retrieveLatestAudit() throws Exception;

    public List<DbJobContent> retrieveJobContentsForJob(UUID jobUuid) throws Exception;

    public DbJobReportOrganisation retrieveJobReportOrganisationForJobReportAndOdsCode(UUID jobReportUuid, String odsCode) throws Exception;

    public DbJobReportItemOrganisation retrieveJobReportItemOrganisationForJobReportItemAndOdsCode(UUID jobReportItemUUid, String odsCode) throws Exception;

    public List<DbJobProcessorResult> retrieveJobProcessorResultsForJob(UUID jobUuid) throws Exception;

    public void deleteAllJobProcessorResults() throws Exception;

    public List<DbSourceOrganisationSet> retrieveAllOrganisationSets(UUID organisationUuid) throws Exception;

    public List<DbSourceOrganisationSet> retrieveOrganisationSetsForSearchTerm(UUID organisationUuid, String searchTerm) throws Exception;

    public List<DbSourceOrganisation> retrieveAllSourceOrganisations(boolean includeUnreferencedOnes) throws Exception;

    public List<DbSourceOrganisation> retrieveSourceOrganisationsForSearch(String searchTerm) throws Exception;

    public List<DbSourceOrganisation> retrieveSourceOrganisationsForOdsCodes(List<String> odsCodes) throws Exception;

    public DbProcessorStatus retrieveCurrentProcessorStatus() throws Exception;

    public void deleteCurrentProcessorStatus() throws Exception;

}
