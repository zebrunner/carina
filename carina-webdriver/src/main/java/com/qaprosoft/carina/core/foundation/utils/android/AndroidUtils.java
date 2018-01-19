/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.utils.android;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.MobileBy;
import io.appium.java_client.PressesKeyCode;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;

/**
 * Useful Android utilities. For usage: import
 * com.qaprosoft.carina.core.foundation.utils.android.AndroidUtils;
 *
 */
public class AndroidUtils extends MobileUtils {

    protected static final Logger LOGGER = Logger.getLogger(AndroidUtils.class);

    /**
     * execute Key Event
     *
     * @param keyCode int
     */
    public static void executeKeyEvent(int keyCode) {
        WebDriver driver = DriverPool.getDriver();
        LOGGER.info("Execute key event: " + keyCode);
        HashMap<String, Integer> keyCodeMap = new HashMap<String, Integer>();
        keyCodeMap.put("keycode", keyCode);
        ((JavascriptExecutor) driver).executeScript("mobile: keyevent", keyCodeMap);

    }

    /**
     * press Key Code
     *
     * @param keyCode int
     * @return boolean
     */
    public static boolean pressKeyCode(int keyCode) {
        try {
            LOGGER.info("Press key code: " + keyCode);
            ((PressesKeyCode) DriverPool.getDriver()).pressKeyCode(keyCode);
            return true;
        } catch (Exception e) {
            LOGGER.error("Exception during pressKeyCode:", e);
            try {
                LOGGER.info("Press key code by javaScript: " + keyCode);
                executeKeyEvent(keyCode);
            } catch (Exception err2) {
                LOGGER.error("Exception during pressKeyCode with JavaScript:", err2);
            }
        }
        return false;
    }

    /**
     * swipe In Container
     *
     * @param elem - scrollable container
     * @param times - swipe times
     * @param direction -Direction {LEFT, RIGHT, UP, DOWN}
     * @param duration - duration in msec.
     */
    @Deprecated
    public static void swipeInContainer(ExtendedWebElement elem, int times, Direction direction, int duration) {

        // Default direction left
        double directMultX1 = 0.9;
        double directMultX2 = 0.1;
        double directMultY1 = 0.5;
        double directMultY2 = 0.5;

        WebDriver driver = DriverPool.getDriver();

        if (direction.equals(Direction.RIGHT)) {
            directMultX1 = 0.2;
            directMultX2 = 0.9;
            directMultY1 = 0.5;
            directMultY2 = 0.5;
            LOGGER.info("Swipe right");
        } else if (direction.equals(Direction.LEFT)) {
            directMultX1 = 0.9;
            directMultX2 = 0.2;
            directMultY1 = 0.5;
            directMultY2 = 0.5;
            LOGGER.info("Swipe left");
        } else if (direction.equals(Direction.UP)) {
            directMultX1 = 0.1;
            directMultX2 = 0.1;
            directMultY1 = 0.2;
            directMultY2 = 0.9;
            LOGGER.info("Swipe up");
        } else if (direction.equals(Direction.DOWN)) {
            directMultX1 = 0.1;
            directMultX2 = 0.1;
            directMultY1 = 0.9;
            directMultY2 = 0.2;
            LOGGER.info("Swipe down");
        } else if (direction.equals(Direction.VERTICAL) || direction.equals(Direction.HORIZONTAL)
                || direction.equals(Direction.HORIZONTAL_RIGHT_FIRST) || direction.equals(Direction.VERTICAL_DOWN_FIRST)) {
            LOGGER.info("Incorrect swipe direction: " + direction.toString());
            return;
        }

        int x = elem.getElement().getLocation().getX();
        int y = elem.getElement().getLocation().getY();
        int width = elem.getElement().getSize().getWidth();
        int height = elem.getElement().getSize().getHeight();
        int screen_size_x = driver.manage().window().getSize().getWidth();
        int screen_size_y = driver.manage().window().getSize().getHeight();

        LOGGER.debug("x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", screen width=" + screen_size_x + ", screen height="
                + screen_size_y);
        LOGGER.info("Swiping in container:" + elem.getNameWithLocator());
        for (int i = 0; i <= times; i++) {
            int pointX1 = (int) (x + (width * directMultX1));
            int pointY1 = (int) (y + (height * directMultY1));
            int pointX2 = (int) (x + (width * directMultX2));
            int pointY2 = (int) (y + (height * directMultY2));

            LOGGER.debug(
                    "Direction:" + direction + ". Try #" + i + ". Points: X1Y1=" + pointX1 + ", " + pointY1 + ", X2Y2=" + pointX2 + ", " + pointY2);
            try {
                swipe(pointX1, pointY1, pointX2, pointY2, duration);
            } catch (Exception e) {
                LOGGER.error("Exception: " + e);
            }
        }
    }
    /**
     * Hide keyboard if needed
     */
    public static void hideKeyboard() {
        try {
            ((AndroidDriver<?>) DriverPool.getDriver()).hideKeyboard();
        } catch (Exception e) {
            LOGGER.info("Keyboard was already hided or error occurs: " + e);
        }
    }
    
