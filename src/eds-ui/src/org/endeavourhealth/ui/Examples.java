package org.endeavourhealth.ui;

import org.endeavourhealth.ui.database.*;
import org.endeavourhealth.ui.database.definition.DbActiveItem;
import org.endeavourhealth.ui.database.definition.DbAudit;
import org.endeavourhealth.ui.database.definition.DbItem;
import org.endeavourhealth.ui.database.definition.DbItemDependency;
import org.endeavourhealth.ui.database.execution.*;
import org.endeavourhealth.ui.querydocument.QueryDocumentSerializer;
import org.endeavourhealth.ui.querydocument.models.*;
import org.endeavourhealth.ui.requestParameters.RequestParametersSerializer;
import org.endeavourhealth.ui.requestParameters.models.RequestParameters;
import org.endeavourhealth.ui.terminology.TerminologyService;

import java.time.Instant;
import java.util.*;

public abstract class Examples {


    public static void findingPendingRequestsAndCreateJob() throws Exception {

        //retrieve pending requests, and if none, return out
        List<DbRequest> pendingRequests = DbRequest.retrieveAllPending();
        if (pendingRequests.isEmpty()) {
            return;
        }

        List<DbAbstractTable> toSave = new ArrayList<>();

        int patientsInDb = 0; //get count of patients
        DbAudit latestAudit = DbAudit.retrieveLatest();
        UUID latestAuditUuid = latestAudit.getAuditUuid();

        //create the job
        DbJob job = new DbJob();
        job.assignPrimaryUUid();
        job.setStartDateTime(Instant.now());
        job.setPatientsInDatabase(patientsInDb);
        job.setBaselineAuditUuid(latestAuditUuid);
        toSave.add(job);

        UUID jobUuid = job.getJobUuid();

        //retrieve pending requests
        for (DbRequest request: pendingRequests) {

            UUID reportUuid = request.getReportUuid();

            //find the current audit for the report
            DbActiveItem activeItem = DbActiveItem.retrieveForItemUuid(reportUuid);
            UUID auditUuid = activeItem.getAuditUuid();

            UUID orgUuid = request.getOrganisationUuid();

            DbJobReport jobReport = new DbJobReport();
            jobReport.assignPrimaryUUid();
            UUID jobReportUuid = jobReport.getJobReportUuid();
            jobReport.setJobUuid(jobUuid);
            jobReport.setReportUuid(reportUuid);
            jobReport.setAuditUuid(auditUuid);
            jobReport.setOrganisationUuid(orgUuid);
            jobReport.setEndUserUuid(request.getEndUserUuid());
            jobReport.setParameters(request.getParameters());
            toSave.add(jobReport);

            //update the request to link back to the job
            request.setJobReportUuid(jobReportUuid);
            toSave.add(request);

            //then create the JobReportItem objects for each query and listOutput in the report being requested
            DbItem dbItem = DbItem.retrieveForUuidAndAudit(reportUuid, auditUuid);
            String itemXml = dbItem.getXmlContent();
            Report report = QueryDocumentSerializer.readReportFromXml(itemXml);
            List<ReportItem> reportItems = report.getReportItem();
            createReportItems(jobUuid, jobReportUuid, null, reportItems, toSave);
        }

        //commit all changes to the DB in one atomic batch
        DatabaseManager.db().writeEntities(toSave);
    }
    private static void createReportItems(UUID jobUuid, UUID jobReportUuid, UUID parentJobReportItemUuid, List<ReportItem> reportItems, List<DbAbstractTable> toSave) throws Exception {

        for (ReportItem reportItem: reportItems) {

            //report item may have a queryUuid OR listOutputUuid
            String queryUuidStr = reportItem.getQueryLibraryItemUuid();
            String listOutputUuidStr = reportItem.getListReportLibraryItemUuid();

            String uuidStr = queryUuidStr;
            if (uuidStr == null) {
                uuidStr = listOutputUuidStr;
            }
            UUID itemUuid = UUID.fromString(uuidStr);

            DbActiveItem activeItem = DbActiveItem.retrieveForItemUuid(itemUuid);
            UUID auditUuid = activeItem.getAuditUuid();

            DbJobReportItem jobReportItem = new DbJobReportItem();
            jobReportItem.assignPrimaryUUid();
            jobReportItem.setJobReportUuid(jobReportUuid);
            jobReportItem.setItemUuid(itemUuid);
            jobReportItem.setAuditUuid(auditUuid);
            jobReportItem.setParentJobReportItemUuid(parentJobReportItemUuid);
            toSave.add(jobReportItem);

            UUID jobReportItemUuid = jobReportItem.getJobReportItemUuid();

            //create the jobContent objects for ALL dependent items
            createReportContents(jobUuid, itemUuid, toSave);

            //then recurse for any child reportItems
            List<ReportItem> childReportItems = reportItem.getReportItem();
            createReportItems(jobUuid, jobReportUuid, jobReportItemUuid, childReportItems, toSave);
        }
    }
    private static void createReportContents(UUID jobUuid, UUID itemUuid, List<DbAbstractTable> toSave) throws Exception {

        DbActiveItem activeItem = DbActiveItem.retrieveForItemUuid(itemUuid);
        UUID auditUuid = activeItem.getAuditUuid();

        //if the same query is in a report more than once, we don't want to create duplicate jobContents for it
        for (DbAbstractTable entity: toSave) {
            if (entity instanceof DbJobContent) {
                DbJobContent existingJobContent = (DbJobContent)entity;
                if (existingJobContent.getItemUuid().equals(itemUuid)) {
                    return;
                }
            }
        }

        DbJobContent jobContent = new DbJobContent();
        jobContent.setJobUuid(jobUuid);
        jobContent.setItemUuid(itemUuid);
        jobContent.setAuditUuid(auditUuid);
        jobContent.setSaveMode(TableSaveMode.INSERT); //because the primary keys have been explicitly set, we need to force insert mode
        toSave.add(jobContent);

        //then recurse to find the dependent items on this item
        List<DbItemDependency> itemDependencies = DbItemDependency.retrieveForActiveItemType(activeItem, DependencyType.Uses);
        for (DbItemDependency itemDependency: itemDependencies) {

            UUID childItemUuid = itemDependency.getDependentItemUuid();
            createReportContents(jobUuid, childItemUuid, toSave);
        }
    }


