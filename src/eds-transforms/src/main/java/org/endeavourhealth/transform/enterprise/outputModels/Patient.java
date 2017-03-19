package org.endeavourhealth.transform.enterprise.outputModels;

import org.apache.commons.csv.CSVFormat;

import java.math.BigDecimal;
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
                             int ageYears,
                             int ageMonths,
                             int ageWeeks,
                            Date dateOfDeath,
                            String postcodePrefix,
                            Long householdId,
                            String lsoaCode,
                            String lsoaName,
                            String msoaCode,
                            String msoaName,
                            BigDecimal townsendScore) throws Exception {

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
                        lsoaName,
                        msoaCode,
                        msoaName,
                        convertBigDecimal(townsendScore));
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
                                        String lsoaName,
                                        String msoaCode,
                                        String msoaName,
                                        BigDecimal townsendScore) throws Exception {

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
                lsoaName,
                msoaCode,
                msoaName,
                convertBigDecimal(townsendScore));
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
                    "lsoa_name",
                    "msoa_code",
                    "msoa_name",
                    "townsend_score"
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
                    "lsoa_name",
                    "msoa_code",
                    "msoa_name",
                    "townsend_score"
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
                    Date.class,
                    Date.class,
                    String.class,
                    Long.TYPE,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    BigDecimal.class
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
                    String.class,
                    String.class,
                    String.class,
                    BigDecimal.class
            };
        }
    }

}
