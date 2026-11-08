package org.endeavourhealth.queuereader.routines;

import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.FhirProfileUri;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectEntity;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.resourceBuilders.GenericBuilder;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;

import static org.endeavourhealth.transform.common.AbstractCsvCallable.handleErrors;

public class SD877 extends AbstractRoutine{

    private static final Logger LOG = LoggerFactory.getLogger(SD877.class);

    private static String BULK_OPERATION_NAME = "Resolving Duplicate Condition Resources (SD-877)";
    public static void resolveDuplicateDataEntry(String ods, String isLive, String configName) throws Exception {

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getByLocalIdentifier(ods);

        LOG.debug("Running resolveDuplicateDataEntry for service: "+service.getName()+" ("+ods+")");
        Map<String, String> tags = service.getTags();
        if (tags == null || !tags.containsKey("EMIS")) {
            LOG.debug("Skipping " + service + " as not an EMIS system");
            return;
        }
        BULK_OPERATION_NAME = BULK_OPERATION_NAME.concat(" - MODE: "+isLive);
        if(isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME)){
            LOG.debug("Skipping " + service + " as already started or done");
            return;
        }
        List<Resource> toDelete = new ArrayList<>();
        HashMap <String, String> conditionObs = new HashMap<>();

        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientIds = patientSearchDal.getPatientIds(service.getId(), false);
        LOG.debug("Patients found at "+service.getName()+": " + patientIds.size());

