package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.Appointment_Session;
import org.endeavourhealth.transform.fhir.*;
import org.endeavourhealth.transform.fhir.ExtensionConverter;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class SessionTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        //first, process the CSV mapping staff to sessions
        Map<String, List<String>> sessionToStaffMap = SessionUserTransformer.transform(folderPath, csvFormat);

        Map<String, Schedule> fhirSessions = new HashMap<>();

        Appointment_Session sessionParser = new Appointment_Session(folderPath, csvFormat);
        try {
            while (sessionParser.nextRecord()) {
                createSchedule(sessionParser, csvProcessor, csvHelper, sessionToStaffMap);
            }
        } finally {
            sessionParser.close();
        }
    }

    private static void createSchedule(Appointment_Session sessionParser,
                                       CsvProcessor csvProcessor,
                                       EmisCsvHelper csvHelper,
                                       Map<String, List<String>> sessionToStaffMap) throws Exception {

        //skip deleted sessions
        if (sessionParser.getDeleted()) {
            return;
        }

        Schedule fhirSession = new Schedule();
        fhirSession.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SCHEDULE));

        String sessionGuid = sessionParser.getAppointmnetSessionGuid();
        fhirSession.setId(sessionGuid);

        String locationGuid = sessionParser.getLocationGuid();
        Reference fhirReference = csvHelper.createLocationReference(locationGuid);
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
        fhirSession.setActor(csvHelper.createPractitionerReference(firstStaffGuid));

        for (String staffGuid: staffGuids) {
            Reference fhirStaffReference = csvHelper.createPractitionerReference(staffGuid);
            fhirSession.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ADDITIONAL_ACTOR, fhirStaffReference));
        }

        csvProcessor.saveAdminResource(fhirSession);
    }
}
