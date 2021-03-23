package com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.desktop;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.R;
import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class DesktopCapabilitiesTest {

    // default FirefoxProfile preferences
    private static final boolean MEDIA_EME_ENABLED = true;
    private static final boolean MEDIA_GMP_MANAGER_UPDATE_ENABLED = true;

    @Test
    public static void getChromeCapabilityTest() {
        String testName = "chrome - getChromeCapabilityTest";

        ChromeCapabilities chromeCapabilities = new ChromeCapabilities();
        DesiredCapabilities capabilities = chromeCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getBrowserName(), BrowserType.CHROME, "Returned browser name is not valid!");

        Assert.assertEquals(capabilities.getCapability("name"), testName, "Returned test name is not valid!");

        Assert.assertEquals(capabilities.getCapability("chrome.switches"), Arrays.asList("--start-maximized", "--ignore-ssl-errors"),
                "Returned capability value is not valid!");
        Assert.assertTrue((Boolean) capabilities.getCapability(CapabilityType.ACCEPT_SSL_CERTS), "Returned capability value is not valid!");

        Assert.assertFalse((Boolean) capabilities.getCapability(CapabilityType.TAKES_SCREENSHOT), "Returned capability value is not valid!");

        Assert.assertTrue((Boolean) capabilities.getCapability(CapabilityType.ACCEPT_INSECURE_CERTS), "Returned capability value is not valid!");
    }

    @Test
    public static void getFirefoxCapabilityWithDefaultFirefoxProfileTest() {
        String testName = "firefox - getFirefoxDefaultCapabilityTest";

        FirefoxCapabilities firefoxCapabilities = new FirefoxCapabilities();
        DesiredCapabilities capabilities = firefoxCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getBrowserName(), BrowserType.FIREFOX, "Returned browser name is not valid!");

        Assert.assertEquals(capabilities.getCapability("name"), testName, "Returned test name is not valid!");

        Assert.assertFalse((Boolean) capabilities.getCapability(CapabilityType.TAKES_SCREENSHOT), "Returned capability value is not valid!");

        boolean actualMediaEmeEnabled = ((FirefoxProfile) capabilities.getCapability("firefox_profile"))
                .getBooleanPreference("media.eme.enabled", false);
        Assert.assertEquals(actualMediaEmeEnabled, MEDIA_EME_ENABLED, "Returned firefox profile preference is not valid!");

        boolean actualMediaGmpManagerUpdateEnabled = ((FirefoxProfile) capabilities.getCapability("firefox_profile"))
                .getBooleanPreference("media.gmp-manager.updateEnabled", false);
        Assert.assertEquals(actualMediaGmpManagerUpdateEnabled, MEDIA_GMP_MANAGER_UPDATE_ENABLED, "Returned firefox profile preference is not valid!");
    }

    @Test
    public static void getFirefoxCapabilityWithCustomFirefoxProfileTest() {
        String testName = "firefox - getFirefoxCustomCapabilityTest";

        FirefoxCapabilities firefoxCapabilities = new FirefoxCapabilities();

        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("media.eme.enabled", !MEDIA_EME_ENABLED);
        profile.setPreference("media.gmp-manager.updateEnabled", !MEDIA_GMP_MANAGER_UPDATE_ENABLED);

        DesiredCapabilities capabilities = firefoxCapabilities.getCapability(testName, profile);

        Assert.assertEquals(capabilities.getBrowserName(), BrowserType.FIREFOX, "Returned browser name is not valid!");

        Assert.assertEquals(capabilities.getCapability("name"), testName, "Returned test name is not valid!");

        Assert.assertFalse((Boolean) capabilities.getCapability(CapabilityType.TAKES_SCREENSHOT), "Returned capability value is not valid!");

        boolean actualMediaEmeEnabled = ((FirefoxProfile) capabilities.getCapability("firefox_profile"))
                .getBooleanPreference("media.eme.enabled", true);
        Assert.assertEquals(actualMediaEmeEnabled, !MEDIA_EME_ENABLED, "Returned firefox profile preference is not valid!");

        boolean actualMediaGmpManagerUpdateEnabled = ((FirefoxProfile) capabilities.getCapability("firefox_profile"))
                .getBooleanPreference("media.gmp-manager.updateEnabled", true);
        Assert.assertEquals(actualMediaGmpManagerUpdateEnabled, !MEDIA_GMP_MANAGER_UPDATE_ENABLED, "Returned firefox profile preference is not valid!");
    }

    @Test
    public static void getOperaCapabilityTest() {
        String testName = "opera - getOperaCapabilityTest";

        OperaCapabilities operaCapabilities = new OperaCapabilities();
        DesiredCapabilities capabilities = operaCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getBrowserName(), BrowserType.OPERA_BLINK, "Returned browser name is not valid!");

        Assert.assertEquals(capabilities.getCapability("name"), testName, "Returned test name is not valid!");

        Assert.assertTrue((Boolean) capabilities.getCapability(CapabilityType.ACCEPT_SSL_CERTS), "Returned capability value is not valid!");

        Assert.assertFalse((Boolean) capabilities.getCapability(CapabilityType.TAKES_SCREENSHOT), "Returned capability value is not valid!");
    }

    @Test
    public static void getSafariCapabilityTest() {
        String testName = "safari - getSafariCapabilityTest";

        SafariCapabilities safariCapabilities = new SafariCapabilities();
        DesiredCapabilities capabilities = safariCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getBrowserName(), BrowserType.SAFARI, "Returned browser name is not valid!");

        Assert.assertEquals(capabilities.getCapability("name"), testName, "Returned test name is not valid!");
    }

    @Test
    public static void getEdgeCapabilityTest() {
        String testName = "edge - getEdgeCapabilityTest";

        EdgeCapabilities edgeCapabilities = new EdgeCapabilities();
        DesiredCapabilities capabilities = edgeCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getBrowserName(), BrowserType.EDGE, "Returned browser name is not valid!");

        Assert.assertEquals(capabilities.getCapability("name"), testName, "Returned test name is not valid!");

        Assert.assertTrue((Boolean) capabilities.getCapability(CapabilityType.ACCEPT_SSL_CERTS), "Returned capability value is not valid!");

        Assert.assertFalse((Boolean) capabilities.getCapability(CapabilityType.TAKES_SCREENSHOT), "Returned capability value is not valid!");
    }

    @Test
    public static void getIECapabilityTest() {
        String testName = "ie - getIECapabilityTest";

        IECapabilities ieCapabilities = new IECapabilities();
        DesiredCapabilities capabilities = ieCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getBrowserName(), BrowserType.IE, "Returned browser name is not valid!");

        Assert.assertEquals(capabilities.getCapability("name"), testName, "Returned test name is not valid!");

        Assert.assertTrue((Boolean) capabilities.getCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS),
                "Returned capability value is not valid!");

        Assert.assertTrue((Boolean) capabilities.getCapability(CapabilityType.ACCEPT_SSL_CERTS), "Returned capability value is not valid!");

        Assert.assertFalse((Boolean) capabilities.getCapability(CapabilityType.TAKES_SCREENSHOT), "Returned capability value is not valid!");
    }

    @Test
    public static void getHTMLUnitCapabilityWithUnixPlatformTest() {
        R.CONFIG.put(SpecialKeywords.PLATFORM, Platform.UNIX.name(), true);

        String testName = "htmlUnit - getHTMLUnitCapabilityTest";

        HTMLUnitCapabilities htmlUnitCapabilities = new HTMLUnitCapabilities();
        DesiredCapabilities capabilities = htmlUnitCapabilities.getCapability(testName);

        Assert.assertEquals(capabilities.getPlatform(), Platform.UNIX, "Returned platform is not valid!");

        Assert.assertTrue((Boolean) capabilities.getCapability("javascriptEnabled"), "Returned capability value is not valid!");
    }
}
