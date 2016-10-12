package org.endeavourhealth.transform.emis.csv.transforms.prescribing;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.AbstractCsvParser;
import org.endeavourhealth.transform.emis.csv.schema.prescribing.IssueRecord;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.Map;

public class IssueRecordPreTransformer {

    public static void transform(String version,
                                 Map<Class, AbstractCsvParser> parsers,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {

        //unlike most of the other parsers, we don't handle record-level exceptions and continue, since a failure
        //to parse any record in this file it a critical error
        IssueRecord parser = (IssueRecord)parsers.get(IssueRecord.class);

        while (parser.nextRecord()) {

            try {
                processLine(parser, csvProcessor, csvHelper);
            } catch (Exception ex) {
                csvProcessor.logTransformRecordError(ex, parser.getCurrentState());
            }

        }
    }

    private static void processLine(IssueRecord parser,
                                    CsvProcessor csvProcessor,
                                    EmisCsvHelper csvHelper) throws Exception {

        String problemGuid = parser.getProblemObservationGuid();
        if (!Strings.isNullOrEmpty(problemGuid)) {

            //if this record is linked to a problem, store this relationship in the helper
            String issueRecordGuid = parser.getIssueRecordGuid();
            String patientGuid = parser.getPatientGuid();

            csvHelper.cacheProblemRelationship(problemGuid,
                    patientGuid,
                    issueRecordGuid,
                    ResourceType.MedicationOrder);
        }

    }

}
