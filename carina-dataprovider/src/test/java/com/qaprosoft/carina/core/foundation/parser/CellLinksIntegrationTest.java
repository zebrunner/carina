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
package com.qaprosoft.carina.core.foundation.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSTable;

public class CellLinksIntegrationTest {
    private List<String> headers;
    private List<String> firstRow;
    private List<String> secondRow;
    private List<String> thirdRow;
    private List<String> fourthRow;
    private List<String> fifsRow;
    private List<String> sixthRow;

    @BeforeTest
    public void setUp() {
        headers = new ArrayList<String>();
        headers.add("Header1");
        headers.add("FK_LINK_HEADER_2");
        headers.add("Header3");
        headers.add("Header7");
        headers.add("Header8");
        headers.add("Header9");
        headers.add("Header4");
        headers.add("Header5");
        headers.add("Header6");

        firstRow = new ArrayList<String>();
        firstRow.add("Data1");
        firstRow.add(null);
        firstRow.add("Data3");
        firstRow.add("Data7");
        firstRow.add("Data8");
        firstRow.add("Data9");
        firstRow.add(null);
        firstRow.add(null);
        firstRow.add(null);

        secondRow = new ArrayList<String>();
        secondRow.add("Name1");
        secondRow.add(null);
        secondRow.add("Name3");
        secondRow.add("User7");
        secondRow.add("User8");
        secondRow.add("User9");
        secondRow.add(null);
        secondRow.add(null);
        secondRow.add(null);

        thirdRow = new ArrayList<String>();
        thirdRow.add("User1");
        thirdRow.add(null);
        thirdRow.add("User3");
        thirdRow.add("");
        thirdRow.add("");
        thirdRow.add("");
        thirdRow.add("User4");
        thirdRow.add("User5");
        thirdRow.add("User6");

        fourthRow = new ArrayList<String>();
        fourthRow.add("Temp1");
        fourthRow.add("Data5");
        fourthRow.add("Temp3");
        fourthRow.add("");
        fourthRow.add("");
        fourthRow.add("");
        fourthRow.add("Data4");
        fourthRow.add("Data5");
        fourthRow.add("Data6");

        fifsRow = new ArrayList<String>();
        fifsRow.add("About1");
        fifsRow.add("Data1");
        fifsRow.add("About3");
        fifsRow.add("");
        fifsRow.add("");
        fifsRow.add("");
        fifsRow.add("");
        fifsRow.add("");
        fifsRow.add("");

        sixthRow = new ArrayList<String>();
        sixthRow.add("New1");
        sixthRow.add("New2");
        sixthRow.add("New3");
        sixthRow.add("");
        sixthRow.add("");
        sixthRow.add("");
        sixthRow.add("");
        sixthRow.add("");
        sixthRow.add("");
    }

    @Test
    public void testCellLinksForCurrentWB() {
        XLSTable table = XLSParser.parseSpreadSheet("xlsx_data/ParentTest.xlsx", "Sheet1");
        verifyHeaders(table.getHeaders());
        verifyDataRow(firstRow, table.getHeaders(), table.getDataRows().get(0));
        verifyDataRow(secondRow, table.getHeaders(), table.getDataRows().get(1));
        verifyDataRow(thirdRow, table.getHeaders(), table.getDataRows().get(2));
        verifyDataRow(fourthRow, table.getHeaders(), table.getDataRows().get(3));
        verifyDataRow(fifsRow, table.getHeaders(), table.getDataRows().get(4));
        verifyDataRow(sixthRow, table.getHeaders(), table.getDataRows().get(5));
    }

    private void verifyHeaders(List<String> actualHeaders) {
        for (int i = 0; i < headers.size(); i++) {
            Assert.assertEquals(actualHeaders.get(i), headers.get(i));
        }
    }

    private void verifyDataRow(List<String> expected, List<String> headers, Map<String, String> dataRow) {
        for (int i = 0; i < headers.size(); i++) {
            Assert.assertEquals(dataRow.get(headers.get(i)), expected.get(i));
        }
    }
}
