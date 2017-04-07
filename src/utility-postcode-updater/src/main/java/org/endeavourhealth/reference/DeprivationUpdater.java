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
        String rank = record.get(DEPRIVATION_RANK);
        String decile = record.get(DEPRIVATION_DECILE);

        //the file has numbes in the format "1,234", so remove the commas
        rank = rank.replace(",", "");
        decile = decile.replace(",", "");

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

        lookup.setImdRank(new Integer(rank));
        lookup.setImdDecile(new Integer(decile));

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
                DEPRIVATION_DECILE
        };
    }
}
