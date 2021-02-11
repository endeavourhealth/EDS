package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SD289 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD289.class);

    /**
     * fixes data SD-289
     */
    public static void fixEmisDeletedSlots(boolean includeStartedButNotFinishedServices, boolean testMode, String odsCodeRegex) {
        LOG.debug("Fixing Emis Deleted Slots at " + odsCodeRegex + " test mode = " + testMode);
        try {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            String bulkOperationName = "Fixing Emis deleted slots (SD-289)";

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
                    //check to see if already done this services
                    if (isServiceStartedOrDoneBulkOperation(service, bulkOperationName, includeStartedButNotFinishedServices)) {
                        continue;
                    }
                }

                LOG.debug("Doing " + service);
                fixEmisDeletedSlotsAtService(testMode, service);

                //record as done
                if (!testMode) {
                    setServiceDoneBulkOperation(service, bulkOperationName);
                }

            }

            LOG.debug("Finished Fixing Emis Deleted Slots at " + odsCodeRegex + " test mode = " + testMode);

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static void fixEmisDeletedSlotsAtService(boolean testMode, Service service) throws Exception {

        //If we get an update to a slot to delete it, and there’s no patient GUID, then cancel any appts that were in the slot
        // (implication is that the patient appt was cancelled before the slot was booked).

    }

}