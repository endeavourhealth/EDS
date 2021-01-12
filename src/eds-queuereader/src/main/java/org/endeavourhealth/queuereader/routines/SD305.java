package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.ScheduleBuilder;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

public class SD305 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD305.class);

    /**
     * fixes data for both SD-305 where the mapping of raw Emis fields to FHIR Schedule elements was wrong
     */
    public static void fixEmisSessions(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing Emis Sessions at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String bulkOperationName = "Fixing Emis sessions (SD-305)";

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("EMIS")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                if (!testMode) {
                    if (includeStartedButNotFinishedServices) {
                        //check if already done, so we can make sure EVERY service is done
                        if (isServiceDoneBulkOperation(service, bulkOperationName)) {
                            LOG.debug("Skipping " + service + " as already done");
                            continue;
                        }

                    } else {
                        //check if already started, to allow us to run multiple instances of this at once
                        if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                            LOG.debug("Skipping " + service + " as already started or done");
                            continue;
                        }
                    }
                }

                LOG.debug("Doing " + service);
                fixEmisSessionsAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, bulkOperationName);
                }

            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixEmisSessionsAtService(boolean testMode, Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.EMIS_CSV);
        if (endpoint == null) {
            LOG.warn("No emis endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> exchanges = exchangeDal.getExchangesByService(serviceId, systemId, Integer.MAX_VALUE);
        LOG.debug("Found " + exchanges.size() + " exchanges");
        Map<String, SessionCache> hmSessionData = findSessionData(exchanges);
        LOG.debug("Cached " + hmSessionData.size() + " session GUIDs");

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, MessageFormat.EMIS_CSV, "Manually created to Emis slot and session practitioners (SD-283 and SD-284)");
            UUID exchangeId = newExchange.getId();
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            LOG.debug("Fixing sessions");
            fixSessions(serviceId, hmSessionData, filer);

        } catch (Throwable ex) {
            LOG.error("Error doing service " + service, ex);
            throw ex;

        } finally {

            //close down filer
            if (filer != null) {
                LOG.debug("Waiting to finish");
                filer.waitToFinish();

                //set multicast header
                String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                newExchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                //post to Rabbit protocol queue
                List<UUID> exchangeIds = new ArrayList<>();
                exchangeIds.add(newExchange.getId());
                QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, null);

                //set this after posting to rabbit so we can't re-queue it later
                newExchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                newExchange.getHeaders().remove(HeaderKeys.BatchIdsJson);
                AuditWriter.writeExchange(newExchange);
            }

            //we'll have a load of stuff cached in here, so clear it down as it won't be applicable to the next service
            IdHelper.clearCache();
        }
    }

    private static void fixSessions(UUID serviceId, Map<String, SessionCache> hmSessionData, FhirResourceFiler filer) throws Exception {

        int done = 0;

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        for (String sessionGuid: hmSessionData.keySet()) {
            SessionCache cache = hmSessionData.get(sessionGuid);

            UUID scheduleUuid = IdHelper.getEdsResourceId(serviceId, ResourceType.Schedule, sessionGuid);
            if (scheduleUuid == null) {
                //seen this a few times but only for cases where new data had arrived and was in the inbound queue
                LOG.warn("No schedule UUID found for raw ID " + sessionGuid);
                continue;
            }

            Schedule schedule = (Schedule)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Schedule, scheduleUuid.toString());
            if (schedule == null) {
                LOG.warn("Missing or deleted schedule for UUID " + scheduleUuid + ", raw ID " + sessionGuid);
                continue;
            }

            ScheduleBuilder builder = new ScheduleBuilder(schedule);

            //clear the three target fields
            schedule.setComment(null);
            schedule.getType().clear();
            ExtensionConverter.removeExtension(schedule, FhirExtensionUri.SCHEDULE_NAME);

            //set the fields
            String typeDesc = cache.getSessionTypeDescription();
            if (!Strings.isNullOrEmpty(typeDesc)) {
                builder.addComment(typeDesc);
            }

            String category = cache.getSessionCategoryDisplayName();
            if (!Strings.isNullOrEmpty(category)) {
                builder.setTypeFreeText(category);
            }

            String desc = cache.getDescription();
            if (Strings.isNullOrEmpty(desc)) {
                builder.setScheduleName(desc);
            }

            //save
            if (filer != null) {
                filer.saveAdminResource(null, false, builder);
            }

            done ++;
            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " / " + hmSessionData.size() + " schedules");
            }
        }

        LOG.debug("Finished " + done + " schedules");
    }

    private static Map<String, SessionCache> findSessionData(List<Exchange> exchanges) throws Exception {

        Map<String, SessionCache> ret = new HashMap<>();

        //list if most-recent-first, so go backwards
        for (int i=exchanges.size()-1; i>=0; i--) {
            Exchange exchange = exchanges.get(i);
            String sessionUserFilePath = findFilePathInExchange(exchange, "Appointment_Session");
            if (sessionUserFilePath == null) {
                continue;
            }

            InputStreamReader isr = FileHelper.readFileReaderFromSharedStorage(sessionUserFilePath);
            CSVParser parser = new CSVParser(isr, CSVFormat.DEFAULT.withHeader());
            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                String sessionGuid = record.get("AppointmentSessionGuid");
                String description = record.get("Description");
                String sessionTypeDescription = record.get("SessionTypeDescription");
                String sessionCategoryDisplayName = record.get("SessionCategoryDisplayName");
                String deleted = record.get("Deleted");
                boolean isDeleted = Boolean.parseBoolean(deleted);
                if (isDeleted) {
                    continue;
                }

                SessionCache c = new SessionCache();
                c.setDescription(description);
                c.setSessionTypeDescription(sessionTypeDescription);
                c.setSessionCategoryDisplayName(sessionCategoryDisplayName);

                ret.put(sessionGuid, c);
            }

            parser.close();

        }

        return ret;
    }

    static class SessionCache {
        private String description;
        private String sessionTypeDescription;
        private String sessionCategoryDisplayName;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSessionTypeDescription() {
            return sessionTypeDescription;
        }

        public void setSessionTypeDescription(String sessionTypeDescription) {
            this.sessionTypeDescription = sessionTypeDescription;
        }

        public String getSessionCategoryDisplayName() {
            return sessionCategoryDisplayName;
        }

        public void setSessionCategoryDisplayName(String sessionCategoryDisplayName) {
            this.sessionCategoryDisplayName = sessionCategoryDisplayName;
        }
    }
}
