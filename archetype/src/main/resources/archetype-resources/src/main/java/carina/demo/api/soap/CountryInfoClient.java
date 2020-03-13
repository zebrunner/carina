#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.api.soap;

import com.qaprosoft.carina.core.foundation.utils.R;
import countryinfo.wsdl.CapitalCity;
import countryinfo.wsdl.CapitalCityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class CountryInfoClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(CountryInfoClient.class);

    public CapitalCityResponse getCapitalCityResponse(String isoCode) {
        CapitalCity capitalCity = new CapitalCity();
        capitalCity.setSCountryISOCode(isoCode);
        log.info("Requesting capital for {}", isoCode);
        return (CapitalCityResponse) getWebServiceTemplate()
                .marshalSendAndReceive(R.TESTDATA.get("ws.soap.wsdl.url"), capitalCity,
                        new SoapActionCallback(String.format("%s/%s", R.TESTDATA.get("ws.soap.wsdl.tns"), "CapitalCity")));
    }

}
