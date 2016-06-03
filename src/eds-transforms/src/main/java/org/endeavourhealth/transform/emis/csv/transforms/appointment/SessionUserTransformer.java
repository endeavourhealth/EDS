package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Appointment_Session;
import org.endeavourhealth.transform.emis.csv.schema.Appointment_SessionUser;
import org.hl7.fhir.instance.model.Schedule;

import java.util.*;

public class SessionUserTransformer {

    public static Map<UUID, List<UUID>> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        //we need to extract the staff UUIDs in proper order, as the first staff member in a session is
        //regarded as the main actor in the session. As the CSV may be out of order, we use a temporary
        //storage object to persist the processingId, which is then used to sort the records before returning.
        Map<UUID, List<UUIDAndProcessingOrder>> sessionToUserMap = new HashMap<>();

        Appointment_SessionUser parser = new Appointment_SessionUser(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createSessionUserMapping(parser, sessionToUserMap);
            }
        } finally {
            parser.close();
        }

        Map<UUID, List<UUID>> ret = new HashMap<>();

        Iterator<UUID> it = sessionToUserMap.keySet().iterator();
        while (it.hasNext()) {
            UUID sessionGuid = it.next();
            List<UUIDAndProcessingOrder> uuidAndProcessingOrders = sessionToUserMap.get(sessionGuid);

            //sort the uuid and processing order objects by processing order
            uuidAndProcessingOrders.sort(Comparator.naturalOrder());

            List<UUID> staffGuids = new ArrayList<>();
            ret.put(sessionGuid, staffGuids);

            for (UUIDAndProcessingOrder uuidAndProcessingOrder: uuidAndProcessingOrders) {
                staffGuids.add(uuidAndProcessingOrder.getUuid());
            }

        }

        return ret;
    }

    private static void createSessionUserMapping(Appointment_SessionUser sessionUserParser, Map<UUID, List<UUIDAndProcessingOrder>> sessionToUserMap) throws Exception {

        //ignore deleted records
        if (sessionUserParser.getdDeleted()) {
            return;
        }

        UUID sessionGuid = sessionUserParser.getSessionGuid();
        UUID userGuid = sessionUserParser.getUserInRoleGuid();
        int processingId = sessionUserParser.getProcessingId();

        List<UUIDAndProcessingOrder> l = sessionToUserMap.get(sessionGuid);
        if (l == null) {
            l = new ArrayList<>();
            sessionToUserMap.put(sessionGuid, l);
        }
        l.add(new UUIDAndProcessingOrder(userGuid, processingId));
    }
}

/**
 * temporary storage object to maintian the processing ID along with a staff UUID
 */
class UUIDAndProcessingOrder implements Comparable<UUIDAndProcessingOrder> {
    private UUID uuid = null;
    private int processingId = -1;

    public UUIDAndProcessingOrder(UUID uuid, int processingId) {
        this.uuid = uuid;
        this.processingId = processingId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getProcessingId() {
        return processingId;
    }

    @Override
    public int compareTo(UUIDAndProcessingOrder o) {
        if (processingId < o.getProcessingId()) {
            return -1;
        } else if (processingId > o.getProcessingId()) {
            return 1;
        } else {
            return 0;
        }
    }
}