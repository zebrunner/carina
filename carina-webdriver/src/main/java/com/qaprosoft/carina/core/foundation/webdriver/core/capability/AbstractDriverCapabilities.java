package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import io.appium.java_client.remote.options.BaseOptions;

public class AbstractDriverCapabilities<T extends BaseOptions> extends AbstractCapabilities<T> {

    @Override
    public T getCapability(String testName) {
        return null;
    }
}
