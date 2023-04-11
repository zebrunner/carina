#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.dataprovider;

import java.lang.invoke.MethodHandles;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.dataprovider.IAbstractDataProvider;
import com.zebrunner.carina.dataprovider.annotations.XlsDataSourceParameters;

public class XlsDataproviderRetryTest implements IAbstractTest, IAbstractDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    @Test(dataProvider = "DataProvider")
    @MethodOwner(owner = "qpsdemo")
    @XlsDataSourceParameters(path = "data_source/demo.xlsx", sheet = "Data", dsUid = "TestTitle", dsArgs = "Args")
    public void testMethod(String arg) {
        LOGGER.info("arg: " + arg);
        boolean isPassed = (new Random().nextInt(3) == 1) ? true : false;
        Assert.assertTrue(isPassed);
    }
    
}
