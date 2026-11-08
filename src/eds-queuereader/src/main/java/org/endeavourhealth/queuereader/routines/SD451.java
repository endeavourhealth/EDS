package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.common.fhir.PeriodHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.PatientBuilder;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SD451 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD451.class);

    private static final String BULK_OPERATION_NAME = "Fixing TPP records with multiple addresses (SD-451)";

    /**
     * fixes data SD-451
     */
    public static void fixTppRecordsWithMultipleAddresses(boolean includeStartedButNotFinishedServices, boolean testMode, int minsDelay, String odsCodeRegex, UUID filterOnPatientUuid) {
        LOG.debug("Fixing TPP Records with Multiple Addresses at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
                    continue;
                }

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                if (!testMode) {
                    //check to see if already done this services
                    if (isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME, includeStartedButNotFinishedServices)) {
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                fixTppRecordsWithMultipleAddressesAtService(testMode, service, filterOnPatientUuid);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

                //pause for whatever delay
                if (!testMode) {
                    Thread.sleep(minsDelay * 60 * 1000);
                }


            }

            LOG.debug("Finished Fixing TPP Records with Multiple Addresses at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixTppRecordsWithMultipleAddressesAtService(boolean testMode, Service service, UUID filterOnPatientUuid) throws Exception {

        String messageFormat = MessageFormat.TPP_CSV;

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, messageFormat);
        if (endpoint == null) {
            LOG.warn("No endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();


        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        if (!testMode) {
            newExchange = createNewExchange(service, systemId, messageFormat, "Manually created: " + BULK_OPERATION_NAME);
            UUID exchangeId = newExchange.getId();
            filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);
        }

        try {
            findAndFixPatientRecords(serviceId, filer, filterOnPatientUuid);

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

    private static void findAndFixPatientRecords(UUID serviceId, FhirResourceFiler filer, UUID filterOnPatientUuid) throws Exception {

        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
        LOG.debug("Found " + patientIds.size() + " patient IDs");

        int done = 0;
        int changed = 0;

        for (UUID patientId: patientIds) {

            if (filterOnPatientUuid != null
                    && !filterOnPatientUuid.equals(patientId)) {
                continue;
            }

            boolean makeChange = fixPatientRecord(serviceId, patientId, filer);
            if (makeChange) {
                changed ++;
            }

            done ++;

            if (done % 1000 == 0) {
                LOG.debug("Done " + done + " patients, fixed " + changed);
            }
        }
        LOG.debug("Finished " + done + " patients, fixed " + changed);
    }

    private static boolean fixPatientRecord(UUID serviceId, UUID patientId, FhirResourceFiler filer) throws Exception {

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        //retrieve the FHIR resource
        Patient patient = (Patient)resourceDal.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientId.toString());

        if (patient == null) {
            throw new Exception("Failed to find patient " + patientId);
        }

        if (!patient.hasAddress()) {
            return false;
        }

        List<Address> addresses = patient.getAddress();

        //if the patient record only has a single address, then it won't have been affected by the issue with sorting
        if (addresses.size() <= 1) {
            return false;
        }

        List<Address> activeAddresses = new ArrayList<>();
        List<Address> endedAddresses = new ArrayList<>();

        for (Address address: addresses) {

            if (PeriodHelper.isActive(address.getPeriod())) {
                activeAddresses.add(address);
            } else {
                endedAddresses.add(address);
            }
        }

        //work out if this patient was affected by the sorting issue
        boolean patientAffected = false;

        //if we have multiple active addresses and one has a null start, then the patient was affected
        if (activeAddresses.size() > 1) {
            for (Address address: activeAddresses) {
                if (!address.hasPeriod()
                        || !address.getPeriod().hasStart()) {
                    patientAffected = true;
                    break;
                }
            }

        } else if (activeAddresses.isEmpty()) {
            //if the patient has no active addresses and one of the ended ones has a null start, then the patient was affected
            for (Address address: endedAddresses) {
                if (!address.hasPeriod()
                        || !address.getPeriod().hasStart()) {
                    patientAffected = true;
                    break;
                }
            }
        }

        if (!patientAffected) {
            return false;
        }

        //if we get here, then the record will have been affected
        if (filer == null) {
            LOG.debug("Patient " + patientId + " is affected");

        } else {

            //creates an artificial change so FhirResourceFiler will save it
            ExtensionConverter.setResourceChanged(patient);

            //save FHIR patient to trigger the outbound process again
            PatientBuilder builder = new PatientBuilder(patient);
            filer.savePatientResource(null, false, builder);
        }

        return true;
    }



}