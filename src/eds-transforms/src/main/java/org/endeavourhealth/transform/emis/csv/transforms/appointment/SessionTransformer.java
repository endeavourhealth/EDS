package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Appointment_Session;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionHelper;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class SessionTransformer {

    public static Map<UUID, Schedule> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        //first, process the CSV mapping staff to sessions
        Map<UUID, List<UUID>> sessionToStaffMap = SessionUserTransformer.transform(folderPath, csvFormat);

        Map<UUID, Schedule> fhirSessions = new HashMap<>();

        Appointment_Session sessionParser = new Appointment_Session(folderPath, csvFormat);
        try {
            while (sessionParser.nextRecord()) {
                createSchedule(sessionParser, fhirSessions, sessionToStaffMap);
            }
        } finally {
            sessionParser.close();
        }

        return fhirSessions;
    }

    private static void createSchedule(Appointment_Session sessionParser, Map<UUID, Schedule> fhirSessions, Map<UUID, List<UUID>> sessionToStaffMap) throws Exception {

        //skip deleted sessions
        if (sessionParser.getDeleted()) {
            return;
        }

        Schedule fhirSession = new Schedule();
        fhirSession.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SCHEDULE));

        UUID sessionGuid = sessionParser.getAppointmnetSessionGuid();
        fhirSession.setId(sessionGuid.toString());

        //add to the map
        fhirSessions.put(sessionGuid, fhirSession);

        UUID locationGuid = sessionParser.getLocationGuid();
        Reference fhirReference = ReferenceHelper.createReference(ResourceType.Location, locationGuid.toString());
        fhirSession.addExtension(ExtensionHelper.createExtension(FhirExtensionUri.LOCATION, fhirReference));

        Date start = sessionParser.getStartDateTime();
        Date end = sessionParser.getEndDateTime();
        Period fhirPeriod = PeriodHelper.createPeriod(start, end);
        fhirSession.setPlanningHorizon(fhirPeriod);

        String sessionType = sessionParser.getSessionTypeDescription();
        fhirSession.addType(CodeableConceptHelper.createCodeableConcept(sessionType));

        String description = sessionParser.getDescription();
        fhirSession.setComment(description);

        List<UUID> staffGuids = sessionToStaffMap.get(sessionGuid);
        UUID firstStaffGuid = staffGuids.remove(0);
        fhirSession.setActor(ReferenceHelper.createReference(ResourceType.Practitioner, firstStaffGuid.toString()));

        for (UUID staffGuid: staffGuids) {
            Reference fhirStaffReference = ReferenceHelper.createReference(ResourceType.Practitioner, staffGuid.toString());
            fhirSession.addExtension(ExtensionHelper.createExtension(FhirExtensionUri.ADDITIONAL_ACTOR, fhirStaffReference));
        }
    }
}
