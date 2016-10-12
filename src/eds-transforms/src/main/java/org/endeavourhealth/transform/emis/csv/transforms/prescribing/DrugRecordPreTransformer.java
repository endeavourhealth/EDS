package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.DrugRecord;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.Map;

public class DrugRecordPreTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        DrugRecord parser = (DrugRecord)parsers.get(DrugRecord.class);

        while (parser.nextRecord()) {

            try {
                processLine(parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void processLine(DrugRecord parser,
                                    CsvProcessor csvProcessor,
                                    EmisCsvHelper csvHelper) throws Exception {

        String problemGuid = parser.getProblemObservationGuid();
        if (!Strings.isNullOrEmpty(problemGuid)) {

            //if this record is linked to a problem, store this relationship in the helper
            String drugRecordGuid = parser.getDrugRecordGuid();
            String patientGuid = parser.getPatientGuid();


            csvHelper.cacheProblemRelationship(problemGuid,
                    patientGuid,
                    drugRecordGuid,
                    ResourceType.MedicationStatement);
        }
    }

}