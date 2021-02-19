package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberCohortDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberCohortRecord;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.transform.subscriber.BulkHelper;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;

public class ReferenceRangeSignificance {
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceRangeSignificance.class);

    static class ObservationAndConditionCallable implements Callable {
        private UUID serviceUUID;
        private UUID patientId;
        private String debug;
        private ResourceDalI dal;
        private String subscriberConfigName;
        private UUID batchUUID;
        public ObservationAndConditionCallable(UUID serviceUUID, UUID patientId, String debug, ResourceDalI dal, String subscriberConfigName, UUID batchUUID) {
            this.serviceUUID = serviceUUID;
            this.patientId = patientId;
            this.debug = debug;
            this.dal = dal;
            this.subscriberConfigName = subscriberConfigName;
            this.batchUUID = batchUUID;
            LOG.info(patientId.toString() + " passed to thread");
        }
        @Override
        public Object call() throws Exception {

            try {

                LOG.info(patientId.toString() + " processing");

                List<ResourceWrapper> observationResources = new ArrayList<>();
                List<ResourceWrapper> conditionResources = new ArrayList<>();
                observationResources = dal.getResourcesByPatient(serviceUUID, patientId, org.hl7.fhir.instance.model.ResourceType.Observation.toString());

                conditionResources = dal.getResourcesByPatient(serviceUUID, patientId, org.hl7.fhir.instance.model.ResourceType.Condition.toString());

                LOG.info(patientId.toString() + " got resources");
                if (observationResources.isEmpty() && conditionResources.isEmpty()) {
                    LOG.warn("Null patient resource for Patient " + patientId);
                    return null;
                }


                if (debug.equals("1")) {
                    LOG.info("Service: " + serviceUUID.toString());
                    LOG.info("Configname: " + subscriberConfigName);
                    LOG.info("Patientid: " + patientId.toString());
                }

                String observationContainerString = BulkHelper.getSubscriberContainerForObservationAdditionalData(observationResources, serviceUUID, batchUUID, subscriberConfigName, patientId);

                String conditionContainerString = BulkHelper.getSubscriberContainerForConditionAdditionalData(conditionResources, serviceUUID, batchUUID, subscriberConfigName, patientId);

                if (observationContainerString != null) {
                    LOG.info("filing data");
                    org.endeavourhealth.subscriber.filer.SubscriberFiler.file(batchUUID, UUID.randomUUID(), observationContainerString, subscriberConfigName);
                }

                if (conditionContainerString != null) {
                    LOG.info("filing data");
                    org.endeavourhealth.subscriber.filer.SubscriberFiler.file(batchUUID, UUID.randomUUID(), conditionContainerString, subscriberConfigName);
                }

                return null;
            }
            catch(Exception e) {
                LOG.error(e.toString());
            }
            return null;
        }
    }

    public static void bulkProcessReferenceRangesThreaded(String subscriberConfigName, String filePath, String debug, Integer threads, Integer QBeforeBlock) throws Exception {

        Set<UUID> hsPatientUuids = new HashSet<>();
        Set<UUID> hsServiceUuids = new HashSet<>();
        File f = new File(filePath);
        if (f.exists()) {
            List<String> lines = Files.readAllLines(f.toPath());
            for (String line : lines) {
                hsPatientUuids.add(UUID.fromString(line));
            }
        }

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> services = serviceDal.getAll();
        ResourceDalI dal = DalProvider.factoryResourceDal();
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

        SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);


        ThreadPool threadPool = new ThreadPool(threads, QBeforeBlock);

        for (Service service: services) {

            List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());

            if (!subscriberConfigNames.contains(subscriberConfigName)) {
                LOG.debug("Skipping " + service + " as not a publisher");
                continue;
            }

            LOG.debug("Doing " + service);
            UUID serviceId = service.getId();

            List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
            LOG.debug("Found " + patientIds.size() + " patients");

            for (UUID patientId : patientIds) {
                UUID batchUUID = UUID.randomUUID();

                // check if we have processed the patient already
                if (hsPatientUuids.contains(patientId)) {
                    continue;
                }
                List<String> newLines = new ArrayList<>();
                newLines.add(patientId.toString());
                Files.write(f.toPath(), newLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

                // check if in cohort
                SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                if (cohortRecord == null
                        || !cohortRecord.isInCohort()) {
                    continue;
                }

                List<ThreadPoolError> errors = threadPool.submit(new ObservationAndConditionCallable(serviceId, patientId, debug, dal, subscriberConfigName, batchUUID));

            }

            hsServiceUuids.add(serviceId);
        }

        List<ThreadPoolError> errors = threadPool.waitAndStop();

    }
}
