package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.Browser;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.UIAutomator2Capabilities;

import io.appium.java_client.android.options.EspressoOptions;
import io.appium.java_client.gecko.options.GeckoOptions;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.mac.options.Mac2Options;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.safari.options.SafariOptions;
import io.appium.java_client.windows.options.WindowsOptions;

public class CapabilitiesFactory {

    private CapabilitiesFactory() {
    }

    public static Capabilities getCapabilities() {
        Capabilities capabilities = null;
        String automationName = Configuration.getAutomationName();

        if (AutomationName.ANDROID_UIAUTOMATOR2.equalsIgnoreCase(automationName)) {
            capabilities = new UIAutomator2Capabilities().getCapabilities();
        }
        if (AutomationName.ESPRESSO.equalsIgnoreCase(automationName)) {
            capabilities = new EspressoOptions();
        }
        if (AutomationName.SAFARI.equalsIgnoreCase(automationName)) {
            capabilities = new SafariOptions();
        }

        if (AutomationName.GECKO.equalsIgnoreCase(automationName)) {
            capabilities = new GeckoOptions();
        }

        if (AutomationName.IOS_XCUI_TEST.equalsIgnoreCase(automationName)) {
            capabilities = new XCUITestOptions();
        }
        if (AutomationName.MAC2.equalsIgnoreCase(automationName)) {
            capabilities = new Mac2Options();
        }
        if (AutomationName.WINDOWS.equalsIgnoreCase(automationName)) {
            capabilities = new WindowsOptions();
        }

        String browser = Configuration.getBrowser();
        if (Browser.CHROME.browserName().equalsIgnoreCase(browser)) {
            capabilities = new ChromeOptions();
        }

        if (Browser.EDGE.browserName().equalsIgnoreCase(browser)) {
            capabilities = new ChromeOptions();
        }

        if (Browser.FIREFOX.browserName().equalsIgnoreCase(browser)) {
            capabilities = new FirefoxOptions();
        }

        if (capabilities == null) {
            throw new RuntimeException("Cannot understand what type of driver should be choosen. Please, provide browser name or automation name");
        }
        return capabilities;
    }
}