    public static void pressBack() {
        ((AndroidDriver<?>) DriverPool.getDriver()).pressKeyCode(AndroidKeyCode.BACK);
    }

    /**
     * Pressing "search" key of Android keyboard by coordinates.
     * <p>
     * Tested at Nexus 6P Android 8.0.0 standard keyboard. Coefficients of
     * coordinates for other devices and custom keyboards could be different.
     * <p>
     * Following options are not working: 1.
     * AndroidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_SEARCH); 2.
     * searchEditText.sendKeys("textToSearch" + "\n")
     */
    public static void pressSearchKey() {
        pressBottomRightKey();
    }

    public static void pressNextKey() {
        pressBottomRightKey();
    }

    /**
     * Pressing bottom right button on the keyboard by coordinates: "search",
     * "ok", "next", etc. - various keys appear at this position. Tested at
     * Nexus 6P Android 8.0.0 standard keyboard. Coefficients of coordinates for
     * other devices and custom keyboards could be different.
     */
    public static void pressBottomRightKey() {
    	WebDriver driver = DriverPool.getDriver();
    	Dimension size = driver.manage().window().getSize();
        int height =  size.getHeight();
        int width = size.getWidth();

        new TouchAction((AndroidDriver<?>) driver).tap(Double.valueOf(width * 0.915).intValue(), Double.valueOf(height * 0.945).intValue()).perform();
    }

