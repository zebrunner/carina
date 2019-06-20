/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.qaprosoft.carina.core.foundation.exception.InvalidArgsException;
import com.qaprosoft.carina.core.foundation.exception.NotSupportedOperationException;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpreadsheetParser extends AbstractXLSParser {

    private static final Logger LOGGER = Logger.getLogger(SpreadsheetParser.class);

    public static XLSTable parseSpreadSheet(String spreadsheetId, String sheetName, String executeColumn, String executeValue) {
        XLSTable dataTable = prepareDataTable(executeColumn, executeValue);

        Spreadsheet spreadsheet = getSpreadsheetById(spreadsheetId);
        if(spreadsheet == null) {
            throw new InvalidArgsException(String.format("No spreadsheet by id: '%s'!", spreadsheetId));
        }

        Sheet sheet = getSheetByName(spreadsheet, sheetName);
        if(sheet == null) {
            throw new InvalidArgsException(String.format("No sheet: '%s' in spreadsheet: '%s'!", sheetName, spreadsheet.getSpreadsheetUrl()));
        }

        List<List<Object>> values = getSheetValues(sheet, spreadsheetId);
        IntStream.range(0, values.size() - 1).forEach(i -> {
            List<String> rowValues = (List<String>) castToStringCollection(values.get(i));
            if (i == 0) {
                dataTable.setHeaders(rowValues);
            } else {
                dataTable.addDataRow(rowValues);
            }
        });

        return dataTable;
    }

    private static Spreadsheet getSpreadsheetById(String spreadsheetId) {
        Spreadsheet result = null;
        try {
            result = retrieveService().spreadsheets().get(spreadsheetId).execute();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    private static List<List<Object>> getSheetValues(Sheet sheet, String spreadsheetId) {
        List<List<Object>> result = null;
        try {
            String range = buildRange(sheet.getProperties().getTitle(), sheet.getProperties().getGridProperties().getColumnCount(), sheet.getProperties().getGridProperties().getRowCount());
            ValueRange valueRange = retrieveService().spreadsheets().values().get(spreadsheetId, range).execute();
            result = valueRange.getValues();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    private static Collection<String> castToStringCollection(Collection<Object> collection) {
        return collection.stream().map(Object::toString).collect(Collectors.toList());
    }

    private static Sheet getSheetByName(Spreadsheet spreadsheet, String sheetName) {
        return spreadsheet.getSheets().stream().filter(sheet -> sheet.getProperties().getTitle().equals(sheetName)).findFirst().orElse(null);
    }

    private static String buildRange(String sheetName, int columnsCount, int rowsCount) {
        return String.format("%s!%s%d:%s%d", sheetName, "A", 1, Character.toString((char) (columnsCount + 96)).toUpperCase(), rowsCount);
    }

    private static Sheets retrieveService() {
        return ZafiraSingleton.INSTANCE.getClient().getSpreadsheetService().orElseThrow(() ->
                new NotSupportedOperationException("Google spreadsheet service is unavailable right now"));
    }
}
