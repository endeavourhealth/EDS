package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.util.Date;

public class Patient extends AbstractEnterpriseCsvWriter {

    private boolean pseduonymised = false;

    public Patient(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat, boolean pseduonymised) throws Exception {
        super(fileName, csvFormat, dateFormat, timeFormat);

        this.pseduonymised = pseduonymised;
    }

    public void writeDelete(long id) throws Exception {

        super.printRecord(OutputContainer.DELETE,
                "" + id);
    }

    public boolean isPseduonymised() {
        return pseduonymised;
    }


    public void writeUpsertPseudonymised(long id,
                            long organizationId,
                            long personId,
                            int patientGenderId,
                            String pseudoId,
                            Integer ageYears,
                            Integer ageMonths,
                            Integer ageWeeks,
                            Date dateOfDeath,
                            String postcodePrefix,
                            Long householdId,
                            String lsoaCode,
                            String msoaCode) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                        "" + id,
                        "" + organizationId,
                        "" + personId,
                        "" + patientGenderId,
                        pseudoId,
                        convertInt(ageYears),
                        convertInt(ageMonths),
                        convertInt(ageWeeks),
                        convertDate(dateOfDeath),
                        postcodePrefix,
                        convertLong(householdId),
                        lsoaCode,
                        msoaCode);
    }


    public void writeUpsertIdentifiable(long id,
                                        long organizationId,
                                        long personId,
                                        int patientGenderId,
                                        String nhsNumber,
                                        Date dateOfBirth,
                                        Date dateOfDeath,
                                        String postcode,
                                        Long householdId,
                                        String lsoaCode,
                                        String msoaCode) throws Exception {

        super.printRecord(OutputContainer.UPSERT,
                "" + id,
                "" + organizationId,
                "" + personId,
                "" + patientGenderId,
                nhsNumber,
                convertDate(dateOfBirth),
                convertDate(dateOfDeath),
                postcode,
                convertLong(householdId),
                lsoaCode,
                msoaCode);
    }

    @Override
    public String[] getCsvHeaders() {
        if (isPseduonymised()) {
            return new String[] {
                    "save_mode",
                    "id",
                    "organization_id",
                    "person_id",
                    "patient_gender_id",
                    "pseudo_id",
                    "age_years",
                    "age_months",
                    "age_weeks",
                    "date_of_death",
                    "postcode_prefix",
                    "household_id",
                    "lsoa_code",
                    "msoa_code"
            };
        } else {
            return new String[]{
                    "save_mode",
                    "id",
                    "organization_id",
                    "person_id",
                    "patient_gender_id",
                    "nhs_number",
                    "date_of_birth",
                    "date_of_death",
                    "postcode",
                    "household_id",
                    "lsoa_code",
                    "msoa_code"
            };
        }
    }

    @Override
    public Class[] getColumnTypes() {
        if (isPseduonymised()) {
            return new Class[] {
                    String.class,
                    Long.TYPE,
                    Long.TYPE,
                    Long.TYPE,
                    Integer.TYPE,
                    String.class,
                    Integer.class,
                    Integer.class,
                    Integer.class,
                    Date.class,
                    String.class,
                    Long.TYPE,
                    String.class,
                    String.class
            };
        } else {
            return new Class[] {
                    String.class,
                    Long.TYPE,
                    Long.TYPE,
                    Long.TYPE,
                    Integer.TYPE,
                    String.class,
                    Date.class,
                    Date.class,
                    String.class,
                    Long.TYPE,
                    String.class,
                    String.class
            };
        }
    }

}
