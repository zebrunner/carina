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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class XLSCache {
    private static Map<String, Workbook> xlsCache = new HashMap<String, Workbook>();

    public static synchronized Workbook getWorkbook(String xlsPath) {
        if (!xlsCache.keySet().contains(xlsPath)) {
            Workbook wb;
            try {
                InputStream is = ClassLoader.getSystemResourceAsStream(xlsPath);
                try {
                    wb = WorkbookFactory.create(is);
                } finally {
                    // [VD] code cleanup based on Sonar 
                    is.close();
                    // if (is != null) {
                    //    is.close();
                    // }
                }
            } catch (Exception e) {
                throw new RuntimeException("Can't read xls: " + xlsPath);
            }
            xlsCache.put(xlsPath, wb);
        }
        return xlsCache.get(xlsPath);
    }

    public static synchronized String getWorkbookPath(Workbook book) {
        for (Entry<String, Workbook> entry : xlsCache.entrySet()) {
            if (entry.getValue() == book)
                return entry.getKey();
        }
        return null;
    }
}
