package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.ExtensionConverter;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.IdHelper;
import org.endeavourhealth.transform.common.resourceBuilders.AppointmentBuilder;
import org.hl7.fhir.instance.model.Appointment;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SD324 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD283.class);

    /**
     * fixes data for SD-324
     */
    public static void fixAppointmentStartTimes(boolean includeStartedButNotFinishedServices, String odsCodeRegex) {
        LOG.debug("Fixing Appointment Start Times at " + odsCodeRegex);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String bulkOperationName = "Fixing appointment start times (SD-324)";

            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                if (shouldSkipService(service, odsCodeRegex)) {
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
                fixAppointmentStartTimesAtService(service);

                //record as done
                setServiceDoneBulkOperation(service, bulkOperationName);

            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixAppointmentStartTimesAtService(Service service) throws Exception {

        Map<String, String> tags = service.getTags();
        if (tags == null) {
            throw new Exception("Service has no tags: " + service);
        }

        String messageFormat = null;
        if (tags.containsKey("TPP") && tags.containsKey("EMIS")) {
            throw new Exception("Service has both Emis and TPP tags " + service);

        } else if (tags.containsKey("TPP")) {
            messageFormat = MessageFormat.TPP_CSV;

        } else if (tags.containsKey("EMIS")) {
            messageFormat = MessageFormat.EMIS_CSV;

        } else {
            LOG.warn("Skipping service as not TPP or EMIS " + service);
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dFixed = dateFormat.parse("2020-04-02");

        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, messageFormat);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return;
        }
        UUID systemId = endpoint.getSystemUuid();

        //get all patient IDs
        UUID serviceId = service.getId();
        PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
        List<UUID> patientIds = patientSearchDal.getPatientIds(serviceId, false);
        LOG.debug("Found " + patientIds.size() + " patients");

        int done = 0;
        int processedAppts = 0;
        int changedAppts = 0;

        Exchange newExchange = null;
        FhirResourceFiler filer = null;
        List<UUID> batchIdsCreated = new ArrayList<>();

        newExchange = createNewExchange(service, systemId, MessageFormat.TPP_CSV, "Manually created to fix appointment start times (SD-324)");
        UUID exchangeId = newExchange.getId();
        filer = new FhirResourceFiler(exchangeId, service.getId(), systemId, new TransformError(), batchIdsCreated);

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();

        try {
            for (UUID patientId: patientIds) {

                List<ResourceWrapper> wrappers = resourceDal.getResourcesByPatient(serviceId, patientId, ResourceType.Appointment.toString());
                for (ResourceWrapper wrapper: wrappers) {

                    processedAppts ++;

                    //if the appointment was last updated BEFORE the fix was made, then force a change so it'll get sent out again
                    Date dtLastUpdated = wrapper.getCreatedAt();
                    if (!dtLastUpdated.after(dFixed)) {

                        Appointment appointment = (Appointment)wrapper.getResource();
                        ExtensionConverter.setResourceChanged(appointment); //make a change so it'll get saved
                        AppointmentBuilder builder = new AppointmentBuilder(appointment);

                        filer.savePatientResource(null, false, builder);
                        changedAppts ++;
                    }
                }

                done ++;
                if (done % 1000 == 0) {
                    LOG.debug("Done " + done + " patients, processed " + processedAppts + " appts and changed " + changedAppts);
                }
            }

            LOG.debug("Finished " + done + " patients, processed " + processedAppts + " appts and changed " + changedAppts);

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


}