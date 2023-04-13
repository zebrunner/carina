#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.dataprovider;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;

/**
 * Carina regression test with retries and it's registration in Zafira.
 * It just generate failure in 75% of cases and on retry should improve statistic
 *
 * @author qpsdemo
 */
public class DataproviderRetryTest2 implements IAbstractTest {

    @Test(dataProvider = "DP1", dataProviderClass=DataproviderRetryTest.class)
    @MethodOwner(owner = "qpsdemo")
    public void testDataproviderRetry2(String testRailColumn, int a, int b, int c) {
        boolean isPassed = (new Random().nextInt(4) == 1) ? true : false;
        Assert.assertTrue(isPassed);
        
        setCases(testRailColumn.split(","));
        int actual = a * b;
        int expected = c;
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

}
