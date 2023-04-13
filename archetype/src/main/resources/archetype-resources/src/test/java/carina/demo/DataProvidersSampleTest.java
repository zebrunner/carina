#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.report.testrail.TestRailCases;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.core.registrar.tag.Priority;
import com.zebrunner.carina.core.registrar.tag.TestPriority;

/**
 * This sample shows how to use data-providers by TestNG approach.
 *
 * @author qpsdemo
 */
public class DataProvidersSampleTest implements IAbstractTest {
    /**
     * Parametrization using TestNG dataproviders:
     * <ol>
     *     <li>Create data-provider method that returns Object[][] and set DataProvider annotation.</li>
     *     <li>Specify data-provider name in @Test annotation.</li>
     * </ol>
     *
     * @param TUID           String unique test identifier
     * @param testRailColumn String
     * @param a              int
     * @param b              int
     * @param c              int
     */
    @Test(dataProvider = "DP1")
    @MethodOwner(owner = "qpsdemo")
    @TestPriority(Priority.P3)
    @TestRailCases(testCasesId = "44")
    public void testMultiplyOperation(String TUID, String testRailColumn, int a, int b, int c) {
        setCases(testRailColumn.split(","));
        int actual = a * b;
        int expected = c;
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

    @DataProvider(name = "DP1")
    public Object[][] dataprovider() {
        return new Object[][]{
                {"TUID: Data1", "111,112", 2, 3, 6},
                {"TUID: Data2", "114", 6, 6, 36},
                {"TUID: Data3", "113", 5, 8, 40}
        };
    }

    /**
     * Parametrization using TestNG annotation @Parameters:
     * <ol>
     *      <li>List all parameter names in appropriate annotation.</li>
     *      <li>Pass all parameters from TestNG xml file (check testng_suites/dataprovider.xml).</li>
     * </ol>
     *
     * @param a int
     * @param b int
     * @param c int
     */
    @Test()
    @MethodOwner(owner = "qpsdemo")
    @Parameters({"a", "b", "c"})
    @TestRailCases(testCasesId = "55")
    public void testSubtractOperation(int a, int b, int c) {
        int actual = Integer.valueOf(a) - Integer.valueOf(b);
        int expected = Integer.valueOf(c);
        Assert.assertEquals(actual, expected, "Invalid Subtract result!");
    }

}
