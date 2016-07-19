package org.endeavourhealth.transform.emis.csv.schema;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractCsvTransformer {

    public static String DATE_FORMAT = "yyyyMMdd";

    private CSVParser csvReader = null;
    private Iterator<CSVRecord> csvIterator = null;
    private CSVRecord csvRecord = null;
    private DateFormat dateFormat = null;
    private DateFormat timeFormat = null;

    public AbstractCsvTransformer(String folderPath, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        File file = new File(folderPath, getFileName());

        //calling withHeader() on the format, forces it to read in the first row as the headers, which we can then validate against
        this.csvReader = CSVParser.parse(file, Charset.defaultCharset(), csvFormat.withHeader());
        this.csvIterator = csvReader.iterator();
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.timeFormat = new SimpleDateFormat(timeFormat);

        Map<String, Integer> headerMap = csvReader.getHeaderMap();
        String[] expectedHeaders = getCsvHeaders();
        if (headerMap.size() != expectedHeaders.length) {
            throw new TransformException("Mismatch in number of CSV columns in " + getFileName() + " expected " + expectedHeaders.length + " but found " + headerMap.size());
        }

        for (int i=0; i<expectedHeaders.length; i++) {
            String expectedHeader = expectedHeaders[i];
            Integer mapIndex = headerMap.get(expectedHeader);
            if (mapIndex == null) {
                throw new TransformException("Missing column " + expectedHeader + " in " + getFileName());
            } else if (mapIndex.intValue() != i) {
                throw new TransformException("Out of order column " + expectedHeader + " in " + getFileName() + " expected at " + i + " but found at " + mapIndex);
            }
        }
    }

    private String getFileName() {
        return getClass().getSimpleName() + ".csv";
    }


    protected abstract String[] getCsvHeaders();

    public boolean nextRecord() {
        if (csvIterator.hasNext()) {
            this.csvRecord = csvIterator.next();
            return true;
        } else {
            this.csvRecord = null;
            return false;
        }
    }

    public void close() throws IOException {
        this.csvReader.close();
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
            throw new TransformException("Invalid date format [" + s + "]", pe);
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
            throw new TransformException("Invalid time format [" + s + "]", pe);
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
    public String getErrorLine() {
        return "Error processing line " + csvReader.getCurrentLineNumber() + " of " + getFileName();
    }
}
