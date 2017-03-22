package org.endeavourhealth.transform.common;

import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractCsvWriter {

    private final String fileName;
    private final CSVFormat csvFormat;
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;

    private ByteArrayOutputStream byteOutput = null;
    private CSVPrinter csvPrinter;
    private int rowCount;

    public AbstractCsvWriter(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        this.fileName = fileName;
        this.csvFormat = csvFormat;
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.timeFormat = new SimpleDateFormat(timeFormat);
        this.rowCount = 0;
    }

    /**
     * syncrhonized so we can transform multiple resources to CSV in parallel, but not have problems writing them to file
     */
    protected synchronized void printRecord(String... columns) throws IOException {

        //changed to create the printer lazily, so the pseudonymised boolean is set in the Patient class
        //before we try to get the headers
        if (csvPrinter == null) {
            byteOutput = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(byteOutput);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            csvPrinter = new CSVPrinter(bufferedWriter, csvFormat.withHeader(getCsvHeaders()));
        }

        csvPrinter.printRecord((Object[])columns);
        rowCount ++;
    }

    /**
     * DateFormat class isn't thread safe, so synchronize all calls on it
     */
    protected synchronized String convertDate(Date d) {
        if (d == null) {
            return null;
        } else {
            return dateFormat.format(d);
        }
    }

    /**
     * DateFormat class isn't thread safe, so synchronize all calls on it
     */
    protected synchronized String convertTime(Date d) {
        if (d == null) {
            return null;
        } else {
            return timeFormat.format(d);
        }
    }

    protected String convertInt(Integer i) {
        if (i == null) {
            return null;
        } else {
            return i.toString();
        }
    }

    protected String convertLong(Long l) {
        if (l == null) {
            return null;
        } else {
            return l.toString();
        }
    }

    protected String convertBigDecimal(BigDecimal d) {
        if (d == null) {
            return null;
        } else {
            return d.toString();
        }
    }

    protected String convertBoolean(Boolean b) {
        if (b == null) {
            return null;
        } else if (b.booleanValue()) {
            return "TRUE";
        } else {
            return "FALSE";
        }
    }

    protected String convertBoolean(boolean b) {
        if (b) {
            return "TRUE";
        } else {
            return "FALSE";
        }
    }

    public byte[] close() throws IOException {
        csvPrinter.flush();
        csvPrinter.close();

        return byteOutput.toByteArray();
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileNameWithoutExtension() {
        return Files.getNameWithoutExtension(fileName);
    }

    /*public String getFileName() {
        Class cls = getClass();
        Package p = cls.getPackage();
        String[] packages = p.getName().split("\\.");
        String domain = packages[packages.length-1];
        String name = cls.getSimpleName();

        return domain + "_" + name + ".csv";
    }*/

    protected abstract String[] getCsvHeaders();

    public boolean isEmpty() {
        return rowCount == 0;
    }
}
