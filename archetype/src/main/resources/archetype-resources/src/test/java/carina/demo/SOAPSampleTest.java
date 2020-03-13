#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;
import ${package}.carina.demo.api.soap.CountryInfoClient;
import ${package}.carina.demo.api.soap.CountryInfoConfiguration;
import ${package}.carina.demo.base.SOAPTest;
import countryinfo.wsdl.CapitalCityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This sample shows how to create SOAP API tests.
 *
 * @author qpsdemo
 */
@SpringBootTest
@ContextConfiguration(classes = {CountryInfoConfiguration.class})
public class SOAPSampleTest extends SOAPTest {

    @Autowired
    private CountryInfoClient countryInfoClient;

    @Test(testName = "Get capital city by country ISO code")
    @MethodOwner(owner = "qpsdemo")
    @Parameters({"countryISOCode", "capitalCity"})
    @TestPriority(value = Priority.P1)
    public void getInfo(String countryISOCode, String capitalCity) {
        CapitalCityResponse capitalCityResponse = countryInfoClient.getCapitalCityResponse(countryISOCode);
        Assert.assertEquals(String.valueOf(capitalCityResponse.getCapitalCityResult()), capitalCity);
    }

}
