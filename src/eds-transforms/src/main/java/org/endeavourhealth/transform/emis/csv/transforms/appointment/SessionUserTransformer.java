package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.FutureException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.appointment.SessionUser;

public class SessionUserTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        SessionUser parser = new SessionUser(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createSessionUserMapping(parser, csvProcessor, csvHelper);
            }
        } catch (FutureException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
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

