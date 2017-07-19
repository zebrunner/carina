package com.qaprosoft.carina.core.foundation.webdriver.appium.gestures;

import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.apache.log4j.Logger;

/**
 * Created by yauhenipatotski on 2/9/17.
 */
public abstract class IGesturesUtils {

    private static final Logger LOGGER = Logger.getLogger(IGesturesUtils.class);

    public static void tap(int x, int y) {
        LOGGER.info("Tapping on the coordinates: x: " + x + " y: ");
        new TouchAction((AppiumDriver) DriverPool.getDriver()).tap(x, y).perform().release();
    }

}
