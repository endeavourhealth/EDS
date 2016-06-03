package org.endeavourhealth.transform.emis.csv.transforms.careRecord;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Observation;
import org.endeavourhealth.transform.emis.csv.schema.CareRecord_Problem;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.Map;

public class ProblemTransformer {

    public static void transform(String folderPath, CSVFormat csvFormat, Map<String, List<Resource>> fhirResources) throws Exception {

        CareRecord_Problem parser = new CareRecord_Problem(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                createProblem(parser, fhirResources);
            }
        } finally {
            parser.close();
        }
    }

    private static void createProblem(CareRecord_Problem problemParser, Map<String, List<Resource>> fhirResources) throws Exception {

    }
}
