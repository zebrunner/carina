package com.qaprosoft.carina.core.foundation.webdriver.locator.converter;

import java.util.Map;
import java.util.Optional;

import com.qaprosoft.carina.core.foundation.webdriver.locator.LocatorType;

public class SeleniumToAppiumConverter implements LocatorConverter {

    private static final Map<LocatorType, LocatorType> CONVERTIBLE_LOCATORS = Map.of(
            LocatorType.CLASSNAME, LocatorType.APPIUM_BY_CLASSNAME,
            LocatorType.ID, LocatorType.APPIUM_BY_ID,
            LocatorType.NAME, LocatorType.APPIUM_BY_NAME);

    @Override
    public String convert(String by) {
        Optional<Map.Entry<LocatorType, LocatorType>> locatorType = CONVERTIBLE_LOCATORS.entrySet()
                .stream()
                .filter(entity -> entity.getKey().is(by))
                .findFirst();

        return locatorType.isPresent() ? locatorType.get().getValue().buildLocatorFromString(by).toString() : by;
    }
}
