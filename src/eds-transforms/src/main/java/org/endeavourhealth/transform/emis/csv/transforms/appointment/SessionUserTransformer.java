package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.appointment.SessionUser;

import java.util.Map;

public class SessionUserTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        SessionUser parser = (SessionUser)parsers.get(SessionUser.class);

        while (parser.nextRecord()) {

            try {
                createSessionUserMapping(parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void createSessionUserMapping(SessionUser parser,
                                                 CsvProcessor csvProcessor,
                                                 EmisCsvHelper csvHelper) throws Exception {

        boolean deleted = parser.getdDeleted();
        String sessionGuid = parser.getSessionGuid();
        String userGuid = parser.getUserInRoleGuid();

        csvHelper.cacheSessionPractitionerMap(sessionGuid, userGuid, deleted);
    }
}

