package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Appointment_Session;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class SessionTransformer {

    public static Map<String, Schedule> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        //first, process the CSV mapping staff to sessions
        Map<String, List<String>> sessionToStaffMap = SessionUserTransformer.transform(folderPath, csvFormat);

        Map<String, Schedule> fhirSessions = new HashMap<>();

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

    private static void createSchedule(Appointment_Session sessionParser, Map<String, Schedule> fhirSessions, Map<String, List<String>> sessionToStaffMap) throws Exception {

        //skip deleted sessions
        if (sessionParser.getDeleted()) {
            return;
        }

        Schedule fhirSession = new Schedule();
        fhirSession.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SCHEDULE));

        String sessionGuid = sessionParser.getAppointmnetSessionGuid();

        //ID is set on the resource when it's copied for use in the object store
        //fhirSession.setId(sessionGuid);

        //add to the map
        fhirSessions.put(sessionGuid, fhirSession);

        String locationGuid = sessionParser.getLocationGuid();
        Reference fhirReference = ReferenceHelper.createReference(ResourceType.Location, locationGuid);
        fhirSession.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.LOCATION, fhirReference));

        Date start = sessionParser.getStartDateTime();
        Date end = sessionParser.getEndDateTime();
        Period fhirPeriod = PeriodHelper.createPeriod(start, end);
        fhirSession.setPlanningHorizon(fhirPeriod);

        String sessionType = sessionParser.getSessionTypeDescription();
        fhirSession.addType(CodeableConceptHelper.createCodeableConcept(sessionType));

        String description = sessionParser.getDescription();
        fhirSession.setComment(description);

        List<String> staffGuids = sessionToStaffMap.get(sessionGuid);
        String firstStaffGuid = staffGuids.remove(0);
        fhirSession.setActor(ReferenceHelper.createReference(ResourceType.Practitioner, firstStaffGuid));

        for (String staffGuid: staffGuids) {
            Reference fhirStaffReference = ReferenceHelper.createReference(ResourceType.Practitioner, staffGuid);
            fhirSession.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ADDITIONAL_ACTOR, fhirStaffReference));
        }
    }
}
