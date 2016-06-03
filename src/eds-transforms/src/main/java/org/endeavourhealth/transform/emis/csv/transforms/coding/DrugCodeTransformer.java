package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Coding_DrugCode;

import java.io.IOException;
import java.util.HashMap;

public class DrugCodeTransformer {


    public static HashMap<Long, Object> transformDrugCodes(String folderPath, CSVFormat csvFormat) throws IOException {

        HashMap<Long, Object> ret = new HashMap<Long, Object>();

        Coding_DrugCode drugParser = new Coding_DrugCode(folderPath, csvFormat);
        try {
            while (drugParser.nextRecord()) {

                Long codeId = drugParser.getCodeId();

                ret.put(codeId, null);
                //TODO - finish parsing clinical codes

/**
 *

 public String getTerm() {
 return super.getString(1);
 }
 public Long getDmdProductCodeId() {
 return super.getLong(2);

 */
            }
        } finally {
            drugParser.close();
        }

        return ret;
    }
}
