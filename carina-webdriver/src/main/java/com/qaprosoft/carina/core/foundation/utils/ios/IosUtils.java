package com.qaprosoft.carina.core.foundation.utils.ios;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;

import io.appium.java_client.ios.IOSDriver;

/**
 * Useful iOS utilities. For usage: import
 * com.qaprosoft.carina.core.foundation.utils.ios.IosUtils;
 *
 */
public class IosUtils extends MobileUtils {

    private static final Logger LOGGER = Logger.getLogger(IosUtils.class);
    
    /**
     * Hide Keyboard
     * 
     */
    public static void hideKeyboard() {
        try {
            ((IOSDriver<?>) DriverPool.getDriver()).hideKeyboard();
        } catch (Exception e) {
            LOGGER.info("Keyboard was already hided");
        }
    }
    
}