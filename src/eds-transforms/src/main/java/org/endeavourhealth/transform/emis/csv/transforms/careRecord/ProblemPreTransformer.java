package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.endeavourhealth.transform.common.FhirResourceFiler;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Problem;

import java.util.Map;

public class ProblemPreTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 FhirResourceFiler fhirResourceFiler,
                                 EmisCsvHelper csvHelper) throws Exception {

        AbstractCsvParser parser = parsers.get(Problem.class);
        while (parser.nextRecord()) {

            try {
                processLine((Problem) parser, fhirResourceFiler, csvHelper, version);
            } catch (Exception ex) {
                fhirResourceFiler.logTransformRecordError(ex, parser.getCurrentState());
            }
        }
    }

    private static void processLine(Problem parser,
                                       FhirResourceFiler fhirResourceFiler,
                                       EmisCsvHelper csvHelper,
                                       String version) throws Exception {

        //all this transformer does is cache the observation GUIDs of problems, so
        //that we know what is a problem when we run the observation pre-transformer
        String patientGuid = parser.getPatientGuid();
        String observationGuid = parser.getObservationGuid();
        csvHelper.cacheProblemObservationGuid(patientGuid, observationGuid, null);
    }
}
