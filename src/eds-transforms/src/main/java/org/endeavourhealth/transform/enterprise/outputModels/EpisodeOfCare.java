package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class EpisodeOfCare extends AbstractEnterpriseCsvWriter {

    public EpisodeOfCare(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                            int organisationId,
                            int patientId,
                            int registrationTypeId,
                            Date dateRegistered,
                            Date dateRegisteredEnd,
                            Integer usualGpPractitionerId) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                "" + patientId,
                "" + registrationTypeId,
                convertDate(dateRegistered),
                convertDate(dateRegisteredEnd),
                convertInt(usualGpPractitionerId));
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "organization_id",
                "patient_id",
                "registration_type_id",
                "date_registered",
                "date_registered_end",
                "usual_gp_practitioner_id"
        };
    }

    @Override
    public Class[] getColumnTypes() {
        return new Class[] {
                String.class,
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Date.class,
                Date.class,
                Integer.class
        };
    }
}
