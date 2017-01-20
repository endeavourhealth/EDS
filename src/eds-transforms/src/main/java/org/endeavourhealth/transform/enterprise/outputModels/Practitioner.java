package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

public class Practitioner extends AbstractCsvWriter {

    public Practitioner(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                            int organizaationId,
                            String name,
                            String roleCode,
                            String roleDesc) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organizaationId,
                name,
                roleCode,
                roleDesc);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "organization_id",
                "name",
                "role_code",
                "role_desc"
        };

    }
}
