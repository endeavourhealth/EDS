package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.MessageFormat;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SD299 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD299.class);

    /**
     * fixes data for both SD-299, SD-300 and SD-301
     */
    public static void fixingTppVisits(boolean includeStartedButNotFinishedServices) throws Exception {
        LOG.debug("Fixing TPP Visits");
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String bulkOperationName = "Fixing TPP visits (SD-299, SD-300, SD-301)";


            List<Service> services = serviceDal.getAll();
            for (Service service : services) {

                Map<String, String> tags = service.getTags();
                if (tags == null
                        || !tags.containsKey("TPP")) {
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
                fixingTppVisitsAtService(service);

                //wait three mins after each one, just to avoid getting the queues too large
                Thread.sleep(1000 * 60 * 3);

                //record as done
                setServiceDoneBulkOperation(service, bulkOperationName);
            }

            LOG.debug("Finished Fixing TPP Visits");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    /**
     * to fix the visits, simply re-queue all exchanges but filtering on the Visit file only
     */
    private static void fixingTppVisitsAtService(Service service) throws Exception {

        //pre-cache all schedule and slot info
        ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftware(service, MessageFormat.TPP_CSV);
        if (endpoint == null) {
            LOG.warn("No TPP endpoint found for " + service);
            return;
        }

        UUID serviceId = service.getId();
        UUID systemId = endpoint.getSystemUuid();

        ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
        List<UUID> exchangeIds = auditRepository.getExchangeIdsForService(serviceId, systemId);

        QueueHelper.ExchangeName exchangeName = QueueHelper.ExchangeName.INBOUND;

        Set<String> fileTypesToFilterOn = new HashSet<>();
        fileTypesToFilterOn.add("Visit");

        QueueHelper.postToExchange(exchangeIds, exchangeName, null, "SD-299, SD-300, SD-301", fileTypesToFilterOn, null);
    }
}