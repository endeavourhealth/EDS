package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberCohortDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberCohortRecord;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberId;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.subscriber.filer.SubscriberFiler;
import org.endeavourhealth.transform.subscriber.FhirToSubscriberCsvTransformer;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.endeavourhealth.transform.subscriber.SubscriberTransformHelper;
import org.endeavourhealth.transform.subscriber.targetTables.OutputContainer;
import org.endeavourhealth.transform.subscriber.targetTables.SubscriberTableId;
import org.endeavourhealth.transform.subscriber.transforms.PatientTransformer;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class SD477 extends AbstractRoutine {

    private static final Logger LOG = LoggerFactory.getLogger(SD477.class);

    // For the config name provided, get the list of services which are publishers
    // and send their transformed Patient FHIR resources to the Filer
    public static void transformAndFilePatientDeIdentifiedTables(boolean includeStartedButNotFinishedServices, String subscriberConfigName, String orgOdsCodeRegex) {
        LOG.debug("Populating CompassV2 patient de-identified tables for " + subscriberConfigName + " regex " + orgOdsCodeRegex);
        try {

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

            String bulkOperationName = "Bulk load of patient de-identified tables for " + subscriberConfigName;

            for (Service service: services) {

                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping " + service + " as not a publisher");
                    continue;
                }

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

                LOG.debug("Doing " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int done = 0;
                int batchSize = 0;
                // org.endeavourhealth.transform.enterprise.outputModels.OutputContainer compassV1Container = null;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    // check if in cohort
                    SubscriberCohortRecord cohortRecord = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    // retrieve FHIR patient
                    ResourceWrapper patientWrapper = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null
                            || patientWrapper.isDeleted()) {
                        LOG.warn("Null patient resource for patient ID " + patientId);
                        continue;
                        // throw new Exception("Null patient resource for patient ID " + patientId);
                    }
                    List<ResourceWrapper> wrappers = new ArrayList<>();
                    wrappers.add(patientWrapper);

                    Patient fhirPatient = (Patient)patientWrapper.getResource();

                    String discoveryPersonId = patientLinkDal.getPersonId(fhirPatient.getId());
                    if (Strings.isNullOrEmpty(discoveryPersonId)) {
                        LOG.warn("Null or empty person ID for " + fhirPatient.getId());
                        continue;
                        // throw new Exception("Null or empty person ID for " + fhirPatient.getId());
                    }

                    // transform patient
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        if (compassV2Container == null) {
                            compassV2Container = new OutputContainer();
                        }

                        String ref = patientWrapper.getReferenceString();
                        SubscriberId subscriberPatientId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                        if (subscriberPatientId == null) {
                            LOG.warn("Failed to find subscriberPatientId for " + ref);
                            continue;
                            // throw new Exception("Failed to find subscriberPatientId for " + ref);
                        }

                        try {
                            SubscriberTransformHelper params = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, false, compassV2Container);
                            Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                            params.setSubscriberOrganisationId(orgId);
                            PatientTransformer pt = new PatientTransformer();
                            pt.transformResources(wrappers, params);
                            // if batch is full then save what has been done
                            batchSize++;
                            if (batchSize >= 100) {
                                saveCompassV2DeIdentifiedTables(serviceId, subscriberConfigName, compassV2Container);
                                compassV2Container = null;
                                batchSize = 0;
                            }
                        } catch (Throwable t) {
                            LOG.warn("Error with transform for patient " + patientId, t);
                        }
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }

                    done ++;
                    if (done % 1000 == 0) {
                        LOG.debug("Done " + done + " / " + patientIds.size() + " patients");
                    }
                }

                //save any part completed batch
                if (batchSize > 0) {
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        saveCompassV2DeIdentifiedTables(serviceId, subscriberConfigName, compassV2Container);
                    } else {
                        throw new Exception("Unexpected subscriber type [" + subscriberConfig.getSubscriberType() + "]");
                    }
                }

                LOG.debug("Completed " + done + " / " + patientIds.size() + " patients");

                //audit what has been done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished populating CompassV2 patient de-identified tables for " + subscriberConfigName + " regex " + orgOdsCodeRegex);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void saveCompassV2DeIdentifiedTables(UUID serviceId, String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.PATIENT);
        toKeep.add(SubscriberTableId.PATIENT_ADDRESS);
        toKeep.add(SubscriberTableId.PATIENT_CONTACT);
        toKeep.add(SubscriberTableId.PATIENT_PSEUDO_ID);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(serviceId, null, null, batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }
}
