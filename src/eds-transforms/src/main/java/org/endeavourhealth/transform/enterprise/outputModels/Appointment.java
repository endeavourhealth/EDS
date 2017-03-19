package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class Appointment extends AbstractEnterpriseCsvWriter {

    public Appointment(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
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
                            Long practitionerId,
                            Long scheduleId,
                            Date startDate,
                            Integer plannedDuration,
                            Integer actualDuration,
                            int statusId,
                            Integer patientWait,
                            Integer patientDelay,
                            Date sentIn,
                            Date left) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                "" + patientId,
                "" + personId,
                convertLong(practitionerId),
                convertLong(scheduleId),
                convertDate(startDate),
                convertInt(plannedDuration),
                convertInt(actualDuration),
                "" + statusId,
                convertInt(patientWait),
                convertInt(patientDelay),
                convertDate(sentIn),
                convertDate(left));
    }

    @Override
    public String[] getCsvHeaders() {
        return new String[] {
                "save_mode",
                "id",
                "organization_id",
                "patient_id",
                "person_id",
                "practitioner_id",
                "schedule_id",
                "start_date",
                "planned_duration",
                "actual_duration",
                "appointment_status_id",
                "patient_wait",
                "patient_delay",
                "sent_in",
                "left"
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
                Long.class,
                Long.class,
                Date.class,
                Integer.class,
                Integer.class,
                Integer.TYPE,
                Integer.class,
                Integer.class,
                Date.class,
                Date.class
        };
    }
}
