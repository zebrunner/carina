package com.qaprosoft.carina.core.foundation.webdriver.core.factory;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IAbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Creates new instance of {@link WebDriver} according to specified {@link Capabilities}.
     *
     * @param testName - where driver is initiated
     * @param capabilities - driver desired capabilitues
     * @param seleniumHost - selenium server URL
     * @return instance of {@link WebDriver}
     */
    abstract public WebDriver create(String testName, Capabilities capabilities, String seleniumHost);

}
