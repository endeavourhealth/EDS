package org.endeavourhealth.transform.common;

import org.apache.commons.csv.CSVParser;
import org.endeavourhealth.transform.common.exceptions.FileFormatException;

import java.io.File;
import java.util.Map;

public class CsvHelper {

    public static void validateCsvHeaders(CSVParser parser, File parserSource, String[] expectedHeaders) throws FileFormatException {

        Map<String, Integer> headerMap = parser.getHeaderMap();

        if (headerMap.size() != expectedHeaders.length) {
            throw new FileFormatException(parserSource, "Mismatch in number of CSV columns in " + parserSource + " expected " + expectedHeaders.length + " but found " + headerMap.size());
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            String expectedHeader = expectedHeaders[i];
            Integer mapIndex = headerMap.get(expectedHeader);

            if (mapIndex == null) {
                throw new FileFormatException(parserSource, "Missing column " + expectedHeader + " in " + parserSource);
            } else if (mapIndex.intValue() != i) {
                throw new FileFormatException(parserSource, "Out of order column " + expectedHeader + " in " + parserSource + " expected at " + i + " but found at " + mapIndex);
            }
        }
    }
}
