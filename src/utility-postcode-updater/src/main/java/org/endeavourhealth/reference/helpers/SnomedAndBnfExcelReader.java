package org.endeavourhealth.reference.helpers;
import java.io.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnomedAndBnfExcelReader {

    private static final Logger LOG = LoggerFactory.getLogger(SnomedAndBnfExcelReader.class);

    public void buildCSV(String csvFilePath, Sheet sheet, String bnfColumnName, String snomedColumnName) throws Exception {
        try {
            LOG.info("Start build CSV ");
            Row row = null;
            Cell bnfCodeCell = null;
            Cell snomedCell = null;

            int bnfColumn = -1;
            int snomedColumn = -1;
            int emptyCounter = 0;
            int j = 0;

            row = sheet.getRow(0);
            while (emptyCounter < 4) {
                Cell cell = row.getCell(j);
                if (cell != null) {
                    String value = cell.getStringCellValue();
                    if (value.equalsIgnoreCase(bnfColumnName)) {
                        bnfColumn = j;
                    }
                    if (value.equalsIgnoreCase(snomedColumnName)) {
                        snomedColumn = j;
                    }
                    if (StringUtils.isEmpty(value)) {
                        emptyCounter++;
                    }
                } else {
                    emptyCounter++;
                }
                j++;
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

            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                row = sheet.getRow(i);
                bnfCodeCell = row.getCell(bnfColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK );
                snomedCell = row.getCell(snomedColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (!isCellEmpty(bnfCodeCell) && !isCellEmpty(snomedCell)) {
                    csvPrinter.printRecord(bnfCodeCell, snomedCell);
                }
            }
            csvPrinter.close();

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
        Workbook wb = null;
        try {
            inp = new FileInputStream(xlsxFilePath);
            wb = WorkbookFactory.create(inp);
            for(int i=0; i<wb.getNumberOfSheets(); i++) {
                buildCSV(csvFilePath, wb.getSheetAt(i), bnfColumnName, snomedColumnName);
            }
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

    /**
     * Checks if the value of a given {@link Cell} is empty.
     *
     * @param cell
     *            The {@link Cell}.
     * @return {@code true} if the {@link Cell} is empty. {@code false}
     *         otherwise.
     */
    public boolean isCellEmpty(Cell cell) {
        if (cell == null) { // use row.getCell(x, Row.CREATE_NULL_AS_BLANK) to avoid null cells
            return true;
        }

        if (cell.getCellTypeEnum() == CellType.BLANK) {
            return true;
        }

        if (cell.getCellTypeEnum() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
            return true;
        }

        if (cell.getCellTypeEnum() == CellType.NUMERIC && cell.getNumericCellValue() == 0) {
            return true;
        }
        return false;
    }
}