        int total = 0;
        for (UUID patientId : patientIds) {
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();

            //get patient conditions first
            List<ResourceWrapper> conditionWrappers
                    = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.Condition.toString());
            if (conditionWrappers == null || conditionWrappers.size() == 0) {
                continue;
            }
            //only get observations if the patient has some conditions to compare with
            List<ResourceWrapper> observationWrappers
                    = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.Observation.toString());
            if (observationWrappers == null || observationWrappers.size() == 0) {
                continue;
            }
            LOG.debug("Checking patient's "+conditionWrappers.size() + " Conditions against "
                    + observationWrappers.size() + " Observations...");
            for (ResourceWrapper conditionWrapper : conditionWrappers) {

                if (conditionWrapper.isDeleted()) {
                    continue;
                }
                String conditionJson = conditionWrapper.getResourceData();
                Condition condition = (Condition) FhirSerializationHelper.deserializeResource(conditionJson);
                if (condition == null) {
                    continue;
                }
                //we are only checking non-problem Condition resources
                if (condition.getMeta().hasProfile(FhirProfileUri.PROFILE_URI_CONDITION)) {
                    if (!condition.hasOnsetDateTimeType()) {
                        continue;
                    }
                    if (!condition.hasCode()) {
                        continue;
                    }
                    if (!condition.getCode().hasCoding()) {
                        continue;
                    }
                    if (condition.getCode().getCoding().get(0) == null) {
                        continue;
                    }
                    if (condition.getCode().getCoding().get(1) == null) {
                        continue;
                    }
                    for (ResourceWrapper observationWrapper : observationWrappers) {

                        if (observationWrapper.isDeleted()) {
                            continue;
                        }
                        String observationJson = observationWrapper.getResourceData();
                        Observation observation
                                = (Observation) FhirSerializationHelper.deserializeResource(observationJson);

                        if (observation == null) {
                            continue;
                        }
                        if (!observation.hasEffectiveDateTimeType()) {
                            continue;
                        }
                        if (!observation.hasCode()) {
                            continue;
                        }
                        if (observation.getCode().getCoding() == null) {
                            continue;
                        }
                        if (observation.getCode().getCoding().get(0) == null) {
                            continue;
                        }
                        if (observation.getCode().getCoding().get(1) == null) {
                            continue;
                        }
                        //LOG.info("Comparing condition: {}, with observation {}", condition.getId(), observation.getId());
                        if (observation.getEffectiveDateTimeType().getValue().compareTo(condition.getOnsetDateTimeType().getValue()) == 0) {
                            if (observation.getCode().getText().equals(condition.getCode().getText())) {
                                //if the original coding, i.e. Read2 codes are different, i.e. ones a code, the other a Synonym
                                if (!observation.getCode().getCoding().get(0).getCode().equals(condition.getCode().getCoding().get(0).getCode())) {
                                    //and then if the Snomed coding is the same
                                    if (observation.getCode().getCoding().get(1).getCode().equals(condition.getCode().getCoding().get(1).getCode())) {

                                        //add the duplicate condition resource to the list
                                        Resource r = conditionWrapper.getResource();
                                        toDelete.add(r);
                                        conditionObs.put(condition.getId(), observation.getId());
                                        //LOG.info("Condition Resource: {}, matches Observation Resource {}", condition.getId(), observation.getId());
                                        total++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isLive.equals("0")) {

            LOG.debug("Total Condition <-> Observation matches found: " + total);
            for (Resource condition : toDelete) {
                LOG.debug("Condition found to be a duplicate: " + condition.getId() + " of Observation: "+conditionObs.get(condition.getId()));
            }
        } else if (isLive.equals("1")){

            LOG.info("Total Condition <-> Observation matches found for deletion: " + total);
            if (total > 0) {

                String messageFormat = MessageFormat.EMIS_CSV;
                ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, messageFormat);
                if (endpoint == null) {
                    LOG.warn("No endpoint found for " + service);
                    return;
                }
                UUID systemId = endpoint.getSystemUuid();
                Exchange newExchange
                        = createNewExchange(service, systemId, messageFormat, "Manually created: " + BULK_OPERATION_NAME);
                UUID exchangeId = newExchange.getId();
                List<UUID> batchIdsCreated = new ArrayList<>();
                FhirResourceFiler filer
                        = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
                try {
                    if (null != filer) {
                        for (Resource condition : toDelete) {

                            LOG.info("Deleting Condition Resource: " + condition.getId());
                            filer.deletePatientResource(null, false, new GenericBuilder(condition));
                        }
                    } else {
                        LOG.error("SD877 - Resource Filer is null. Resources not deleted.");
                    }
                }
                finally {
                    //close down filer and create non-re-queueable rabbit exchange
                    if (filer != null) {
                        LOG.debug("Filer waiting to finish");
                        filer.waitToFinish();

                        //set multicast header
                        String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                        newExchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                        //post to Rabbit protocol queue
                        List<UUID> exchangeIds = new ArrayList<>();
                        exchangeIds.add(newExchange.getId());

                        if (!Strings.isNullOrEmpty(configName)) {

                            Set<String> setOfConfigs = new HashSet<>();
                            setOfConfigs.add(configName);
                            QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, setOfConfigs, null);
                        } else {
                            QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, null);
                        }

                        //set this after posting to rabbit so we can't re-queue it later
                        newExchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                        newExchange.getHeaders().remove(HeaderKeys.BatchIdsJson);
                        AuditWriter.writeExchange(newExchange);
                    }
                }
            }
        }

        //set the bulk job as completed
        setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
    }
    public static void resolveDuplicateDataEntry(String ods) throws Exception {
        LOG.warn("No mode clarified, running in testing mode. To specify mode argument 3 should be 0 for logging or 1" +
                " for altering data");
            resolveDuplicateDataEntry(ods, "0", "");
    }

    public static void resolveAllDuplicateDataEntries(String configName, String isLive) throws Exception {

        List<Service> publishers = getSubscriberFeedPublishers (configName);
        for(Service publisher : publishers){

            String odsCode = publisher.getLocalId();
            if (!Strings.isNullOrEmpty(odsCode)) {

                resolveDuplicateDataEntry(odsCode, isLive, configName);
            }
        }
    }

    public static void resolveAllDuplicateDataEntries(String configName) throws Exception{
        LOG.warn("No mode clarified, running in testing mode. To specify mode argument 3 should be 0 for logging or 1" +
                " for altering data");
        resolveAllDuplicateDataEntries(configName, "0");
    }

    //get a list of all the Services which publish data to this Subscriber configuration
    private static List<Service> getSubscriberFeedPublishers(String subscriberConfig) throws Exception  {

        List<Service> subscriberPublishers = new ArrayList<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> allServices = serviceDal.getAll();
        for (Service service: allServices) {

            //only looking for actual data publisher services
            String publisherConfig = service.getPublisherConfigName();
            if (Strings.isNullOrEmpty(publisherConfig)) {
                continue;
            }

            //retrieve the current distribution projects for the publishing service
            String odsCode = service.getLocalId();
            List<ProjectEntity> distributionProjects = ProjectCache.getValidDistributionProjectsForPublisher(odsCode);
            if (distributionProjects != null) {
                for (ProjectEntity pe : distributionProjects) {

                    //get the subscriber configuration for this service distribution project
                    String configName = pe.getConfigName();

                    //if the publisher service has the subscriber configuration in its list of
                    //distribution projects then add the service into the subscriber publishers list
                    if (!Strings.isNullOrEmpty(configName) && configName.equalsIgnoreCase(subscriberConfig)) {

                        LOG.info("Publishing service: "+service.getName()+" found for configuration: "+configName);
                        subscriberPublishers.add(service);
                    }
                }
            }
        }
        return subscriberPublishers;
    }

    private static final int threads = 5;

    public static void resolveDuplicateDataEntryThreaded(String ods, String isLive, String configName) throws Exception {

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getByLocalIdentifier(ods);

        LOG.debug("Running resolveDuplicateDataEntryThreaded for service: "+service.getName()+" ("+ods+")");
        Map<String, String> tags = service.getTags();
        if (tags == null || !tags.containsKey("EMIS")) {
            LOG.debug("Skipping " + service + " as not an EMIS system");
            return;
        }
        BULK_OPERATION_NAME = BULK_OPERATION_NAME.concat(" - MODE: "+isLive);
        if(isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME)){
            LOG.debug("Skipping " + service + " as already started or done");
            return;
        }

        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientIds = patientSearchDal.getPatientIds(service.getId(), false);
        LOG.debug("Patients found at "+service.getName()+": " + patientIds.size());

        int totalPatients = patientIds.size();
        int patientsPerThread = totalPatients / threads;
        LOG.debug("Patients per thread: " + patientsPerThread);

        ThreadPool threadPool = new ThreadPool(threads, 1000);
        for(int i = 0; i < threads; i++){
            List<UUID> threadPatients = new ArrayList<>();
            for(int j = 0; j < patientsPerThread; j++) {
                if(patientIds.size() > 0) {
                    threadPatients.add(patientIds.get(0));
                    patientIds.remove(0);
                } else {
                    break;
                }
            }
            CallableTask callable = new CallableTask(threadPatients, service, isLive, configName);
            List<ThreadPoolError> errors = threadPool.submit(callable);
            handleErrors(errors);
        }
        List<ThreadPoolError> errors = threadPool.waitAndStop();
        handleErrors(errors);

        //set the bulk job as completed
        setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
    }

    public static void resolveDuplicateDataEntryThreaded(String ods) throws Exception {
        LOG.warn("No mode clarified, running in testing mode. To specify mode argument 3 should be 0 for logging or 1" +
                " for altering data");
        resolveDuplicateDataEntryThreaded(ods, "0", "");
    }

    public static void resolveAllDuplicateDataEntriesThreaded(String configName, String isLive) throws Exception {

        List<Service> publishers = getSubscriberFeedPublishers(configName);
        for(Service publisher : publishers){

            String odsCode = publisher.getLocalId();
            if (!Strings.isNullOrEmpty(odsCode)) {

                resolveDuplicateDataEntryThreaded(odsCode, isLive, configName);
            }
        }
    }

    public static void resolveAllDuplicateDataEntriesThreaded(String configName) throws Exception{
        LOG.warn("No mode clarified, running in testing mode. To specify mode argument 3 should be 0 for logging or 1" +
                " for altering data");
        resolveAllDuplicateDataEntries(configName, "0");
    }
}

