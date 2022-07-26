package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.ChromeCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.FirefoxCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.UIAutomator2Capabilities;

public class CapabilitiesBuilder {

    private MutableCapabilities additionalCapabilities = new MutableCapabilities();
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

    public CapabilitiesBuilder withCapabilities(Capabilities capabilities) {
        this.additionalCapabilities = this.additionalCapabilities.merge(capabilities);
        return this;
    }

    public CapabilitiesBuilder chooseOptionsType(OptionsType optionsType) {
        this.optionsType = optionsType;
        return this;
    }

    public Capabilities build() {
        Capabilities capabilities = null;
        if (!Objects.equals(this.testName, StringUtils.EMPTY)) {
            this.additionalCapabilities.setCapability("name", testName);
        }

        if (this.optionsType == null) {
            throw new RuntimeException("Options type should be choosen");
        }

        switch (optionsType) {
        case CHROME_SELENIUM:
            capabilities = new ChromeCapabilities()
                    .getCapabilitiesWithCustom(this.additionalCapabilities);
            break;
        case FIREFOX_SELENIUM:
            capabilities = new FirefoxCapabilities()
                    .getCapabilitiesWithCustom(this.additionalCapabilities);
            break;
        case ANDROID_UIAUTOMATOR2_APPIUM:
            capabilities = new UIAutomator2Capabilities()
                    .getCapabilitiesWithCustom(this.additionalCapabilities);
            break;
        }

        if (capabilities == null) {
            throw new RuntimeException("Cannot understand what type of driver should be chosen. Please, provide browser name or automation name");
        }

        return capabilities;
    }

}
