package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class Appointment extends AbstractEnterpriseCsvWriter {

    public Appointment(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                            int organisationId,
                            int patientId,
                            Integer practitionerId,
                            Integer scheduleId,
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
                convertInt(practitionerId),
                convertInt(scheduleId),
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
                Integer.TYPE,
                Integer.TYPE,
                Integer.TYPE,
                Integer.class,
                Integer.class,
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
