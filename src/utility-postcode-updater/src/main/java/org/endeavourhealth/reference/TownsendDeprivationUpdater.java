package org.endeavourhealth.reference;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.common.utility.ThreadPool;
import org.endeavourhealth.common.utility.ThreadPoolError;
import org.endeavourhealth.core.csv.CsvHelper;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class TownsendDeprivationUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(TownsendDeprivationUpdater.class);

    private static final String POSTCODE = "PCD2";
    private static final String SCORE_2011 = "Townsend2011";
    private static final String RELEASE_VERSION = "releaseversion";
    private static final String RELEASE_DATE = "releasedate";


    /**
     * updates the townsend_deprivation_lookup table in the reference DB
     * data file provided by Julia Hippisley-Cox
     *
     * expected CSV format:
     * - PCD2
     * - Townsend2011
     * - releaseversion
     * - releasedate
     *
     * Usage
     * =================================================================================
     * 1. Get new file from Jula
     * 2. Put file on Core-QueueReader-01
     * 3. Run this utility on that CSV file, as:
     *      Main townsend <csv file>
     */
    public static void updateDeprivationScores(String[] args) throws Exception {

        if (args.length != 2) {
            LOG.error("Incorrect number of parameters");
            LOG.error("Usage: townsend <deprivation CSV file>");
            return;
        }

        LOG.info("Townsend Deprivation Update Starting");

        File f = new File(args[1]);

        if (!f.exists()) {
            LOG.error("" + f + " doesn't exist");
        }

        LOG.info("Processing Townsend Deprivation file");
        processFile(f);
        LOG.info("Finished Townsend Deprivation file");
    }

    private static void processFile(File f) throws Exception {

        CSVFormat format = CSVFormat.DEFAULT;

        ThreadPool threadPool = new ThreadPool(5, 1000);

        int rowsDone = 0;
        CSVParser parser = null;
        try {
            //the postcode CSV file doesn't contain headers, so we must just pass in the headers we know should be there
            parser = CSVParser.parse(f, Charset.defaultCharset(), format.withHeader());
            CsvHelper.validateCsvHeaders(parser, f.getAbsolutePath(), getCsvHeadings());

            Iterator<CSVRecord> iterator = parser.iterator();

            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                List<ThreadPoolError> errors = threadPool.submit(new SaveTownsendCallable(record));
                handleErrors(errors);

                rowsDone ++;
                if (rowsDone % 1000 == 0) {
                    LOG.info("Done " + rowsDone + " (of approx 2.6M)");
                }
            }

            List<ThreadPoolError> errors = threadPool.waitAndStop();
            handleErrors(errors);

            LOG.info("Finshed at " + rowsDone + " rows");

        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }

    private static void handleErrors(List<ThreadPoolError> errors) throws Exception {
        if (errors == null || errors.isEmpty()) {
            return;
        }

        //if we've had multiple errors, just throw the first one, since they'll most-likely be the same
        ThreadPoolError first = errors.get(0);
        Throwable cause = first.getException();
        //the cause may be an Exception or Error so we need to explicitly
        //cast to the right type to throw it without changing the method signature
        if (cause instanceof Exception) {
            throw (Exception)cause;
        } else if (cause instanceof Error) {
            throw (Error)cause;
        }
    }


    public static String[] getCsvHeadings() {
        return new String[]{
                POSTCODE,
                SCORE_2011,
                RELEASE_VERSION,
                RELEASE_DATE
        };
    }

    static class SaveTownsendCallable implements Callable {

        private CSVRecord record = null;

        public SaveTownsendCallable(CSVRecord record) {
            this.record = record;
        }

        @Override
        public Object call() throws Exception {

            Connection connection = ConnectionManager.getReferenceConnection();
            PreparedStatement ps = null;
            try {
                String sql = "INSERT INTO townsend_deprivation_lookup (postcode_no_space, townsend_2011_score, release_version, release_date)"
                        + " VALUES (?, ?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE"
                        + " townsend_2011_score = values(townsend_2011_score),"
                        + " release_version = values(release_version),"
                        + " release_date = values(release_date)";
                ps = connection.prepareStatement(sql);

                int col = 1;
                String postcode = record.get(POSTCODE);

                //remove space and force upper case, so it's consistent with postcode_lookup
                postcode = postcode.replaceAll(" ", "");
                postcode = postcode.toUpperCase();

                String scoreStr = record.get(SCORE_2011);
                double score = Double.parseDouble(scoreStr);

                String versionStr = record.get(RELEASE_VERSION);
                double version = Double.parseDouble(versionStr);

                String dateStr = record.get(RELEASE_DATE);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(dateStr);

                ps.setString(col++, postcode);
                ps.setDouble(col++, score);
                ps.setDouble(col++, version);
                ps.setTimestamp(col++, new java.sql.Timestamp(date.getTime()));

                ps.executeUpdate();
                connection.commit();

            } finally {
                if (ps != null) {
                    ps.close();
                }
                connection.close();
            }

            return null;
        }
    }
}

