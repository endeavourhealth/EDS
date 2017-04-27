package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class EpisodeOfCare extends AbstractEnterpriseCsvWriter {

    public EpisodeOfCare(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(long id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(long id,
                            long organisationId,
                            long patientId,
                            long personId,
                            Integer registrationTypeId,
                            Date dateRegistered,
                            Date dateRegisteredEnd,
                            Long usualGpPractitionerId) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                "" + patientId,
                "" + personId,
                convertInt(registrationTypeId),
                convertDate(dateRegistered),
                convertDate(dateRegisteredEnd),
                convertLong(usualGpPractitionerId));
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "organization_id",
                "patient_id",
                "person_id",
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
                Long.TYPE,
                Long.TYPE,
                Long.TYPE,
                Long.TYPE,
                Integer.class,
                Date.class,
                Date.class,
                Long.class
        };
    }
}
