package com.qaprosoft.carina.core.foundation.webdriver.appium.gestures;

import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.Dimension;

/**
 * Created by yauhenipatotski on 2/9/17.
 */
public final class AndroidGesturesUtils extends IGesturesUtils {

    private AndroidGesturesUtils() {
    }

    public static synchronized void swipeToBottom() {
        Dimension size = DriverPool.getDriver().manage().window().getSize();
        TouchAction swipe = new TouchAction((AppiumDriver) DriverPool.getDriver()).press(size.width / 2, (int) (size.height * 0.90))
                .waitAction(2000).moveTo(0, (int) (size.height * 0.10)).release();
        swipe.perform();
    }

    public static synchronized void swipeToTop() {
        Dimension size = DriverPool.getDriver().manage().window().getSize();
        TouchAction swipe = new TouchAction((AppiumDriver) DriverPool.getDriver()).press(size.width / 2, (int) (size.height * 0.10))
                .waitAction(2000).moveTo(0, (int) (size.height * 0.90)).release();
        swipe.perform();
    }

    public static synchronized void swipeLeft() {
        throw new UnsupportedOperationException("Method not supported for following platform");
    }

    public static synchronized void swipeRight() {
        throw new UnsupportedOperationException("Method not supported for following platform");
    }

    public static synchronized void scrollToElement(ExtendedWebElement extendedWebElement, int swipeCount) {

        if (!extendedWebElement.getElement().isDisplayed()) {
            int i = 0;
            do {
                swipeToBottom();
                i++;
            } while (!extendedWebElement.getElement().isDisplayed() || swipeCount > i);
        }
    }

}
