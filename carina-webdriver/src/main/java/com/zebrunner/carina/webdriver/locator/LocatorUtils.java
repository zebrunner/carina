package com.zebrunner.carina.webdriver.locator;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Optional;

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
     * Get type of locator
     * 
     * @param by {@link By}
     * @return {@link Optional} with {@link LocatorType} if locator type detected , {@link Optional#empty()} otherwise
     */
    public static Optional<LocatorType> getLocatorType(By by) {
        String locator = by.toString();
        Optional<LocatorType> locatorType = Arrays.stream(LocatorType.values())
                .filter(l -> l.is(locator))
                .findFirst();
        if (locatorType.isEmpty()) {
            LOGGER.debug("Cannot find suitable locator: '{}'. Investigate....", by);
        }
        return locatorType;
    }
}
