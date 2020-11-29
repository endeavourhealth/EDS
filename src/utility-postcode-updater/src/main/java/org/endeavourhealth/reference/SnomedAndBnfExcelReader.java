package org.endeavourhealth.reference;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.LoggerFactory;
/**
 * Uses Apache POI library to build csv from XLSX file
 */
public class SnomedAndBnfExcelReader {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SnomedAndBnfExcelReader.class);
    
    public  void buildCSV(String filePath, String csvFilePath, Sheet sheet) {
        try {
            LOG.info("Start build CSV ");
            File dstFile = new File(csvFilePath); //TODO
            FileOutputStream fos = new FileOutputStream(dstFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osw);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("BNF_Code", "SNOMED_Code")); //TODO
            Row row = null;
            Cell bnfCodeCell = null;
            Cell snomedCell = null;

            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                row = sheet.getRow(i);
                //bnfCodeCell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
                //snomedCell = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
                bnfCodeCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK ); //TODO column 2 = BNF Code and column 4 = SNOMED Code
                snomedCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                //if ((bnfCodeCell != null && bnfCodeCell.getStringCellValue().length() > 0) && (snomedCell != null && snomedCell.getNumericCellValue() > 0)) {
                if (!isCellEmpty(bnfCodeCell) && !isCellEmpty(snomedCell)) {
                    csvPrinter.printRecord(bnfCodeCell, snomedCell);
                }
            }
            csvPrinter.close();

        }catch(Exception e) {
            LOG.error("Error " + e.getMessage());
        }
        LOG.info("End build CSV ");
    }

    /**
     * @param xlsxFilePath the file path for input XLSX
     * @param csvFilePath the file path for csv file
     */
    public void createCSV(String xlsxFilePath, String csvFilePath) {
        InputStream inp = null;
        try {
            inp = new FileInputStream(xlsxFilePath);
            Workbook wb = WorkbookFactory.create(inp);
            for(int i=0; i<wb.getNumberOfSheets(); i++) {
                buildCSV(xlsxFilePath, csvFilePath, wb.getSheetAt(i));
            }
        } catch (InvalidFormatException ex) {
            Logger.getLogger(SnomedAndBnfExcelReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SnomedAndBnfExcelReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SnomedAndBnfExcelReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SnomedAndBnfExcelReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inp.close();
            } catch (IOException ex) {
                Logger.getLogger(SnomedAndBnfExcelReader.class.getName()).log(Level.SEVERE, null, ex);
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
