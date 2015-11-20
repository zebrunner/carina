package com.qaprosoft.carina.core.foundation.webdriver.core.factory;

import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

public abstract class AbstractFactory {

    protected static final Logger LOGGER = Logger.getLogger(AbstractFactory.class);

    public  WebDriver create(String testName) {
        return create(testName, new Device());
    }

    abstract public WebDriver create(String testName, Device device);
}
