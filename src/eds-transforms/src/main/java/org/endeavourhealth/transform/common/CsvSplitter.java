package org.endeavourhealth.transform.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CsvSplitter {

    private File srcFile = null;
    private File dstDir = null;
    private CSVFormat csvFormat = null;
    private String splitColumn = null;
    private String[] columnHeaders = null;
    private Map<String, CSVPrinter> csvPrinterMap = new HashMap<>();

    public CsvSplitter(File srcFile, File dstDir, CSVFormat csvFormat, String splitColumn) {

        this.srcFile = srcFile;
        this.dstDir = dstDir;
        this.csvFormat = csvFormat;
        this.splitColumn = splitColumn;
    }

    public void go() throws Exception {

        //adding .withHeader() to the csvFormat forces it to treat the first row as the column headers,
        //and read them in, instead of ignoring them
        CSVParser csvParser = CSVParser.parse(srcFile, Charset.defaultCharset(), csvFormat.withHeader());

        try
        {
            //validate the split column is present
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            Integer columnIndex = headerMap.get(splitColumn);
            if (columnIndex == null) {
                throw new IllegalArgumentException("No column [" + splitColumn + "] in " + srcFile);
            }
            int splitIndex = columnIndex.intValue();

            //convert the map into an ordered String array, so we can populate the column headers on new CSV files
            columnHeaders = new String[headerMap.size()];
            Iterator<String> headerIterator = headerMap.keySet().iterator();
            while (headerIterator.hasNext()) {
                String headerName = headerIterator.next();
                int headerIndex = headerMap.get(headerName);
                columnHeaders[headerIndex] = headerName;
            }

            //go through the content of the source file
            Iterator<CSVRecord> csvIterator = csvParser.iterator();
            while (csvIterator.hasNext()) {
                CSVRecord csvRecord = csvIterator.next();
                splitRecord(csvRecord, splitIndex);
            }

            //close all the csv printers created
            Iterator<CSVPrinter> printerIterator = csvPrinterMap.values().iterator();
            while (printerIterator.hasNext()) {
                CSVPrinter csvPrinter = printerIterator.next();
                csvPrinter.close();
            }

        } finally {
            csvParser.close();
        }

    }

    private void splitRecord(CSVRecord csvRecord, int index) throws Exception {

        String value = csvRecord.get(index);
        CSVPrinter csvPrinter = findCsvPrinter(value);
        csvPrinter.printRecord(csvRecord);
    }

    private CSVPrinter findCsvPrinter(String value) throws Exception {

        //the value is used as the child folder name, so replace any non-valid chars
        value = value.replaceAll("[:\\\\/*\"?|<>']", " ");

        CSVPrinter csvPrinter = csvPrinterMap.get(value);
        if (csvPrinter == null) {

            File folder = new File(dstDir, value);
            if (!folder.exists()
                && !folder.mkdirs()) {
                throw new FileNotFoundException("Couldn't create folder " + folder);
            }

            String fileName = srcFile.getName();
            File f = new File(folder, fileName);
            if (f.exists()) {
                throw new FileAlreadyExistsException(f.getAbsolutePath());
            }

            FileWriter fileWriter = new FileWriter(f);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            csvPrinter = new CSVPrinter(bufferedWriter, csvFormat);

            //write the headers
            for (String columnHeader: columnHeaders) {
                csvPrinter.print(columnHeader);
            }
            csvPrinter.println();

            csvPrinterMap.put(value, csvPrinter);
        }
        return csvPrinter;
    }


}
