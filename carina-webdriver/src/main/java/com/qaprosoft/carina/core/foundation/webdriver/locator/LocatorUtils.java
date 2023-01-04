package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.openqa.selenium.Beta;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For internal usage only
 */
@Beta
public final class LocatorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private LocatorUtils() {

    }

    /**
     * Generate locator with index
     * 
     * @param by {@link By}
     * @param index index of an element starts from 0 (first element will have 0 index)
     */
    public static By generateByForList(By by, int index) {
        String locator = by.toString();
        By resBy = null;
        LocatorType locatorType = Arrays.stream(LocatorType.values())
                .filter(l -> l.is(locator))
                .findFirst()
                .orElse(null);

        if (locatorType == null) {
            LOGGER.debug("Cannot detect type of locator: {}", by);
        } else {
            try {
                resBy = locatorType.buildLocatorWithIndex(locator, index);
            } catch (Exception e) {
                // do nothing
            }
        }
        if (resBy == null) {
            LOGGER.error("Cannot create locator with index: {}", by);
            resBy = by;
        }
        return resBy;
    }

    public static boolean isGenerateByForListSupported(By by) {
        String locator = by.toString();
        LocatorType locatorType = Arrays.stream(LocatorType.values())
                .filter(l -> l.is(locator))
                .findFirst()
                .orElse(null);
        return locatorType != null && locatorType.isIndexSupport();
    }
}
