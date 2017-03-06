package com.qaprosoft.carina.core.foundation.webdriver.appium.gestures;

import com.qaprosoft.carina.core.foundation.webdriver.DriverPoolEx;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.Dimension;

/**
 * Created by yauhenipatotski on 2/9/17.
 */
public final class IOSGesturesUtils extends IGesturesUtils {

    private IOSGesturesUtils() {
    }


    public static void swipeToBottom() {
        Dimension size = DriverPoolEx.getDriver().manage().window().getSize();
        TouchAction swipe = new TouchAction((AppiumDriver) DriverPoolEx.getDriver()).press(size.width / 2, (int) (size.height * 0.7))
                .waitAction(2000).moveTo(0, (int) (-size.height * 0.5)).release().perform();
        swipe.perform();
    }

    public static void swipeToTop() {
        Dimension size = DriverPoolEx.getDriver().manage().window().getSize();
        TouchAction swipe = new TouchAction((AppiumDriver) DriverPoolEx.getDriver()).press(size.width / 2, (int) (size.height * 0.2))
                .waitAction(2000).moveTo(0, (int) (size.height * 0.7)).release();
        swipe.perform();
    }

    public static void swipeLeft() {
        throw new UnsupportedOperationException("Method not supported for following platform");
    }

    public static void swipeRight() {
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