    public static void findNonCompletedJobsAndContents() throws Exception {

        //retrieve Jobs where status is Executing (should only be ONE in reality, if jobs are always completed before another created)
        List<DbJob> jobs = DbJob.retrieveForStatus(ExecutionStatus.Executing);
        for (DbJob job: jobs) {

            //retrieve JobReports for job
            UUID jobUuid = job.getJobUuid();
            List<DbJobReport> jobReports = DbJobReport.retrieveForJob(jobUuid);
            List<DbJobContent> jobContents = DbJobContent.retrieveForJob(jobUuid);

            for (DbJobReport jobReport: jobReports) {

                UUID jobReportUuid = jobReport.getJobReportUuid();
                List<DbJobReportItem> jobReportItems = DbJobReportItem.retrieveForJobReport(jobReportUuid);

                //also get the parameters objects and query document
                RequestParameters requestParameters = getRequestParametersFromJobReport(jobReport);
                QueryDocument queryDocument = getQueryDocumentComponentsFromJobReport(jobReport);
            }
        }
    }

    public static void markingJobReportAsFinished(DbJobReport jobReport, ExecutionStatus status) throws Exception {
        jobReport.setStatusId(status);
        jobReport.writeToDb();
    }

    public static void markingJobAsFailed(DbJob job) throws Exception {
        job.setStatusId(ExecutionStatus.Failed);
        job.setEndDateTime(Instant.now());
        job.writeToDb();
    }

