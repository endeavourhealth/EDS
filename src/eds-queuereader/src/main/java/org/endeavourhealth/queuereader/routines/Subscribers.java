package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractActive;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.endeavourhealth.subscriber.filer.EnterpriseFiler;
import org.endeavourhealth.subscriber.filer.SubscriberFiler;
import org.endeavourhealth.transform.subscriber.BulkHelper;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;

public class Subscribers extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(Subscribers.class);


    public static void bulkProcessTransformAndFilePatientsAndEpisodesForProtocolServices(String subscriberConfigName,
                                                                                         String protocolName,
                                                                                         String compassVersion,
                                                                                         String filePath,
                                                                                         String debug,
                                                                                         Integer threads,
                                                                                         Integer QBeforeBlock) throws Exception {

        Set<UUID> hsPatientUuids = new HashSet<>();
        Set<UUID> hsServiceUuids = new HashSet<>();
        File f = new File(filePath);
        if (f.exists()) {
            List<String> lines = Files.readAllLines(f.toPath());
            for (String line : lines) {
                hsPatientUuids.add(UUID.fromString(line));
            }
        }

        LibraryItem matchedLibraryItem = BulkHelper.findProtocolLibraryItem(protocolName);

        if (matchedLibraryItem == null) {
            System.out.println("Protocol not found : " + protocolName);
            return;
        }
        List<ServiceContract> serviceContracts = matchedLibraryItem.getProtocol().getServiceContract();
        ResourceDalI dal = DalProvider.factoryResourceDal();
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        SubscriberResourceMappingDalI enterpriseIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
        ThreadPool threadPool = new ThreadPool(threads, QBeforeBlock);

        for (ServiceContract serviceContract : serviceContracts) {
            if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                    && serviceContract.getActive() == ServiceContractActive.TRUE) {

                UUID batchUUID = UUID.randomUUID();
                String serviceId = serviceContract.getService().getUuid();
                UUID serviceUUID = UUID.fromString(serviceId);

                if (hsServiceUuids.contains(serviceUUID)) {
                    // already processed the service
                    continue;
                }

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceUUID, true);

                for (UUID patientId : patientIds) {

                    // check if we have processed the patient already
                    if (hsPatientUuids.contains(patientId)) {
                        continue;
                    }
                    List<String> newLines = new ArrayList<>();
                    newLines.add(patientId.toString());
                    Files.write(f.toPath(), newLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);

                    LOG.info(patientId.toString());
                    Long ret = enterpriseIdDal.findEnterpriseIdOldWay("Patient", patientId.toString());
                    if (ret != null) {
                        // check if the patient has previously been processed?
                        LOG.info(ret.toString());
                    }
                    List<ThreadPoolError> errors = threadPool.submit(new PatientsAndEpisodesCallable(serviceUUID,
                            patientId, debug, dal, compassVersion, subscriberConfigName, batchUUID));
                    //handleErrors(errors);
                }

                hsServiceUuids.add(serviceUUID);
            }
        }

        List<ThreadPoolError> errors = threadPool.waitAndStop();
    }


    static class PatientsAndEpisodesCallable implements Callable {

        private UUID serviceUuid;
        private UUID patientId;
        private String debug;
        private ResourceDalI dal;
        private String compassVersion;
        private String subscriberConfigName;
        private UUID batchUuid;
        private UUID protocolUuid;

        public PatientsAndEpisodesCallable(UUID serviceUuid, UUID patientId, String debug,
                                           ResourceDalI dal, String compassVersion, String subscriberConfigName,
                                           UUID batchUuid) {

            this.serviceUuid = serviceUuid;
            this.patientId = patientId;
            this.debug = debug;
            this.dal = dal;
            this.compassVersion = compassVersion;
            this.subscriberConfigName = subscriberConfigName;
            this.batchUuid = batchUuid;
        }

        @Override
        public Object call() throws Exception {

            try {
                List<ResourceWrapper> resources = new ArrayList<>();

                ResourceWrapper patientWrapper =
                        dal.getCurrentVersion(serviceUuid, ResourceType.Patient.toString(), patientId);

                if (patientWrapper == null) {
                    // LOG.warn("Null patient resource for Patient " + patientId);
                    return null;
                }

                resources.add(patientWrapper);

                if (debug.equals("1")) {
                    LOG.info("Service: " + serviceUuid.toString());
                    LOG.info("Configname: " + subscriberConfigName);
                    LOG.info("Patientid: " + patientId.toString());
                }

                if (compassVersion.equalsIgnoreCase(SpecialRoutines.COMPASS_V1)) {
                    String patientContainerString
                            = BulkHelper.getEnterpriseContainerForPatientData(resources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                    //  Use  a random UUID for a queued message ID
                    if (patientContainerString != null) {
                        EnterpriseFiler.file(batchUuid, UUID.randomUUID(), patientContainerString, subscriberConfigName);
                    }
                } else if (compassVersion.equalsIgnoreCase(SpecialRoutines.COMPASS_V2)) {
                    String patientContainerString
                            = BulkHelper.getSubscriberContainerForPatientData(resources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                    //  Use  a random UUID for a queued message ID
                    if (patientContainerString != null) {
                        SubscriberFiler.file(batchUuid, UUID.randomUUID(), patientContainerString, subscriberConfigName);
                    }
                }

                List<ResourceWrapper> episodeResources = new ArrayList<>();

                //patient may have multiple episodes of care at the service, so pass them in
                List<ResourceWrapper> episodeWrappers
                        = dal.getResourcesByPatient(serviceUuid, patientId, ResourceType.EpisodeOfCare.toString());

                if (episodeWrappers.isEmpty()) {
                    LOG.warn("No episode resources for Patient " + patientId);
                } else {
                    for (ResourceWrapper episodeWrapper: episodeWrappers  ) {
                        episodeResources.add(episodeWrapper);
                    }

                    if (compassVersion.equalsIgnoreCase(SpecialRoutines.COMPASS_V1)) {
                        String episodeContainerString
                                = BulkHelper.getEnterpriseContainerForEpisodeData(episodeResources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                        //  Use  a random UUID for a queued message ID
                        if (episodeContainerString != null) {
                            EnterpriseFiler.file(batchUuid, UUID.randomUUID(), episodeContainerString, subscriberConfigName);
                        }
                    } else if (compassVersion.equalsIgnoreCase(SpecialRoutines.COMPASS_V2)) {
                        String episodeContainerString
                                = BulkHelper.getSubscriberContainerForEpisodeData(episodeResources, serviceUuid, batchUuid, protocolUuid, subscriberConfigName, patientId);

                        //  Use  a random UUID for a queued message ID
                        if (episodeContainerString != null) {
                            SubscriberFiler.file(batchUuid, UUID.randomUUID(), episodeContainerString, subscriberConfigName);
                        }
                    }
                }
                return null;
            }
            catch(Exception e) {
                LOG.error(e.toString());
            }
            return null;
        }
    }
}
