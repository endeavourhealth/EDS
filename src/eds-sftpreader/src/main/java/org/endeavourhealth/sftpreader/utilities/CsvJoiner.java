package org.endeavourhealth.sftpreader.utilities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CsvJoiner {

    private List<File> srcFiles = null;
    private File dstFile = null;
    private CSVFormat csvFormat = null;

    public CsvJoiner(List<File> srcFiles, File dstFile, CSVFormat csvFormat) {
        this.srcFiles = srcFiles;
        this.dstFile = dstFile;
        this.csvFormat = csvFormat;
    }

    public void go() throws Exception {

        CSVPrinter csvPrinter = null;
        CSVParser csvParser = null;
        List<String> firstColumnHeaders = null;

        try {

            for (File srcFile : srcFiles) {

                csvParser = CSVParser.parse(srcFile, Charset.defaultCharset(), csvFormat.withHeader());

                //read the headers
                Map<String, Integer> headerMap = csvParser.getHeaderMap();
                String[] columnHeadersArray = new String[headerMap.size()];
                Iterator<String> headerIterator = headerMap.keySet().iterator();
                while (headerIterator.hasNext()) {
                    String headerName = headerIterator.next();
                    int headerIndex = headerMap.get(headerName);
                    columnHeadersArray[headerIndex] = headerName;
                }

                List<String> columnHeadersList = Arrays.asList(columnHeadersArray);

                //create the printer to the destination file if required
                if (csvPrinter == null) {
                    FileWriter fileWriter = new FileWriter(dstFile);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                    csvPrinter = new CSVPrinter(bufferedWriter, csvFormat.withHeader(columnHeadersArray));
                    firstColumnHeaders = columnHeadersList;

                } else {
                    //validate that the column headers are the same as for the first file
                    if (!columnHeadersList.equals(firstColumnHeaders)) {
                        throw new Exception("Column headers for " + srcFile + " aren't the same as the first file");
                    }
                }

                //simply print each record from the source into the destination
                Iterator<CSVRecord> csvIterator = csvParser.iterator();
                while (csvIterator.hasNext()) {
                    CSVRecord csvRecord = csvIterator.next();
                    csvPrinter.printRecord(csvRecord);
                }

                csvParser.close();
            }

        } finally {

            //make sure everything is closed
            if (csvParser != null) {
                csvParser.close();
            }
            if (csvPrinter != null) {
                csvPrinter.close();
            }
        }
    }
}