    /**
     * wait Until Element Not Present
     *
     * @param locator By
     * @param timeout long
     * @param pollingTime long
     */
    public static void waitUntilElementNotPresent(final By locator, final long timeout, final long pollingTime) {
        LOGGER.info(String.format("Wait until element %s disappear", locator.toString()));
        WebDriver driver = DriverPool.getDriver();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        try {
            if (new WebDriverWait(driver, timeout, pollingTime).until(ExpectedConditions.invisibilityOfElementLocated(locator))) {
                LOGGER.info(String.format("Element located by: %s not present.", locator.toString()));
            } else {
                LOGGER.info(String.format("Element located by: %s is still present.", locator.toString()));
            }
        } catch (TimeoutException e) {
            LOGGER.debug(e.getMessage());
            LOGGER.info(String.format("Element located by: %s is still present.", locator.toString()));
        }
        driver.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * change Android Device Language
     * <p>
     * Url: <a href=
     * "http://play.google.com/store/apps/details?id=net.sanapeli.adbchangelanguage&hl=ru&rdid=net.sanapeli.adbchangelanguage">
     * ADBChangeLanguage apk </a> Change locale (language) of your device via
     * ADB (on Android OS version 6.0, 5.0, 4.4, 4.3, 4.2 and older). No need to
     * root your device! With ADB (Android Debug Bridge) on your computer, you
     * can fast switch the device locale to see how your application UI looks on
     * different languages. Usage: - install this app - setup adb connection to
     * your device (http://developer.android.com/tools/help/adb.html) - Android
     * OS 4.2 onwards (tip: you can copy the command here and paste it to your
     * command console): adb shell pm grant net.sanapeli.adbchangelanguage
     * android.permission.CHANGE_CONFIGURATION
     * <p>
     * English: adb shell am start -n
     * net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language en Russian:
     * adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage
     * -e language ru Spanish: adb shell am start -n
     * net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language es
     *
     * @param language to set. Can be es, en, etc.
     * @return boolean
     */
    public static boolean setDeviceLanguage(String language) {

        AndroidService executor = AndroidService.getInstance();

        boolean status = executor.setDeviceLanguage(language);

        return status;
    }
    
    public static boolean isChecked(final ExtendedWebElement element) {
		return element.isElementPresent(5)
				&& (element.getElement().isSelected() || element.getAttribute("checked").equals("true"));
    }

    public enum SelectorType {
        TEXT, TEXT_CONTAINS, TEXT_STARTS_WITH, ID, DESCRIPTION, DESCRIPTION_CONTAINS, CLASS_NAME
    }

    /**
     * Scrolls into view in specified container by text only and return ExtendedWebElement
     *
     * @param container  ExtendedWebElement     - works only with resourceId
     * @param scrollToElement String has to be id, className, text, contentDesc, etc
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("Hindi", genresTab);
     **/
    public static ExtendedWebElement scroll(String scrollToElement, ExtendedWebElement container) {
    	return scroll(scrollToElement, container, SelectorType.TEXT);
    }
    /**
     * Scrolls into view in specified container and return ExtendedWebElement
     *
     * @param container  ExtendedWebElement     - works only with resourceId
     * @param scrollToElement String has to be id, className, text, contentDesc, etc
     * @param selectorType  SelectorType can be TEXT, TEXT_CONTAINS, TEXT_STARTS_WITH, ID, DESCRIPTION, DESCRIPTION_CONTAINS, CLASS_NAME
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("Hindi", genresTab , AndroidUtils.SelectorType.TEXT);
     **/
    public static ExtendedWebElement scroll(String scrollToElement, ExtendedWebElement container, SelectorType selectorType) {

        LOGGER.debug(container.getBy().toString());
        ExtendedWebElement el;

        String scrollableContainer = container.getBy().toString().replace("By.id:", "").trim();
        if (!scrollableContainer.contains("id/")) {
        	Assert.fail("scrollable container should be pointed By.id!");
        	//TODO: investigate possibility to read package of currently focues app 
            //scrollableContainer = getCurrentFocusedApkPackageName() + ":id/" + scrollableContainer;
        }

        String scrollViewContainer_finder = "new UiSelector().resourceId(\"" + scrollableContainer + "\")";
        String neededElement_finder = "";

        switch (selectorType) {
            case TEXT:
                neededElement_finder = "new UiSelector().text(\"" + scrollToElement + "\")";
                break;
            case TEXT_CONTAINS:
                neededElement_finder = "new UiSelector().textContains(\"" + scrollToElement + "\")";
                break;
            case TEXT_STARTS_WITH:
                neededElement_finder = "new UiSelector().textStartsWith(\"" + scrollToElement + "\")";
                break;
            case ID:
                neededElement_finder = "new UiSelector().resourceId(\"" + scrollToElement + "\")";
                break;
            case DESCRIPTION:
                neededElement_finder = "new UiSelector().description(\"" + scrollToElement + "\")";
                break;
            case DESCRIPTION_CONTAINS:
                neededElement_finder = "new UiSelector().descriptionContains(\"" + scrollToElement + "\")";
                break;
            case CLASS_NAME:
                neededElement_finder = "new UiSelector().className(\"" + scrollToElement + "\")";
                break;
            default:
                LOGGER.info("Please provide valid selectorType for element to be found...");
                break;
        }

        try {
            By by = MobileBy.AndroidUIAutomator("new UiScrollable(" + scrollViewContainer_finder + ").scrollIntoView(" + neededElement_finder + ")");
            LOGGER.debug(by.toString());

            WebElement ele = DriverPool.getDriver().findElement(by);

            if (ele.isDisplayed()) {
                LOGGER.info(String.format("Element %s:%s was found.", selectorType.toString(), scrollToElement));
                el = new ExtendedWebElement(ele, scrollToElement, by, DriverPool.getDriver());
            } else {
                LOGGER.error(String.format("Element %s:%s was NOT found.", selectorType.toString(), scrollToElement));
                throw new NoSuchElementException("Element was not found after scroll using " + by.toString());
            }
        } catch (NoSuchElementException noSuchElement) {
            throw new NoSuchElementException(String.format("Element %s:%s was NOT found.", selectorType.toString(), scrollToElement), noSuchElement);
        } catch (Exception e) {
            LOGGER.error("Error happen.", e);
            throw e;
        }

        return el;
    }

}
