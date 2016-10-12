package org.endeavourhealth.sftpreader.utilities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CsvSplitter {

    private static final Logger LOG = LoggerFactory.getLogger(CsvSplitter.class);

    private File srcFile = null;
    private File dstDir = null;
    private CSVFormat csvFormat = null;
    private String[] splitColumns = null;
    private String[] columnHeaders = null;
    private Map<String, CSVPrinter> csvPrinterMap = new HashMap<>();

    /*public static void main(String[] args) {

        try {
            javax.swing.JFileChooser f = new javax.swing.JFileChooser();
            f.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
            f.setMultiSelectionEnabled(false);
            f.setCurrentDirectory(new File("C:\\Users\\drewl\\Desktop\\EMIS CSV Sample"));

            int r = f.showOpenDialog(null);
            if (r == javax.swing.JFileChooser.CANCEL_OPTION) {
                return;
            }

            File file = f.getSelectedFile();

            File dst = new File("C:\\Users\\drewl\\Desktop");


            CsvSplitter splitter = new CsvSplitter(file, dst, CSVFormat.DEFAULT, "OrganisationGuid", "ProcessingId");
            splitter.go();

        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }*/

    public CsvSplitter(File srcFile, File dstDir, CSVFormat csvFormat, String... splitColumns) {

        this.srcFile = srcFile;
        this.dstDir = dstDir;
        this.csvFormat = csvFormat;
        this.splitColumns = splitColumns;
    }

    public void go() throws Exception {

        //adding .withHeader() to the csvFormat forces it to treat the first row as the column headers,
        //and read them in, instead of ignoring them
        CSVParser csvParser = CSVParser.parse(srcFile, Charset.defaultCharset(), csvFormat.withHeader());

        try
        {
            //validate the split columns are present
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            int[] splitIndexes = new int[splitColumns.length];

            for (int i=0; i<splitColumns.length; i++) {
                String splitColumn = splitColumns[i];

                Integer columnIndex = headerMap.get(splitColumn);
                if (columnIndex == null) {
                    throw new IllegalArgumentException("No column [" + splitColumn + "] in " + srcFile);
                }
                splitIndexes[i] = columnIndex.intValue();
            }

            //convert the map into an ordered String array, so we can populate the column headers on new CSV files
            columnHeaders = new String[headerMap.size()];
            Iterator<String> headerIterator = headerMap.keySet().iterator();
            while (headerIterator.hasNext()) {
                String headerName = headerIterator.next();
                int headerIndex = headerMap.get(headerName);
                columnHeaders[headerIndex] = headerName;
            }

            //go through the content of the source file
            //changing to also drop duplicated lines. The EMIS test pack has huge duplication of lines (see Admin_Orgamisation)
            //and when the exact same resource is inserted into Resource_History etc. in rapid succession, we get a failure
            //so just drop any lines that are exactly the same as the previous one
            Iterator<CSVRecord> csvIterator = csvParser.iterator();
            CSVRecord previousLine = null;
            while (csvIterator.hasNext()) {
                CSVRecord csvRecord = csvIterator.next();
                if (!isSame(csvRecord, previousLine)) {
                    splitRecord(csvRecord, splitIndexes);
                    previousLine = csvRecord;
                }
            }
            /*Iterator<CSVRecord> csvIterator = csvParser.iterator();
            while (csvIterator.hasNext()) {
                CSVRecord csvRecord = csvIterator.next();
                splitRecord(csvRecord, splitIndexes);
            }*/


        } finally {
            csvParser.close();

            //close all the csv printers created
            Iterator<CSVPrinter> printerIterator = csvPrinterMap.values().iterator();
            while (printerIterator.hasNext()) {
                CSVPrinter csvPrinter = printerIterator.next();
                csvPrinter.close();
            }
        }

    }

    private static boolean isSame(CSVRecord one, CSVRecord two) {
        if (one == null
                || two == null
                || one.size() != two.size()) {
            return false;
        }

        for (int i=0; i<one.size(); i++) {
            if (!one.get(i).equals(two.get(i))) {
                return false;
            }
        }

        return true;
    }

    private void splitRecord(CSVRecord csvRecord, int[] columnIndexes) throws Exception {

        String[] values = new String[columnIndexes.length];
        for (int i=0; i<values.length; i++) {
            values[i] = csvRecord.get(columnIndexes[i]);
        }

        CSVPrinter csvPrinter = findCsvPrinter(values);
        csvPrinter.printRecord(csvRecord);
    }

    private CSVPrinter findCsvPrinter(String[] values) throws Exception {

        String mapKey = String.join("_", values);

        CSVPrinter csvPrinter = csvPrinterMap.get(mapKey);
        if (csvPrinter == null) {

            File folder = new File(dstDir.getAbsolutePath());
            for (String value: values) {

                //ensure it can be used as a valid folder name
                value = value.replaceAll("[:\\\\/*\"?|<>']", " ");
                folder = new File(folder, value);

                if (!folder.exists()
                        && !folder.mkdirs()) {
                    throw new FileNotFoundException("Couldn't create folder " + folder);
                }
            }

            String fileName = srcFile.getName();
            File f = new File(folder, fileName);
            /*if (f.exists()) {
                throw new FileAlreadyExistsException(f.getAbsolutePath());
            }*/
            //LOG.debug("Creating " + f);
            FileWriter fileWriter = new FileWriter(f);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            csvPrinter = new CSVPrinter(bufferedWriter, csvFormat.withHeader(columnHeaders));

            csvPrinterMap.put(mapKey, csvPrinter);
        }
        return csvPrinter;
    }


}
