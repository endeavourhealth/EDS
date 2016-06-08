package org.endeavourhealth.transform.emis.csv.transforms.coding;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.emis.csv.schema.Coding_ClinicalCode;
import org.endeavourhealth.transform.emis.csv.schema.Coding_DrugCode;

import java.io.IOException;
import java.util.HashMap;

public abstract class ClinicalCodeTransformer {

    public static HashMap<Long, ClinicalCode> transform(String folderPath, CSVFormat csvFormat) throws Exception {

        HashMap<Long, ClinicalCode> ret = new HashMap<>();

        Coding_ClinicalCode parser = new Coding_ClinicalCode(folderPath, csvFormat);
        try {
            while (parser.nextRecord()) {
                transform(parser, ret);
            }
        } finally {
            parser.close();
        }

        return ret;
    }

    private static void transform(Coding_ClinicalCode codeParser, HashMap<Long, ClinicalCode> map) throws Exception {

        Long codeId = codeParser.getCodeId();

        String emisTerm = codeParser.getTerm();
        String emisCode = codeParser.getReadTermId();
        String nationalCategory = codeParser.getNationalCodeCategory();
        String nationalCode = codeParser.getNationalCode();
        String nationalDescription = codeParser.getNationalDescription();
        Long snomedConceptId = codeParser.getSnomedCTConceptId();
        Long snomedDescriptionId = codeParser.getSnomedCTDescriptionId();
        String emisCategory = codeParser.getEmisCodeCategoryDescription();

        ClinicalCode c = new ClinicalCode(emisTerm, emisCode, emisCategory,
                nationalCategory, nationalCode, nationalDescription,
                snomedConceptId, snomedDescriptionId);

        map.put(codeId, c);
    }

}
