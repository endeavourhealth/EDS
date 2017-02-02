package org.endeavourhealth.transform.emis.csv.transforms.appointment;

import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.appointment.SessionUser;

import java.util.Map;

public class SessionUserTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        AbstractCsvParser parser = parsers.get(SessionUser.class);

        while (parser.nextRecord()) {

            try {
                createSessionUserMapping((SessionUser)parser, fhirResourceFiler, csvHelper);
            } catch (Exception ex) {
                throw new TransformException(parser.getCurrentState().toString(), ex);
            }
        }
    }

    private static void createSessionUserMapping(SessionUser parser,
                                                 FhirResourceFiler fhirResourceFiler,
                                                 EmisCsvHelper csvHelper) throws Exception {

        boolean deleted = parser.getdDeleted();
        String sessionGuid = parser.getSessionGuid();
        String userGuid = parser.getUserInRoleGuid();

        csvHelper.cacheSessionPractitionerMap(sessionGuid, userGuid, deleted);
    }
}

