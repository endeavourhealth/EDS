package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.rdbms.reference.DeprivationLookup;
import org.endeavourhealth.core.rdbms.reference.ReferenceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;

public class DeprivationUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(DeprivationUpdater.class);

    private static final String LSOA_CODE = "\uFEFFLSOA code (2011)"; //the raw file seems to have this weird character at the start
    private static final String LSOA_NAME = "LSOA name (2011)";
    private static final String LA_CODE = "Local Authority District code (2013)";
    private static final String LA_NAME = "Local Authority District name (2013)";
    private static final String DEPRIVATION_RANK = "Index of Multiple Deprivation (IMD) Rank (where 1 is most deprived)";
    private static final String DEPRIVATION_DECILE = "Index of Multiple Deprivation (IMD) Decile (where 1 is most deprived 10% of LSOAs)";
    private static final String INCOME_RANK = "Income Rank (where 1 is most deprived)";
    private static final String INCOME_DECILE = "Income Decile (where 1 is most deprived 10% of LSOAs)";
    private static final String EMPLOYMENT_RANK = "Employment Rank (where 1 is most deprived)";
    private static final String EMPLOYMENT_DECILE = "Employment Decile (where 1 is most deprived 10% of LSOAs)";
    private static final String EDUCATION_RANK = "Education, Skills and Training Rank (where 1 is most deprived)";
    private static final String EDUCATION_DECILE = "Education, Skills and Training Decile (where 1 is most deprived 10% of LSOAs)";
    private static final String HEALTH_RANK = "Health Deprivation and Disability Rank (where 1 is most deprived)";
    private static final String HEALTH_DECILE = "Health Deprivation and Disability Decile (where 1 is most deprived 10% of LSOAs)";
    private static final String CRIME_RANK = "Crime Rank (where 1 is most deprived)";
    private static final String CRIME_DECILE = "Crime Decile (where 1 is most deprived 10% of LSOAs)";
    private static final String HOUSING_AND_SERVICES_BARRIERS_RANK = "Barriers to Housing and Services Rank (where 1 is most deprived)";
    private static final String HOUSING_AND_SERVICES_BARRIERS_DECILE = "Barriers to Housing and Services Decile (where 1 is most deprived 10% of LSOAs)";
    private static final String LIVING_ENVIRONMENT_RANK = "Living Environment Rank (where 1 is most deprived)";
    private static final String LIVING_ENVIRONMENT_DECILE = "Living Environment Decile (where 1 is most deprived 10% of LSOAs)";
    
    /**
     * updates the deprivation_lookup table in the reference DB from ONS data
     *
     * Usage
     * =================================================================================
     * 1. go to https://www.gov.uk/government/statistics/english-indices-of-deprivation-2015
     * 2. download "File 2: domains of deprivation "
     * 3. open in Excel and save the second sheet as a CSV file
     * 4. Then run this utility as:
     *      Main deprivation <csv file>
     */
    public static void updateDeprivationScores(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: lsoa <lsoa txt file>");
            return;
        }

        LOG.info("Deprivation Update Starting");

        File f = new File(args[1]);

        if (!f.exists()) {
            LOG.error("" + f + " doesn't exist");
        }

        LOG.info("Processing Deprivation file");
        processFile(f);
        LOG.info("Finished Deprivation file");
    }

    private static void processFile(File f) throws Exception {

        CSVFormat format = CSVFormat.DEFAULT;

        EntityManager entityManager = ReferenceConnection.getEntityManager();

        int rowsDone = 0;
        CSVParser parser = null;
        try {
            //the postcode CSV file doesn't contain headers, so we must just pass in the headers we know should be there
            parser = CSVParser.parse(f, Charset.defaultCharset(), format.withHeader());
            CsvHelper.validateCsvHeaders(parser, f, getCsvHeadings());

            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                updateDeprivationScore(entityManager, record);

                rowsDone ++;
                if (rowsDone % 1000 == 0) {
                    LOG.info("Done " + rowsDone + " (of approx 33k)");
                }
            }

            LOG.info("Finshed at " + rowsDone + " rows");

        } finally {
            if (parser != null) {
                parser.close();
            }

            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private static void updateDeprivationScore(EntityManager entityManager, CSVRecord record) {

        String lsoaCode = record.get(LSOA_CODE);

        //the file has numbers in the format "1,234", so remove the commas before creating the Integers
        Integer rank = new Integer(record.get(DEPRIVATION_RANK).replace(",", ""));
        Integer decile = new Integer(record.get(DEPRIVATION_DECILE).replace(",", ""));
        Integer incomeRank = new Integer(record.get(INCOME_RANK).replace(",", ""));
        Integer incomeDecile = new Integer(record.get(INCOME_DECILE).replace(",", ""));
        Integer employmentRank = new Integer(record.get(EMPLOYMENT_RANK).replace(",", ""));
        Integer employmentDecile = new Integer(record.get(EMPLOYMENT_DECILE).replace(",", ""));
        Integer educationRank = new Integer(record.get(EDUCATION_RANK).replace(",", ""));
        Integer educationDecile = new Integer(record.get(EDUCATION_DECILE).replace(",", ""));
        Integer healthRank = new Integer(record.get(HEALTH_RANK).replace(",", ""));
        Integer healthDecile = new Integer(record.get(HEALTH_DECILE).replace(",", ""));
        Integer crimeRank = new Integer(record.get(CRIME_RANK).replace(",", ""));
        Integer crimeDecile = new Integer(record.get(CRIME_DECILE).replace(",", ""));
        Integer housingAndServicesBarriersRank = new Integer(record.get(HOUSING_AND_SERVICES_BARRIERS_RANK).replace(",", ""));
        Integer housingAndServicesBarriersDecile = new Integer(record.get(HOUSING_AND_SERVICES_BARRIERS_DECILE).replace(",", ""));
        Integer livingEnvironmentRank = new Integer(record.get(LIVING_ENVIRONMENT_RANK).replace(",", ""));
        Integer livingEnvironmentDecile = new Integer(record.get(LIVING_ENVIRONMENT_DECILE).replace(",", ""));

        String sql = "select r"
                + " from DeprivationLookup r"
                + " where r.lsoaCode = :lsoaCode";

        Query query = entityManager
                .createQuery(sql, DeprivationLookup.class)
                .setParameter("lsoaCode", lsoaCode);

        DeprivationLookup lookup = null;
        try {
            lookup = (DeprivationLookup)query.getSingleResult();
        } catch (NoResultException e) {
            lookup = new DeprivationLookup();
            lookup.setLsoaCode(lsoaCode);
        }

        lookup.setImdRank(rank);
        lookup.setImdDecile(decile);
        lookup.setIncomeRank(incomeRank);
        lookup.setIncomeDecile(incomeDecile);
        lookup.setEmploymentRank(employmentRank);
        lookup.setEmploymentDecile(employmentDecile);
        lookup.setEducationRank(educationRank);
        lookup.setEducationDecile(educationDecile);
        lookup.setHealthRank(healthRank);
        lookup.setHealthDecile(healthDecile);
        lookup.setCrimeRank(crimeRank);
        lookup.setCrimeDecile(crimeDecile);
        lookup.setHousingAndServicesBarriersRank(housingAndServicesBarriersRank);
        lookup.setHousingAndServicesBarriersDecile(housingAndServicesBarriersDecile);
        lookup.setLivingEnvironmentRank(livingEnvironmentRank);
        lookup.setLivingEnvironmentDecile(livingEnvironmentDecile);

        entityManager.getTransaction().begin();
        entityManager.persist(lookup);
        entityManager.getTransaction().commit();
    }

    public static String[] getCsvHeadings() {
        return new String[]{
                LSOA_CODE,
                LSOA_NAME,
                LA_CODE,
                LA_NAME,
                DEPRIVATION_RANK,
                DEPRIVATION_DECILE,
                INCOME_RANK,
                INCOME_DECILE,
                EMPLOYMENT_RANK,
                EMPLOYMENT_DECILE,
                EDUCATION_RANK,
                EDUCATION_DECILE,
                HEALTH_RANK,
                HEALTH_DECILE,
                CRIME_RANK,
                CRIME_DECILE,
                HOUSING_AND_SERVICES_BARRIERS_RANK,
                HOUSING_AND_SERVICES_BARRIERS_DECILE,
                LIVING_ENVIRONMENT_RANK,
                LIVING_ENVIRONMENT_DECILE
        };
    }
}
