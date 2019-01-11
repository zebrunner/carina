/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import org.apache.commons.lang3.StringUtils;
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

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.MobileBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.KeyEventFlag;
import io.appium.java_client.touch.offset.PointOption;
import io.appium.java_client.windows.PressesKeyCode;

/**
 * Useful Android utilities. For usage: import
 * com.qaprosoft.carina.core.foundation.utils.android.AndroidUtils;
 *
 */
public class AndroidUtils extends MobileUtils {

	//TODO: review carefully and remove duplicates and migrate completely to fluent waits
    protected static final Logger LOGGER = Logger.getLogger(AndroidUtils.class);
    private static final int SCROLL_MAX_SEARCH_SWIPES = 55;
    private static final long SCROLL_TIMEOUT = 300;


    /**
     * execute Key Event
     *
     * @param keyCode int
     */
    public static void executeKeyEvent(int keyCode) {
        WebDriver driver = getDriver();
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
            ((PressesKeyCode) getDriver()).pressKeyCode(keyCode);
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
    
    public static void pressKeyboardKey(AndroidKey key) {
        ((AndroidDriver<?>) getDriver()).pressKey(new KeyEvent(key)
                .withFlag(KeyEventFlag.SOFT_KEYBOARD).withFlag(KeyEventFlag.KEEP_TOUCH_MODE).withFlag(KeyEventFlag.EDITOR_ACTION));
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

        WebDriver driver = getDriver();

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
        
        Dimension size = helper.performIgnoreException(() -> elem.getElement().getSize());
        int width = size.getWidth();
        int height = size.getHeight();
        
        
        size = helper.performIgnoreException(() -> driver.manage().window().getSize());
        int screen_size_x = size.getWidth();
        int screen_size_y = size.getHeight();

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
    @Deprecated
    public static void hideKeyboard() {
        MobileUtils.hideKeyboard();
    }
    
    @SuppressWarnings("deprecation")
	public static void pressBack() {
        ((AndroidDriver<?>) getDriver()).pressKeyCode(AndroidKeyCode.BACK);
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
    @SuppressWarnings("rawtypes")
	public static void pressBottomRightKey() {
    	WebDriver driver = getDriver();
    	Dimension size = helper.performIgnoreException(() -> driver.manage().window().getSize());
        int height =  size.getHeight();
        int width = size.getWidth();

		PointOption<?> option = PointOption.point(Double.valueOf(width * 0.915).intValue(), Double.valueOf(height * 0.945).intValue());
        new TouchAction((AndroidDriver<?>) driver).tap(option).perform();
    }

    /**
     * wait Until Element Not Present
     *
     * @param locator By
     * @param timeout long
     * @param pollingTime long
     */
    @Deprecated
    public static void waitUntilElementNotPresent(final By locator, final long timeout, final long pollingTime) {
        LOGGER.info(String.format("Wait until element %s disappear", locator.toString()));
        WebDriver driver = getDriver();
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
    	//TODO: SZ migrate to FluentWaits
		return element.isElementPresent(5)
				&& (element.getElement().isSelected() || element.getAttribute("checked").equals("true"));
    }

    public enum SelectorType {
        TEXT, TEXT_CONTAINS, TEXT_STARTS_WITH, ID, DESCRIPTION, DESCRIPTION_CONTAINS, CLASS_NAME
    }

    /**
     * Scrolls into view in specified container by text only and return boolean
     *
     * @param container  ExtendedWebElement - defaults to id Selector Type
     * @param scrollToElement String defaults to text Selector Type
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer);
     **/
    public static ExtendedWebElement scroll(String scrollToElement, ExtendedWebElement container) {
        return scroll(scrollToElement, container, SelectorType.ID, SelectorType.TEXT);
    }

    /** Scrolls into view in a container specified by it's instance (index)
     * @param scrollToEle - has to be id, text, contentDesc or className
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param containerInstance - has to an instance number of desired container
     * @param eleSelectorType -  has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer, AndroidUtils.SelectorType.CLASS_NAME, 1,
     *                          AndroidUtils.SelectorType.TEXT);
     **/
    public static ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer, SelectorType containerSelectorType,
                          int containerInstance, SelectorType eleSelectorType) {
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		// TODO: support multi threaded WebDriver's removing DriverPool usage
		WebDriver drv = getDriver();

        //workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","),
                    ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
				By scrollBy = MobileBy.AndroidUIAutomator("new UiScrollable("
						+ getScrollContainerSelector(scrollableContainer, containerSelectorType) + ".instance("
						+ containerInstance + "))" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")"
						+ ".scrollIntoView(" + getScrollToElementSelector(scrollToEle, eleSelectorType) + ")");
                
				WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    LOGGER.info("Element found!!!");
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                LOGGER.error(String.format("Element %s:%s was NOT found.", eleSelectorType, scrollToEle), noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                MobileBy.AndroidUIAutomator("new UiScrollable(" +
                        getScrollContainerSelector(scrollableContainer, containerSelectorType)
                        + ".instance("+ containerInstance + ")).scrollForward()");
                LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /** Scrolls into view in specified container
     * @param scrollToEle - has to be id, text, contentDesc or className
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param containerInstance - has to an instance number of desired container
     * @param eleSelectorType -  has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param eleSelectorInstance - has to an instance number of desired container
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer, AndroidUtils.SelectorType.CLASS_NAME, 1,
     *                          AndroidUtils.SelectorType.TEXT, 2);
     **/
    public static ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer, SelectorType containerSelectorType,
                          int containerInstance, SelectorType eleSelectorType, int eleSelectorInstance) {
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		// TODO: support multi threaded WebDriver's removing DriverPool usage
		WebDriver drv = getDriver();

        //workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","),
                    ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
				By scrollBy = MobileBy.AndroidUIAutomator("new UiScrollable("
						+ getScrollContainerSelector(scrollableContainer, containerSelectorType) + ".instance("
						+ containerInstance + "))" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")"
						+ ".scrollIntoView(" + getScrollToElementSelector(scrollToEle, eleSelectorType) + ".instance("
						+ eleSelectorInstance + "))");
				
                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    LOGGER.info("Element found!!!");
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                LOGGER.error(String.format("Element %s:%s was NOT found.", eleSelectorType, scrollToEle), noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                MobileBy.AndroidUIAutomator("new UiScrollable(" +
                        getScrollContainerSelector(scrollableContainer, containerSelectorType)
                        + ".instance("+ containerInstance + ")).scrollForward()");
                LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /** Scrolls into view in specified container
     * @param scrollToEle - has to be id, text, contentDesc or className
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - container Selector type: has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param eleSelectorType -  scrollToEle Selector type: has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer, AndroidUtils.SelectorType.CLASS_NAME,
     *                          AndroidUtils.SelectorType.TEXT);
     **/
    public static ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer, SelectorType containerSelectorType,
                          SelectorType eleSelectorType){
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		// TODO: support multi threaded WebDriver's removing DriverPool usage
		WebDriver drv = getDriver();

        //workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","),
                    ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
				By scrollBy = MobileBy.AndroidUIAutomator(
						"new UiScrollable(" + getScrollContainerSelector(scrollableContainer, containerSelectorType)
								+ ")" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")" + ".scrollIntoView("
								+ getScrollToElementSelector(scrollToEle, eleSelectorType) + ")");
				
                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    LOGGER.info("Element found!!!");
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                LOGGER.error(String.format("Element %s:%s was NOT found.", eleSelectorType, scrollToEle), noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                MobileBy.AndroidUIAutomator("new UiScrollable(" +
                        getScrollContainerSelector(scrollableContainer, containerSelectorType) + ").scrollForward()");
                LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /** Scrolls into view in specified container
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - Selector type: has to be id, text, contentDesc or className
     * @return boolean
     * <p>
     **/
    private static String getScrollContainerSelector(ExtendedWebElement scrollableContainer, SelectorType containerSelectorType){
        LOGGER.debug(scrollableContainer.getBy().toString());
        String scrollableContainerBy;
        String scrollViewContainerFinder = "";

        switch (containerSelectorType){
            case TEXT:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.text:", "").trim();
                scrollViewContainerFinder = "new UiSelector().text(\"" + scrollableContainerBy + "\")";
                break;
            case TEXT_CONTAINS:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.textContains:", "").trim();
                scrollViewContainerFinder = "new UiSelector().textContains(\"" + scrollableContainerBy + "\")";
                break;
            case TEXT_STARTS_WITH:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.textStartsWith:", "").trim();
                scrollViewContainerFinder = "new UiSelector().textStartsWith(\"" + scrollableContainerBy + "\")";
                break;
            case ID:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.id:", "").trim();
                scrollViewContainerFinder = "new UiSelector().resourceId(\"" + scrollableContainerBy + "\")";
                break;
            case DESCRIPTION:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.description:", "").trim();
                scrollViewContainerFinder = "new UiSelector().description(\"" + scrollableContainerBy + "\")";
                break;
            case DESCRIPTION_CONTAINS:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.descriptionContains:", "").trim();
                scrollViewContainerFinder = "new UiSelector().descriptionContains(\"" + scrollableContainerBy + "\")";
                break;
            case CLASS_NAME:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.className:", "").trim();
                scrollViewContainerFinder = "new UiSelector().className(\"" + scrollableContainerBy + "\")";
                break;
            default:
                LOGGER.info("Please provide valid selectorType for element to be found...");
                break;
        }

        return scrollViewContainerFinder;

    }

    /** Scrolls into view in specified container
     * @param scrollToEle - String type
     * @param eleSelectorType - Selector type: has to be id, text, contentDesc or className
     * @return String
     * <p>
     **/
    private static String getScrollToElementSelector(String scrollToEle, SelectorType eleSelectorType){
        String neededElementFinder = "";
        String scrollToEleTrimmed;

        switch (eleSelectorType){
            case TEXT:
                neededElementFinder= "new UiSelector().text(\"" + scrollToEle + "\")";
                break;
            case TEXT_CONTAINS:
                neededElementFinder = "new UiSelector().textContains(\"" + scrollToEle + "\")";
                break;
            case TEXT_STARTS_WITH:
                neededElementFinder = "new UiSelector().textStartsWith(\"" + scrollToEle + "\")";
                break;
            case ID:
                scrollToEleTrimmed = scrollToEle.replace("By.id:", "").trim();
                neededElementFinder = "new UiSelector().resourceId(\"" + scrollToEleTrimmed + "\")";
                break;
            case DESCRIPTION:
                neededElementFinder = "new UiSelector().description(\"" + scrollToEle + "\")";
                break;
            case DESCRIPTION_CONTAINS:
                neededElementFinder = "new UiSelector().descriptionContains(\"" + scrollToEle + "\")";
                break;
            case CLASS_NAME:
                scrollToEleTrimmed = scrollToEle.replace("By.className:", "").trim();
                neededElementFinder = "new UiSelector().className(\"" + scrollToEleTrimmed + "\")";
                break;
            default:
                LOGGER.info("Please provide valid selectorType for element to be found...");
                break;
        }

        return neededElementFinder;
    }

    /** Scroll Timeout check
     * @param startTime - Long initial time for timeout count down
     **/
    public static void checkTimeout(long startTime){
        long elapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())-startTime;

        if (elapsed > SCROLL_TIMEOUT) {
            throw new NoSuchElementException("Scroll timeout has been reached..");
        }
    }

}
