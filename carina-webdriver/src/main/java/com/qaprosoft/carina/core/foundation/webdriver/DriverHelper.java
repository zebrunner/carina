/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hamcrest.BaseMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.performance.ACTION_NAME;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.LogicUtils;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener;
import com.qaprosoft.carina.core.gui.AbstractPage;

/**
 * DriverHelper - WebDriver wrapper for logging and reporting features. Also it
 * contains some complex operations with UI.
 * 
 * @author Alex Khursevich
 */
public class DriverHelper {
    protected static final Logger LOGGER = Logger.getLogger(DriverHelper.class);

    protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);
    
    //TODO: remove in next release
    @Deprecated
    protected static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT) / 3;
    
    protected static final long SHORT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT) / 3;

    protected static final long RETRY_TIME = Configuration.getLong(Parameter.RETRY_INTERVAL);

    protected static Wait<WebDriver> wait;

    protected long timer;

    protected WebDriver driver;

    protected CryptoTool cryptoTool;

    protected static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    public DriverHelper() {
        cryptoTool = new CryptoTool(Configuration.get(Parameter.CRYPTO_KEY_PATH));
    }

    public DriverHelper(WebDriver driver) {
        cryptoTool = new CryptoTool(Configuration.get(Parameter.CRYPTO_KEY_PATH));
        this.driver = driver;

        if (driver == null) {
            throw new RuntimeException("[" + DevicePool.getDevice().getName() + "] WebDriver not initialized, check log files for details!");
        }

    }

    // --------------------------------------------------------------------------
    // Base UI interaction operations
    // --------------------------------------------------------------------------
    /**
     * Set implicit timeout.
     * 
     * @param timeout in seconds. Minimal value - 1 second
     */
    @Deprecated
    public void setImplicitTimeout(long timeout) {
    	LOGGER.error("DO NOTHING AS IMPLICIT_TIMEOUT IS NOT REQUIRED ANYMORE!");
    }

    /**
     *
     * @param element ExtendedWebElement
     * @param waitPeriod long in seconds.
     * @return true if element become clickable
     */
    @Deprecated
    public boolean waitForElementToBeClickable(ExtendedWebElement element, long waitPeriod) {
        return waitForElementToBeClickable(element, (int) waitPeriod);
    }

    /**
     * Wait for element to be clickable. Alternative for isElementPresent with
     * other condition.
     * 
     * @param element
     *            ExtendedWebElement
     * @param waitPeriod int in seconds.
     * @return true if element become clickable
     */
    @Deprecated
    public boolean waitForElementToBeClickable(ExtendedWebElement element, int waitPeriod) {
        return element.isClickable(waitPeriod);
    }

    /**
     * Check that element present.
     * 
     * @param extWebElement
     *            ExtendedWebElement
     * @return element existence status.
     */
    @Deprecated
    public boolean isElementPresent(final ExtendedWebElement extWebElement) {
        return isElementPresent(extWebElement, EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element present within specified timeout.
     * 
     * @param extWebElement
     *            ExtendedWebElement
     * @param timeout
     *            - timeout.
     * @return element existence status.
     */
    @Deprecated
    public boolean isElementPresent(final ExtendedWebElement extWebElement, long timeout) {
        if (extWebElement == null)
            return false;
        return extWebElement.isElementPresent(timeout);
    }

    @Deprecated
    public boolean isElementPresent(String controlInfo, final WebElement element) {
        return new ExtendedWebElement(element, controlInfo, getDriver()).isElementPresent();
    }

    @Deprecated
    public boolean isElementPresent(String controlInfo, final WebElement element, long timeout) {
        return new ExtendedWebElement(element, controlInfo, getDriver()).isElementPresent(timeout);
    }

    @Deprecated
    public boolean isElementPresent(String controlInfo, final By by) {
        return new ExtendedWebElement(by, controlInfo).isElementPresent();
    }
    /**
     * Method which quickly looks for all element and check that they present
     * during EXPLICIT_TIMEOUT
     *
     * @param elements
     *            ExtendedWebElement...
     * @return boolean return true only if all elements present.
     */
    public boolean allElementsPresent(ExtendedWebElement... elements) {
        return allElementsPresent(EXPLICIT_TIMEOUT, elements);
    }

    /**
     * Method which quickly looks for all element and check that they present
     * during timeout sec
     *
     * @param timeout long
     * @param elements
     *            ExtendedWebElement...
     * @return boolean return true only if all elements present.
     */
    public boolean allElementsPresent(long timeout, ExtendedWebElement... elements) {
        int index = 0;
        boolean present = true;
        boolean ret = true;
        int counts = 1;
        timeout = timeout / counts;
        if (timeout < 1)
            timeout = 1;
        while (present && index++ < counts) {
            for (int i = 0; i < elements.length; i++) {
                present = elements[i].isElementPresent(timeout);
                if (!present) {
                    LOGGER.error(elements[i].getNameWithLocator() + " is not present.");
                    ret = false;
                }
            }
        }
        return ret;
    }

    /**
     * Method which quickly looks for all element lists and check that they
     * contain at least one element during SHORT_TIMEOUT
     *
     * @param elements
     *            List&lt;ExtendedWebElement&gt;...
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    public boolean allElementListsAreNotEmpty(List<ExtendedWebElement>... elements) {
        return allElementListsAreNotEmpty(SHORT_TIMEOUT, elements);
    }

    /**
     * Method which quickly looks for all element lists and check that they
     * contain at least one element during timeout
     *
     * @param timeout long
     * @param elements
     *            List&lt;ExtendedWebElement&gt;...
     * @return boolean return true only if All Element lists contain at least
     *         one element
     */
    @SuppressWarnings("unchecked")
    public boolean allElementListsAreNotEmpty(long timeout, List<ExtendedWebElement>... elements) {
        boolean ret;
        int counts = 3;
        timeout = timeout / counts;
        if (timeout < 1)
            timeout = 1;
        for (int i = 0; i < elements.length; i++) {
            boolean present = false;
            int index = 0;
            while (!present && index++ < counts) {
                try {
                    present = elements[i].get(0).isElementPresent(timeout);
                } catch (Exception e) {
                    present = false;
                }
            }
            ret = (elements[i].size() > 0);
            if (!ret) {
                LOGGER.error("List of elements[" + i + "] from elements " + elements.toString() + " is empty.");
                return false;
            }
        }
        return true;
    }

    /**
     * Method which quickly looks for any element presence during
     * SHORT_TIMEOUT
     *
     * @param elements ExtendedWebElement...
     * @return true if any of elements was found.
     */
    public boolean isAnyElementPresent(ExtendedWebElement... elements) {
        return isAnyElementPresent(SHORT_TIMEOUT, elements);
    }

    /**
     * Method which quickly looks for any element presence during timeout sec
     *
     * @param timeout long
     * @param elements
     *            ExtendedWebElement...
     * @return true if any of elements was found.
     */
    public boolean isAnyElementPresent(long timeout, ExtendedWebElement... elements) {
        int index = 0;
        boolean present = false;
        int counts = 10;
        timeout = timeout / counts;
        if (timeout < 1)
            timeout = 1;
        while (!present && index++ < counts) {
            for (int i = 0; i < elements.length; i++) {
                present = elements[i].isElementPresent(timeout);
                if (present) {
                    LOGGER.debug(elements[i].getNameWithLocator() + " is present");
                    return true;
                }
            }
        }
        if (!present) {
            LOGGER.error("Unable to find any element from array: " + elements.toString());
            return false;
        }
        return present;
    }

    /**
     * return Any Present Element from the list which present during
     * SHORT_TIMEOUT
     *
     * @param elements ExtendedWebElement...
     * @return ExtendedWebElement
     */
    public ExtendedWebElement returnAnyPresentElement(ExtendedWebElement... elements) {
        return returnAnyPresentElement(SHORT_TIMEOUT, elements);
    }

    /**
     * return Any Present Element from the list which present during timeout sec
     *
     * @param timeout long
     * @param elements
     *            ExtendedWebElement...
     * @return ExtendedWebElement
     */
    public ExtendedWebElement returnAnyPresentElement(long timeout, ExtendedWebElement... elements) {
        int index = 0;
        boolean present = false;
        int counts = 10;
        timeout = timeout / counts;
        if (timeout < 1)
            timeout = 1;
        while (!present && index++ < counts) {
            for (int i = 0; i < elements.length; i++) {
                present = elements[i].isElementPresent(timeout);
                if (present) {
                    LOGGER.debug(elements[i].getNameWithLocator() + " is present");
                    return elements[i];
                }
            }
        }
        //throw exception anyway if nothing was returned inside for cycle
        LOGGER.error("All elements are not present");
        throw new RuntimeException("Unable to find any element from array: " + elements.toString());
    }

    /**
     * Check that element with text present.
     * 
     * @param extWebElement to check if element with text is present
     * @param text
     *            of element to check.
     * @return element with text existence status.
     */
    public boolean isElementWithTextPresent(final ExtendedWebElement extWebElement, final String text) {
        return isElementWithTextPresent(extWebElement, text, EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element with text present.
     * 
     * @param extWebElement to check if element with text is present
     * @param text
     *            of element to check.
     * @param timeout Long
     * @return element with text existence status.
     */
    public boolean isElementWithTextPresent(final ExtendedWebElement extWebElement, final String text, long timeout) {
        return extWebElement.isElementWithTextPresent(text, timeout);
    }

    /**
     * Check that element not present on page.
     * 
     * @param extWebElement to check if element is not present
     * 
     * @return element non-existence status.
     */
    public boolean isElementNotPresent(final ExtendedWebElement extWebElement) {
        return isElementNotPresent(extWebElement, EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element not present on page.
     * 
     * @param extWebElement to check if element is not present
     * @param timeout to wait
     * 
     * @return element non-existence status.
     */

    public boolean isElementNotPresent(final ExtendedWebElement extWebElement, long timeout) {
        return extWebElement.isElementNotPresent(timeout);
    }

    /**
     * Check that element not present on page.
     * 
     * @param element to check if element is not present
     * @param controlInfo String
     * 
     * @return element non-existence status.
     */

    public boolean isElementNotPresent(String controlInfo, final WebElement element) {
        return isElementNotPresent(new ExtendedWebElement(element, controlInfo));
    }

    /**
     * Types text to specified element.
     * 
     * @param extWebElement to type text to
     * @param text
     *            to type.
     */
    @Deprecated
    public void type(final ExtendedWebElement extWebElement, String text) {
        extWebElement.type(text);
    }

    /**
     * Types text to specified element.
     * 
     * @param control WebElement to type text to
     * @param controlInfo String
     * @param text to type
     * 
     */
    @Deprecated
    public void type(String controlInfo, WebElement control, String text) {
        type(new ExtendedWebElement(control, controlInfo, getDriver()), text);
    }

    /**
     * Clicks on element.
     * 
     * @param extendedWebElement to click on
     *
     */
    @Deprecated
    public void click(final ExtendedWebElement extendedWebElement) {
        click(extendedWebElement, EXPLICIT_TIMEOUT);
    }

    /**
     * Clicks on element.
     * 
     * @param extendedWebElement to click on
     * @param timeout to wait
     *
     */
    @Deprecated
    public void click(final ExtendedWebElement extendedWebElement, long timeout) {
        extendedWebElement.click(timeout);
    }

    /**
     * Clicks on element.
     * 
     * @param control WebElement to click on
     * @param controlInfo String
     *
     */
    @Deprecated
    public void click(String controlInfo, WebElement control) {
        click(new ExtendedWebElement(control, controlInfo, getDriver()));
    }

    /**
     * Clicks on element.
     * 
     * @param elements ExtendedWebElements to click
     *
     */

    public void clickAny(ExtendedWebElement... elements) {
        clickAny(EXPLICIT_TIMEOUT, elements);
    }

    /**
     * Clicks on element.
     * 
     * @param elements ExtendedWebElements to click
     * @param timeout to wait
     *
     */

    public void clickAny(long timeout, ExtendedWebElement... elements) {
        // Method which quickly looks for any element and click during timeout
        // sec
        int index = 0;
        boolean clicked = false;
        int counts = 10;
        while (!clicked && index++ < counts) {
            for (int i = 0; i < elements.length; i++) {
                clicked = elements[i].clickIfPresent(timeout / counts);
                if (clicked) {
                    break;
                }
            }
        }
        if (!clicked) {
            throw new RuntimeException("Unable to click onto any elements from array: " + elements.toString());
        }
    }

    /**
     * Clicks on element.
     * 
     * @param extWebElement element to check presence and click
     * 
     * @return boolean
     *
     */
    @Deprecated
    public boolean clickIfPresent(final ExtendedWebElement extWebElement) {
        return clickIfPresent(extWebElement, EXPLICIT_TIMEOUT);
    }

    /**
     * Clicks on element.
     * 
     * @param extWebElement element to check presence and click
     * @param timeout to wait
     * 
     * @return boolean
     *
     */
    @Deprecated
    public boolean clickIfPresent(final ExtendedWebElement extWebElement, long timeout) {
        return extWebElement.clickIfPresent(timeout);
    }

    /**
     * Double Clicks on element.
     * 
     * @param extendedWebElement to do dowble click
     *
     */
    @Deprecated
    public void doubleClick(final ExtendedWebElement extendedWebElement) {
        extendedWebElement.doubleClick();
    }

    /**
     * Double Clicks on element.
     * 
     * @param controlInfo String
     * @param control WebElement
     *
     */
    @Deprecated
    public void doubleClick(String controlInfo, WebElement control) {
        doubleClick(new ExtendedWebElement(control, controlInfo, getDriver()));
    }

    /**
     * Mouse Right click to element.
     * 
     * @param extendedWebElement to do right click
     * 
     */
    @Deprecated
    public void rightClick(final ExtendedWebElement extendedWebElement) {
        extendedWebElement.rightClick();
    }

    /**
     * Click Hidden Element. useful when element present in DOM but actually is
     * not visible. And can't be clicked by standard click.
     * 
     * @param extendedWebElement to click hidden element
     * 
     * @return boolean
     */
    @Deprecated
    public boolean clickHiddenElement(final ExtendedWebElement extendedWebElement) {
        extendedWebElement.clickHiddenElement();
        return true;
    }
    /**
     * Check checkbox
     * 
     * @param checkbox
     *            Element
     */
    @Deprecated
    public void check(ExtendedWebElement checkbox) {
        checkbox.check();
    }

    /**
     * Uncheck checkbox
     * 
     * @param checkbox
     *            Element
     */
    @Deprecated
    public void uncheck(ExtendedWebElement checkbox) {
        checkbox.uncheck();
    }

    /**
     * Get checkbox state.
     * 
     * @param checkbox
     *            - checkbox to test
     * @return - current state
     */
    @Deprecated
    public boolean isChecked(final ExtendedWebElement checkbox) {
        return checkbox.isChecked();
    }

    /**
     * Inputs file path to specified element.
     *
     * @param extendedWebElement
     *            Element
     * @param filePath
     *            path
     */
    @Deprecated
    public void attachFile(final ExtendedWebElement extendedWebElement, String filePath) {
        extendedWebElement.attachFile(filePath);
    }

    /**
     * Opens full or relative URL.
     * 
     * @param url
     *            to open.
     */
    public void openURL(String url) {
        String decryptedURL = cryptoTool.decryptByPattern(url, CRYPTO_PATTERN);
        decryptedURL = decryptedURL.contains("http:") || decryptedURL.contains("https:") ? decryptedURL
                : Configuration
                        .get(Parameter.URL) + decryptedURL;
        WebDriver drv = getDriver();
        
        try {
            drv.get(decryptedURL);
        } catch (UnhandledAlertException e) {
            drv.switchTo().alert().accept();
        }
        
        try {
    		if ("chrome".equalsIgnoreCase(Configuration.get(Parameter.BROWSER))) {
    			driver.manage().window().setSize(new Dimension(1920, 1040));
    		} else {
    			driver.manage().window().maximize();
    		}
        } catch (Exception e) {
        	LOGGER.error("Unable to maximize browser: " + e.getMessage(), e);
        }
		DriverListener.setMessages(Messager.OPEN_URL.getMessage(url), null);
    }

    /**
     * Checks that current URL is as expected.
     * 
     * @param expectedURL
     *            Expected Url
     * @return validation result.
     */
    public boolean isUrlAsExpected(String expectedURL) {
        String decryptedURL = cryptoTool.decryptByPattern(expectedURL, CRYPTO_PATTERN);
        decryptedURL = decryptedURL.startsWith("http") ? decryptedURL : Configuration.get(Parameter.URL) + decryptedURL;
        WebDriver drv = getDriver();
        if (LogicUtils.isURLEqual(decryptedURL, drv.getCurrentUrl())) {
            Messager.EXPECTED_URL.info(drv.getCurrentUrl());
            return true;
        } else {
            Messager.UNEXPECTED_URL.error(expectedURL, drv.getCurrentUrl());
            return false;
        }
    }

    /**
     * Pause for specified timeout.
     * 
     * @param timeout
     *            in seconds.
     */

    public void pause(long timeout) {
        CommonUtils.pause(timeout);
    }

    public void pause(Double timeout) {
        CommonUtils.pause(timeout);
    }

    /**
     * Checks that page title is as expected.
     * 
     * @param expectedTitle
     *            Expected title
     * @return validation result.
     */
    public boolean isTitleAsExpected(final String expectedTitle) {
        boolean result;
        final String decryptedExpectedTitle = cryptoTool.decryptByPattern(expectedTitle, CRYPTO_PATTERN);
        final WebDriver drv = getDriver();
        wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
        try {
            wait.until((Function<WebDriver, Object>) dr -> drv.getTitle().contains(decryptedExpectedTitle));
            result = true;
            Messager.TITLE_CORERECT.info(drv.getCurrentUrl(), expectedTitle);
        } catch (Exception e) {
            result = false;
            Messager.TITLE_NOT_CORERECT.error(drv.getCurrentUrl(), expectedTitle, drv.getTitle());
        }
        return result;
    }

    /**
     * Checks that page suites to expected pattern.
     * 
     * @param expectedPattern
     *            Expected Pattern
     * @return validation result.
     */
    public boolean isTitleAsExpectedPattern(String expectedPattern) {
        boolean result;
        final String decryptedExpectedPattern = cryptoTool.decryptByPattern(expectedPattern, CRYPTO_PATTERN);
        WebDriver drv = getDriver();
        String actual = drv.getTitle();
        Pattern p = Pattern.compile(decryptedExpectedPattern);
        Matcher m = p.matcher(actual);
        if (m.find()) {
            Messager.TITLE_CORERECT.info(drv.getCurrentUrl(), actual);
            result = true;
        } else {
            Messager.TITLE_DOES_NOT_MATCH_TO_PATTERN.error(drv.getCurrentUrl(), expectedPattern, actual);
            result = false;
        }
        return result;
    }

    /**
     * Go back in browser.
     */
    public void navigateBack() {
        getDriver().navigate().back();
        Messager.BACK.info();
    }

    /**
     * Refresh browser.
     */
    public void refresh() {
        getDriver().navigate().refresh();
        Messager.REFRESH.info();
    }

    /**
     * Refresh browser after timeout.
     * 
     * @param timeout
     *            before refresh.
     */
    public void refresh(long timeout) {
        CommonUtils.pause(timeout);
        refresh();
    }

    /**
     * Selects text in specified select element.
     *
     * @param extendedWebElement
     *            Element
     * @param selectText
     *            select text
     * @return true if item selected, otherwise false.
     */
    @Deprecated
    public boolean select(final ExtendedWebElement extendedWebElement, final String selectText) {
        return extendedWebElement.select(selectText);
    }

    /**
     * Select multiple text values in specified select element.
     * 
     * @param extendedWebElement final ExtendedWebElement
     * @param values final String[]
     * 
     * @return boolean
     */
    @Deprecated
    public boolean select(final ExtendedWebElement extendedWebElement, final String[] values) {
        return extendedWebElement.select(values);
    }

    @Deprecated
    public void select(String controlInfo, WebElement control, String selectText) {
        select(new ExtendedWebElement(control, controlInfo, getDriver()), selectText);
    }

    /**
     * Selects value according to text value matcher.
     *
     * @param extendedWebElement
     *            Element
     * @param matcher
     *            {@link} BaseMatcher
     * @return true if item selected, otherwise false.
     */
    @Deprecated
    public boolean selectByMatcher(final ExtendedWebElement extendedWebElement, final BaseMatcher<String> matcher) {
        return extendedWebElement.selectByMatcher(matcher);
    }

    /**
     * Selects first value according to partial text value.
     * 
     * @param extendedWebElement
     *            Element
     * @param partialSelectText
     *            select by partial text
     * @return true if item selected, otherwise false.
     */
    @Deprecated
    public boolean selectByPartialText(final ExtendedWebElement extendedWebElement, final String partialSelectText) {
        return extendedWebElement.selectByPartialText(partialSelectText);
    }

    /**
     * Selects item by index in specified select element.
     * 
     * @param extendedWebElement final ExtendedWebElement
     * @param index to select by
     * 
     * @return true if item selected, otherwise false.
     */
    @Deprecated
    public boolean select(final ExtendedWebElement extendedWebElement, final int index) {
        return extendedWebElement.select(index);
    }

    @Deprecated
    public void select(String controlInfo, WebElement control, int index) {
        select(new ExtendedWebElement(control, controlInfo, getDriver()), index);
    }

    /**
     * Hovers over element.
     * 
     * @param extendedWebElement
     *            final ExtendedWebElement
     *
     */
    @Deprecated
    public void hover(final ExtendedWebElement extendedWebElement) {
        hover(extendedWebElement, null, null);
    }

    @Deprecated
    public void hover(final ExtendedWebElement extendedWebElement, Integer xOffset, Integer yOffset) {
    	extendedWebElement.hover(xOffset, yOffset);
    }

    @Deprecated
    public void hover(String controlInfo, WebElement control) {
        hover(new ExtendedWebElement(control, controlInfo, getDriver()));
    }

    public void pressTab() {
        Actions builder = new Actions(getDriver());
        builder.sendKeys(Keys.TAB).perform();
    }

    @Deprecated
    public void sendKeys(String keys) {
        final String decryptedKeys = cryptoTool.decryptByPattern(keys, CRYPTO_PATTERN);
        Actions builder = new Actions(getDriver());
        builder.sendKeys(decryptedKeys).perform();
    }

    /**
     * Close alert modal by JS.
     */
    @Deprecated
    public void silentAlert() {
        WebDriver drv = getDriver();

        ((JavascriptExecutor) drv).executeScript("window.alert = function(msg) { return true; }");
        ((JavascriptExecutor) drv).executeScript("window.confirm = function(msg) { return true; }");
        ((JavascriptExecutor) drv).executeScript("window.prompt = function(msg) { return true; }");

    }

    /**
     * Drags and drops element to specified place.
     * 
     * @param from
     *            - element to drag.
     * @param to
     *            - element to drop to.
     */
    public void dragAndDrop(final ExtendedWebElement from, final ExtendedWebElement to) {

        if (isElementPresent(from) && isElementPresent(to)) {
            WebDriver drv = getDriver();
            if (!drv.toString().contains("safari")) {
                Actions builder = new Actions(drv);
                Action dragAndDrop = builder.clickAndHold(from.getElement()).moveToElement(to.getElement())
                        .release(to.getElement()).build();
                dragAndDrop.perform();
            } else {
                WebElement LocatorFrom = from.getElement();
                WebElement LocatorTo = to.getElement();
                String xto = Integer.toString(LocatorTo.getLocation().x);
                String yto = Integer.toString(LocatorTo.getLocation().y);
                ((JavascriptExecutor) driver)
                        .executeScript(
                                "function simulate(f,c,d,e){var b,a=null;for(b in eventMatchers)if(eventMatchers[b].test(c)){a=b;break}if(!a)return!1;document.createEvent?(b=document.createEvent(a),a==\"HTMLEvents\"?b.initEvent(c,!0,!0):b.initMouseEvent(c,!0,!0,document.defaultView,0,d,e,d,e,!1,!1,!1,!1,0,null),f.dispatchEvent(b)):(a=document.createEventObject(),a.detail=0,a.screenX=d,a.screenY=e,a.clientX=d,a.clientY=e,a.ctrlKey=!1,a.altKey=!1,a.shiftKey=!1,a.metaKey=!1,a.button=1,f.fireEvent(\"on\"+c,a));return!0} var eventMatchers={HTMLEvents:/^(?:load|unload|abort|error|select|change|submit|reset|focus|blur|resize|scroll)$/,MouseEvents:/^(?:click|dblclick|mouse(?:down|up|over|move|out))$/}; "
                                        + "simulate(arguments[0],\"mousedown\",0,0); simulate(arguments[0],\"mousemove\",arguments[1],arguments[2]); simulate(arguments[0],\"mouseup\",arguments[1],arguments[2]); ",
                                LocatorFrom, xto, yto);
            }

            Messager.ELEMENTS_DRAGGED_AND_DROPPED.info(from.getName(), to.getName());
        } else {
            Messager.ELEMENTS_NOT_DRAGGED_AND_DROPPED.error(from.getNameWithLocator(), to.getNameWithLocator());
        }
    }

    /**
     * Performs slider move for specified offset.
     * 
     * @param slider
     *            slider
     * @param moveX
     *            move x
     * @param moveY
     *            move y
     */
    public void slide(ExtendedWebElement slider, int moveX, int moveY) {
    	//TODO: SZ migrate to FluentWaits
        if (isElementPresent(slider)) {
            WebDriver drv = getDriver();
            (new Actions(drv)).moveToElement(slider.getElement()).dragAndDropBy(slider.getElement(), moveX, moveY)
                    .build().perform();
			Messager.SLIDER_MOVED.info(slider.getNameWithLocator(), String.valueOf(moveX), String.valueOf(moveY));
        } else {
            Messager.SLIDER_NOT_MOVED.error(slider.getNameWithLocator(), String.valueOf(moveX), String.valueOf(moveY));
        }
    }

    /**
     * Get selected elements from one-value select.
     * 
     * @param select
     *            Element
     * @return selected value
     */
    @Deprecated
    public String getSelectedValue(ExtendedWebElement select) {
        return select.getSelectedValue();
    }

    /**
     * Get selected elements from multi-value select.
     * 
     * @param select
     *            Element
     * @return selected value
     */
    @Deprecated
    public List<String> getSelectedValues(ExtendedWebElement select) {
        return select.getSelectedValues();
    }

    /**
     * Accepts alert modal.
     */
    public void acceptAlert() {
        WebDriver drv = getDriver();
        wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
        try {
            wait.until((Function<WebDriver, Object>) dr -> isAlertPresent());
            drv.switchTo().alert().accept();
            Messager.ALERT_ACCEPTED.info("");
        } catch (Exception e) {
            Messager.ALERT_NOT_ACCEPTED.error("");
        }
    }

    /**
     * Cancels alert modal.
     */
    public void cancelAlert() {
        WebDriver drv = getDriver();
        wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
        try {
            wait.until((Function<WebDriver, Object>) dr -> isAlertPresent());
            drv.switchTo().alert().dismiss();
            Messager.ALERT_CANCELED.info("");
        } catch (Exception e) {
            Messager.ALERT_NOT_CANCELED.error("");
        }
    }

    /**
     * Checks that alert modal is shown.
     * 
     * @return whether the alert modal present.
     */
    public boolean isAlertPresent() {
        try {
            getDriver().switchTo().alert();
            return true;
        } catch (NoAlertPresentException Ex) {
            return false;
        }
    }

    // --------------------------------------------------------------------------
    // Methods from v1.0
    // --------------------------------------------------------------------------
    public boolean isPageOpened(final AbstractPage page) {
        return isPageOpened(page, EXPLICIT_TIMEOUT);
    }

    public boolean isPageOpened(final AbstractPage page, long timeout) {
        boolean result;
        final WebDriver drv = getDriver();
        wait = new WebDriverWait(drv, timeout, RETRY_TIME);
        try {
            wait.until((Function<WebDriver, Object>) dr -> LogicUtils.isURLEqual(page.getPageURL(), drv.getCurrentUrl()));
            result = true;
        } catch (Exception e) {
            result = false;
        }
        if (!result) {
            LOGGER.warn(String.format("Actual URL differs from expected one. Expected '%s' but found '%s'",
                    page.getPageURL(), drv.getCurrentUrl()));
        }
        return result;
    }

    /**
     * Executes a script on an element
     * 
     * Really should only be used when the web driver is sucking at exposing
     * functionality natively
     * 
     * @param script
     *            The script to execute
     * @param element
     *            The target of the script, referenced as arguments[0]
     */
    public void trigger(String script, WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript(script, element);
    }

    /**
     * Executes a script
     * 
     * Really should only be used when the web driver is sucking at exposing
     * functionality natively
     * 
     * @param script
     *            The script to execute
     * 
     * @return Object
     */
    public Object trigger(String script) {
        return ((JavascriptExecutor) getDriver()).executeScript(script);
    }

    /**
     * Opens a new tab for the given URL
     * 
     * @param url
     *            The URL to
     * @throws RuntimeException
     *             If unable to open tab
     */
    public void openTab(String url) {
        final String decryptedURL = cryptoTool.decryptByPattern(url, CRYPTO_PATTERN);
        String script = "var d=document,a=d.createElement('a');a.target='_blank';a.href='%s';a.innerHTML='.';d.body.appendChild(a);return a";
        Object element = trigger(String.format(script, decryptedURL));
        if (element instanceof WebElement) {
            WebElement anchor = (WebElement) element;
            anchor.click();
            trigger("var a=arguments[0];a.parentNode.removeChild(a);", anchor);
        } else {
            throw new RuntimeException("Unable to open tab");
        }
    }

    public void switchWindow() throws NoSuchWindowException {
        WebDriver drv = getDriver();
        Set<String> handles = drv.getWindowHandles();
        String current = drv.getWindowHandle();
        if (handles.size() > 1) {
            handles.remove(current);
        }
        String newTab = handles.iterator().next();
        drv.switchTo().window(newTab);
    }

    // --------------------------------------------------------------------------
    // Base UI validations
    // --------------------------------------------------------------------------
    public void assertElementPresent(final ExtendedWebElement extWebElement) {
        assertElementPresent(extWebElement, EXPLICIT_TIMEOUT);
    }

    public void assertElementPresent(final ExtendedWebElement extWebElement, long timeout) {
        extWebElement.assertElementPresent(timeout);
    }

    public void assertElementWithTextPresent(final ExtendedWebElement extWebElement, final String text) {
        assertElementWithTextPresent(extWebElement, text, EXPLICIT_TIMEOUT);
    }

    public void assertElementWithTextPresent(final ExtendedWebElement extWebElement, final String text, long timeout) {
        extWebElement.assertElementWithTextPresent(text, timeout);
    }

    // --------------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------------

	/**
	 * Sends enter to element.
	 * 
	 * @param extendedWebElement
	 *            to send enter.
	 */
    @Deprecated
	public void pressEnter(final ExtendedWebElement extendedWebElement) {
		extendedWebElement.sendKeys(Keys.ENTER);
	}
    
    /**
     * Find Extended Web Element on page using By.
     * 
     * @param by
     *            Selenium By locator
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(By by) {
        return findExtendedWebElement(by, by.toString(), EXPLICIT_TIMEOUT);
    }

    /**
     * Find Extended Web Element on page using By.
     * 
     * @param by
     *            Selenium By locator
     * @param timeout to wait
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(By by, long timeout) {
        return findExtendedWebElement(by, by.toString(), timeout);
    }

    /**
     * Find Extended Web Element on page using By.
     * 
     * @param by
     *            Selenium By locator
     * @param name
     *            Element name
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(final By by, String name) {
        return findExtendedWebElement(by, name, EXPLICIT_TIMEOUT);
    }

    /**
     * Find Extended Web Element on page using By.
     * 
     * @param by
     *            Selenium By locator
     * @param name
     *            Element name
     * @param timeout
     *            Timeout to find
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(final By by, String name, long timeout) {
		DriverListener.setMessages(Messager.ELEMENT_FOUND.getMessage(name),
				Messager.ELEMENT_NOT_FOUND.getMessage(name));
    	
    	if (!waitUntil(ExpectedConditions.presenceOfElementLocated(by), timeout)) {
    		Messager.ELEMENT_NOT_FOUND.error(name);
    		return null;
    	}

    	return new ExtendedWebElement(by, name, getDriver());
    }

    /**
     * Find List of Extended Web Elements on page using By and explicit timeout.
     * 
     * @param by
     *            Selenium By locator
     * @return List of ExtendedWebElement.
     */
    public List<ExtendedWebElement> findExtendedWebElements(By by) {
        return findExtendedWebElements(by, EXPLICIT_TIMEOUT);
    }

    /**
     * Find List of Extended Web Elements on page using By.
     * 
     * @param by
     *            Selenium By locator
     * @param timeout
     *            Timeout to find
     * @return List of ExtendedWebElement.
     */
    public List<ExtendedWebElement> findExtendedWebElements(final By by, long timeout) {
        List<ExtendedWebElement> extendedWebElements = new ArrayList<ExtendedWebElement>();
        List<WebElement> webElements = new ArrayList<WebElement>();

        String name = "undefined";
    	if (!waitUntil(ExpectedConditions.presenceOfElementLocated(by), timeout)) {
    		Messager.ELEMENT_NOT_FOUND.info(name);
    		return extendedWebElements;
    	}
    	
    	webElements = getDriver().findElements(by);
    	int i = 1;
        for (WebElement element : webElements) {
            try {
                name = element.getText();
            } catch (Exception e) {
            	/* do nothing and keep 'undefined' for control name */
            }

            ExtendedWebElement tempElement = new ExtendedWebElement(element, name);
            tempElement.setBy(tempElement.generateByForList(by, i));
            extendedWebElements.add(tempElement);          
            i++;
        }
        return extendedWebElements;
    }

    protected void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    protected WebDriver getDriver() {
        if (driver == null) {
            long currentThreadId = Thread.currentThread().getId();
            LOGGER.error("There is no any initialized driver for thread: " + currentThreadId);
            throw new RuntimeException("Driver isn't initialized.");
        }
        return driver;
    }

    @Deprecated
    public ExtendedWebElement format(ExtendedWebElement element, Object... objects) {
        return element.format(objects);
    }

    
    /**
     * Wait until any condition happens.
     *
     * @param condition - ExpectedCondition.
     * @param timeout - timeout.
     * @return true if condition happen.
     */
	public boolean waitUntil(ExpectedCondition<?> condition, long timeout) {
		boolean result;
		final WebDriver drv = getDriver();
		Timer.start(ACTION_NAME.WAIT);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME).ignoring(WebDriverException.class)
				.ignoring(NoSuchSessionException.class);
		try {
			wait.until(condition);
			result = true;
			LOGGER.debug("waitUntil: finished true...");
		} catch (NoSuchElementException | TimeoutException e) {
			// don't write exception even in debug mode
			LOGGER.debug("waitUntil: NoSuchElementException | TimeoutException e..." + condition.toString());
			result = false;
		} catch (Exception e) {
			LOGGER.error("waitUntil: " + condition.toString(), e);
			result = false;
		}
		Timer.stop(ACTION_NAME.WAIT);
		return result;
	}
	
	/**
	 * Method to handle SocketException due to okhttp factory initialization (java client 6.*).
	 * Second execution of the same function works as expected.
	 *  
	 * @param supplier
	 * @return result
	 */
	public  <T> T performIgnoreException(Supplier<T> supplier) {
        try {
            LOGGER.info("Command will be performed with the exception ignoring");
            return supplier.get();
        } catch (WebDriverException e) {
            LOGGER.info("Webdriver exception has been fired. One more attempt to execute action.");
            LOGGER.info(supplier.toString());
            LOGGER.info(e);
            return supplier.get();
        }
        
    }
	
}