package org.endeavourhealth.core.csv;

import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.util.Map;

public class CsvHelper {

    public static void validateCsvHeaders(CSVParser parser, File parserSource, String[] expectedHeaders) throws IllegalArgumentException {

        Map<String, Integer> headerMap = parser.getHeaderMap();

        if (headerMap.size() != expectedHeaders.length) {
            throw new IllegalArgumentException("Mismatch in number of CSV columns in " + parserSource + " expected " + expectedHeaders.length + " but found " + headerMap.size());
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            String expectedHeader = expectedHeaders[i];
            Integer mapIndex = headerMap.get(expectedHeader);

            if (mapIndex == null) {
                throw new IllegalArgumentException("Missing column " + expectedHeader + " in " + parserSource);
            } else if (mapIndex.intValue() != i) {
                throw new IllegalArgumentException("Out of order column " + expectedHeader + " in " + parserSource + " expected at " + i + " but found at " + mapIndex);
            }
        }
    }
}
