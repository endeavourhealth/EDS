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

    public static String DATE_FORMAT = "yyyyMMdd";

    /**
     * Assumed file formats:
     *
     * CsvPatient CSV:
     *      	ID	int not null,
     *      	CARERECORDID	uniqueidentifier not null,
     *      	PracticeCode	varchar(20) not null,
     *      	AGE	int not null,
     *      	DATEOFBIRTH	datetime	not null,
     *      	SEX	char	not null,
     *      	REGDATE	datetime	not null,
     *      	REGSTATUS	char	not null,
     *      	DEREGDATE	datetime null,
     *      	DEREGREASON	varchar(60)	null,
     *      	DATEOFDEATH	datetime	null,
     *      	MOSAICCODE	varchar(100) null,
     *      	NHSNUMBER	varchar(50) null,
     *      	POSTCODE	varchar(8)	not null,
     *      	YYYYMMOFBIRTH	varchar(6)	not null,
     *      	REGGP	varchar(263)	not null,
     *      	USUALGP	varchar(263)	not null,
     *      	FORENAME	varchar(297)	not null,
     *      	SURNAME	varchar(1000)	not null
     *
     * CsvPrescription CSV:
     *      	ID	int	not null,
     *      	CARERECORDID	uniqueidentifier	not null,
     *      	PracticeCode	varchar(20) not null,
     *      	ISSUEDATE	datetime	not null,
     *      	ISSUETYPE	varchar(50) not null,
     *      	READCODE varchar(50)	null,
     *      	DRUG varchar(510)	null,
     *      	SUPPLY varchar(max) not null,
     *      	EMISCODE varchar(50)	null,
     *      	[STATUS] char	not null,
     *      	[LAST ISSUE DATE] datetime null,
     *      	DIRECTIONS	varchar(400)	null
     *
     * CsvEvent CSV:
     *      	ID	int not null,
     *      	CARERECORDID	uniqueidentifier not null,
     *      	PracticeCode	varchar(20) not null,
     *      	ODATE	datetime	not null,
     *      	READCODE	varchar(50)	null,
     *      	READTERM	varchar(200)	null,
     *      	NUMRESULT	decimal(19,3)	null,
     *      	NUMRESULT2	decimal(19,3)	null
     *
     * Meta CSV:
     *      	AUDITDATE	datetime	not null,
     *      	RUNDATE	datetime	not null,
     *      	PATIENTCOUNT	int	not null,
     *      	EVENTCOUNT	int	not null,
     *      	PRESCRIPTIONCOUNT	int	not null
     */

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
