package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Coding_DrugCode;

import java.io.IOException;
import java.util.HashMap;

public class DrugCodeTransformer {


    public static HashMap<Long, DrugCode> transform(String folderPath, CSVFormat csvFormat) throws IOException {

        HashMap<Long, DrugCode> ret = new HashMap<>();

        Coding_DrugCode parser = new Coding_DrugCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, ret);
            }
        } finally {
            parser.close();
        }

        return ret;
    }

    private static void transform(Coding_DrugCode drugParser, HashMap<Long, DrugCode> map) {

        Long codeId = drugParser.getCodeId();
        String term = drugParser.getTerm();
        Long dmdId = drugParser.getDmdProductCodeId();

        map.put(codeId, new DrugCode(term, dmdId));
    }
}
