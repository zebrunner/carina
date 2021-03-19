/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.dataprovider.parser;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.exception.DataLoadingException;
import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;

public class XLSParser extends AbstractXLSParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static DataFormatter df;
    private static FormulaEvaluator evaluator;

    static {
        df = new DataFormatter();
    }

    public static String parseValue(String locatorKey, String xlsPath, Locale locale) {
        String value;

        Workbook wb = XLSCache.getWorkbook(xlsPath);
        Sheet sheet = wb.getSheetAt(0);

        List<String> locales = getLocales(sheet);
        if (!locales.contains(locale.getCountry())) {
            throw new RuntimeException("Can't find locale '" + locale.getCountry() + "' in xls '" + xlsPath + "'!");
        }
        int cellN = locales.indexOf(locale.getCountry()) + 1;

        List<String> locatorKeys = getLocatorKeys(sheet);
        if (!locatorKeys.contains(locatorKey)) {
            throw new RuntimeException("Can't find locatorKey '" + locatorKey + "' in xls '" + xlsPath + "'!");
        }
        int rowN = locatorKeys.indexOf(locatorKey) + 1;

        try {
            value = getCellValue(sheet.getRow(rowN).getCell(cellN));
        } catch (Exception e) {
            throw new RuntimeException("Can't find value for locatorKey '" + locatorKey + "' with locale '" + locale.getCountry()
                    + "' in xls '" + xlsPath + "'!");
        }

        return value;
    }

    private static List<String> getLocales(Sheet sheet) {
        List<String> locales = new ArrayList<String>();
        int lastCell = sheet.getRow(0).getLastCellNum();
        for (int i = 1; i < lastCell; i++) {
            locales.add(getCellValue(sheet.getRow(0).getCell(i)));
        }
        return locales;
    }

    private static List<String> getLocatorKeys(Sheet sheet) {
        List<String> locatorKeys = new ArrayList<String>();
        int lastRow = sheet.getLastRowNum();
        for (int i = 1; i <= lastRow; i++) {
            locatorKeys.add(getCellValue(sheet.getRow(i).getCell(0)));
        }
        return locatorKeys;
    }

    public static String parseValue(String xls, String sheetName, String key) {
        String value = null;

        Workbook wb = XLSCache.getWorkbook(xls);

        Sheet sheet = wb.getSheet(sheetName);
        if (sheet == null) {
            throw new InvalidArgsException(String.format("No sheet: '%s' in excel file: '%s'!", sheetName, xls));
        }

        boolean isKeyFound = false;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            if (key.equals(getCellValue(sheet.getRow(i).getCell(0)))) {
                value = getCellValue(sheet.getRow(i).getCell(1));
                isKeyFound = true;
                break;
            }
        }

        if (!isKeyFound) {
            throw new InvalidArgsException(String.format("No key: '%s' on sheet '%s' in excel file: '%s'!", key, sheetName, xls));
        }

        return value;
    }

    public static XLSTable parseSpreadSheet(String xls, String sheetName) {
        return parseSpreadSheet(xls, sheetName, null, null);
    }

    public static XLSTable parseSpreadSheet(String xls, String sheetName, String executeColumn, String executeValue) {
        XLSTable dataTable = prepareDataTable(executeColumn, executeValue);

        Workbook wb = XLSCache.getWorkbook(xls);
        evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sheet = wb.getSheet(sheetName);
        if (sheet == null) {
            throw new InvalidArgsException(String.format("No sheet: '%s' in excel file: '%s'!", sheetName, xls));
        }

        try {
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                if (i == 0) {
                    dataTable.setHeaders(sheet.getRow(i));
                } else {
                    dataTable.addDataRow(sheet.getRow(i), wb, sheet);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return dataTable;
    }

    public static String getCellValue(Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
        case STRING:
            return df.formatCellValue(cell).trim();
        case NUMERIC:
            return df.formatCellValue(cell).trim();
        case BOOLEAN:
            return df.formatCellValue(cell).trim();
        case FORMULA:
            return (cell.getCellFormula().contains("[") && cell.getCellFormula().contains("]")) ? null : df.formatCellValue(cell, evaluator).trim();
        case BLANK:
            return "";
        default:
            return null;
        }
    }

    public static XLSChildTable parseCellLinks(Cell cell, Workbook wb, Sheet sheet) {
        if (cell == null)
            return null;

        if (cell.getCellType() == CellType.FORMULA) {
            if (cell.getCellFormula().contains("#This Row")) {
                if (cell.getCellFormula().contains("!")) {
                    // Parse link to the cell with table name in the external doc([2]!Table1[[#This Row],[Header6]])
                    List<String> paths = Arrays.asList(cell.getCellFormula().split("!"));
                    int externalLinkNumber = Integer.valueOf(paths.get(0).replaceAll("\\D+", "")) - 1;
                    String tableName = paths.get(1).split("\\[")[0];
                    if (wb instanceof XSSFWorkbook) {
                        ExternalLinksTable link = ((XSSFWorkbook) wb).getExternalLinksTable().get(externalLinkNumber);
                        File file = new File(XLSCache.getWorkbookPath(wb));
                        XSSFWorkbook childWb = (XSSFWorkbook) XLSCache.getWorkbook(file.getParent() + "/" + link.getLinkedFileName());
                        if (childWb == null)
                            throw new DataLoadingException(String.format("WorkBook '%s' doesn't exist!", link.getLinkedFileName()));
                        for (int i = 0; i < childWb.getNumberOfSheets(); i++) {
                            XSSFSheet childSheet = childWb.getSheetAt(i);
                            for (XSSFTable table : childSheet.getTables()) {
                                if (table.getName().equals(tableName)) {
                                    return createChildTable(childSheet, cell.getRowIndex());
                                }
                            }
                        }
                    } else {
                        throw new DataLoadingException("Unsupported format. External links supports only for .xlsx documents.");
                    }
                } else {
                    // Parse link to the cell with table name in the same doc(=Table1[[#This Row],[Header6]])
                    List<String> paths = Arrays.asList(cell.getCellFormula().replace("=", "").split("\\["));
                    if (wb instanceof XSSFWorkbook) {
                        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                            XSSFSheet childSheet = (XSSFSheet) wb.getSheetAt(i);
                            for (XSSFTable table : childSheet.getTables()) {
                                if (table.getName().equals(paths.get(0))) {
                                    return createChildTable(childSheet, cell.getRowIndex());
                                }
                            }
                        }
                    } else {
                        throw new DataLoadingException("Unsupported format. Links with table name supports only for .xlsx documents.");
                    }
                }
            } else {
                String cellValue = cell.getCellFormula().replace("=", "").replace("[", "").replace("]", "!").replace("'", "");
                List<String> paths = Arrays.asList(cellValue.split("!"));
                int rowNumber = 0;
                Sheet childSheet = null;

                switch (paths.size()) {
                // Parse link to the cell in the same sheet(=A4)
                case 1:
                    rowNumber = Integer.valueOf(paths.get(0).replaceAll("\\D+", "")) - 1;
                    return createChildTable(sheet, rowNumber);
                // Parse link to the cell in another sheet in the same doc(=SheetName!A4)
                case 2:
                    childSheet = wb.getSheet(paths.get(0));
                    if (childSheet == null)
                        throw new DataLoadingException(String.format("Sheet '%s' doesn't exist!", paths.get(0)));
                    rowNumber = Integer.valueOf(paths.get(1).replaceAll("\\D+", "")) - 1;
                    return createChildTable(childSheet, rowNumber);
                // Parse link to the cell in another doc(=[2]SheetName!A4)
                case 3:
                    if (wb instanceof XSSFWorkbook) {
                        ExternalLinksTable link = ((XSSFWorkbook) wb).getExternalLinksTable().get(Integer.valueOf(paths.get(0)) - 1);
                        File file = new File(XLSCache.getWorkbookPath(wb));
                        XSSFWorkbook childWb = (XSSFWorkbook) XLSCache.getWorkbook(file.getParent() + "/" + link.getLinkedFileName());

                        if (childWb == null)
                            throw new DataLoadingException(String.format("WorkBook '%s' doesn't exist!", paths.get(0)));
                        childSheet = childWb.getSheet(paths.get(1));
                        if (childSheet == null)
                            throw new DataLoadingException(String.format("Sheet '%s' doesn't exist!", paths.get(0)));
                        rowNumber = Integer.valueOf(paths.get(2).replaceAll("\\D+", "")) - 1;
                        return createChildTable(childSheet, rowNumber);
                    } else {
                        throw new DataLoadingException("Unsupported format. External links supports only for .xlsx documents.");
                    }
                default:
                    return null;
                }
            }
        }
        return null;
    }

    private static XLSChildTable createChildTable(Sheet sheet, int rowNumber) {
        XLSChildTable childTable = new XLSChildTable();
        childTable.setHeaders(sheet.getRow(0));
        childTable.addDataRow(sheet.getRow(rowNumber));
        return childTable;
    }
}
