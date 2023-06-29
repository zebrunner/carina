#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.regression.esg;

import com.zebrunner.agent.core.registrar.Artifact;
import com.zebrunner.carina.core.IAbstractTest;
import ${package}.carina.demo.gui.pages.desktop.HomePage;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.SessionContext;
import com.zebrunner.carina.webdriver.Screenshot;
import com.zebrunner.carina.webdriver.ScreenshotType;
import com.zebrunner.carina.webdriver.proxy.ZebrunnerProxyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProxyTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void modifyBodyTest() throws IOException {
        R.CONFIG.put("proxy_type", "Zebrunner", true);
        LOGGER.info("Creating command for Zebrunner proxy.");
        ZebrunnerProxyBuilder.getInstance()
                .addBodyModify("Phone finder", "MODIFIED PHONE FINDER")
                .build(true);

        getDriver();
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");
        LOGGER.info("Checking is button text modified from 'Phone finder' to 'MODIFIED PHONE FINDER'.");
        Assert.assertEquals(homePage.getPhoneFinderButton().getText(), "MODIFIED PHONE FINDER",
                "'Phone Finder' link text should be modified in response.");

        String pageSourceFileName = "ModifiedPageSource.txt";
        LOGGER.info(
                "Page title and 'Phone finder' element's text modified in response to '{}' and '{}' respectively. Review changes in attached '{}' artifact.",
                "Modified title", "MODIFIED PHONE FINDER", pageSourceFileName);
        Screenshot.capture(getDriver(), ScreenshotType.EXPLICIT_VISIBLE);
        Screenshot.capture(homePage.getPhoneFinderButton().getElement(), ScreenshotType.EXPLICIT_VISIBLE,
                "The modified representation of the 'Phone Finder' element on the page");

        Path file = SessionContext.getArtifactsFolder().resolve(pageSourceFileName);
        Files.writeString(file, getDriver().getPageSource());
        Artifact.attachToTest(pageSourceFileName, file);
    }
}