package org.endeavourhealth.transform.emis.csv.schema;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.csv.CsvHelper;
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
import java.util.Set;

public abstract class AbstractCsvParser implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCsvParser.class);

    private final String version;
    private final File file;
    private final CSVFormat csvFormat;
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;

    private CSVParser csvReader = null;
    private Iterator<CSVRecord> csvIterator = null;
    private CSVRecord csvRecord = null;
    private Set<Long> recordNumbersToProcess = null;


    public AbstractCsvParser(String version, File file, boolean openParser, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        this.version = version;
        this.file = file;
        this.csvFormat = csvFormat;
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.timeFormat = new SimpleDateFormat(timeFormat);

        if (openParser) {
            open();
        }
    }

    private void open() throws Exception {

        //calling withHeader() on the format, forces it to read in the first row as the headers, which we can then validate against
        this.csvReader = CSVParser.parse(file, Charset.defaultCharset(), csvFormat.withHeader());
        try {
            this.csvIterator = csvReader.iterator();

            //refactored out
            String[] expectedHeaders = getCsvHeaders(version);
            CsvHelper.validateCsvHeaders(csvReader, file, expectedHeaders);

            /*Map<String, Integer> headerMap = csvReader.getHeaderMap();
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
            }*/
        } catch (Exception e) {
            //if we get any exception thrown during the constructor, make sure to close the reader
            close();
            throw e;
        }
    }

    public void close() throws IOException {
        if (csvReader != null) {
            csvReader.close();
            csvReader = null;
        }
    }

    /*public void reset() throws Exception {

        //if we've opened the parser but only read the header, don't bother resetting
        if (getCurrentLineNumber() <= 1) {
            return;
        }

        close();
        open();
    }*/

    protected abstract String[] getCsvHeaders(String version);

    public boolean nextRecord() throws Exception {

        //we now only open the first set of parsers when starting a transform, so
        //need to check to open the subsequent ones
        if (csvReader == null) {
            open();
        }

        //if the source file couldn't be found, the iterator will be null
        if (csvIterator == null) {
            return false;
        }

        while (csvIterator.hasNext()) {
            this.csvRecord = csvIterator.next();

            if (csvReader.getCurrentLineNumber() % 50000 == 0) {
                LOG.trace("Starting line {} of {}", csvReader.getCurrentLineNumber(), file.getAbsolutePath());
            }

            //if we're restricting the record numbers to process, then check if the new line we're on is one we want to process
            if (recordNumbersToProcess == null
                || recordNumbersToProcess.contains(Long.valueOf(getCurrentLineNumber()))) {
                return true;

            } else {
                continue;

            }
        }

        //only log out we "completed" the file if we read any rows from it
        if (csvReader.getCurrentLineNumber() > 1) {
            LOG.info("Completed file {}", file.getAbsolutePath());
        }

        this.csvRecord = null;

        //automatically close the parser once we reach the end, to cut down on memory use
        close();

        return false;
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

    /**
     * called to restrict this parser to only processing specific rows
     */
    public void setRecordNumbersToProcess(Set<Long> recordNumbersToProcess) {
        this.recordNumbersToProcess = recordNumbersToProcess;
    }


    public String getString(String column) {
        return csvRecord.get(column);
    }
    public Integer getInt(String column) {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return Integer.valueOf(s);
    }
    public Long getLong(String column) {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return Long.valueOf(s);
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
    public boolean getBoolean(String column) {
        String s = csvRecord.get(column);
        if (Strings.isNullOrEmpty(s)) {
            return false;
        }

        return Boolean.parseBoolean(s);
    }

}
