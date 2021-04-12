package main;

import helpers.NTFileHelper;
import helpers.NTLogHelper;

import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ParamFileReader {
    private static NTLogHelper logger = new NTLogHelper(ParamFileReader.class);

    private static final String REGEXP_LINE_CHECK = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final String CSV_FILE_SEPARATOR = ",";
    private static final String CSV_EXTENSION = "csv";
    private static final String XLSX_EXTENSION = "xlsx";

    // first list - always params names
    // first value of every list - always script name
    private List<List<String>> allValues = new ArrayList<>();

    public ParamFileReader() {
    }

    public void readFile(Path filePath) {
        if (filePath.toString().matches(".*" + CSV_EXTENSION)) {
            logger.logInfo("-- file type: " + CSV_EXTENSION + " -> reading..");
            readCSVFile(filePath);
            return;
        }
        if (filePath.toString().matches(".*" + XLSX_EXTENSION)) {
            logger.logInfo("-- file type: " + XLSX_EXTENSION + " -> reading..");
            readXLSXFile(filePath);
            return;
        }
        logger.logError("wrong param file extension! It has to be " + XLSX_EXTENSION + " or " + CSV_EXTENSION +
                " || file: " + filePath.toString());
    }

    public List<String> getParamNames() {
        return allValues.get(0);
    }

    public List<String> getParamValues(String scriptName) {
        if (scriptName == null || scriptName.isEmpty()) {
            return null;
        }
        for (List<String> oneScriptParams : allValues) {
            if (oneScriptParams.get(0).equals(scriptName))
                return oneScriptParams;
        }
        return null;
    }

    private void readCSVFile(Path filePath) {
        List<String> foundLines = new ArrayList<>();
        try {
            foundLines.addAll(NTFileHelper.getFileInfo(filePath.toString()));
        } catch (Exception ex) {
            logger.logError("can't read file: " + filePath.toString(), ex);
            return;
        }
        if (foundLines.size() <= 0) {
            logger.logError("empty file " + filePath.toString());
            return;
        }
        logger.logInfo("-- file was read. lines count: " + foundLines.size());
        for (String foundLine : foundLines) {
            if (null == foundLine || foundLine.trim().isEmpty() || !foundLine.contains(CSV_FILE_SEPARATOR)) {
                continue;
            }
            List<String> foundValues = new ArrayList<>(Arrays.asList(foundLine.split(REGEXP_LINE_CHECK)));
            if (isListsOK(foundValues)) {
                allValues.add(foundValues);
            }
        }
    }

    private void readXLSXFile(Path filePath) {
        try (InputStream inp = new FileInputStream(filePath.toString())) {
            // get workbook
            Workbook wb = WorkbookFactory.create(inp);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            // get first sheet
            Sheet sheet = wb.getSheetAt(0);
            logger.logInfo("-- reading sheet: " + sheet.getSheetName());
            // get last created row
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 1) {
                logger.logError("wrong number of rows in file! found " + lastRowNum + " rows");
                return;
            }
            logger.logInfo("-- rows count: " + lastRowNum);
            // check all found rows
            for (int i = 0; i <= lastRowNum; i++) {
                Row currentRow = sheet.getRow(i);
                if (null == currentRow)
                    continue;
                if (!getStringValueFromCell(currentRow.getCell(0), evaluator).trim().isEmpty()) {
                    List<String> foundValues = new ArrayList<>(getValuesFromRow(currentRow, evaluator));
                    if (isListsOK(foundValues)) {
                        allValues.add(foundValues);
                    }
                }
            }
        } catch (Exception ex) {
            logger.logError("can't read workbook in file: " + filePath.toString(), ex);
        }
    }

    private List<String> getValuesFromRow(Row row, FormulaEvaluator evaluator) {
        List<String> valuesList = new ArrayList<>();
        // get last cell + 1
        int lastCellNum = row.getLastCellNum();
        // check all found cells
        for (int j = 0; j < lastCellNum; j++) {
            Cell cell = row.getCell(j);
            if (null == cell)
                continue;
            String foundValue = getStringValueFromCell(cell, evaluator);
            if (!foundValue.isEmpty())
                valuesList.add(foundValue);
        }
        return valuesList;
    }

    private String getStringValueFromCell(Cell cell, FormulaEvaluator evaluator) {
        if (null == cell)
            return "";
        CellValue cellValue = evaluator.evaluate(cell);
        switch (cellValue.getCellType()) {
            case STRING:
                String cellStringValue = cellValue.getStringValue();
                if (cellStringValue == null || cellStringValue.trim().isEmpty()) {
                    return "";
                }
                return cellStringValue;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm");
                    return dateFormat.format(date);
                }
                double numericCellValue = cellValue.getNumberValue();
                return optimiseDoubleValue(numericCellValue);
            case BOOLEAN:
                boolean boolCellValue = cellValue.getBooleanValue();
                return String.valueOf(boolCellValue);
            case BLANK:
                logger.logInfo("found empty cell: " + cell.getAddress());
                break;
            case ERROR:
                logger.logError("found error in cell: " + cell.getAddress());
                break;
            default:
                logger.logError("unknown cell type! found type: " + cellValue.getCellType() +
                        " || in cell: " + cell.getAddress());
                break;
        }
        return "";
    }

    private String optimiseDoubleValue(double dbExcelParam) {
        String excelParam = String.valueOf(dbExcelParam);
        String[] paramParts = excelParam.split("\\.");
        if (paramParts.length != 2)
            return excelParam;
        return (excelParam.matches(".*\\.0$")) ? paramParts[0] : excelParam;
    }

    private boolean isListsOK(List<String> values) {
        if (allValues.size() <= 0)
            return true;
        boolean isOK = allValues.get(0).size() == values.size();
        if (!isOK) {
            logger.logError("params names and values count are different" +
                    " || found names count: " + allValues.get(0).size() +
                    " || found values count: " + values.size() +
                    " || names: " + allValues.get(0).toString() +
                    " || values: " + values.toString());
        }
        return isOK;
    }
}
