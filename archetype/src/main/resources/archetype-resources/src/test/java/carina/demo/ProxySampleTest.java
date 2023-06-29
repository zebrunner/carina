#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import ${package}.carina.demo.gui.pages.desktop.HomePage;
import ${package}.carina.demo.gui.pages.desktop.NewsPage;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.report.SessionContext;
import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test proxy in different modes
 *
 * @author qpsdemo
 */
public class ProxySampleTest implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @BeforeClass
    public void beforeClass() {
        // disable setting proxy to the system params
        R.CONFIG.put("proxy_set_to_system", "false");
    }

    @Test(description = "Test 'MANUAL' proxy mode")
    @MethodOwner(owner = "qpsdemo")
    public void manualModeTest() {
        R.CONFIG.put("proxy_type", "MANUAL", true);

        Assert.assertFalse(Configuration.get(WebDriverConfiguration.Parameter.PROXY_HOST).isEmpty(),
                "'proxy_host' configuration parameter should be set.");
        Assert.assertFalse(Configuration.get(WebDriverConfiguration.Parameter.PROXY_PORT).isEmpty(),
                "'proxy_port' configuration parameter should be set.");

        Capabilities capabilities = ((HasCapabilities) getDriver()).getCapabilities();
        Assert.assertNotNull(capabilities.getCapability(CapabilityType.PROXY), "Proxy capability should exists.");
        Assert.assertEquals(((Proxy) capabilities.getCapability(CapabilityType.PROXY)).getProxyType(),
                Proxy.ProxyType.MANUAL,
                "Type of the Selenium Proxy should be 'MANUAL'.");
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu()
                .openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
    }

    @Test(description = "Test 'DIRECT' proxy mode")
    @MethodOwner(owner = "qpsdemo")
    public void directModeTest() {
        R.CONFIG.put("proxy_type", "DIRECT", true);

        Capabilities capabilities = ((HasCapabilities) getDriver()).getCapabilities();
        Assert.assertNotNull(capabilities.getCapability(CapabilityType.PROXY), "Proxy capability should exists.");
        Assert.assertEquals(((Proxy) capabilities.getCapability(CapabilityType.PROXY)).getProxyType(),
                Proxy.ProxyType.DIRECT,
                "Type of the Selenium Proxy should be 'DIRECT'.");

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
    }

    @Test(description = "Test 'PAC' proxy mode (send local pac file)")
    @MethodOwner(owner = "qpsdemo")
    public void pacModeTest() throws IOException {
        R.CONFIG.put("proxy_type", "PAC", true);
        R.CONFIG.put("proxy_pac_local", "true", true);

        Assert.assertFalse(Configuration.get(WebDriverConfiguration.Parameter.PROXY_HOST).isEmpty(),
                "'proxy_host' configuration parameter should be set.");
        Assert.assertFalse(Configuration.get(WebDriverConfiguration.Parameter.PROXY_PORT).isEmpty(),
                "'proxy_port' configuration parameter should be set.");

        // We create local pac file from manual proxy parameters
        String pacContent = String.format("function FindProxyForURL(url, host) {\n"
                        + "return \"PROXY %s:%s\";\n"
                        + "}",
                Configuration.get(WebDriverConfiguration.Parameter.PROXY_HOST),
                Configuration.get(WebDriverConfiguration.Parameter.PROXY_PORT));

        Path path = SessionContext.getArtifactsFolder().resolve("test.pac");
        Files.writeString(path, pacContent);

        R.CONFIG.put("proxy_autoconfig_url", path.toAbsolutePath().toString(), true);

        Capabilities capabilities = ((HasCapabilities) getDriver()).getCapabilities();
        Assert.assertNotNull(capabilities.getCapability(CapabilityType.PROXY), "Proxy capability should exists.");
        Assert.assertEquals(((Proxy) capabilities.getCapability(CapabilityType.PROXY)).getProxyType(),
                Proxy.ProxyType.PAC,
                "Type of the Selenium Proxy should be 'PAC'.");

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
    }

    @Test(description = "Test 'AUTODETECT' proxy mode.")
    @MethodOwner(owner = "qpsdemo")
    public void autodetectModeTest() {
        R.CONFIG.put("proxy_type", "AUTODETECT", true);

        Capabilities capabilities = ((HasCapabilities) getDriver()).getCapabilities();
        Assert.assertNotNull(capabilities.getCapability(CapabilityType.PROXY), "Proxy capability should exists.");
        Assert.assertEquals(((Proxy) capabilities.getCapability(CapabilityType.PROXY)).getProxyType(),
                Proxy.ProxyType.AUTODETECT,
                "Type of the Selenium Proxy should be 'AUTODETECT'.");

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
    }

    @Test(description = "Test 'SYSTEM' proxy mode.")
    @MethodOwner(owner = "qpsdemo")
    public void systemModeTest() {
        R.CONFIG.put("proxy_type", "SYSTEM", true);

        Capabilities capabilities = ((HasCapabilities) getDriver()).getCapabilities();
        Assert.assertNotNull(capabilities.getCapability(CapabilityType.PROXY), "Proxy capability should exists.");
        Assert.assertEquals(((Proxy) capabilities.getCapability(CapabilityType.PROXY)).getProxyType(),
                Proxy.ProxyType.SYSTEM,
                "Type of the Selenium Proxy should be 'SYSTEM'.");

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");
    }

}
