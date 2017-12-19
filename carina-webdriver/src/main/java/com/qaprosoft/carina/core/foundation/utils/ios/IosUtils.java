package com.qaprosoft.carina.core.foundation.utils.ios;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

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
    
    public static boolean swipeTillElement(ExtendedWebElement element, ExtendedWebElement container, Direction direction, 
			int count, int duration) {
    	//TODO: test on real devices to identify if below anomaly exists
    	//due to the strange behaviour iOS emulator perform swipes in opposite direction by default. So we have to redeclare direction on this level temporary until issue is fixed in appium java client
		switch (direction) {
		case UP:
			direction = Direction.DOWN;
			break;
		case DOWN:
			direction = Direction.UP;
			break;
		case LEFT:
			direction = Direction.RIGHT;
			break;
		case RIGHT:
			direction = Direction.LEFT;
			break;
		default:
			throw new RuntimeException("Unsupported direction for swipeInContainerTillElement: " + direction);
		}
		
    	return swipe(element, container, direction, count, duration);
    }


}
