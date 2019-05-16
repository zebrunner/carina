package com.qaprosoft.carina.core.foundation.utils.ios;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.mobile.IMobileUtils;

import io.appium.java_client.ios.IOSDriver;

public interface IIosUtils extends IMobileUtils {
    
    static final Logger LOGGER = Logger.getLogger(IosUtils.class);

    /**
     * Hide Keyboard
     * Use com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils.hideKeyboard()
     */
    @Deprecated
    default public void hideKeyboard() {
        try {
            ((IOSDriver<?>) castDriver()).hideKeyboard();
        } catch (Exception e) {
            LOGGER.info("Keyboard was already hided");
        }
    }

}
