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

        Schedule fhirSchedule = new Schedule();
        fhirSchedule.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SCHEDULE));

        String sessionGuid = sessionParser.getAppointmnetSessionGuid();
        fhirSchedule.setId(sessionGuid);

        String locationGuid = sessionParser.getLocationGuid();
        Reference fhirReference = csvHelper.createLocationReference(locationGuid);
        fhirSchedule.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.LOCATION, fhirReference));

        Date start = sessionParser.getStartDateTime();
        Date end = sessionParser.getEndDateTime();
        Period fhirPeriod = PeriodHelper.createPeriod(start, end);
        fhirSchedule.setPlanningHorizon(fhirPeriod);

        String sessionType = sessionParser.getSessionTypeDescription();
        fhirSchedule.addType(CodeableConceptHelper.createCodeableConcept(sessionType));

        String description = sessionParser.getDescription();
        fhirSchedule.setComment(description);

        List<String> staffGuids = sessionToStaffMap.get(sessionGuid);
        String firstStaffGuid = staffGuids.remove(0);
        fhirSchedule.setActor(csvHelper.createPractitionerReference(firstStaffGuid));

        for (String staffGuid: staffGuids) {
            Reference fhirStaffReference = csvHelper.createPractitionerReference(staffGuid);
            fhirSchedule.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.ADDITIONAL_ACTOR, fhirStaffReference));
        }

        csvProcessor.saveAdminResource(fhirSchedule);
    }
}
