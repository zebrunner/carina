#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import org.apache.commons.math3.util.Precision;

import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.dataprovider.IAbstractDataProvider;
import com.zebrunner.carina.dataprovider.annotations.CsvDataSourceParameters;
import com.zebrunner.carina.dataprovider.annotations.XlsDataSourceParameters;

/**
 * <p>This sample shows how to use carina's custom data-providers.</p>
 * <p>Steps:</p>
 * <ol>
 *     <li>Implement IAbstractDataProvider interface for a test class;
 *     <li>Specify usage of carina's custom data provider for a test method:
 *      <ul>
 *         <li>@Test(dataProvider = "DataProvider") allows parallel execution
 *         <li>@Test(dataProvider = "SingleDataProvider") allows single-thread execution
 *      </ul>
 *     <li>Specify data-provider type:
 *      <ul>
 *          <li>@XlsDataSourceParameters for creating data-provider from xls/xlsx file
 *          <li>@CsvDataSourceParameters for creating data-provider from csv file
 *      </ul>
 *     <li>Add some columns to data-source file:
 *      <ul>
 *          <li>Execute - mark rows that should be added to test run with 'y'
 *          <li>TUID - unique test identifier
 *      </ul>
 * </ol>
 *
 * @author qpsdemo
 */
public class CustomDataProvidersSampleTest implements IAbstractTest, IAbstractDataProvider {

    /**
     * Parametrization using external xls/xlsx:
     * <br>
     * Every row in spreadsheet provides tests arguments set for 1 test.
     * <p>{@link XlsDataSourceParameters} annotation should contain:</p>
     *  <ul>
     *      <li>path - xls/xlsx file path located in src/test/resources
     *      <li>sheet - xls spreadsheet name
     *      <li>dsUid - column name from spreadsheet with unique identifiers
     *      <li>dsArgs - column names from spreadsheet that contains test value
     * </ul>
     *
     * @param a String
     * @param b String
     * @param c String
     */
    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @XlsDataSourceParameters(path = "data_source/demo.xlsx", sheet = "Calculator", dsUid = "TUID", dsArgs = "a,b,c")
    public void testXlsSumOperation(String a, String b, String c) {
        int actual = Integer.valueOf(a) + Integer.valueOf(b);
        int expected = Integer.valueOf(c);
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

    /**
     * Parametrization using external csv:
     * <br>
     * Every row in table provides tests arguments set for 1 test.
     * <p>{@link CsvDataSourceParameters} annotation should contain:</p>
     *  <ul>
     *      <li>path - csv file path located in src/test/resources
     *      <li>dsUid - column name of data-source unique identifiers
     *      <li>dsArgs - column names from spreadsheet that contains test value
     * </ul>
     *
     * @param a String
     * @param b String
     * @param c String
     */
    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @CsvDataSourceParameters(path = "data_source/calculator.csv", dsUid = "TUID", dsArgs = "a,b,c")
    public void testCsvSumOperation(String a, String b, String c) {
        int actual = Integer.valueOf(a) + Integer.valueOf(b);
        int expected = Integer.valueOf(c);
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

    /**
     * Parametrization using external xls/xlsx:
     * <br>
     * Every row in spreadsheet provides tests arguments as Map&lt;String, String&gt; for 1 test.
     * <p>
     * For datasets with huge number of columns just remove dsArgs parameter to return whole row as single map object
     * </p>
     *
     * @param args HashMap&lt;String, String&gt;
     */
    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @XlsDataSourceParameters(path = "data_source/demo.xlsx", sheet = "Calculator", dsUid = "TUID")
    public void testSumOperationFromMap(HashMap<String, String> args) {
        int actual = Integer.valueOf(args.get("a")) + Integer.valueOf(args.get("b"));
        int expected = Integer.valueOf(args.get("c"));
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

    /**
     * Grouped parametrization using external csv:
     * <p>
     * Every row in table being grouped by value from
     * selected groupColumn and provides tests arguments as List&lt;Map&lt;String, String&gt;&gt; for 1 test.
     * </p>
     * <ul>
     *      <li>List includes all rows that were grouped by unique value
     *      <li>If dsArgs were specified, map will contain only specified arguments. If not - map will contain all arguments from row
     * </ul>
     *
     * @param args List&lt;Map&lt;String, String&gt;&gt;
     */
    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @CsvDataSourceParameters(path = "data_source/finances.csv", dsUid = "CompanyID", dsArgs = "income, expenses, country", groupColumn = "country")
    public void testDataSourceGroup(List<Map<String, String>> args) {
        double expectedRegionProfit = 0.05 * 100;
        String region = args.get(0).get("country");

        SoftAssert softAssert = new SoftAssert();
        double totalIncomeRegion = 0;
        double totalExpensesRegion = 0;
        for (Map<String, String> row : args) {
            totalIncomeRegion += Double.parseDouble(row.get("income"));
            totalExpensesRegion += Double.parseDouble(row.get("expenses"));
            softAssert.assertEquals(region, row.get("country"), "Should be grouped by region " + region);
        }
        softAssert.assertAll();

        double actualRegionProfit = Precision.round((totalIncomeRegion / totalExpensesRegion - 1) * 100, 2);

        Assert.assertTrue(actualRegionProfit >= expectedRegionProfit,
                "Actual profit for " + region + " is lower than expected${symbol_escape}n" +
                        "Expected: " + expectedRegionProfit + "%${symbol_escape}n" +
                        "Actual: " + actualRegionProfit + "%${symbol_escape}n" +
                        "Expected to fail for IT region${symbol_escape}n"
        );
    }

    /**
     * Usage of others Carina's custom dataProvider annotation parameters
     * <p>Common annotation parameters for data sources could contain:</p>
     *  <ul>
     *      <li>staticArgs - value from testNG suite by defined static arguments name. The same argument will present in every test
     *      <li>testMethodColumn - column name which contains values for test name overriding
     *      <li>executeColumn - column name that determines whether to add row to test test or not. <br>Default - 'Execute'
     *      <li>executeValue - value for adding test to run if it equals to value from executeColumn. <br>Default - 'y'
     * </ul>
     * <p>{@link CsvDataSourceParameters} can also contain:</p>
     *  <ul>
     *      <li>separator - defines column separator for parsing. <br>Default - ','
     *      <li>quote - defines the character to use for quoted elements when parsing. <br> Default - '"'
     * </ul>
     * <p>{@link XlsDataSourceParameters} can also contain:</p>
     *  <ul>
     *      <li>sheet - Defines sheet to parse
     *      <li>spreadsheetId - Defines spreadsheet's id. Is mutually exclusive with path
     * </ul>
     *
     * @param args String
     * @param expectedValue String
     */
    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @XlsDataSourceParameters(path = "data_source/demo.xlsx", sheet = "data",dsUid = "TUID", dsArgs = "Args", staticArgs = "expectedValue",
            testMethodColumn = "TestTitle", executeColumn = "Execute", executeValue = "y")
    public void staticParamTest(String args, String expectedValue) {
        Assert.assertTrue(args.contains(expectedValue));
    }

}
