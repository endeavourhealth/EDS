package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

import java.util.Date;

public class Appointment extends AbstractCsvWriter {

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
                            Integer encounterId,
                            Integer practitionerId,
                            Date clinicalEffectiveDate,
                            Integer datePrecisionId,
                            Long snomedConceptId,
                            String originalCode,
                            String originalTerm) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organisationId,
                "" + patientId,
                convertInt(encounterId),
                convertInt(practitionerId),
                convertDate(clinicalEffectiveDate),
                convertInt(datePrecisionId),
                convertLong(snomedConceptId),
                originalCode,
                originalTerm);
    }

    @Override
    protected String[] getCsvHeaders() {
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

        /**
         *                     <xs:element name="" type="xs:int"/>
         <xs:element name="" type="xs:int"/>
         <xs:element name="" type="xs:int" minOccurs="0"/>
         <xs:element name="" type="xs:int" minOccurs="0"/>
         <xs:element name="" type="xs:dateTime" minOccurs="0"/>
         <xs:element name="" type="xs:int" minOccurs="0"/>
         <xs:element name="" type="xs:int" minOccurs="0"/>
         <xs:element name="" type="xs:int"/>
         <xs:element name="" type="xs:int" minOccurs="0"/>
         <xs:element name="" type="xs:int" minOccurs="0"/>
         <xs:element name="" type="xs:dateTime" minOccurs="0"/>
         <xs:element name="" type="xs:dateTime" minOccurs="0"/>
         */
    }
}
