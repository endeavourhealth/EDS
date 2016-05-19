package org.endeavourhealth.transform.emis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.schema.CsvMetadata;
import org.endeavourhealth.transform.emis.csv.transforms.EventTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.PatientTransformer;
import org.endeavourhealth.transform.emis.csv.transforms.PrescriptionTransformer;
import org.hl7.fhir.instance.model.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EmisCsvTransformer {

    public static String DATE_FORMAT = "dd/MM/yyyy";
    public static String TIME_FORMAT = "hh:mm:ss";

    public static Map<String, List<Resource>> transform(String folderPath) {


        Map<String, List<Resource>> ret = new HashMap<>();

        

        return ret;
    }


    public static Map<String, List<Resource>> transform(String patientCsvFile, String prescriptionCsvFile,
                                                        String eventCsvFile, String metadataCsvFile) throws Exception {

        //assume CSV files are in default format
        CSVParser patientCsv = CSVParser.parse(patientCsvFile, CSVFormat.DEFAULT);
        CSVParser prescriptionCsv = CSVParser.parse(prescriptionCsvFile, CSVFormat.DEFAULT);
        CSVParser eventCsv = CSVParser.parse(eventCsvFile, CSVFormat.DEFAULT);
        CSVParser metadataCsv = CSVParser.parse(metadataCsvFile, CSVFormat.DEFAULT);

        //extract the metadata, which we'll use to validate the clinical information
        List<CSVRecord> metadataRows = metadataCsv.getRecords();
        if (metadataRows.isEmpty()) {
            throw new TransformException("No metadata row");
        }
        CSVRecord csvRecord = metadataRows.get(0);
        int patientCount = Integer.parseInt(csvRecord.get(CsvMetadata.PATIENTCOUNT.getValue()));
        int eventCount = Integer.parseInt(csvRecord.get(CsvMetadata.EVENTCOUNT.getValue()));
        int prescriptionCount = Integer.parseInt(csvRecord.get(CsvMetadata.PRESCRIPTIONCOUNT.getValue()));

        //parse the patient data
        Map<String, List<Resource>> ret = new HashMap<>();

        PatientTransformer.transform(patientCsv, ret, patientCount);
        PrescriptionTransformer.transform(prescriptionCsv, ret, prescriptionCount);
        EventTransformer.transform(eventCsv, ret, eventCount);

        return ret;
    }

}
