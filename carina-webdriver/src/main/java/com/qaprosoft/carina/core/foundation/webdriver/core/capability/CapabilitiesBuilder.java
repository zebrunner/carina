package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.EdgeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.FirefoxCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.SafariCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.UIAutomator2Capabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.WindowsCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.XCUITestCapabilities;

public class CapabilitiesBuilder {

    private MutableCapabilities customCapabilities = null;
    private String testName = StringUtils.EMPTY;
    private OptionsType optionsType;

    private CapabilitiesBuilder() {
    }

    public static CapabilitiesBuilder builder() {
        return new CapabilitiesBuilder();
    }

    public CapabilitiesBuilder testName(String testName) {
        this.testName = testName;
        return this;
    }

    public CapabilitiesBuilder withCustomCapabilities(Capabilities capabilities) {
        this.customCapabilities = this.customCapabilities.merge(capabilities);
        return this;
    }

    public CapabilitiesBuilder chooseOptionsType(OptionsType optionsType) {
        this.optionsType = optionsType;
        return this;
    }

    public Capabilities build() {
        Capabilities capabilities = null;
        if (!Objects.equals(this.testName, StringUtils.EMPTY)) {
            this.customCapabilities.setCapability("name", testName);
        }

        if (this.optionsType == null) {
            throw new RuntimeException("Options type should be chosen");
        }

        switch (optionsType) {
        case CHROME_SELENIUM:
            ChromeCapabilities chromeCapabilities = new ChromeCapabilities();
            capabilities = this.customCapabilities == null ? chromeCapabilities.getCapabilities()
                    : chromeCapabilities.getCapabilitiesWithCustom(this.customCapabilities);
            break;
        case EDGE_SELENIUM:
            EdgeCapabilities edgeCapabilities = new EdgeCapabilities();
            capabilities = this.customCapabilities == null ? edgeCapabilities.getCapabilities()
                    : edgeCapabilities.getCapabilitiesWithCustom(this.customCapabilities);
            break;
        case FIREFOX_SELENIUM:
            FirefoxCapabilities firefoxCapabilities = new FirefoxCapabilities();
            capabilities = this.customCapabilities == null ? firefoxCapabilities.getCapabilities()
                    : firefoxCapabilities.getCapabilitiesWithCustom(this.customCapabilities);
            break;
        case ANDROID_UIAUTOMATOR2_APPIUM:
            UIAutomator2Capabilities uiAutomator2Capabilities = new UIAutomator2Capabilities();
            capabilities = this.customCapabilities == null ? uiAutomator2Capabilities.getCapabilities()
                    : uiAutomator2Capabilities.getCapabilitiesWithCustom(this.customCapabilities);
            break;
        case IOS_XCUI_TEST_APPIUM:
            XCUITestCapabilities xcuiTestCapabilities = new XCUITestCapabilities();
            capabilities = this.customCapabilities == null ? xcuiTestCapabilities.getCapabilities()
                    : xcuiTestCapabilities.getCapabilitiesWithCustom(this.customCapabilities);
            break;
        case WINDOWS_APPIUM:
            WindowsCapabilities windowsCapabilities = new WindowsCapabilities();
            capabilities = this.customCapabilities == null ? windowsCapabilities.getCapabilities()
                    : windowsCapabilities.getCapabilitiesWithCustom(this.customCapabilities);
            break;
        case SAFARI_APPIUM:
            SafariCapabilities safariCapabilities = new SafariCapabilities();
            capabilities = this.customCapabilities == null ? safariCapabilities.getCapabilities()
                    : safariCapabilities.getCapabilitiesWithCustom(this.customCapabilities);
            break;
        }

        if (capabilities == null) {
            throw new RuntimeException("Cannot understand what type of driver should be chosen. Please, provide browser name or automation name");
        }

        return capabilities;
    }

}
