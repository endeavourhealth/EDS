package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;

public class ObservationPreTransformer {

    public static void transform(String folderPath,
                                 CSVFormat csvFormat,
                                 CsvProcessor csvProcessor,
                                 EmisCsvHelper csvHelper) throws Exception {


        CareRecord_Observation parser = new CareRecord_Observation(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {

                String parentGuid = parser.getParentObservationGuid();
                if (!Strings.isNullOrEmpty(parentGuid)) {
                    String patientGuid = parser.getPatientGuid();
                    String observationGuid = parser.getObservationGuid();

                    //TODO - only cache the child relationships where the child will become an Observation resource itself
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
