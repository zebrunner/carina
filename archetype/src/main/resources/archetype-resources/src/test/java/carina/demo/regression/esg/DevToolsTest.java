#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.esg;

import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.gui.pages.common.HomePageBase;
import com.zebrunner.carina.webdriver.DriverHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;

public class DevToolsTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void devToolsTest() {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened");
        DriverHelper driverHelper = new DriverHelper(getDriver());

        LOGGER.info("Trying to get Chrome DevTools...");
        ChromeDevToolsService chromeDevToolsService = driverHelper.getChromeDevTools();
        LOGGER.info("Got Chrome DevTools. Trying to get browser js version...");
        String jsVersion = chromeDevToolsService.getBrowser()
                .getVersion()
                .getJsVersion();
        LOGGER.info("Got browser js version: {}", jsVersion);
        chromeDevToolsService.close();
        chromeDevToolsService.waitUntilClosed();
    }

}