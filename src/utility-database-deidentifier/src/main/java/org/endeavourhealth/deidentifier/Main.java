package org.endeavourhealth.deidentifier;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.Resources;
import org.hl7.fhir.instance.model.Enumerations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String RESOURCE_FIRST_NAMES = "first_names.txt";
    private static final String RESOURCE_LAST_NAMES = "last_names.txt";
    private static final String RESOURCE_POSTCODE_PREFIXES = "postcode_prefixes.txt";
    private static final String RESOURCE_LSOA_CODES = "lsoa_codes.txt";
    private static final String RESOURCE_MSOA_CODES = "msoa_codes.txt";
    private static final String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final String STEP_TEMP_TABLE = "temp_deidentification_steps";
    private static final String PERSON_TEMP_TABLE = "temp_person_deidentification";
    private static final String PERSON_TEMP_BATCH_TABLE = "temp_person_batch_deidentification";
    private static final String PERSON_TEMP_ORG_BATCH_TABLE = "temp_person_org_batch_deidentification";
    private static final String LSOA_TEMP_TABLE = "temp_lsoa_deidentification";
    private static final String MSOA_TEMP_TABLE = "temp_msoa_deidentification";
    private static final String POSTCODE_TEMP_TABLE = "temp_postcode_deidentification";

    private static HikariDataSource connectionPool = null;

    private static final String STEP_PRACTITIONER_NAMES = "PractitionerNames";
    private static final String STEP_ORG_DETAILS = "OrgDetails";
    private static final String STEP_DELETE_APPOINTMENTS = "DeleteAppts";
    private static final String STEP_DELETE_OLD_PATIENTS = "DeleteOldPatients";
    private static final String STEP_DELETE_GENDER_PATIENTS = "DeleteGenderPatients";
    private static final String STEP_DELETE_OBSERVATIONS = "DeleteObservations";
    private static final String STEP_DELETE_ALLERGIES = "DeleteAllergies";
    private static final String STEP_DELETE_REFERRALS = "DeleteReferrals";
    private static final String STEP_DELETE_PROCEDURE_REQUESTS = "DeleteProcedureRequests";
    private static final String STEP_DELETE_MEDICATION_STATEMENTS = "DeleteMedicationStatements";
    private static final String STEP_DELETE_MEDICATION_ORDERS = "DeleteMedicationOrders";
    private static final String STEP_PREPARE_LSOA_CODES = "PrepareLsoaCodes";
    private static final String STEP_PREPARE_MSOA_CODES = "PrepareMsoaCodes";
    private static final String STEP_PREPARE_POSTCODES = "PreparePostcodes";
    private static final String STEP_PREPARE_PATIENTS = "PreparePatients";
    private static final String STEP_CHANGE_PATIENT_DATES = "AdjustDates";

    /**
     * utility to apply level 2 de-identification to a pseudonymised Compass database
     *
     * It connects to the database specificed by the connection string, then applies the
     * level 2 de-identification process as documented in:
     * \Google Drive\Endeavour Health CT\Old Drive\Endeavour Health\Products (1)\Discovery (EDS)\Specifications\De-identification levels for Subscribers (Version 1-1).docx
     * =================================================================================
     *
     * Parameters:
     * <db_connection_url> <driver_class> <db_username> <db_password>
     */
    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize("EnterpriseDeidentifier");

        if (args.length != 4) {
            LOG.error("Expecting four parameters:");
            LOG.error("<db_connection_url> <driver_class> <db_username> <db_password>");
            System.exit(0);
            return;
        }

        String url = args[0];
        String driverClass = args[1];
        String user = args[2];
        String pass = args[3];

        LOG.info("Starting Level 2 De-identification on " + url);

        try {
            LOG.info("Opening connection pool");
            openConnectionPool(url, driverClass, user, pass);
            LOG.info("...Done");
        } catch (Exception ex) {
            LOG.error("Failed to init connection pool", ex);
            System.exit(0);
            return;
        }

        try {
            LOG.info("Creating table of steps if needed");
            createTempTableIfNotExists(STEP_TEMP_TABLE);
            LOG.info("...Done");
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_PRACTITIONER_NAMES)) {
                LOG.info("Randomising practitioner names");
                randomisePractitionerNames();
                LOG.info("...Done");
                stepDone(STEP_PRACTITIONER_NAMES);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_ORG_DETAILS)) {
                LOG.info("Randomising organisation details");
                randomiseOrgDetails();
                LOG.info("...Done");
                stepDone(STEP_ORG_DETAILS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_APPOINTMENTS)) {
                LOG.info("Deleting all appointment data");
                deleteAllAppoointments();
                LOG.info("...Done");
                stepDone(STEP_DELETE_APPOINTMENTS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_OLD_PATIENTS)) {
                LOG.info("Deleting patients over 100yrs old");
                deletePatientsOlderThan(100);
                LOG.info("...Done");
                stepDone(STEP_DELETE_OLD_PATIENTS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_GENDER_PATIENTS)) {
                LOG.info("Deleting patients with non-male non-female gender");
                deletePatientsWithOtherGenders();
                LOG.info("...Done");
                stepDone(STEP_DELETE_GENDER_PATIENTS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        int populationCount;
        try {
            LOG.info("Getting population count");
            populationCount = getPopulationCount();
            LOG.info("...Done = " + populationCount);
        } catch (Exception ex) {
            LOG.error("Failed to get population count", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_OBSERVATIONS)) {
                LOG.info("Deleting low incidence observations");
                deleteLowIncidenceRecords(populationCount, "observation");
                LOG.info("...Done");
                stepDone(STEP_DELETE_OBSERVATIONS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_ALLERGIES)) {
                LOG.info("Deleting low incidence allergies");
                deleteLowIncidenceRecords(populationCount, "allergy_intolerance");
                LOG.info("...Done");
                stepDone(STEP_DELETE_ALLERGIES);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_REFERRALS)) {
                LOG.info("Deleting low incidence referrals");
                deleteLowIncidenceRecords(populationCount, "referral_request");
                LOG.info("...Done");
                stepDone(STEP_DELETE_REFERRALS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_PROCEDURE_REQUESTS)) {
                LOG.info("Deleting low incidence procedure requests");
                deleteLowIncidenceRecords(populationCount, "procedure_request");
                LOG.info("...Done");
                stepDone(STEP_DELETE_PROCEDURE_REQUESTS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_MEDICATION_ORDERS)) {
                LOG.info("Deleting low incidence medication_orders");
                deleteLowIncidenceMedicationOrders(populationCount);
                LOG.info("...Done");
                stepDone(STEP_DELETE_MEDICATION_ORDERS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_DELETE_MEDICATION_STATEMENTS)) {
                LOG.info("Deleting low incidence medication_statements");
                deleteLowIncidenceMedicationStatements(populationCount);
                LOG.info("...Done");
                stepDone(STEP_DELETE_MEDICATION_STATEMENTS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_PREPARE_LSOA_CODES)) {
                LOG.info("Preparing LSOA codes");
                createLookupTempTable("lsoa_code", RESOURCE_LSOA_CODES, LSOA_TEMP_TABLE);
                LOG.info("...Done");
                stepDone(STEP_PREPARE_LSOA_CODES);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_PREPARE_MSOA_CODES)) {
                LOG.info("Preparing MSOA codes");
                createLookupTempTable("msoa_code", RESOURCE_MSOA_CODES, MSOA_TEMP_TABLE);
                LOG.info("...Done");
                stepDone(STEP_PREPARE_MSOA_CODES);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_PREPARE_POSTCODES)) {
                LOG.info("Preparing postcodes");
                createLookupTempTable("postcode_prefix", RESOURCE_POSTCODE_PREFIXES, POSTCODE_TEMP_TABLE);
                LOG.info("...Done");
                stepDone(STEP_PREPARE_POSTCODES);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_PREPARE_PATIENTS)) {
                LOG.info("Preparing patients");
                createPersonTempTable(populationCount);
                LOG.info("...Done");
                stepDone(STEP_PREPARE_PATIENTS);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            if (stepNotDone(STEP_CHANGE_PATIENT_DATES)) {
                LOG.info("Adjusting dates");
                updatePatients();
                LOG.info("...Done");
                stepDone(STEP_CHANGE_PATIENT_DATES);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }

        try {
            LOG.info("Dropping temp tables");
            dropTempTables();
            LOG.info("...Done");
        } catch (Exception ex) {
            LOG.error("", ex);
            System.exit(0);
            return;
        }



        //HAVE TEMP TABLE OF STEPS SO WE CAN RESUME FROM ANY STEP!!!

        //DONE LSOA code
        //DONE MSOA code
        //DONE postcode prefix
        //DONE person/patient household ID
        //DONE person/patient pseudo ID
        //DONE practitioner names
        //org names
        //org code
        //org postcode

        //DONE age
        //DONE if > 5 - increase age by up to 5 yrs
        //DONE if 2-5 - increase age by up to 2 yrs
        //DONE if < 2 - increase age by up to 6 months

        //DONE data of death
        //DONE event dates
        //DONE move back by between 0 and dob shift

        //DONE appointments - remove
        //DONE need to delete low incidence referrals and allergies!
        //DONE 1.	All observations whose code frequency is less than 0.1 %
        //DONE 2.	All medication entries whose medication code frequency is less than 0.1
        //DONE 3.	All patients whose age at time of anonymisation is >100 years
        //DONE 4.	All patients with indeterminate gender
    }



    private static void stepDone(String step) throws Exception {
        String sql = "INSERT INTO " + STEP_TEMP_TABLE + " VALUES ('" + step + "');";
        executeUpdate(sql);
    }

    private static boolean stepNotDone(String step) throws Exception {
        String sql = "SELECT 1 FROM " + STEP_TEMP_TABLE + " WHERE code = '" + step + "';";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);
        boolean ret = !rs.next();
        rs.close();
        connection.close();
        return ret;
    }

    private static void randomiseOrgDetails() throws Exception {

        List<String> postcodes = getResourceAsList(RESOURCE_POSTCODE_PREFIXES);
        Random r = new Random();

        Map<String, Integer> hmCounts = new HashMap<>();

        String sql = "SELECT id, type_desc FROM organization;";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);
        while (rs.next()) {

            long id = rs.getLong(1);
            String typeDesc = rs.getString(2);

            Integer count = hmCounts.get(typeDesc);
            if (count == null) {
                count = new Integer(1);
            } else {
                count = new Integer(count.intValue() + 1);
            }
            hmCounts.put(typeDesc, count);

            String name = typeDesc + " " + count;
            String odsCode = createRandomId(6);
            String postcode = postcodes.get(r.nextInt(postcodes.size()));

            sql = "UPDATE organization"
                    + " SET name = '" + name + "', ods_code = '" + odsCode + "', postcode = '" + postcode + "'"
                    + " WHERE id = " + id;
            executeUpdate(sql);
        }

        rs.close();
        connection.close();
    }

    private static void dropTempTables() throws Exception {

        //drop the table now we're done
        String sql = "DROP TABLE " + PERSON_TEMP_TABLE;
        executeUpdate(sql);

        sql = "DROP TABLE " + LSOA_TEMP_TABLE;
        executeUpdate(sql);

        sql = "DROP TABLE " + MSOA_TEMP_TABLE;
        executeUpdate(sql);

        sql = "DROP TABLE " + POSTCODE_TEMP_TABLE;
        executeUpdate(sql);

        sql = "DROP TABLE " + STEP_TEMP_TABLE;
        executeUpdate(sql);
    }

    private static void createLookupTempTable(String personField, String resourceFile, String tempTableName) throws Exception {

        //make sure to drop any existing temp table
        String sql = "DROP TABLE IF EXISTS " + tempTableName + ";";
        executeUpdate(sql);

        //create the new one
        sql = "CREATE TABLE " + tempTableName + " (code varchar(255));";
        executeUpdate(sql);

        //get the count of distinct values, so we have an idea of the distribution
        sql = "SELECT COUNT(DISTINCT " + personField + ") FROM person;";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);
        rs.next();
        int count = rs.getInt(1);

        rs.close();
        connection.close();

        List<String> resourceAsList = getResourceAsList(resourceFile);
        Random r = new Random();

        for (int i=0; i<count; i++) {
            int index = r.nextInt(resourceAsList.size());
            String prefix = resourceAsList.get(index);

            sql = "INSERT INTO " + tempTableName + " VALUES ('" + prefix + "');";
            executeUpdate(sql);
        }
    }

    /*private static void createTempTable(String field, String lookupTable, String tempTableName) throws Exception {

        //make sure to drop any existing temp table
        String sql = "DROP TABLE IF EXISTS " + tempTableName + ";";
        executeUpdate(sql);

        //create the new one
        sql = "CREATE TABLE " + tempTableName + " (code varchar(255));";
        executeUpdate(sql);

        //get the count of distinct values, so we have an idea of the distribution
        sql = "SELECT COUNT(DISTINCT " + field + ") FROM person;";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);
        rs.next();
        int count = rs.getInt(1);

        rs.close();
        connection.close();

        //select the replacements into the table
        sql = "INSERT INTO " + tempTableName
                + " SELECT " + field + " FROM " + lookupTable + " ORDER BY rand() LIMIT " + count + ";";
        executeUpdate(sql);
    }*/

    private static boolean createTempTableIfNotExists(String tempTableName) throws Exception {

        //test if our temp table is already there
        Connection connection = getConnection();
        try {
            String sql = "SELECT 1 FROM " + tempTableName;
            ResultSet rs = executeQuery(connection, sql);
            rs.close();

            //if we get here, the table exists
            return false;
        } catch (SQLException ex) {
            //if we get an exception, the table doesn't exist
            //create the temp table
            String sql = "CREATE TABLE " + tempTableName + " (code varchar(255));";
            executeUpdate(sql);

            return true;
        } finally {
            connection.close();
        }
    }

    private static List<String> retrieveTempTableRows(String tempTableName) throws Exception {
        String sql = "SELECT code FROM " + tempTableName;
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        List<String> ret = new ArrayList<>();

        while (rs.next()) {
            String s = rs.getString(1);
            ret.add(s);
        }

        rs.close();
        connection.close();

        return ret;
    }

    /*private static void updatePatients() throws Exception {

        //create table if necessary
        String sql = "SELECT 1 FROM " + PERSON_TEMP_TABLE;

        Connection connection = getConnection();
        try {
            //test if the table exists by selecting from it and catching the error,
            //which will work on all databases, unlike doing something specific for MySQL
            executeQuery(connection, sql);

        } catch (SQLException se) {

            LOG.info("Creating adjustments table");

            sql = "CREATE TABLE " + PERSON_TEMP_TABLE + " ("
                    + " person_id bigint,"
                    + " age_years int,"
                    + " age_months int,"
                    + " age_weeks int,"
                    + " done boolean,"
                    + " adjustment_days int"
                    + ");";
            executeUpdate(sql);

            sql = "INSERT INTO " + PERSON_TEMP_TABLE
                    + " (person_id, age_years, age_months, age_weeks, done)"
                    + " SELECT id, age_years, age_months, age_weeks, 0"
                    + " FROM person";
            executeUpdate(sql);

            sql = "UPDATE " + PERSON_TEMP_TABLE
                    + " SET adjustment_days = "
                    + " CASE"
                    + " WHEN age_years > 5 THEN FLOOR(RAND() * -1825)"
                    + " WHEN age_years > 2 OR age_months > 24 THEN FLOOR(RAND() * -730)"
                    + " ELSE FLOOR(RAND() * -180)"
                    + " END;";
            executeUpdate(sql);

            LOG.info("Created adjustments table");
        } finally {
            connection.close();
        }

        List<String> lsoaCodes = retrieveTempTableRows(LSOA_TEMP_TABLE);
        List<String> msoaCodes = retrieveTempTableRows(MSOA_TEMP_TABLE);
        List<String> postcodes = retrieveTempTableRows(POSTCODE_TEMP_TABLE);
        Random r = new Random();

        sql = "SELECT person_id, adjustment_days FROM " + PERSON_TEMP_TABLE + " WHERE done = false;";
        connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        Set<Long> personIds = new HashSet<>();
        while (rs.next()) {
            long personId = rs.getLong(1);
            personIds.add(new Long(personId));
        }

        rs.close();
        connection.close();

        int count = 0;

        for (Long personId: personIds) {

            sql = "SELECT age_years, age_months, age_weeks, adjustment_days"
                    + " FROM " + PERSON_TEMP_TABLE + " WHERE person_id = " + personId + " LIMIT 1;";
            connection = getConnection();
            rs = executeQuery(connection, sql);

            rs.next();
            Integer ageYears = new Integer(rs.getInt(1));
            if (rs.wasNull()) {
                ageYears = null;
            }

            Integer ageMonths = new Integer(rs.getInt(2));
            if (rs.wasNull()) {
                ageMonths = null;
            }

            Integer ageWeeks = new Integer(rs.getInt(3));
            if (rs.wasNull()) {
                ageWeeks = null;
            }

            Integer adjustment = new Integer(rs.getInt(4));

            rs.close();
            connection.close();

            count ++;
            if (count % 1000 == 0) {
                LOG.info("Done " + count + " patients out of " + personIds.size());
            }


            if (ageYears != null) {
                ageYears = new Integer(ageYears.intValue() - (adjustment / 365));

            } else if (ageMonths != null) {
                ageMonths = new Integer(ageMonths.intValue() - (adjustment / 30));
                if (ageMonths.intValue() >= 60) {
                    ageYears = new Integer(ageMonths.intValue() / 12);
                    ageMonths = null;
                }

            } else if (ageWeeks != null) {
                ageWeeks = new Integer(ageWeeks.intValue() - (adjustment / 7));
                if (ageWeeks.intValue() > 52) {
                    ageMonths = new Integer(ageWeeks.intValue() / 4);
                    ageWeeks = null;
                }
            }

            String pseudoId = createRandomId(64);
            int householdId = count; //just use the count as an arbitrary number for the household
            String lsoaCode = lsoaCodes.get(r.nextInt(lsoaCodes.size()));
            String msoaCode = msoaCodes.get(r.nextInt(msoaCodes.size()));
            String postcode = postcodes.get(r.nextInt(postcodes.size()));

            //because the table is indexed by organisation and person ID, it's quicker to do more updates
            //but using those columns
            sql = "SELECT DISTINCT organization_id FROM patient WHERE person_id = " + personId + ";";
            Connection orgsConnection = getConnection();
            ResultSet rsOrgs = executeQuery(orgsConnection, sql);

            while (rsOrgs.next()) {
                long orgId = rsOrgs.getLong(1);

                sql = "UPDATE allergy_intolerance"
                        + " SET clinical_effective_date = DATE_ADD(clinical_effective_date, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND clinical_effective_date IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE observation"
                        + " SET clinical_effective_date = DATE_ADD(clinical_effective_date, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND clinical_effective_date IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE medication_statement"
                        + " SET clinical_effective_date = DATE_ADD(clinical_effective_date, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND clinical_effective_date IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE medication_order"
                        + " SET clinical_effective_date = DATE_ADD(clinical_effective_date, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND clinical_effective_date IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE referral_request"
                        + " SET clinical_effective_date = DATE_ADD(clinical_effective_date, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND clinical_effective_date IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE procedure_request"
                        + " SET clinical_effective_date = DATE_ADD(clinical_effective_date, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND clinical_effective_date IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE encounter"
                        + " SET clinical_effective_date = DATE_ADD(clinical_effective_date, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND clinical_effective_date IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE episode_of_care"
                        + " SET date_registered = DATE_ADD(date_registered, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND date_registered IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE episode_of_care"
                        + " SET date_registered_end = DATE_ADD(date_registered_end, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND date_registered_end IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE patient"
                        + " SET date_of_death = DATE_ADD(date_of_death, INTERVAL " + adjustment + " DAY)"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId
                        + " AND date_of_death IS NOT NULL";
                executeUpdate(sql);

                sql = "UPDATE patient"
                        + " SET age_years = " + ageYears + ", age_months = " + ageMonths + ", age_weeks = " + ageWeeks + ","
                        + " pseudo_id = '" + pseudoId + "', household_id = " + householdId + ","
                        + " lsoa_code = '" + lsoaCode + "', msoa_code = '" + msoaCode + "', postcode_prefix = '" + postcode + "'"
                        + " WHERE organization_id = " + orgId + " AND person_id = " + personId;
                executeUpdate(sql);
            }

            rsOrgs.close();
            orgsConnection.close();

            sql = "UPDATE person"
                    + " SET date_of_death = DATE_ADD(date_of_death, INTERVAL " + adjustment + " DAY)"
                    + " WHERE id = " + personId
                    + " AND date_of_death IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE person"
                    + " SET age_years = " + ageYears + ", age_months = " + ageMonths + ", age_weeks = " + ageWeeks + ","
                    + " pseudo_id = '" + pseudoId + "', household_id = " + householdId + ","
                    + " lsoa_code = '" + lsoaCode + "', msoa_code = '" + msoaCode + "', postcode_prefix = '" + postcode + "'"
                    + " WHERE id = " + personId;
            executeUpdate(sql);

            //update the temp table, so we don't lose all progress if we restart
            sql = "UPDATE " + PERSON_TEMP_TABLE
                    + " SET done = true"
                    + " WHERE person_id = " + personId;
            executeUpdate(sql);
        }

    }*/

    private static void createPersonTempTable(int populationCount) throws Exception {

        Connection connection = getConnection();

        String sql = "DROP TABLE IF EXISTS " + PERSON_TEMP_TABLE + ";";
        executeUpdate(sql);

        sql = "CREATE TABLE " + PERSON_TEMP_TABLE + " ("
                + " person_id bigint,"
                + " done boolean,"
                + " adjustment_days int,"
                + " age_years int,"
                + " age_months int,"
                + " age_weeks int,"
                + " pseudo_id varchar(255),"
                + " household_id bigint,"
                + " lsoa_code varchar(50),"
                + " msoa_code varchar(50),"
                + " postcode_prefix varchar(20)"
                + ");";
        executeUpdate(sql);

        //populate with person data
        sql = "INSERT INTO " + PERSON_TEMP_TABLE
                + " (person_id, age_years, age_months, age_weeks, done)"
                + " SELECT id, age_years, age_months, age_weeks, 0"
                + " FROM person";
        executeUpdate(sql);

        //generate random adjustments for each person
        sql = "UPDATE " + PERSON_TEMP_TABLE
                + " SET adjustment_days = "
                + " CASE"
                + " WHEN age_years > 5 THEN FLOOR(RAND() * -1825)"
                + " WHEN age_years > 2 OR age_months > 24 THEN FLOOR(RAND() * -730)"
                + " ELSE FLOOR(RAND() * -180)"
                + " END;";
        executeUpdate(sql);

        LOG.info("Populated rows in person temp table");

        List<String> lsoaCodes = retrieveTempTableRows(LSOA_TEMP_TABLE);
        List<String> msoaCodes = retrieveTempTableRows(MSOA_TEMP_TABLE);
        List<String> postcodes = retrieveTempTableRows(POSTCODE_TEMP_TABLE);
        Random r = new Random();

        //go through each person and work out new postcode, lsoa, msoa, age and pseudo IDs
        sql = "SELECT person_id, adjustment_days, age_years, age_months, age_weeks, adjustment_days FROM " + PERSON_TEMP_TABLE;
        ResultSet rs = executeQuery(connection, sql);

        int count = 0;
        List<String> batch = new ArrayList<>();

        while (rs.next()) {

            count ++;
            if (count % 1000 == 0) {
                LOG.info("Done " + count + " patients out of " + populationCount);
            }

            long personId = rs.getLong(1);
            int adjustmentDays = rs.getInt(2);

            Integer ageYears = new Integer(rs.getInt(3));
            if (rs.wasNull()) {
                ageYears = null;
            }

            Integer ageMonths = new Integer(rs.getInt(4));
            if (rs.wasNull()) {
                ageMonths = null;
            }

            Integer ageWeeks = new Integer(rs.getInt(5));
            if (rs.wasNull()) {
                ageWeeks = null;
            }

            if (ageYears != null) {
                ageYears = new Integer(ageYears.intValue() - (adjustmentDays / 365));

            } else if (ageMonths != null) {
                ageMonths = new Integer(ageMonths.intValue() - (adjustmentDays / 30));
                if (ageMonths.intValue() >= 60) {
                    ageYears = new Integer(ageMonths.intValue() / 12);
                    ageMonths = null;
                }

            } else if (ageWeeks != null) {
                ageWeeks = new Integer(ageWeeks.intValue() - (adjustmentDays / 7));
                if (ageWeeks.intValue() > 52) {
                    ageMonths = new Integer(ageWeeks.intValue() / 4);
                    ageWeeks = null;
                }
            }

            String pseudoId = createRandomId(64);
            int householdId = count; //just use the count as an arbitrary number for the household
            String lsoaCode = lsoaCodes.get(r.nextInt(lsoaCodes.size()));
            String msoaCode = msoaCodes.get(r.nextInt(msoaCodes.size()));
            String postcode = postcodes.get(r.nextInt(postcodes.size()));

            sql = "UPDATE " + PERSON_TEMP_TABLE
                    + " SET age_years = " + ageYears + ","
                    + " age_months = " + ageMonths + ","
                    + " age_weeks = " + ageWeeks + ","
                    + " pseudo_id = '" + pseudoId + "',"
                    + " household_id = " + householdId + ","
                    + " lsoa_code = '" + lsoaCode + "',"
                    + " msoa_code = '" + msoaCode + "',"
                    + " postcode_prefix = '" + postcode + "'"
                    + " WHERE person_id = " + personId + ";";

            batch.add(sql);
            if (batch.size() >= 500) {
                executeUpdate(batch);
                batch.clear();
            }
            //executeUpdate(sql);
        }

        if (!batch.isEmpty()) {
            executeUpdate(batch);
        }

        connection.close();

        //index the table
        sql = " CREATE INDEX ix_person_done"
                + " ON " + PERSON_TEMP_TABLE
                + " (done);";
        executeUpdate(sql);
    }

    private static boolean patientsLeftToConvert() throws Exception {

        Connection connection = getConnection();
        String sql = "SELECT 1 FROM " + PERSON_TEMP_TABLE + " WHERE done = false;";
        ResultSet rs = executeQuery(connection, sql);
        boolean ret = rs.next();

        rs.close();
        connection.close();
        return ret;
    }

    private static void updatePatients() throws Exception {

        String sql = null;

        //make sure there is no existing "batch" table
        sql = "DROP TABLE IF EXISTS " + PERSON_TEMP_BATCH_TABLE;
        executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS " + PERSON_TEMP_ORG_BATCH_TABLE;
        executeUpdate(sql);

        int countDone = 0;

        while (patientsLeftToConvert()) {

            //create batch table
            sql = "CREATE TABLE " + PERSON_TEMP_BATCH_TABLE + " ("
                    + " person_id bigint,"
                    + " adjustment_days int,"
                    + " age_years int,"
                    + " age_months int,"
                    + " age_weeks int,"
                    + " pseudo_id varchar(255),"
                    + " household_id bigint,"
                    + " lsoa_code varchar(50),"
                    + " msoa_code varchar(50),"
                    + " postcode_prefix varchar(20)"
                    + ");";
            executeUpdate(sql);

            sql = "INSERT INTO " + PERSON_TEMP_BATCH_TABLE
                    + " SELECT person_id,"
                    + " adjustment_days,"
                    + " age_years,"
                    + " age_months,"
                    + " age_weeks,"
                    + " pseudo_id,"
                    + " household_id,"
                    + " lsoa_code,"
                    + " msoa_code,"
                    + " postcode_prefix"
                    + " FROM " + PERSON_TEMP_TABLE
                    + " WHERE done = false"
                    + " LIMIT 1000;";
            executeUpdate(sql);

            //create batch table with extra org ID column
            sql = "CREATE TABLE " + PERSON_TEMP_ORG_BATCH_TABLE + " ("
                    + " person_id bigint,"
                    + " adjustment_days int,"
                    + " age_years int,"
                    + " age_months int,"
                    + " age_weeks int,"
                    + " pseudo_id varchar(255),"
                    + " household_id bigint,"
                    + " lsoa_code varchar(50),"
                    + " msoa_code varchar(50),"
                    + " postcode_prefix varchar(20),"
                    + " organization_id bigint"
                    + ");";
            executeUpdate(sql);

            sql = "INSERT INTO " + PERSON_TEMP_ORG_BATCH_TABLE
                    + " SELECT src.person_id,"
                    + " src.adjustment_days,"
                    + " src.age_years,"
                    + " src.age_months,"
                    + " src.age_weeks,"
                    + " src.pseudo_id,"
                    + " src.household_id,"
                    + " src.lsoa_code,"
                    + " src.msoa_code,"
                    + " src.postcode_prefix,"
                    + " p.organization_id"
                    + " FROM " + PERSON_TEMP_BATCH_TABLE + " src"
                    + " JOIN patient p"
                    + " ON p.person_id = src.person_id;";
            executeUpdate(sql);

            //perform updates
            sql = "UPDATE allergy_intolerance a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.clinical_effective_date = DATE_ADD(a.clinical_effective_date, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND clinical_effective_date IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE observation a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.clinical_effective_date = DATE_ADD(a.clinical_effective_date, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND clinical_effective_date IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE medication_statement a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.clinical_effective_date = DATE_ADD(a.clinical_effective_date, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND clinical_effective_date IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE medication_order a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.clinical_effective_date = DATE_ADD(a.clinical_effective_date, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND clinical_effective_date IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE referral_request a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.clinical_effective_date = DATE_ADD(a.clinical_effective_date, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND clinical_effective_date IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE procedure_request a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.clinical_effective_date = DATE_ADD(a.clinical_effective_date, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND clinical_effective_date IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE encounter a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.clinical_effective_date = DATE_ADD(a.clinical_effective_date, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND clinical_effective_date IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE episode_of_care a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.date_registered = DATE_ADD(a.date_registered, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND date_registered IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE episode_of_care a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.date_registered_end = DATE_ADD(a.date_registered_end, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND date_registered_end IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE patient a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.date_of_death = DATE_ADD(a.date_of_death, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id"
                    + " AND date_of_death IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE patient a, " + PERSON_TEMP_ORG_BATCH_TABLE + " b"
                    + " SET a.age_years = b.age_years,"
                    + " a.age_months = b.age_months, "
                    + " a.age_weeks = b.age_weeks, "
                    + " a.pseudo_id = b.pseudo_id, "
                    + " a.household_id = b.household_id, "
                    + " a.lsoa_code = b.lsoa_code, "
                    + " a.msoa_code = b.msoa_code, "
                    + " a.postcode_prefix = b.postcode_prefix"
                    + " WHERE a.organization_id = b.organization_id AND a.person_id = b.person_id";
            executeUpdate(sql);

            sql = "UPDATE person a, " + PERSON_TEMP_BATCH_TABLE + " b"
                    + " SET a.date_of_death = DATE_ADD(a.date_of_death, INTERVAL b.adjustment_days DAY)"
                    + " WHERE a.id = b.person_id"
                    + " AND a.date_of_death IS NOT NULL";
            executeUpdate(sql);

            sql = "UPDATE person a, " + PERSON_TEMP_BATCH_TABLE + " b"
                    + " SET a.age_years = b.age_years,"
                    + " a.age_months = b.age_months, "
                    + " a.age_weeks = b.age_weeks, "
                    + " a.pseudo_id = b.pseudo_id, "
                    + " a.household_id = b.household_id, "
                    + " a.lsoa_code = b.lsoa_code, "
                    + " a.msoa_code = b.msoa_code, "
                    + " a.postcode_prefix = b.postcode_prefix"
                    + " WHERE a.id = b.person_id";
            executeUpdate(sql);

            //update done column
            sql = "UPDATE " + PERSON_TEMP_TABLE + " t, " + PERSON_TEMP_BATCH_TABLE + " b"
                    + " SET t.done = true"
                    + " WHERE t.person_id = b.person_id";
            executeUpdate(sql);

            Connection connection = getConnection();
            sql = "SELECT COUNT(1) FROM " + PERSON_TEMP_BATCH_TABLE;
            ResultSet rs = executeQuery(connection, sql);
            rs.next();
            countDone += rs.getInt(1);
            connection.close();

            if (countDone % 1000 == 0) {
                LOG.info("Done " + countDone);
            }

            //drop batch table
            sql = "DROP TABLE " + PERSON_TEMP_BATCH_TABLE + ";";
            executeUpdate(sql);

            sql = "DROP TABLE " + PERSON_TEMP_ORG_BATCH_TABLE + ";";
            executeUpdate(sql);
        }
    }

    private static String createRandomId(int len) {

        Random r = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i=0; i<len; i++) {
            int index = r.nextInt(SYMBOLS.length());
            char c = SYMBOLS.charAt(index);
            sb.append(c);
        }

        return sb.toString();
    }

    private static void randomisePractitionerNames() throws Exception {

        List<Long> practitionerIds = getPractitionerIds();

        Random r = new Random();
        List<String> firstNames = getResourceAsList(RESOURCE_FIRST_NAMES);
        List<String> lastNames = getResourceAsList(RESOURCE_LAST_NAMES);

        for (int i=0; i<practitionerIds.size(); i++) {
            Long id = practitionerIds.get(i);

            int rInt = r.nextInt(firstNames.size());
            String firstName = firstNames.get(rInt);

            rInt = r.nextInt(lastNames.size());
            String lastName = lastNames.get(rInt);
            lastName = lastName.toUpperCase();

            String sql = "UPDATE practitioner SET name = '" + lastName + ", " + firstName + " (Dr)'"
                    + " WHERE id = " + id;
            executeUpdate(sql);

            if ((i+1)%100 == 0) {
                LOG.info("Updated " + (i+1) + " practitioner names");
            }
        }
    }

    private static List<String> getResourceAsList(String resourceName) throws Exception {
        String s = Resources.getResourceAsString(resourceName);
        StringReader sr = new StringReader(s);
        BufferedReader br = new BufferedReader(sr);

        List<String> ret = new ArrayList<>();

        while (true) {
            String line = br.readLine();
            if (Strings.isNullOrEmpty(line)) {
                break;
            }
            ret.add(line);
        }

        return ret;
    }

    /*private static List<Long> getPersonIds() throws Exception {

        //apply the order by so our person IDs come out in a random order
        String sql = "SELECT id FROM person ORDER BY rand();";
        ResultSet rs = executeQuery(sql);

        List<Long> ret = new ArrayList<>();
        while (rs.next()) {
            long l = rs.getLong(1);
            ret.add(new Long(l));
        }

        rs.close();

        return ret;
    }*/

    /*private static void randomisePostcodePrefixes(List<Long> personIds) throws Exception {

        List<String> postcodePrefixes = getResourceAsList(RESOURCE_POSTCODE_PREFIXES);
        randomisePersonFields(personIds, "postcode_prefix", postcodePrefixes);
    }


    private static void randomiseLsoaAndMsoaCodes(List<Long> personIds, String field, String lookupTable) throws Exception {

        String sql = "SELECT DISTINCT " + field + " FROM " + lookupTable + ";";
        ResultSet rs = executeQuery(sql);

        List<String> replacementCodes = new ArrayList<>();
        while (rs.next()) {
            String s = rs.getString(1);
            replacementCodes.add(s);
        }

        rs.close();

        randomisePersonFields(personIds, field, replacementCodes);
    }*/


    private static void randomisePersonFields(List<Long> personIds, String field, List<String> replacements) throws Exception {

        String sql = "SELECT " + field + ", COUNT(" + field + ") FROM person GROUP BY " + field + ";";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        List<Integer> lsoaCounts = new ArrayList<>();
        while (rs.next()) {
            int count = rs.getInt(1);
            lsoaCounts.add(count);
        }

        rs.close();
        connection.close();

        Random r = new Random();
        personIds = new ArrayList<>(personIds);

        for (int i=0; i<lsoaCounts.size(); i++) {
            int count = lsoaCounts.get(i);

            int rInt = r.nextInt(replacements.size());
            String replacement = replacements.get(rInt);
            LOG.info("Replacing " + count + " " + field + "s with " + replacement);

            //update the person table in batches, so we're not doing an IN (..) with thousands of items
            while (count > 0) {

                List<Long> ids = new ArrayList<>();

                int batch = Math.min(200, count);
                count -= batch;

                for (int j=0; j<batch; j++) {
                    Long id = personIds.remove(0);
                    ids.add(id);
                }

                String idStr = String.join(", " + ids);

                sql = "UPDATE person "
                        + " SET " + field + " = '" + replacement + "'"
                        + " WHERE id IN (" + idStr + ");";
                executeUpdate(sql);
            }
        }

        //finally carry over the changes to the patient table
        sql = "UPDATE patient, person"
                + " SET patient." + field + " = person." + field
                + " WHERE patient.person_id = person.id;";
        executeUpdate(sql);
    }



    private static void deleteAllAppoointments() throws Exception {

        String sql = "DELETE FROM appointment";
        executeUpdate(sql);
    }

    private static void deletePatientsWithOtherGenders() throws Exception {

        String sql = "SELECT id FROM person WHERE patient_gender_id NOT IN ("
                   + Enumerations.AdministrativeGender.MALE.ordinal() + ", "
                   + Enumerations.AdministrativeGender.FEMALE.ordinal() + ");";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        while (rs.next()) {
            long personId = rs.getLong(2);
            LOG.info("Deleting person " + personId);

            deletePerson(personId);
        }

        rs.close();
        connection.close();
    }

    /**
     * deletes all data for patients older than a certain age
     */
    private static void deletePatientsOlderThan(int age) throws Exception {

        String sql = "SELECT id FROM person WHERE age_years > " + age;
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        while (rs.next()) {
            long personId = rs.getLong(1);
            LOG.info("Deleting person " + personId);

            deletePerson(personId);
        }

        rs.close();
        connection.close();
    }

    private static void deletePerson(long personId) throws Exception {

        String sql = "SELECT DISTINCT organization_id FROM patient WHERE person_id = " + personId;
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);
        while (rs.next()) {
            long orgId = rs.getLong(1);
            LOG.info("Deleting data for org " + orgId);
            deletePatient(orgId, personId);
        }

        sql = "DELETE FROM person WHERE id = " + personId;
        executeUpdate(sql);

        rs.close();
        connection.close();
    }

    private static void deletePatient(long orgId, long personId) throws Exception {
        for (String tableName: getPatientTables()) {
            String sql = "DELETE FROM " + tableName + " WHERE organization_id = " + orgId + " AND person_id = " + personId;
            executeUpdate(sql);
        }
    }

    private static List<String> getPatientTables() {
        List<String> ret = new ArrayList<>();
        ret.add("medication_order");
        ret.add("medication_statement");
        ret.add("allergy_intolerance");
        ret.add("appointment");
        ret.add("observation");
        ret.add("procedure_request");
        ret.add("referral_request");
        ret.add("encounter");
        ret.add("episode_of_care");
        ret.add("patient");
        return ret;
    }

    /**
     * deletes medication_statements and medication_ where the code frequence is less than 0.1% of population
     */
    private static void deleteLowIncidenceMedicationStatements(int populationCount) throws Exception {

        //first delete any medication orders that link to statements with null DM+D ID (we've already deleted
        //medication_orders with a null DM+D ID, but there may be some orders with non-null DM+D IDs that link
        //to statements with null DM+D IDs)
        String sql = "SELECT organization_id, person_id, id FROM medication_statement WHERE dmd_id IS NULL;";
        Connection connectionNullStatements = getConnection();
        ResultSet rsNullStatements = executeQuery(connectionNullStatements, sql);

        while (rsNullStatements.next()) {
            long orgId = rsNullStatements.getLong(1);
            long personId = rsNullStatements.getLong(2);
            long statementId = rsNullStatements.getLong(3);
            sql = "DELETE FROM medication_order WHERE organization_id = " + orgId + " AND person_id = " + personId + " AND medication_statement_id = " + statementId + ";";
            executeUpdate(sql);
        }

        rsNullStatements.close();
        connectionNullStatements.close();

        //next delete any medication statements that have a null DM+D ID
        LOG.info("Deleting all instances of medication_order where DM+D ID is null");
        sql = "DELETE FROM medication_statement WHERE dmd_id IS NULL";
        executeUpdate(sql);

        //now count the low volume non-null dm+d IDs
        int cutoff = populationCount / 1000;

        sql = "SELECT dmd_id, original_term, COUNT(1)"
                + " FROM medication_statement"
                + " WHERE dmd_id IS NOT NULL"
                + " GROUP BY dmd_id, original_term"
                + " HAVING COUNT(1) < " + cutoff + ";";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        while (rs.next()) {
            long dmdId = rs.getLong(1);
            String term = rs.getString(2);
            int count = rs.getInt(3);
            LOG.info("Deleting all medication_statement instances of " + dmdId + " " + term + " as only " + count + " instances");

            //we have to delete all medication orders that link to the statements first
            sql = "SELECT organization_id, person_id, id FROM medication_statement WHERE dmd_id = " + dmdId + ";";
            Connection connectionOrders = getConnection();
            ResultSet rsOrders = executeQuery(connectionOrders, sql);

            while (rsOrders.next()) {
                long orgId = rsOrders.getLong(1);
                long personId = rsOrders.getLong(2);
                long id = rsOrders.getLong(3);
                sql = "DELETE FROM medication_order WHERE organization_id = " + orgId + " AND person_id = " + personId + " AND medication_statement_id = " + id + ";";
                executeUpdate(sql);
            }

            rsOrders.close();
            connectionOrders.close();

            sql = "DELETE FROM medication_statement WHERE dmd_id = " + dmdId;
            executeUpdate(sql);
        }

        rs.close();
        connection.close();
    }

    /**
     * deletes medication_orders
     */
    private static void deleteLowIncidenceMedicationOrders(int populationCount) throws Exception {

        //first delete any where the dm+d ID is null, since they are low-volume and we don't know what they mean
        LOG.info("Deleting all instances of medication_order where DM+D ID is null");
        String sql = "DELETE FROM medication_order WHERE dmd_id IS NULL";
        executeUpdate(sql);

        //now get the low-count DM+D IDs
        int cutoff = populationCount / 1000; //want to delete where <0.1% of population has it

        sql = "SELECT dmd_id, original_term, COUNT(1)"
                + " FROM medication_order"
                + " WHERE dmd_id IS NOT NULL"
                + " GROUP BY dmd_id, original_term"
                + " HAVING COUNT(1) < " + cutoff + ";";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        while (rs.next()) {
            long dmdId = rs.getLong(1);
            String term = rs.getString(2);
            int count = rs.getInt(3);
            LOG.info("Deleting all medication_order instances of " + dmdId + " " + term + " as only " + count + " instances");

            //then delete any orders that match the DMD_ID
            sql = "DELETE FROM medication_order WHERE dmd_id = " + dmdId;
            executeUpdate(sql);
        }

        rs.close();
        connection.close();
    }


    /**
     * deletes observations where the code frequency is less than 0.1% of population
     */
    private static void deleteLowIncidenceRecords(int populationCount, String tableName) throws Exception {

        //first delete any where the concept ID is null, since they are low-volume and we don't know what they mean
        LOG.info("Deleting all instances of from " + tableName + " where concept ID is null");
        String sql = "DELETE FROM " + tableName + " WHERE snomed_concept_id IS NULL";
        executeUpdate(sql);

        int cutoff = populationCount / 1000;

        sql = "SELECT snomed_concept_id, original_term, COUNT(1)"
                + " FROM " + tableName
                + " WHERE snomed_concept_id IS NOT NULL"
                + " GROUP BY snomed_concept_id, original_term"
                + " HAVING COUNT(1) < " + cutoff + " ORDER BY COUNT(1) DESC;";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        while (rs.next()) {
            long snomedConceptId = rs.getLong(1);
            String term = rs.getString(2);
            int count = rs.getInt(3);
            LOG.info("Deleting all instances of " + snomedConceptId + " " + term + " from " + tableName + " as only " + count + " instances");

            sql = "DELETE FROM " + tableName + " WHERE snomed_concept_id = " + snomedConceptId;
            executeUpdate(sql);
        }

        rs.close();
        connection.close();
    }

    private static int getPopulationCount() throws Exception {

        String sql = "SELECT COUNT(1) FROM person;";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        rs.next();
        int count = rs.getInt(1);

        rs.close();
        connection.close();

        return count;
    }

    private static void executeUpdate(List<String> sql) throws Exception {

        LOG.info("Executing batch of " + sql.size());
        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            LOG.info("Created statement");
            for (String item: sql) {
                statement.addBatch(item);
            }
            LOG.info("Going to execute batch");
            statement.executeBatch();
            LOG.info("Going to execute commit");
            connection.commit();
        } finally {
            connection.close();
        }
        LOG.info("Finished batch of " + sql.size());
    }

    private static void executeUpdate(String sql) throws Exception {

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
        } finally {
            connection.close();
        }
    }

    private static ResultSet executeQuery(Connection connection, String sql) throws Exception {

        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    private static void openConnectionPool(String url, String driverClass, String username, String password) throws Exception {

        //force the driver to be loaded
        Class.forName(driverClass);

        HikariDataSource pool = new HikariDataSource();
        pool.setJdbcUrl(url);
        pool.setUsername(username);
        pool.setPassword(password);
        pool.setMaximumPoolSize(3);
        pool.setMinimumIdle(1);
        pool.setIdleTimeout(60000);
        pool.setPoolName("DeidentifierPool" + url);
        pool.setAutoCommit(false);

        connectionPool = pool;

        //test getting a connection
        Connection conn = pool.getConnection();
        conn.close();
    }

    private static Connection getConnection() throws Exception {
        return connectionPool.getConnection();
    }

    public static List<Long> getPractitionerIds() throws Exception  {
        String sql = "SELECT id FROM practitioner;";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        List<Long> ret = new ArrayList<>();
        while (rs.next()) {
            long l = rs.getLong(1);
            ret.add(new Long(l));
        }

        rs.close();
        connection.close();

        return ret;
    }
}
