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
package ${package}.carina.demo.regression.dataprovider;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;

/**
 * Carina regression test with retries and it's registration in Zafira.
 * It just generate failure in 50% of cases and on retry should improve statistic
 *
 * @author qpsdemo
 */
public class DataproviderRetryTest extends AbstractTest {
    @Test(dataProvider = "DP1")
    @MethodOwner(owner = "qpsdemo")
    public void testDataproviderRetry(String testRailColumn, int a, int b, int c) {
        boolean isPassed = (new Random().nextInt(2) == 1) ? true : false;
        Assert.assertTrue(isPassed);
        
        setCases(testRailColumn.split(","));
        int actual = a * b;
        int expected = c;
        Assert.assertEquals(actual, expected, "Invalid sum result!");
    }

    @DataProvider(parallel = false, name = "DP1")
    public static Object[][] dataprovider() {
        return new Object[][]{
                {"111,112", 2, 3, 6},
                {"114", 6, 6, 36},
                {"111,112", 2, 3, 6},
                {"114", 6, 6, 36},
                {"111,112", 2, 3, 6},
                {"114", 6, 6, 36},
                {"111,112", 2, 3, 6},
                {"114", 6, 6, 36},
                {"111,112", 2, 3, 6},
                {"114", 6, 6, 36}};
    }


}