    public static void markingJobAsSucceeded(DbJob job) throws Exception {

        List<DbAbstractTable> toSave = new ArrayList<>();

        job.setEndDateTime(Instant.now());
        job.setStatusId(ExecutionStatus.Succeeded);
        toSave.add(job);

        HashMap<Object, Object> hmProcessorResults = null; //placeholder for results from each processor node
        List<String> organisationOdsCodes = null; //placeholder for organisations found in the results

        //then combine the results from each processor node
        List<DbJobProcessorResult> processorResults = DbJobProcessorResult.retrieveForJob(job);
        for (DbJobProcessorResult processorResult: processorResults) {

            String xml = processorResult.getResultXml();
            //...process XML from this processor node

            //mark the result as to be deleted, since we no longer need it
            processorResult.setSaveMode(TableSaveMode.DELETE);
            toSave.add(processorResult);
        }

        List<DbJobReport> jobReports = DbJobReport.retrieveForJob(job);
        for (DbJobReport jobReport: jobReports) {

            Integer populationCount = null; //...get from processor node results

            jobReport.setPopulationCount(populationCount);
            jobReport.setStatusId(ExecutionStatus.Succeeded);
            toSave.add(jobReport);

            //create the organisation breakdown of population counts
            for (String organisationOdsCode: organisationOdsCodes) {

                Integer organisationPopulationCount = null; //...get from processor node results

                DbJobReportOrganisation jobReportOrganisation = new DbJobReportOrganisation();
                jobReportOrganisation.setJobReportUuid(jobReport.getJobReportUuid());
                jobReportOrganisation.setOrganisationOdsCode(organisationOdsCode);
                jobReportOrganisation.setPopulationCount(organisationPopulationCount);
                jobReportOrganisation.setSaveMode(TableSaveMode.INSERT);
                toSave.add(jobReportOrganisation);
            }

            //update the job report items with the results of each query in the report
            List<DbJobReportItem> jobReportItems = DbJobReportItem.retrieveForJobReport(jobReport);
            for (DbJobReportItem jobReportItem: jobReportItems) {

                Integer resultCount = null; //...get from processor node results
                jobReportItem.setResultCount(resultCount);
                toSave.add(jobReportItem);

                //create the organisation breakdown for the query results
                for (String organisationOdsCode: organisationOdsCodes) {

                    Integer organisationResultCount = null; //...get from processor node results

                    DbJobReportItemOrganisation jobReportItemOrganisation = new DbJobReportItemOrganisation();
                    jobReportItemOrganisation.setJobReportItemUuid(jobReportItem.getJobReportItemUuid());
                    jobReportItemOrganisation.setOrganisationOdsCode(organisationOdsCode);
                    jobReportItemOrganisation.setResultCount(organisationResultCount);
                    jobReportItemOrganisation.setSaveMode(TableSaveMode.INSERT);
                    toSave.add(jobReportItemOrganisation);
                }
            }
        }

        //update our job, delete the processor results and insert the result entities in one transaction
        DatabaseManager.db().writeEntities(toSave);
    }

    public static RequestParameters getRequestParametersFromJobReport(DbJobReport jobReport) throws Exception {

        RequestParameters requestParameters = RequestParametersSerializer.readFromJobReport(jobReport);
        return requestParameters;
    }

    public static QueryDocument getQueryDocumentComponentsFromJobReport(DbJobReport jobReport) throws Exception {

        UUID reportUuid = jobReport.getReportUuid();
        UUID auditUuid = jobReport.getAuditUuid();

        DbItem item = DbItem.retrieveForUuidAndAudit(reportUuid, auditUuid);
        Report report = QueryDocumentSerializer.readReportFromItem(item);

        QueryDocument queryDocument = new QueryDocument();
        queryDocument.getReport().add(report);

        //get dependent items, by recursing down the dependency table
        recursivelyGetDependentLibraryItems(reportUuid, queryDocument);

        return queryDocument;
    }
    private static void recursivelyGetDependentLibraryItems(UUID itemUuid, QueryDocument queryDocument) throws Exception {

        List<DbItem> dependentItems = DbItem.retrieveDependentItems(itemUuid, DependencyType.Uses);
        for (DbItem dependentItem: dependentItems) {

            LibraryItem libraryItem = QueryDocumentSerializer.readLibraryItemFromItem(dependentItem);
            queryDocument.getLibraryItem().add(libraryItem);

            UUID libraryItemUuid = dependentItem.getItemUuid();
            recursivelyGetDependentLibraryItems(libraryItemUuid, queryDocument);
        }
    }

    public static HashSet<String> getConceptCodesForCodeSet(CodeSet codeSet) throws Exception {
        HashSet<String> codes = TerminologyService.enumerateConcepts(codeSet);
        return codes;
    }

    public static void retrieveLibraryItemsForJob(DbJob job) throws Exception {
        UUID jobUuid = job.getJobUuid();
        List<LibraryItem> hm = DbItem.retrieveLibraryItemsForJob(jobUuid);
    }
}
