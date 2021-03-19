#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013-2021 QAPROSOFT (http://qaprosoft.com/).
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
 */
package ${package}.carina.demo;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.report.testrail.TestRailCases;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestTag;

/**
 * This sample shows how to use data-providers.
 *
 * @author qpsdemo
 */
public class DataprovidersSampleTest extends AbstractTest {
    /**
     * Parametrization using external XLS/XLSX: every row in spreadsheet provides tests arguments set for 1 test.
     * <p>
     * 1. Specify data-provider type: 
     * - @Test(dataProvider = "XLSDataProvider") allows parallel execution
     * - @Test(dataProvider = "SingleDataProvider") allows single-thread execution 
     * 2. In @XlsDataSourceParameters should contain: 
     * - path - xls/xlsx file path located in src/test/resources
     * - sheet - xls spreadsheet name 
     * - dsUid - data-source unique identifier, use TUID or set of parameters 
     * - dsArgs - column names from spreadsheet
     */
    @Test(dataProvider = "DataProvider", description = "JIRA${symbol_pound}DEMO-0005")
    @MethodOwner(owner = "qpsdemo")
    @TestRailCases(testCasesId = "1")
    @XlsDataSourceParameters(path = "xls/demo.xlsx", sheet = "Calculator", dsUid = "TUID", dsArgs = "a,b,c", testRailColumn = "a")
    public void testSumOperation(String a, String b, String c) {
        int actual = Integer.valueOf(a) + Integer.valueOf(b);
        int expected = Integer.valueOf(c);
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }
    
    /**
     * Parametrization using external XLS/XLSX: every row in spreadsheet provides tests arguments as Map<String, String> for 1 test.
     * For datasets with huge number of columns just remove dsArgs parameter to return whole row as single map object  
     * <p>
     * 1. Specify data-provider type: 
     * - @Test(dataProvider = "XLSDataProvider") allows parallel execution
     * - @Test(dataProvider = "SingleDataProvider") allows single-thread execution 
     * 2. In @XlsDataSourceParameters should contain: 
     * - path - xls/xlsx file path located in src/test/resources
     * - sheet - xls spreadsheet name 
     * - dsUid - data-source unique identifier, use TUID or set of parameters 
     */
    @Test(dataProvider = "DataProvider", description = "JIRA${symbol_pound}DEMO-0005")
    @MethodOwner(owner = "qpsdemo")
    @TestRailCases(testCasesId = "1")
    @XlsDataSourceParameters(path = "xls/demo.xlsx", sheet = "Calculator", dsUid = "TUID", testRailColumn = "a")
    public void testSumOperationEx(HashMap<String, String> args) {
        int actual = Integer.valueOf(args.get("a")) + Integer.valueOf(args.get("b"));
        int expected = Integer.valueOf(args.get("c"));
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }
    
    /**
     * Paramatrization using TestNG dataproviders:
     * <p>
     * 1. Create data-provider method that returns Object[][] and set DataProvider annotation. 2. Specify data-provider
     * name in @Test annotation.
     */
    @Test(dataProvider = "DP1", description = "JIRA${symbol_pound}DEMO-0006")
    @MethodOwner(owner = "qpsdemo")
    @TestPriority(Priority.P3)
    @TestRailCases(testCasesId = "44")
    @TestTag(name = "area multi", value = "data provider multiply")
    public void testMuliplyOperation(String TUID, String testRailColumn, int a, int b, int c) {
        setCases(testRailColumn.split(","));
        int actual = a * b;
        int expected = c;
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

    @DataProvider(parallel = false, name = "DP1")
    public static Object[][] dataprovider() {
        return new Object[][]{
                {"TUID: Data1", "111,112", 2, 3, 6},
                {"TUID: Data2", "114", 6, 6, 36},
                {"TUID: Data3", "113", 5, 8, 40}};
    }

    /**
     * Parametrization using TestNG annotation @Parameters:
     * <p>
     * 1. List all parameter names in appropriate annotation. 2. Pass all parameters from TestNG xml file (check
     * test_suites/dataproviders.xml).
     */
    @Test(description = "JIRA${symbol_pound}DEMO-0007")
    @MethodOwner(owner = "qpsdemo")
    @Parameters({"a", "b", "c"})
    @TestRailCases(testCasesId = "55")
    public void testSubstractOperation(int a, int b, int c) {
        int actual = Integer.valueOf(a) - Integer.valueOf(b);
        int expected = Integer.valueOf(c);
        Assert.assertEquals(actual, expected, "Invalid substract result!");
    }

}