class CallableTask extends AbstractRoutine implements Callable<List<Resource>>{

    private static final Logger LOG = LoggerFactory.getLogger(SD877.class);

    private List<UUID> patientIds;
    private Service service;
    private String isLive, configName;
    private static final String BULK_OPERATION_NAME = "Resolving Duplicate Entry (SD-877)";

    public CallableTask(List<UUID> patients, Service service, String isLive, String configName){
        patientIds = patients;
        this.service = service;
        this.isLive = isLive;
        this.configName = configName;
    }

    @Override
    public List<Resource> call() throws Exception {
        resolveDuplicateDataEntry(patientIds, isLive, service, configName);

        return null;
    }

    public static void resolveDuplicateDataEntry(List<UUID> patientIds, String isLive, Service service, String configName) throws Exception {

        List<Resource> toDelete = new ArrayList<>();
        HashMap <String, String> dupeConditionObs = new HashMap<>();
        int total = 0;
        for (UUID patientId : patientIds) {
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();

            //get patient conditions first
            List<ResourceWrapper> conditionWrappers
                    = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.Condition.toString());
            if (conditionWrappers == null || conditionWrappers.size() == 0) {
                continue;
            }
            //only get observations if the patient has some conditions to compare with
            List<ResourceWrapper> observationWrappers
                    = resourceDal.getResourcesByPatient(service.getId(), patientId, ResourceType.Observation.toString());
            if (observationWrappers == null || observationWrappers.size() == 0) {
                continue;
            }
            //LOG.debug("Checking patient's "+conditionWrappers.size() + " Conditions against "
            //      + observationWrappers.size() + " Observations...");
            for (ResourceWrapper conditionWrapper : conditionWrappers) {

                if (conditionWrapper.isDeleted()) {
                    continue;
                }
                String conditionJson = conditionWrapper.getResourceData();
                Condition condition = (Condition) FhirSerializationHelper.deserializeResource(conditionJson);
                if (condition == null) {
                    continue;
                }
                //we are only checking non-problem Condition resources
                if (condition.getMeta().hasProfile(FhirProfileUri.PROFILE_URI_CONDITION)) {
                    if (!condition.hasOnsetDateTimeType()) {
                        continue;
                    }
                    if (!condition.hasCode()) {
                        continue;
                    }
                    if (!condition.getCode().hasCoding()) {
                        continue;
                    }
                    if (condition.getCode().getCoding().get(0) == null) {
                        continue;
                    }
                    if (condition.getCode().getCoding().get(1) == null) {
                        continue;
                    }
                    for (ResourceWrapper observationWrapper : observationWrappers) {

                        if (observationWrapper.isDeleted()) {
                            continue;
                        }
                        String observationJson = observationWrapper.getResourceData();
                        Observation observation
                                = (Observation) FhirSerializationHelper.deserializeResource(observationJson);

                        if (observation == null) {
                            continue;
                        }
                        if (!observation.hasEffectiveDateTimeType()) {
                            continue;
                        }
                        if (!observation.hasCode()) {
                            continue;
                        }
                        if (observation.getCode().getCoding() == null) {
                            continue;
                        }
                        if (observation.getCode().getCoding().get(0) == null) {
                            continue;
                        }
                        if (observation.getCode().getCoding().get(1) == null) {
                            continue;
                        }
                        //LOG.info("Comparing condition: {}, with observation {}", condition.getId(), observation.getId());
                        if (observation.getEffectiveDateTimeType().getValue().compareTo(condition.getOnsetDateTimeType().getValue()) == 0) {
                            if (observation.getCode().getText().equals(condition.getCode().getText())) {
                                //if the original coding, i.e. Read2 codes are different, i.e. ones a code, the other a Synonym
                                String obsCode = observation.getCode().getCoding().get(0).getCode();     //the observation Read2 code
                                String conditionCode = condition.getCode().getCoding().get(0).getCode(); //the condition Read2 code
                                if (!obsCode.equals(conditionCode)) {

                                    //and then if the Snomed coding is the same
                                    if (observation.getCode().getCoding().get(1).getCode().equals(condition.getCode().getCoding().get(1).getCode())) {

                                        //add the duplicate condition resource to the list
                                        Resource r = conditionWrapper.getResource();
                                        toDelete.add(r);
                                        dupeConditionObs.put(condition.getId(), observation.getId().concat(" ("+obsCode+")"));
                                        //LOG.info("Condition Resource: {}, matches Observation Resource {}", condition.getId(), observation.getId());
                                        total++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isLive.equals("0")) {

            LOG.debug("Total Condition <-> Observation matches found: " + total);
            for (Resource condition : toDelete) {
                LOG.debug("Condition (with adjusted Read code) found to be a duplicate: " + condition.getId() + " of Observation: "+dupeConditionObs.get(condition.getId()));
            }
        } else if (isLive.equals("1")){

            LOG.info("Total Condition <-> Observation matches found for deletion: " + total);
            if (total > 0) {

                String messageFormat = MessageFormat.EMIS_CSV;
                ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, messageFormat);
                if (endpoint == null) {
                    LOG.warn("No endpoint found for " + service);
                    return;
                }
                UUID systemId = endpoint.getSystemUuid();
                Exchange newExchange
                        = createNewExchange(service, systemId, messageFormat, "Manually created: " + BULK_OPERATION_NAME);
                UUID exchangeId = newExchange.getId();
                List<UUID> batchIdsCreated = new ArrayList<>();
                FhirResourceFiler filer
                        = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

                try {
                    if (null != filer) {
                        for (Resource condition : toDelete) {

                            LOG.info("Deleting Condition Resource: " + condition.getId());
                            filer.deletePatientResource(null, false, new GenericBuilder(condition));
                        }
                    } else {
                        LOG.error("SD877 - Resource Filer is null. Resources not deleted.");
                    }
                }
                finally {
                    //close down filer and create non-re-queueable rabbit exchange
                    if (filer != null) {
                        LOG.debug("Filer waiting to finish");
                        filer.waitToFinish();

                        //set multicast header
                        String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsCreated.toArray());
                        newExchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

                        //post to Rabbit protocol queue
                        List<UUID> exchangeIds = new ArrayList<>();
                        exchangeIds.add(newExchange.getId());

                        if (!Strings.isNullOrEmpty(configName)) {

                            Set<String> setOfConfigs = new HashSet<>();
                            setOfConfigs.add(configName);
                            QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, setOfConfigs, null);
                        } else {
                            QueueHelper.postToExchange(exchangeIds, QueueHelper.ExchangeName.PROTOCOL, null, null);
                        }

                        //set this after posting to rabbit so we can't re-queue it later
                        newExchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, new Boolean(false)); //don't allow this to be re-queued
                        newExchange.getHeaders().remove(HeaderKeys.BatchIdsJson);
                        AuditWriter.writeExchange(newExchange);
                    }
                }
            }
        }

        //set the bulk job as completed
        //setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
    }

    //get a list of all the Services which publish data to this Subscriber configuration
    private static List<Service> getSubscriberFeedPublishers(String subscriberConfig) throws Exception  {

        List<Service> subscriberPublishers = new ArrayList<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> allServices = serviceDal.getAll();
        for (Service service: allServices) {

            //only looking for actual data publisher services
            String publisherConfig = service.getPublisherConfigName();
            if (Strings.isNullOrEmpty(publisherConfig)) {
                continue;
            }

            //retrieve the current distribution projects for the publishing service
            String odsCode = service.getLocalId();
            List<ProjectEntity> distributionProjects = ProjectCache.getValidDistributionProjectsForPublisher(odsCode);
            if (distributionProjects != null) {
                for (ProjectEntity pe : distributionProjects) {

                    //get the subscriber configuration for this service distribution project
                    String configName = pe.getConfigName();

                    //if the publisher service has the subscriber configuration in its list of
                    //distribution projects then add the service into the subscriber publishers list
                    if (!Strings.isNullOrEmpty(configName) && configName.equalsIgnoreCase(subscriberConfig)) {

                        LOG.info("Publishing service: "+service.getName()+" found for configuration: "+configName);
                        subscriberPublishers.add(service);
                    }
                }
            }
        }
        return subscriberPublishers;
    }
}