package org.endeavourhealth.transform.emis.csv.schema;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.exceptions.FileFormatException;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.CsvCurrentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractCsvTransformer implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCsvTransformer.class);

    private final String version;
    private final File file;
    private final CSVFormat csvFormat;
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;

    private CSVParser csvReader = null;
    private Iterator<CSVRecord> csvIterator = null;
    private CSVRecord csvRecord = null;


    public AbstractCsvTransformer(String version, File file, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        this.version = version;
        this.file = file;
        this.csvFormat = csvFormat;
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.timeFormat = new SimpleDateFormat(timeFormat);

        open();
    }

    private void open() throws Exception {

        //calling withHeader() on the format, forces it to read in the first row as the headers, which we can then validate against
        this.csvReader = CSVParser.parse(file, Charset.defaultCharset(), csvFormat.withHeader());
        try {
            this.csvIterator = csvReader.iterator();

            Map<String, Integer> headerMap = csvReader.getHeaderMap();
            String[] expectedHeaders = getCsvHeaders(version);
            if (headerMap.size() != expectedHeaders.length) {
                throw new FileFormatException(file.getName(), "Mismatch in number of CSV columns in " + file.getName() + " expected " + expectedHeaders.length + " but found " + headerMap.size());
            }

            for (int i = 0; i < expectedHeaders.length; i++) {
                String expectedHeader = expectedHeaders[i];
                Integer mapIndex = headerMap.get(expectedHeader);
                if (mapIndex == null) {
                    throw new FileFormatException(file.getName(), "Missing column " + expectedHeader + " in " + file.getName());
                } else if (mapIndex.intValue() != i) {
                    throw new FileFormatException(file.getName(), "Out of order column " + expectedHeader + " in " + file.getName() + " expected at " + i + " but found at " + mapIndex);
                }
            }
        } catch (Exception e) {
            //if we get any exception thrown during the constructor, make sure to close the reader
            close();
            throw e;
        }
    }

    public void close() throws IOException {
        if (csvRecord != null) {
            csvReader.close();
        }
    }

    public void reset() throws Exception {

        //if we've only read the header, don't bother resetting
        if (getCurrentLineNumber() <= 1) {
            return;
        }

        close();
        open();
    }

    protected abstract String[] getCsvHeaders(String version);

    public boolean nextRecord() {

        //if the source file couldn't be found, the iterator will be null
        if (csvIterator == null) {
            return false;
        }

        if (csvIterator.hasNext()) {
            this.csvRecord = csvIterator.next();

            if (csvReader.getCurrentLineNumber() % 20000 == 0) {
                LOG.info("Starting line {} of {}", csvReader.getCurrentLineNumber(), file.getAbsolutePath());
            }

            return true;
        } else {
            this.csvRecord = null;
            LOG.info("Completed file {}", file.getAbsolutePath());
            return false;
        }
    }


    public File getFile() {
        return file;
    }

    public long getCurrentLineNumber() {
        return csvReader.getCurrentLineNumber();
    }

    public CsvCurrentState getCurrentState() {
        return new CsvCurrentState(file, getCurrentLineNumber());
    }

    public String getString(String column) {
        return csvRecord.get(column);
    }
    public Integer getInt(String column) {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return new Integer(s);
    }
    public Long getLong(String column) {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return new Long(s);
    }
    public Double getDouble(String column) {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return new Double(s);
    }
    public Date getDate(String column) throws TransformException {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        try {
            return dateFormat.parse(s);
        } catch (ParseException pe) {
            throw new FileFormatException(file.getName(), "Invalid date format [" + s + "]", pe);
        }
    }
    public Date getTime(String column) throws TransformException {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        try {
            return timeFormat.parse(s);
        } catch (ParseException pe) {
            throw new FileFormatException(file.getName(), "Invalid time format [" + s + "]", pe);
        }
    }
    public Date getDateTime(String dateColumn, String timeColumn) throws TransformException {
        Date d = getDate(dateColumn);
        Date t = getTime(timeColumn);
        if (d == null) {
            return null;
        } else if (t == null) {
            return d;
        } else {
            return new Date(d.getTime() + t.getTime());
        }
    }
    /*public UUID getUniqueIdentifier(int index) {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return UUID.fromString(s);
    }*/
    public boolean getBoolean(String column) {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return false;
        }

        return Boolean.parseBoolean(s);
    }

    /**
     * if an error is encountered in a transform, this function is used to get detail on the line at fault
     */
    public String getErrorLinex() {
        return "Error processing line " + csvReader.getCurrentLineNumber() + " of " + file.getAbsolutePath();
    }
}
