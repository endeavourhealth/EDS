package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

public class Organization extends AbstractCsvWriter {

    public Organization(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                          String odsCode,
                          String name,
                          String typeCode,
                          String typeDesc,
                          String postCode,
                          Integer parentOrganisationId) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                odsCode,
                name,
                typeCode,
                typeDesc,
                postCode,
                convertInt(parentOrganisationId));
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "ods_code",
                "name",
                "type_code",
                "type_desc",
                "postcode",
                "parent_organization_id"
        };
    }
}
