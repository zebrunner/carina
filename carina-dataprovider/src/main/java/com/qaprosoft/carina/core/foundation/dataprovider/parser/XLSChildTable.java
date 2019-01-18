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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;

public class XLSChildTable extends XLSTable {
    public void addDataRow(Row row) {
        Map<String, String> dataMap = new HashMap<String, String>();
        for (int i = 0; i < super.getHeaders().size(); i++) {
            synchronized (dataMap) {
                dataMap.put(super.getHeaders().get(i), XLSParser.getCellValue(row.getCell(i)));
            }
        }
        super.getDataRows().add(dataMap);
    }
}
