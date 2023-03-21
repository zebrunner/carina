#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.proxy.CaptureType;
import com.qaprosoft.carina.core.foundation.IAbstractTest;
import ${package}.carina.demo.gui.pages.desktop.HomePage;
import ${package}.carina.demo.gui.pages.desktop.NewsPage;
import ${package}.carina.demo.proxy.CustomProxy;
import ${package}.carina.demo.proxy.CustomProxyRule;
import ${package}.carina.demo.proxy.DemoResponseFilter;
import com.zebrunner.agent.core.registrar.Artifact;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.proxy.ProxyPool;
import com.zebrunner.carina.proxy.browserup.CarinaBrowserUpProxy;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.report.ReportContext;
import com.zebrunner.carina.webdriver.Screenshot;
import com.zebrunner.carina.webdriver.ScreenshotType;

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

        Assert.assertFalse(Configuration.get(Configuration.Parameter.PROXY_HOST).isEmpty(),
                "'proxy_host' configuration parameter should be set.");
        Assert.assertFalse(Configuration.get(Configuration.Parameter.PROXY_PORT).isEmpty(),
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
    public void pacModeTest() throws FileNotFoundException {
        R.CONFIG.put("proxy_type", "PAC", true);
        R.CONFIG.put("proxy_pac_local", "true", true);

        Assert.assertFalse(Configuration.get(Configuration.Parameter.PROXY_HOST).isEmpty(),
                "'proxy_host' configuration parameter should be set.");
        Assert.assertFalse(Configuration.get(Configuration.Parameter.PROXY_PORT).isEmpty(),
                "'proxy_port' configuration parameter should be set.");

        // We create local pac file from manual proxy parameters
        String pacContent = String.format("function FindProxyForURL(url, host) {\n"
                        + "return \"PROXY %s:%s\";\n"
                        + "}",
                Configuration.get(Configuration.Parameter.PROXY_HOST),
                Configuration.get(Configuration.Parameter.PROXY_PORT));

        File file = new File(ReportContext.getArtifactsFolder() + "/test.pac");

        try (PrintWriter out = new PrintWriter(file)) {
            out.write(pacContent);
        }

        R.CONFIG.put("proxy_autoconfig_url", file.getAbsolutePath(), true);

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

    @Test(description = "Test 'DYNAMIC' proxy mode (default CarinaBrowserUpProxy implementation)")
    @MethodOwner(owner = "qpsdemo")
    public void defaultDynamicModeTest() {
        R.CONFIG.put("browserup_proxy", "true", true);
        R.CONFIG.put("proxy_type", "DYNAMIC", true);
        R.CONFIG.put("proxy_port", "0", true);

        getDriver();

        BrowserUpProxy browserUpProxy = ProxyPool.getOriginal(CarinaBrowserUpProxy.class)
                .orElseThrow()
                .getProxy();
        browserUpProxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        browserUpProxy.newHar();

        Capabilities capabilities = ((HasCapabilities) getDriver()).getCapabilities();
        Assert.assertNotNull(capabilities.getCapability(CapabilityType.PROXY), "Proxy capability should exists.");
        Assert.assertEquals(((Proxy) capabilities.getCapability(CapabilityType.PROXY)).getProxyType(),
                Proxy.ProxyType.MANUAL,
                "Type of the Selenium Proxy should be 'MANUAL'.");

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");

        BrowserUpProxy proxy = ProxyPool.getOriginal(CarinaBrowserUpProxy.class)
                .orElseThrow()
                .getProxy();

        // Saving har to a file...
        String name = "ProxyReport.har";
        File file = new File(ReportContext.getArtifactsFolder() + "/" + name);
        Assert.assertNotNull(proxy.getHar(), "Har is NULL!");

        try {
            proxy.getHar().writeTo(file);
            Artifact.attachToTest(name, file);
        } catch (IOException e) {
            LOGGER.error("Unable to generate har archive!", e);
        }
    }

    @Test(description = "Test 'DYNAMIC' proxy mode (CustomProxy implementation with chained proxy)")
    @MethodOwner(owner = "qpsdemo")
    public void customDynamicModeTest() {
        R.CONFIG.put("browserup_proxy", "true", true);
        R.CONFIG.put("proxy_type", "DYNAMIC", true);
        R.CONFIG.put("proxy_port", "0", true);

        String proxyChainHost = R.CONFIG.get("proxy_chain_host");
        String proxyChainPort = R.CONFIG.get("proxy_chain_port");

        Assert.assertFalse(proxyChainHost.isEmpty() || proxyChainHost.equalsIgnoreCase("NULL"),
                "'proxy_chain_host' configuration parameter should be set.");
        Assert.assertFalse(proxyChainPort.isEmpty() || proxyChainPort.equalsIgnoreCase("NULL"),
                "'proxy_chain_port' configuration parameter should be set.");

        // setting custom proxy rule
        ProxyPool.setRule(new CustomProxyRule(), true);

        getDriver();

        Capabilities capabilities = ((HasCapabilities) getDriver()).getCapabilities();
        Assert.assertNotNull(capabilities.getCapability(CapabilityType.PROXY), "Proxy capability should exists.");
        Assert.assertEquals(((Proxy) capabilities.getCapability(CapabilityType.PROXY)).getProxyType(),
                Proxy.ProxyType.MANUAL,
                "Type of the Selenium Proxy should be 'MANUAL'.");

        BrowserUpProxy browserUpProxy = ProxyPool.getOriginal(CustomProxy.class)
                .orElseThrow()
                .getProxy();
        browserUpProxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        browserUpProxy.newHar();

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        NewsPage newsPage = homePage.getFooterMenu().openNewsPage();
        Assert.assertTrue(newsPage.isPageOpened(), "News page is not opened!");

        BrowserUpProxy proxy = ProxyPool.getOriginal(CustomProxy.class)
                .orElseThrow()
                .getProxy();
        // Saving har to a file...
        String name = "ProxyReport.har";
        File file = new File(ReportContext.getArtifactsFolder() + "/" + name);
        Assert.assertNotNull(proxy.getHar(), "Har is NULL!");

        try {
            proxy.getHar().writeTo(file);
            Artifact.attachToTest(name, file);
        } catch (IOException e) {
            LOGGER.error("Unable to generate har archive!", e);
        }
    }

    @Test(description = "Test 'DYNAMIC' proxy mode with response filtering")
    @MethodOwner(owner = "qpsdemo")
    public void dynamicModeWithResponseFilterTest() throws FileNotFoundException {
        R.CONFIG.put("browserup_proxy", "true", true);
        R.CONFIG.put("proxy_type", "DYNAMIC", true);
        R.CONFIG.put("proxy_port", "0", true);

        getDriver();
        BrowserUpProxy proxy = ProxyPool.getOriginal(CarinaBrowserUpProxy.class)
                .orElseThrow()
                .getProxy();
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        proxy.newHar();

        String modifiedTitleText = "Modified title";
        String modifiedPhoneFinderLinkText = "MODIFIED PHONE FINDER";

        // replace original page title by custom in response
        proxy.addResponseFilter(new DemoResponseFilter(
                "GSMArena.com - mobile phone reviews, news, specifications and more...",
                modifiedTitleText));

        // replace phone finder link content by custom in response
        proxy.addResponseFilter(new DemoResponseFilter(
                "Phone finder",
                modifiedPhoneFinderLinkText));

        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        Assert.assertTrue(homePage.isPageOpened(), "Home page is not opened!");

        Assert.assertEquals(getDriver().getTitle(), modifiedTitleText, "Page title should be modified in response.");
        Assert.assertEquals(homePage.getPhoneFinderButton().getText(), modifiedPhoneFinderLinkText,
                "'Phone Finder' link text should be modified in response.");

        String pageSourceFileName = "ModifiedPageSource.txt";
        LOGGER.info(
                "Page title and 'Phone finder' element's text modified in response to '{}' and '{}' respectively. Review changes in attached '{}' artifact.",
                modifiedTitleText, modifiedPhoneFinderLinkText, pageSourceFileName);
        Screenshot.capture(getDriver(), ScreenshotType.EXPLICIT_VISIBLE);
        Screenshot.capture(homePage.getPhoneFinderButton().getElement(), ScreenshotType.EXPLICIT_VISIBLE,
                "The modified representation of the 'Phone Finder' element on the page");

        File file = new File(ReportContext.getArtifactsFolder() + "/" + pageSourceFileName);
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.write(getDriver().getPageSource());
        }
        Artifact.attachToTest(pageSourceFileName, file);
    }

}
