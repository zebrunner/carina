package com.qaprosoft.carina.core.foundation.webdriver.core.factory.chain;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;

import io.appium.java_client.gecko.GeckoDriver;
import io.appium.java_client.gecko.options.GeckoOptions;

@Beta
public class GeckoFactory extends AbstractFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected boolean isSuitable(Capabilities capabilities) {
        return capabilities instanceof GeckoOptions;
    }

    @Override
    public WebDriver getDriver(String seleniumHost, Capabilities capabilities) {
        LOGGER.debug("Gecko capabilities: {}", capabilities);

        // todo investigate creating driver with EventFiringAppiumCommandExecutor
        return new GeckoDriver(getURL(seleniumHost), capabilities);
    }
}
