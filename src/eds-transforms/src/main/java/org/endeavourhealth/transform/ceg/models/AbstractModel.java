package org.endeavourhealth.transform.ceg.models;

import org.apache.commons.csv.CSVPrinter;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractModel {


    public abstract void writeHeaderToCsv(CSVPrinter csvPrinter) throws Exception;
    public abstract void writeRecordToCsv(CSVPrinter csvPrinter) throws Exception;

    public abstract BigInteger getServiceProviderId();
    public abstract void setServiceProviderId(BigInteger serviceProviderId);

    protected void printDate(Date date, CSVPrinter csvPrinter) throws Exception {
        if (date == null) {
            printString(null, csvPrinter);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String s = sdf.format(date);
            printString(s, csvPrinter);
        }
    }
    protected void printTime(Date date, CSVPrinter csvPrinter) throws Exception {
        if (date == null) {
            printString(null, csvPrinter);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String s = sdf.format(date);
            printString(s, csvPrinter);
        }
    }
    protected void printString(String s, CSVPrinter csvPrinter) throws Exception {
        csvPrinter.print(s);
    }
    protected void printInt(Integer i, CSVPrinter csvPrinter) throws Exception {
        csvPrinter.print(i);
    }
    protected void printLong(Long l, CSVPrinter csvPrinter) throws Exception {
        csvPrinter.print(l);
    }
    protected void printBigInt(BigInteger l, CSVPrinter csvPrinter) throws Exception {
        csvPrinter.print(l);
    }
    protected void printBoolean(Boolean b, CSVPrinter csvPrinter) throws Exception {
        csvPrinter.print(b);
    }
    protected void printDouble(Double d, CSVPrinter csvPrinter) throws Exception {
        csvPrinter.print(d);
    }
}
