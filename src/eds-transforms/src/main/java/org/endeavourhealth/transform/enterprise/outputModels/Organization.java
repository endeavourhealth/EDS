package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

public class Organization extends AbstractEnterpriseCsvWriter {

    public Organization(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(long id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(long id,
                          String odsCode,
                          String name,
                          String typeCode,
                          String typeDesc,
                          String postcode,
                          Long parentOrganisationId) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                odsCode,
                name,
                typeCode,
                typeDesc,
                postcode,
                convertLong(parentOrganisationId));
    }

    @Override
    public String[] getCsvHeaders() {
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

    @Override
    public Class[] getColumnTypes() {
        return new Class[] {
                String.class,
                Long.TYPE,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                Long.class
        };
    }

}
