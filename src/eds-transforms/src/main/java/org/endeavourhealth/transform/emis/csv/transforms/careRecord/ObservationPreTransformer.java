package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.careRecord.Observation;

public class ObservationPreTransformer {

    public static void transform(String version,
                                 String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {


        Observation parser = new Observation(version, folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {

                String parentGuid = parser.getParentObservationGuid();
                if (!Strings.isNullOrEmpty(parentGuid)) {

                    String observationGuid = parser.getObservationGuid();
                    String patientGuid = parser.getPatientGuid();

                    csvHelper.cacheObservationParentRelationship(parentGuid, patientGuid, observationGuid);
                }
            }
        } catch (Exception ex) {
            throw new TransformException(parser.getErrorLine(), ex);
        } finally {
            parser.close();
        }
    }


}
