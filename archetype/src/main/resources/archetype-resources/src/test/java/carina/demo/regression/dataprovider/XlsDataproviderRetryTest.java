#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2013-2020 QAPROSOFT (http://qaprosoft.com/).
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

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.AbstractTest;
import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;

public class XlsDataproviderRetryTest extends AbstractTest {
    protected static final Logger LOGGER = Logger.getLogger(XlsDataproviderRetryTest.class);

    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @XlsDataSourceParameters(path = "xls/demo.xlsx", sheet = "Data", dsUid = "TestTitle", dsArgs = "Args")
    public void testMethod(String arg) {
    	LOGGER.info("arg: " + arg);
        boolean isPassed = (new Random().nextInt(3) == 1) ? true : false;
        Assert.assertTrue(isPassed);
    }

}
