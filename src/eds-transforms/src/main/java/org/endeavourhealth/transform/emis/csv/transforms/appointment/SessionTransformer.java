package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FutureException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.appointment.Session;
import org.endeavourhealth.transform.fhir.*;
import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.Period;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Schedule;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        //first, process the CSV mapping staff to sessions
        Map<String, List<String>> sessionToStaffMap = SessionUserTransformer.transform(version, folderPath, csvFormat);

        Map<String, Schedule> fhirSessions = new HashMap<>();

        Session parser = new Session(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createSchedule(parser, csvProcessor, csvHelper, sessionToStaffMap);
            }
        } catch (FutureException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }

    private static void createSchedule(Session sessionParser,
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
        fhirSchedule.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.SCHEDULE_LOCATION, fhirReference));

        Date start = sessionParser.getStartDateTime();
        Date end = sessionParser.getEndDateTime();
        Period fhirPeriod = PeriodHelper.createPeriod(start, end);
        fhirSchedule.setPlanningHorizon(fhirPeriod);

        String sessionType = sessionParser.getSessionTypeDescription();
        fhirSchedule.addType(CodeableConceptHelper.createCodeableConcept(sessionType));

        String category = sessionParser.getSessionCategoryDisplayName();
        fhirSchedule.setComment(category); //the FHIR description of "Comment" seems approproate to store the category

        String description = sessionParser.getDescription();
        fhirSchedule.setComment(description);

        List<String> staffGuids = sessionToStaffMap.get(sessionGuid);

        //in production data, there should always be a staff GUID for a session, but the
        //test data contains at least one session that doesn, so this check is required
        if (staffGuids != null && staffGuids.isEmpty()) {
            String firstStaffGuid = staffGuids.remove(0);
            fhirSchedule.setActor(csvHelper.createPractitionerReference(firstStaffGuid));

            for (String staffGuid: staffGuids) {
                Reference fhirStaffReference = csvHelper.createPractitionerReference(staffGuid);
                fhirSchedule.addExtension(ExtensionConverter.createExtension(FhirExtensionUri.SCHEDULE_ADDITIONAL_ACTOR, fhirStaffReference));
            }
        }

        csvProcessor.saveAdminResource(fhirSchedule);
    }
}
