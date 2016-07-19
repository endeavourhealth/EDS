package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.CsvProcessor;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.hl7.fhir.instance.model.ResourceType;

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
