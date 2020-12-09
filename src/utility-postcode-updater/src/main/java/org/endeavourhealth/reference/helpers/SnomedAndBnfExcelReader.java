package org.endeavourhealth.reference.helpers;
import java.io.*;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnomedAndBnfExcelReader {

    private static final Logger LOG = LoggerFactory.getLogger(SnomedAndBnfExcelReader.class);

    private void buildCSV(String csvFilePath, List<Row> rows, String bnfColumnName, String snomedColumnName) throws Exception {
        try {
            LOG.info("Start build CSV ");

            int bnfColumn = -1;
            int snomedColumn = -1;

            Row row = rows.get(0);
            for (int i = 0; i < row.getCellCount(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    String value = cell.getRawValue();
                    if (value.equalsIgnoreCase(bnfColumnName)) {
                        bnfColumn = i;
                    }
                    if (value.equalsIgnoreCase(snomedColumnName)) {
                        snomedColumn = i;
                    }
                }
                if (bnfColumn != -1 && snomedColumn != -1) {
                    break;
                }
            }

            if (bnfColumn == -1) {
                LOG.error(bnfColumnName + " column not found in the excel file.");
                throw new Exception(bnfColumnName + " column not found in the excel file.");
            }
            if (snomedColumn == -1) {
                LOG.error(snomedColumnName + " column not found in the excel file.");
                throw new Exception(snomedColumnName + " column not found in the excel file.");
            }

            LOG.info(bnfColumnName + " column: " + bnfColumn);
            LOG.info(snomedColumnName + " column: " + snomedColumn);

            File dstFile = new File(csvFilePath);
            FileOutputStream fos = new FileOutputStream(dstFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osw);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("BNF_Code", "SNOMED_Code"));

            for (int i = 1; i < rows.size(); i++) {
                row = rows.get(i);
                String bnfCodeCell = row.getCell(bnfColumn).getText();
                String snomedCell = row.getCell(snomedColumn).getText();
                if (!StringUtils.isEmpty(bnfCodeCell) && !StringUtils.isEmpty(snomedCell)) {
                    csvPrinter.printRecord(bnfCodeCell, snomedCell);
                }
            }
            csvPrinter.close();
            writer.close();
            osw.close();
            fos.close();
        } catch(Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
        LOG.info("End build CSV ");
    }

    /**
     * @param xlsxFilePath the file path for input XLSX
     * @param csvFilePath the file path for csv file
     */
    public void createCSV(String xlsxFilePath, String csvFilePath, String bnfColumnName, String snomedColumnName) throws Exception {

        InputStream inp = null;
        ReadableWorkbook wb = null;
        try {
            inp = new FileInputStream(xlsxFilePath);
            wb = new ReadableWorkbook(inp);
            Sheet sheet = wb.getFirstSheet();
            List<Row> rows = sheet.read();
            buildCSV(csvFilePath, rows, bnfColumnName, snomedColumnName);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw ex;
        } finally {
            try {
                wb.close();
                inp.close();
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }
}
