package org.endeavourhealth.transform.emis.csv.schema;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.transform.common.TransformException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

public abstract class AbstractCsvTransformer {

    public static String DATE_FORMAT = "yyyyMMdd";

    private CSVParser csvReader = null;
    private Iterator<CSVRecord> csvIterator = null;
    private CSVRecord csvRecord = null;
    private DateFormat dateFormat = null;
    private DateFormat timeFormat = null;

    public AbstractCsvTransformer(CSVParser csvReader, String dateFormat, String timeFormat) {
        this.csvReader = csvReader;
        this.csvIterator = csvReader.iterator();
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.timeFormat = new SimpleDateFormat(timeFormat);
    }

    /*public CSVRecord currentRecord() {
        return csvRecord;
    }*/

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

    public String getString(int index) {
        return csvRecord.get(index);
    }
    public Integer getInt(int index) {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return new Integer(s);
    }
    public Long getLong(int index) {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return new Long(s);
    }
    public Double getDouble(int index) {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return new Double(s);
    }
    public Date getDate(int index) throws TransformException {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        try {
            return dateFormat.parse(s);
        } catch (ParseException pe) {
            throw new TransformException("Invalid date format [" + s + "]", pe);
        }
    }
    public Date getTime(int index) throws TransformException {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        try {
            return timeFormat.parse(s);
        } catch (ParseException pe) {
            throw new TransformException("Invalid time format [" + s + "]", pe);
        }
    }
    public Date getDateTime(int dateIndex, int timeIndex) throws TransformException {
        Date d = getDate(dateIndex);
        Date t = getTime(timeIndex);
        if (d == null) {
            return null;
        } else if (t == null) {
            return d;
        } else {
            return new Date(d.getTime() + t.getTime());
        }
    }
    public UUID getUniqueIdentifier(int index) {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }

        return UUID.fromString(s);
    }
    public boolean getBoolean(int index) {
        String s = csvRecord.get(index);
        if (Strings.isNullOrEmpty(s)) {
            return false;
        }

        return Boolean.parseBoolean(s);
    }
}
