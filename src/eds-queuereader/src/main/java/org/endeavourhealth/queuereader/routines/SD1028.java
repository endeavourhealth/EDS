
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
import org.endeavourhealth.transform.subscriber.transforms.ReferralRequestTransformer;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class SD1028 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD1028.class);

    private static final String BULK_OPERATION_NAME = "Bulk FHIR->Subscriber transform of Compass v2 Referral data (SD-1028) for ";

    // For the config name provided, get the list of services which are publishers and send
    // their transformed Referral FHIR resources to the Filer for subscriber transform - TPP only,
    // so check the TPP tag and any non TPP ODS regex when calling.  Compass v2 only. Filter on year yyyy.
    public static void transformAndFileTPPReferralForProtocolServices(String subscriberConfigName, String orgOdsCodeRegex, String year) {

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

            LOG.debug("Re-populating Compass v2 Referrals for " + subscriberConfigName + " ("+year+"+)");
            String bulkOperationName = BULK_OPERATION_NAME + subscriberConfigName + " ("+year+"+)";

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

                    //transform referrals
                    if (compassV2Container == null) {
                        compassV2Container = new OutputContainer();
                    }

                    //get the patient reference only (no transform needed)
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

                    List<ResourceWrapper> referralWrappers
                            = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.ReferralRequest.toString());
                    params.populatePatientAndPersonIds();
                    ReferralRequestTransformer req = new ReferralRequestTransformer();

                    //if a year filter applied, i.e. 2023 only include a resource with a clinical effective date
                    //equal of greater than the year filer
                    if (!Strings.isNullOrEmpty(yearFilter)) {

                        List<ResourceWrapper> referralWrappersToKeep = new ArrayList<>();
                        for (ResourceWrapper referralWrapper : referralWrappers) {

                            ReferralRequest fhir = (ReferralRequest) referralWrapper.getResource();
                            if (fhir.hasDate()) {

                                DateTimeType dt = fhir.getDateElement();
                                LocalDate clinicalEffectiveDate
                                        = dt.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                if (clinicalEffectiveDate.getYear() >= Integer.parseInt(yearFilter)) {

                                    referralWrappersToKeep.add(referralWrapper);
                                }
                            }
                        }
                        req.transformResources(referralWrappersToKeep, params);

                    } else {
                        req.transformResources(referralWrappers, params);
                    }

                    //if batch is full then save what we've done
                    batchSize++;
                    if (batchSize >= 100) {
                        saveCompassV2ReferralData(serviceId, subscriberConfigName, compassV2Container);
                        compassV2Container = null;
                        batchSize = 0;
                    }
                }

                //save any part-completed batch
                if (batchSize > 0) {
                    saveCompassV2ReferralData(serviceId, subscriberConfigName, compassV2Container);
                }

                //audit that we've done all patient referrals for the service
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished Bulk FHIR->Subscriber transform of Referral data for "+ subscriberConfigName + " ("+year+"+)");
        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void saveCompassV2ReferralData(UUID serviceId, String subscriberConfigName, OutputContainer compassV2Container) throws Exception {
        List<SubscriberTableId> toKeep = new ArrayList<>();
        toKeep.add(SubscriberTableId.REFERRAL_REQUEST);
        toKeep.add(SubscriberTableId.REFERRAL_REQUEST_ADDITIONAL);
        compassV2Container.clearDownOutputContainer(toKeep);

        byte[] bytes = compassV2Container.writeToZip();
        if (bytes != null) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            UUID batchId = UUID.randomUUID();
            SubscriberFiler.file(serviceId, null, null, batchId, UUID.randomUUID(), base64, subscriberConfigName);
        }
    }
}