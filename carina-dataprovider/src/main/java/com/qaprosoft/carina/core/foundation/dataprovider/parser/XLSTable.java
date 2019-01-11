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

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class XLSTable {
    private static final Logger LOGGER = Logger.getLogger(XLSTable.class);
    private final static String FK_PREFIX = "FK_LINK_";

    private List<String> headers;
    private List<Map<String, String>> dataRows;
    private String executeColumn;
    private String executeValue;

    public XLSTable() {
        headers = new LinkedList<String>();
        dataRows = Collections.synchronizedList(new LinkedList<Map<String, String>>());
    }

    public XLSTable(String executeColumn, String executeValue) {
        this();
        this.executeColumn = executeColumn;
        this.executeValue = executeValue;
    }

    public void setHeaders(Row row) {
        headers.clear();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            headers.add(XLSParser.getCellValue(row.getCell(i)));
        }
    }

    public void addDataRow(Row row, Workbook wb, Sheet sheet) {
        if (row == null) {
            // don't add any data row if it is null. It seems like there is empty row in xls file
            return;
        }

        if (executeColumn != null && executeValue != null && row != null && headers.contains(executeColumn)) {
            if (!executeValue.equalsIgnoreCase(XLSParser.getCellValue(row.getCell(headers.indexOf(executeColumn))))) {
                return;
            }
        }

        XLSChildTable childRow = null;

        Map<String, String> dataMap = new HashMap<String, String>();
        LOGGER.debug("Loading data from row: ");
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header.startsWith(FK_PREFIX)) {
                childRow = XLSParser.parseCellLinks(row.getCell(i), wb, sheet);
            }

            synchronized (dataMap) {
                dataMap.put(header, XLSParser.getCellValue(row.getCell(i)));
            }
            LOGGER.debug(header + ": " + dataMap.get(header));
        }

        // If row has foreign key than merge headers and data
        if (childRow != null) {
            LOGGER.debug("Loading data from child row: ");
            for (int i = 0; i < childRow.getHeaders().size(); i++) {
                String currentHeader = childRow.getHeaders().get(i);

                if (StringUtils.isBlank(dataMap.get(currentHeader))) {
                    // Merge headers
                    if (!this.headers.contains(currentHeader))
                        this.headers.add(currentHeader);

                    // Merge data
                    synchronized (dataMap) {
                        dataMap.put(currentHeader, childRow.getDataRows().get(0).get(currentHeader));
                    }
                }
                LOGGER.debug(currentHeader + ": " + childRow.getDataRows().get(0).get(currentHeader));
            }
        }

        LOGGER.debug("Merged row: ");
        for (int i = 0; i < headers.size(); i++) {
            LOGGER.debug(headers.get(i) + ": " + dataMap.get(headers.get(i)));
        }

        dataRows.add(dataMap);
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<Map<String, String>> getDataRows() {
        return dataRows;
    }

    public String getExecuteColumn() {
        return executeColumn;
    }

    public void setExecuteColumn(String executeColumn) {
        this.executeColumn = executeColumn;
    }

    public String getExecuteValue() {
        return executeValue;
    }

    public void setExecuteValue(String executeValue) {
        this.executeValue = executeValue;
    }
}
