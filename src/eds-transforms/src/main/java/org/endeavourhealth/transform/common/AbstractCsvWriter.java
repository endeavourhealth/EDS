package org.endeavourhealth.transform.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractCsvWriter {

    private final String fileName;
    private ByteArrayOutputStream byteOutput = null;
    private final CSVPrinter csvPrinter;
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;

    public AbstractCsvWriter(String fileName, CSVFormat csvFormat, String dateFormat, String timeFormat) throws Exception {

        byteOutput = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteOutput);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);

        csvPrinter = new CSVPrinter(bufferedWriter, csvFormat.withHeader(getCsvHeaders()));

        this.fileName = fileName;
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.timeFormat = new SimpleDateFormat(timeFormat);
    }

    protected void printRecord(String... columns) throws IOException {
        csvPrinter.printRecord((Object[])columns);
    }

    protected String convertDate(Date d) {
        if (d == null) {
            return null;
        } else {
            return dateFormat.format(d);
        }
    }

    protected String convertTime(Date d) {
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

    protected String convertDouble(Double d) {
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

    /*public String getFileName() {
        Class cls = getClass();
        Package p = cls.getPackage();
        String[] packages = p.getName().split("\\.");
        String domain = packages[packages.length-1];
        String name = cls.getSimpleName();

        return domain + "_" + name + ".csv";
    }*/

    protected abstract String[] getCsvHeaders();
}
