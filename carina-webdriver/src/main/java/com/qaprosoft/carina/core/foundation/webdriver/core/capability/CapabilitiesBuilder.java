package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.EdgeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.FirefoxCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.STFAndroidCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.STFIOSCapabilities;
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

    public CapabilitiesBuilder fromCustomCapabilities(Capabilities capabilities) {
        this.customCapabilities = this.customCapabilities.merge(capabilities);
        return this;
    }

    public CapabilitiesBuilder chooseOptionsType(OptionsType optionsType) {
        this.optionsType = optionsType;
        return this;
    }

    public Capabilities build() {
        MutableCapabilities capabilities = null;

        if (this.optionsType == null) {
            throw new RuntimeException("Options type should be chosen");
        }

        switch (optionsType) {
        case CHROME:
            ChromeCapabilities chromeCapabilities = new ChromeCapabilities();
            capabilities = this.customCapabilities == null ? chromeCapabilities.getCapabilities()
                    : chromeCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case EDGE:
            EdgeCapabilities edgeCapabilities = new EdgeCapabilities();
            capabilities = this.customCapabilities == null ? edgeCapabilities.getCapabilities()
                    : edgeCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case FIREFOX:
            FirefoxCapabilities firefoxCapabilities = new FirefoxCapabilities();
            capabilities = this.customCapabilities == null ? firefoxCapabilities.getCapabilities()
                    : firefoxCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case ANDROID:
            UIAutomator2Capabilities uiAutomator2Capabilities = new UIAutomator2Capabilities();
            capabilities = this.customCapabilities == null ? uiAutomator2Capabilities.getCapabilities()
                    : uiAutomator2Capabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case STF_ANDROID:
            STFAndroidCapabilities stfAndroidCapabilities = new STFAndroidCapabilities();
            capabilities = this.customCapabilities == null ? stfAndroidCapabilities.getCapabilities()
                    : stfAndroidCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case STF_IOS:
            STFIOSCapabilities stfiosCapabilities = new STFIOSCapabilities();
            capabilities = this.customCapabilities == null ? stfiosCapabilities.getCapabilities()
                    : stfiosCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case IOS:
            XCUITestCapabilities xcuiTestCapabilities = new XCUITestCapabilities();
            capabilities = this.customCapabilities == null ? xcuiTestCapabilities.getCapabilities()
                    : xcuiTestCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case WINDOWS:
            WindowsCapabilities windowsCapabilities = new WindowsCapabilities();
            capabilities = this.customCapabilities == null ? windowsCapabilities.getCapabilities()
                    : windowsCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        case SAFARI:
            SafariCapabilities safariCapabilities = new SafariCapabilities();
            capabilities = this.customCapabilities == null ? safariCapabilities.getCapabilities()
                    : safariCapabilities.createCapabilitiesFromCustom(this.customCapabilities);
            break;
        }

        if (capabilities == null) {
            throw new RuntimeException("Cannot understand what type of driver should be chosen. Please, provide browser name or automation name");
        }

        if (!Objects.equals(this.testName, StringUtils.EMPTY)) {
            capabilities.setCapability("name", testName);
        }

        return capabilities;
    }

}
