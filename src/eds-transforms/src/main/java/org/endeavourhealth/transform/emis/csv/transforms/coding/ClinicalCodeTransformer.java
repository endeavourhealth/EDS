package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Coding_ClinicalCode;
import org.endeavourhealth.transform.emis.csv.schema.Coding_DrugCode;

import java.io.IOException;
import java.util.HashMap;

public abstract class ClinicalCodeTransformer {

    public static HashMap<Long, Object> transformClinicalCodes(String folderPath, CSVFormat csvFormat) throws IOException {

        HashMap<Long, Object> ret = new HashMap<Long, Object>();

        Coding_ClinicalCode codeParser = new Coding_ClinicalCode(folderPath, csvFormat);
        try {
            while (codeParser.nextRecord()) {

                Long codeId = codeParser.getCodeId();

                ret.put(codeId, null);
                //TODO - finish parsing clinical codes

                //
                // String read

/**

 public String getCodeTerm() {
 return super.getString(1);
 }
 public String getReadTermId() {
 return super.getString(2);
 }
 public String getNationalCodeCategory() {
 return super.getString(3);
 }
 public String getNationalCode() {
 return super.getString(4);
 }
 public String getNationalDescription() {
 return super.getString(5);
 }
 public Long getSnomedCTConceptId() {
 return super.getLong(6);
 }
 public Long getSnomedCTDescriptionId() {
 return super.getLong(7);
 }
 public String getEmisCodeCategoryDescription() {
 return super.getString(8);

 */

            }
        } finally {
            codeParser.close();
        }

        return ret;
    }



}
