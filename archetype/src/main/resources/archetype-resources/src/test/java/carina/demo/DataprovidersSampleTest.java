#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.report.testrail.TestRailCases;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.core.registrar.tag.Priority;
import com.zebrunner.carina.core.registrar.tag.TestPriority;

/**
 * This sample shows how to use data-providers.
 *
 * @author qpsdemo
 */
public class DataprovidersSampleTest implements IAbstractTest {
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
     *
     * @param a String
     *
     * @param b String
     *
     * @param c String
     */
    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @TestRailCases(testCasesId = "1")
    @XlsDataSourceParameters(path = "xls/demo.xlsx", sheet = "Calculator", dsUid = "TUID", dsArgs = "a,b,c", testRailColumn = "a")
    public void testSumOperation(String a, String b, String c) {
        int actual = Integer.valueOf(a) + Integer.valueOf(b);
        int expected = Integer.valueOf(c);
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

    /**
     *
     * Parametrization using external XLS/XLSX: every row in spreadsheet provides tests arguments as Map&lt;String, String&gt; for 1 test.
     * For datasets with huge number of columns just remove dsArgs parameter to return whole row as single map object
     * <p>
     * 1. Specify data-provider type:
     * - @Test(dataProvider = "XLSDataProvider") allows parallel execution
     * - @Test(dataProvider = "SingleDataProvider") allows single-thread execution
     * 2. In @XlsDataSourceParameters should contain:
     * - path - xls/xlsx file path located in src/test/resources
     * - sheet - xls spreadsheet name
     * - dsUid - data-source unique identifier, use TUID or set of parameters
     *
     * @param args HashMap&lt;String, String&gt;
     */
    @Test(dataProvider = "DataProvider")
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
     *
     * @param TUID String
     *
     * @param testRailColumn String
     *
     * @param a int
     *
     * @param b int
     *
     * @param c int
     */
    @Test(dataProvider = "DP1")
    @MethodOwner(owner = "qpsdemo")
    @TestPriority(Priority.P3)
    @TestRailCases(testCasesId = "44")
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
     * @param a int
     *
     * @param b int
     *
     * @param c int
     */
    @Test()
    @MethodOwner(owner = "qpsdemo")
    @Parameters({"a", "b", "c"})
    @TestRailCases(testCasesId = "55")
    public void testSubstractOperation(int a, int b, int c) {
        int actual = Integer.valueOf(a) - Integer.valueOf(b);
        int expected = Integer.valueOf(c);
        Assert.assertEquals(actual, expected, "Invalid substract result!");
    }

}