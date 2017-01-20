package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

import java.util.Date;

public class Schedule extends AbstractCsvWriter {

    public Schedule(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                            int organisationId,
                            Integer practitionerId,
                            Date startDate,
                            String type,
                            String location) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                convertInt(practitionerId),
                convertDate(startDate),
                type,
                location);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "organization_id",
                "practitioner_id",
                "start_date",
                "type",
                "location"
        };
    }
}
