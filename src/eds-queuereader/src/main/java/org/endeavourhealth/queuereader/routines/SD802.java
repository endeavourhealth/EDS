
package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
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
import org.endeavourhealth.transform.subscriber.transforms.EncounterTransformer;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class SD802 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD802.class);

    private static final String BULK_OPERATION_NAME = "Bulk FHIR->Subscriber transform of Compass v2 encounter data (SD-802) for ";

    // For the config name provided, get the list of services which are publishers and send
    // their transformed Encounters FHIR resources to the Filer for subscriber transform - TPP only,
    //so check the TPP tag and any non TPP ODS regex when calling.  Compass v2 only. Filter on year yyyy.
    public static void transformAndFileTPPEncountersForProtocolServices(String subscriberConfigName, String orgOdsCodeRegex, String year) {

        try {

            SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
            //compass v2 only
            if (subscriberConfig.getSubscriberType() != SubscriberConfig.SubscriberType.CompassV2) {
                LOG.debug("Ignoring "+subscriberConfigName+" as not a Compass v2 configuration");
                return;
            }

            String yearFilter = null;
            if (!Strings.isNullOrEmpty(year)) {
                yearFilter = year;
            } else {
                year = "All";
            }

            LOG.debug("Re-populating Compass v2 Encounters for " + subscriberConfigName + " ("+year+")");
            String bulkOperationName = BULK_OPERATION_NAME + subscriberConfigName + " ("+year+")";

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> services = serviceDal.getAll();

            SubscriberCohortDalI subscriberCohortDalI = DalProvider.factorySubscriberCohortDal();
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            SubscriberResourceMappingDalI patientIdDal
                    = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            for (Service service: services) {

                //pass in known non TPP ODS codes to skip
                if (shouldSkipService(service, orgOdsCodeRegex)) {
                    continue;
                }

                //check TPP only
                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    LOG.debug("Skipping service: " + service + " as not a TPP publisher");
                    continue;
                }

                List<String> subscriberConfigNames
                        = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                if (!subscriberConfigNames.contains(subscriberConfigName)) {
                    LOG.debug("Skipping service: " + service + " as not a publisher to: "+subscriberConfigName);
                    continue;
                }

                //check if service already done or currently started to allow multiple instance of this method
                if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName)) {
                    LOG.debug("Skipping service: " + service + " as already started or done");
                    continue;
                }

                LOG.debug("Doing service: " + service);
                UUID serviceId = service.getId();

                List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
                LOG.debug("Found " + patientIds.size() + " patients");

                int batchSize = 0;
                OutputContainer compassV2Container = null;

                for (UUID patientId : patientIds) {

                    //check if patient matches cohort definition for subscriber configuration
                    SubscriberCohortRecord cohortRecord
                            = subscriberCohortDalI.getLatestCohortRecord(subscriberConfigName, patientId, UUID.randomUUID());
                    if (cohortRecord == null
                            || !cohortRecord.isInCohort()) {
                        continue;
                    }

                    //retrieve FHIR patient
                    ResourceWrapper patientWrapper
                            = resourceDal.getCurrentVersion(serviceId, ResourceType.Patient.toString(), patientId);
                    if (patientWrapper == null
                            || patientWrapper.isDeleted()) {
                        LOG.warn("Null patient FHIR resource for patient ID " + patientId);
                        continue;
                    }
                    List<ResourceWrapper> wrappers = new ArrayList<>();
                    wrappers.add(patientWrapper);

                    //transform patient and encounters
                    if (compassV2Container == null) {
                        compassV2Container = new OutputContainer();
                    }

                    String ref = patientWrapper.getReferenceString();
                    SubscriberId subscriberPatientId
                            = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                    if (subscriberPatientId == null) {
                        LOG.warn("Failed to find subscriberPatientId for " + ref);
                        continue;
                    }

                    SubscriberTransformHelper params
                            = new SubscriberTransformHelper(serviceId, null, null, null, subscriberConfig, wrappers, false, false, compassV2Container);
                    Long orgId = FhirToSubscriberCsvTransformer.findEnterpriseOrgId(serviceId, params, new ArrayList<>());
                    params.setSubscriberOrganisationId(orgId);

                    //no need to transform the Patient resource
                    //PatientTransformer t = new PatientTransformer();
                    //t.transformResources(wrappers, params);

                    List<ResourceWrapper> encounterWrappers
                            = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Encounter.toString());
                    params.populatePatientAndPersonIds();
                    EncounterTransformer enc = new EncounterTransformer();

                    //if a year filter applied, i.e. 2020 only include a resource with a clinical effective date
                    //equal of greater than the year filer
                    if (!Strings.isNullOrEmpty(yearFilter)) {

                        List<ResourceWrapper> encounterWrappersToKeep = new ArrayList<>();
                        for (ResourceWrapper encounterWrapper : encounterWrappers) {

                            Encounter fhir = (Encounter) encounterWrapper.getResource();
                            if (fhir.hasPeriod()) {

                                Period period = fhir.getPeriod();
                                DateTimeType dt = period.getStartElement();
                                LocalDate clinicalEffectiveDate
                                        = dt.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                if (clinicalEffectiveDate.getYear() >= Integer.parseInt(yearFilter)) {

                                    encounterWrappersToKeep.add(encounterWrapper);
                                }
                            }
                        }
                        enc.transformResources(encounterWrappersToKeep, params);

                    } else {
                        enc.transformResources(encounterWrappers, params);
                    }

                    //if batch is full then save what we've done
                    batchSize++;
                    if (batchSize >= 100) {
                        saveCompassV2EncounterData(serviceId, subscriberConfigName, compassV2Container);
                        compassV2Container = null;
                        batchSize = 0;
                    }
                }

                //save any part-completed batch
                if (batchSize > 0) {
                    saveCompassV2EncounterData(serviceId, subscriberConfigName, compassV2Container);
                }

                //audit that we've done all patient encounter for the service
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished Bulk FHIR->Subscriber transform of encounter data for "+ subscriberConfigName + " ("+year+")");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void saveCompassV2EncounterData(UUID serviceId, String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.ENCOUNTER);
        toKeep.add(SubscriberTableId.ENCOUNTER_EVENT);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(serviceId, null, null, batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }

}