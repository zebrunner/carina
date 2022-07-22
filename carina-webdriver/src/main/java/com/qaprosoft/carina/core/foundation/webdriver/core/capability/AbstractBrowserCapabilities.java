package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import org.openqa.selenium.remote.AbstractDriverOptions;

public abstract class AbstractBrowserCapabilities<T extends AbstractDriverOptions<?>> extends AbstractCapabilities<T> {

    @Override
    public T getCapability(String testName) {
        return null;
    }

}
