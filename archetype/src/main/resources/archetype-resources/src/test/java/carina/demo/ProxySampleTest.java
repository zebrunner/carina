#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.qaprosoft.carina.browserupproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.IAbstractTest;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import ${package}.carina.demo.gui.pages.HomePage;
import ${package}.carina.demo.gui.pages.NewsPage;
import com.zebrunner.agent.core.registrar.Artifact;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.proxy.CaptureType;

/**
 * This sample shows how generate har file with proxied Web test content.
 *
 * @author qpsdemo
 */
public class ProxySampleTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    BrowserUpProxy proxy;

    @BeforeMethod(alwaysRun = true)
    public void startProxy()
    {
        R.CONFIG.put("browserup_proxy", "true");
        getDriver();
        proxy = ProxyPool.getProxy();
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        proxy.newHar();
    }

    @Test()
    @MethodOwner(owner = "qpsdemo")
    public void testNewsSearch() {
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");

        // Saving har to a file...
        String name = "ProxyReport.har";
        File file = new File(name);
        Assert.assertNotNull(proxy.getHar(), "Har is NULL!");

        try {
            proxy.getHar().writeTo(file);
            Artifact.attachToTest(name, file);
        } catch (IOException e) {
            LOGGER.error("Unable to generate har archive!", e);
        }
    }


}
