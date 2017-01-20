package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;
import org.endeavourhealth.transform.common.AbstractCsvWriter;

import java.util.Date;

public class Patient extends AbstractCsvWriter {

    public Patient(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);
    }

    public void writeDelete(int id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public void writeUpsert(int id,
                          int organizationId,
                          int patientGenderId,
                          String pseudoId,
                          String nhsNumber,
                          Date dateOfBirth,
                          Date dateOfDeath,
                          String postcode) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                        "" + id,
                        "" + organizationId,
                        "" + patientGenderId,
                        pseudoId,
                        nhsNumber,
                        convertDate(dateOfBirth),
                        convertDate(dateOfDeath),
                        postcode);
    }

    @Override
    protected String[] getCsvHeaders() {
        return new String[] {
            "save_mode",
            "id",
            "organization_id",
            "patient_gender_id",
            "pseudo_id",
            "nhs_number",
            "date_of_birth",
            "date_of_death",
            "postcode"
        };
    }
}
