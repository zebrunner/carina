/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.decorator;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.exception.DriverPoolException;
import com.qaprosoft.carina.core.foundation.performance.ACTION_NAME;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.IWebElement;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.resources.L10N;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.CaseInsensitiveXPath;
import com.qaprosoft.carina.core.foundation.webdriver.listener.DriverListener;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocator;
import com.qaprosoft.carina.core.foundation.webdriver.locator.LocatorType;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.LocatorConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.CaseInsensitiveConverter;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.ParamsToConvert;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive.Platform;
import com.sun.jersey.core.util.Base64;

import io.appium.java_client.MobileBy;

public class ExtendedWebElement implements IWebElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    private static final long RETRY_TIME = Configuration.getLong(Parameter.RETRY_INTERVAL);

    // we should keep both properties: driver and searchContext obligatory
    // driver is used for actions, javascripts execution etc
    // searchContext is used for searching element by default
    private WebDriver driver;
    private SearchContext searchContext;
    // todo replace by boolean after successfully testing
    private Boolean isSearchContextWebElement = null;

    private CryptoTool cryptoTool = new CryptoTool(Configuration.get(Parameter.CRYPTO_KEY_PATH));

    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    private WebElement element = null;
    private String name;
    private By by;

    private boolean caseInsensitive;

    private ElementLoadingStrategy loadingStrategy = ElementLoadingStrategy.valueOf(Configuration.get(Parameter.ELEMENT_LOADING_STRATEGY));

    private boolean isLocalized = false;

    // Converted array of objects to String for dynamic element locators
    private String formatValues = "";

    private LocatorConverter caseInsensitiveConverter;
    
    public ExtendedWebElement(By by, String name, WebDriver driver, SearchContext searchContext) {
        if (by == null) {
            throw new RuntimeException("By couldn't be null!");
        }
        if (driver == null) {
            throw new RuntimeException("driver couldn't be null!");
        }

        if (searchContext == null) {
            throw new RuntimeException("review stacktrace to analyze why searchContext is null");
        }

        this.by = by;
        this.name = name;
        this.driver = driver;
        this.searchContext = searchContext;
        this.isSearchContextWebElement = this.searchContext instanceof WebElement;
    }

    public ExtendedWebElement(By by, String name, WebDriver driver, SearchContext searchContext, Object[] formatValues) {
        this(by, name, driver, searchContext);
        this.formatValues = Arrays.toString(formatValues);
    }

    public ExtendedWebElement(WebElement element, String name, By by) {
        this(element, name);
        this.by = by;
    }

    public ExtendedWebElement(WebElement element, String name) {
    	this.name = name;
        this.element = element;
        
        //read searchContext from not null elements only
        if (element == null) {
            // it seems like we have to specify WebElement or By annotation! Add verification that By is valid in this case!
            if (getBy() == null) {
                try {
                    throw new RuntimeException("review stacktrace to analyze why tempBy is not populated correctly via reflection!");
                } catch (Throwable thr) {
                    LOGGER.warn("getBy() is null!", thr);
                }
            }
            return;
        }

		try {
			Field locatorField, searchContextField, byContextField, caseInsensitiveContextField = null;
			SearchContext tempSearchContext = null;

			if (element.getClass().toString().contains("EventFiringWebDriver$EventFiringWebElement")) {
				// reuse reflection to get internal fields
				locatorField = element.getClass().getDeclaredField("underlyingElement");
				locatorField.setAccessible(true);
				element = (RemoteWebElement) locatorField.get(element);
			}

			if (element instanceof RemoteWebElement) {
				tempSearchContext = ((RemoteWebElement) element).getWrappedDriver();
			} else if (element instanceof Proxy) {
				InvocationHandler innerProxy = Proxy.getInvocationHandler(((Proxy) element));

				locatorField = innerProxy.getClass().getDeclaredField("locator");
				locatorField.setAccessible(true);

                ExtendedElementLocator locator = (ExtendedElementLocator) locatorField.get(innerProxy);
                this.isLocalized = locator.isLocalized();

                if (isLocalized) {
                    this.name = locator.getClassName() + "." + name;
                }

				searchContextField = locator.getClass().getDeclaredField("searchContext");
				searchContextField.setAccessible(true);
				this.searchContext = tempSearchContext = (SearchContext) searchContextField.get(locator);

                caseInsensitiveContextField = locator.getClass().getDeclaredField("caseInsensitive");
                caseInsensitiveContextField.setAccessible(true);
                this.caseInsensitive = (Boolean) caseInsensitiveContextField.get(locator);

                byContextField = locator.getClass().getDeclaredField("by");
                byContextField.setAccessible(true);
                //TODO: identify if it is a child element and
                //	1. get rootBy
                //  2. append current "by" to the rootBy
                // -> it should allow to search via regular driver and fluent waits - getBy()
                this.by = (By) byContextField.get(locator);

                while (tempSearchContext instanceof Proxy) {
                    innerProxy = Proxy.getInvocationHandler(((Proxy) tempSearchContext));

					locatorField = innerProxy.getClass().getDeclaredField("locator");
					locatorField.setAccessible(true);

					locator = (ExtendedElementLocator) locatorField.get(innerProxy);
                    // #1691 fix L10N Localized annotation does not work when elements are nested and the
                    // parent element does not have an annotation.
					//this.isLocalized = locator.isLocalized();

					searchContextField = locator.getClass().getDeclaredField("searchContext");
					searchContextField.setAccessible(true);
					tempSearchContext = (SearchContext) searchContextField.get(locator);

                    caseInsensitiveContextField = locator.getClass().getDeclaredField("caseInsensitive");
                    caseInsensitiveContextField.setAccessible(true);
                    this.caseInsensitive = (Boolean) caseInsensitiveContextField.get(locator);

                    if (this.caseInsensitive) {
                        CaseInsensitiveXPath csx = locator.getCaseInsensitiveXPath();
                        Platform platform = Objects.equals(Configuration.getMobileApp(), "") ? Platform.WEB : Platform.MOBILE;
                        caseInsensitiveConverter = new CaseInsensitiveConverter(
                                new ParamsToConvert(csx.id(), csx.name(), csx.text(), csx.classAttr()), platform);
                    }
				}
			}

			if (tempSearchContext instanceof EventFiringWebDriver) {
				EventFiringWebDriver eventFirDriver = (EventFiringWebDriver) tempSearchContext;
				this.driver = eventFirDriver.getWrappedDriver();
				//TODO: [VD] it seems like method more and more complex. Let's analyze and avoid return from this line
				return;
			}

			if (tempSearchContext != null && tempSearchContext.getClass().toString().contains("EventFiringWebDriver$EventFiringWebElement")) {
				// reuse reflection to get internal fields
				locatorField = tempSearchContext.getClass().getDeclaredField("underlyingElement");
				locatorField.setAccessible(true);
				this.searchContext = tempSearchContext = (RemoteWebElement) locatorField.get(tempSearchContext);
			}

			if (tempSearchContext instanceof RemoteWebElement) {
//				this.driver = ((RemoteWebElement) searchContext).getWrappedDriver();
				tempSearchContext = ((RemoteWebElement) tempSearchContext).getWrappedDriver();
			}
			if (tempSearchContext != null && tempSearchContext instanceof RemoteWebDriver) {
				SessionId sessionId = ((RemoteWebDriver) tempSearchContext).getSessionId();
				if (this.searchContext == null) {
					// do not override if it was already initialized as it has
					// real searchContext which shouldn't be replaced by actual driver
					this.searchContext = tempSearchContext; 
				}
				//this.driver = (WebDriver) tempSearchContext;
				// that's the only place to use DriverPool to get driver.
                try {
                    //try to search securely in driver pool by sessionId
                    this.driver = IDriverPool.getDriver(sessionId);
                } catch (DriverPoolException ex) {
                    // seems like driver started outside of IDriverPool so try to register it as well
                    this.driver = (WebDriver) tempSearchContext;
                }
			} else {
				LOGGER.error("Undefined error for searchContext: " + tempSearchContext.toString());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (Throwable thr) {
			thr.printStackTrace();
			LOGGER.error("Unable to get Driver, searchContext and By via reflection!", thr);
        } finally {
            if (this.searchContext == null) {
                throw new RuntimeException("review stacktrace to analyze why searchContext is not populated correctly via reflection!");
            }
            this.isSearchContextWebElement = this.searchContext instanceof WebElement;
        }
    }

    /**
     * Get {@link WebElement} from current {@link ExtendedWebElement}<br>
     * Can produce {@link NoSuchElementException}
     * 
     * @return {@link WebElement}
     */
    public WebElement getElement() {
        if (this.element == null) {
            this.element = findElement();
        }
        return this.element;
    }

    /**
     * Refresh the element
     * 
     * @return current {@link ExtendedWebElement} element
     * @throws NoSuchElementException if the element was not found on the page
     */
    public ExtendedWebElement refresh() {
        this.element = findElement();
        return this;
    }

    /**
     * Check that element exists on the page<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and element search is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @return true if the element exists on the page, false otherwise
     */
    public boolean isPresent() {
        return isPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element exists on the page<br>
     * Element search is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param timeout timeout in seconds
     * @return true if the element exists on the page, false otherwise
     */
    public boolean isPresent(long timeout) {
        boolean isPresent = false;
        try {
            isPresent = waitUntil(getDefaultCondition(), timeout);
        } catch (StaleElementReferenceException e) {
            // there is no sense to continue as StaleElementReferenceException captured
            LOGGER.debug("waitUntil: StaleElementReferenceException", e);
        }
        return isPresent;
    }

    /**
     * @deprecated this method is incorrect. Use {@link #isPresent()} or {@link #isPresent(long)} instead
     */
    @Deprecated(since = "7.4.27", forRemoval = true)
    public boolean isPresent(By by, long timeout) {
        boolean res = false;
        try {
            res = waitUntil(getDefaultCondition(), timeout);
        } catch (StaleElementReferenceException e) {
            // there is no sense to continue as StaleElementReferenceException captured
            LOGGER.debug("waitUntil: StaleElementReferenceException", e);
        }
        return res;
    }

    /**
     * Wait until any condition happens
     *
     * @param condition see {@link ExpectedCondition}
     * @param timeout how long to wait for the evaluated condition to be true in seconds
     * @return true if condition happen, false otherwise
     */
    private boolean waitUntil(ExpectedCondition<?> condition, long timeout) {
        if (timeout < 1) {
            LOGGER.warn("Fluent wait less than 1sec timeout might hangs! Updating to 1 sec.");
            timeout = 1;
        }

        long retryInterval = getRetryInterval(timeout);

        // try to use better tickMillis clock
        Wait<WebDriver> wait = new WebDriverWait(getDriver(),
                java.time.Clock.tickMillis(java.time.ZoneId.systemDefault()),
                Sleeper.SYSTEM_SLEEPER,
                timeout,
                retryInterval)
                .withTimeout(Duration.ofSeconds(timeout));

        // [VD] Notes:
        // do not ignore TimeoutException or NoSuchSessionException otherwise you can wait for minutes instead of timeout!
        // [VD] note about NoSuchSessionException is pretty strange. Let's ignore here and return false only in case of
        // TimeoutException putting details into the debug log message. All the rest shouldn't be ignored

        // 7.3.17-SNAPSHOT. Removed NoSuchSessionException (Mar-11-2022)
        // .ignoring(NoSuchSessionException.class) // why do we ignore noSuchSession? Just to minimize errors?

        // 7.3.20.1686-SNAPSHOT. Removed ignoring WebDriverException (Jun-03-2022).
        // Goal to test if inside timeout happens first and remove interruption and future call
        // removed ".ignoring(NoSuchElementException.class);" as NotFoundException ignored by waiter itself
        // added explicit .withTimeout(Duration.ofSeconds(timeout));

        LOGGER.debug("waitUntil: starting... timeout: {}", timeout);
        boolean res = false;
        try {
            wait.until(condition);
            res = true;
        } catch (TimeoutException e) {
            LOGGER.debug("waitUntil: org.openqa.selenium.TimeoutException", e);
        } finally {
            LOGGER.debug("waiter is finished. conditions: {}", condition);
        }
        return res;
    }

    /**
     * Find and update current element
     * 
     * @return {@link WebElement}
     * @throws NoSuchElementException if element was not found
     */
    private WebElement findElement() {
        List<WebElement> elements = this.searchContext.findElements(this.by);
        if (elements.isEmpty()) {
            throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + this.by.toString());
        }
        if (elements.size() > 1) {
            // TODO: think about moving into the debug or info level
            LOGGER.warn("returned first but found {} elements by xpath: {}", elements.size(), getBy());
        }
        this.element = elements.get(0);
        return this.element;
    }
    
    public void setElement(WebElement element) {
        this.element = element;
    }

    public String getName() {
        return this.name + this.formatValues;
    }

    public String getNameWithLocator() {
        // todo investigate, how it can be that by is null?
        if (this.by != null) {
            return String.format("%s%s (%s)", this.name, this.formatValues, by);
        } else {
            return String.format("%s%s (n/a)", this.name, this.formatValues);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get {@link By} of the current element
     *
     * @return {@link By}
     */
    public By getBy() {
        // todo move this code from getter
        By value = by;
        if (caseInsensitiveConverter != null) {
            value = caseInsensitiveConverter.convert(this.by);
        }
        return value;
    }

    public void setBy(By by) {
        this.by = by;
    }

	public void setSearchContext(SearchContext searchContext) {
		this.searchContext = searchContext;
	}

	@Override
    public String toString() {
        return this.name;
    }

    /**
     * Get element text<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @return element text
     */
    public String getText() {
        return (String) doAction(ACTION_NAME.GET_TEXT, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Get element location<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @return point location, see {@link Point}
     */
    public Point getLocation() {
        return (Point) doAction(ACTION_NAME.GET_LOCATION, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Get element size<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @return dimension size, see {@link Dimension}
     */
    public Dimension getSize() {
        return (Dimension) doAction(ACTION_NAME.GET_SIZE, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Get element attribute<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param name name of attribute
     * @return attribute value as a string
     */
    public String getAttribute(String name) {
        return (String) doAction(ACTION_NAME.GET_ATTRIBUTE, EXPLICIT_TIMEOUT, getDefaultCondition(), name);
    }

    /**
     * Click on element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void click() {
        click(EXPLICIT_TIMEOUT);
    }

    /**
     * Click on element<br>
     * Checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param timeout timeout in seconds
     */
    public void click(long timeout) {
        click(timeout, getDefaultCondition());
    }

    /**
     * Click on element<br>
     *
     * @param timeout timeout in seconds
     * @param waitCondition to check element conditions before action
     */
    public void click(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.CLICK, timeout, waitCondition);
    }

    /**
     * Click on element by javascript<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void clickByJs() {
        clickByJs(EXPLICIT_TIMEOUT);
    }

    /**
     * Click on element by javascript<br>
     * Checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param timeout timeout in seconds
     */
    public void clickByJs(long timeout) {
        clickByJs(timeout, getDefaultCondition());
    }

    /**
     * Click on element by javascript
     *
     * @param timeout timeout, in seconds
     * @param waitCondition to check element conditions before action
     */
    public void clickByJs(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.CLICK_BY_JS, timeout, waitCondition);
    }

    /**
     * Click on element by Actions<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void clickByActions() {
        clickByActions(EXPLICIT_TIMEOUT);
    }

    /**
     * Click on element by Actions<br>
     * Checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param timeout timeout in seconds
     */
    public void clickByActions(long timeout) {
        clickByActions(timeout, getDefaultCondition());
    }

    /**
     * Click on element by Actions<br>
     *
     * @param timeout timeout in seconds
     * @param waitCondition to check element conditions before action
     */
    public void clickByActions(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.CLICK_BY_ACTIONS, timeout, waitCondition);
    }

    /**
     * Double-click on element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void doubleClick() {
        doubleClick(EXPLICIT_TIMEOUT);
    }

    /**
     * Double-click on element<br>
     * Checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param timeout timeout in seconds
     */
    public void doubleClick(long timeout) {
        doubleClick(timeout, getDefaultCondition());
    }

    /**
     * Double-click on element
     *
     * @param timeout timeout in seconds
     * @param waitCondition to check element conditions before action
     */
    public void doubleClick(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.DOUBLE_CLICK, timeout, waitCondition);
    }

    /**
     * Right click on element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void rightClick() {
        rightClick(EXPLICIT_TIMEOUT);
    }

    /**
     * Right click on element<br>
     * Checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param timeout timeout in seconds
     */
    public void rightClick(long timeout) {
        rightClick(timeout, getDefaultCondition());
    }

    /**
     * Right click on element
     *
     * @param timeout timeout in seconds
     * @param waitCondition to check element conditions before action
     */
    public void rightClick(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.RIGHT_CLICK, timeout, waitCondition);
    }

    /**
     * Hover mouse over element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void hover() {
        hover(null, null);
    }

    /**
     * Hover mouse over element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param xOffset x offset for moving
     * @param yOffset y offset for moving
     */
    public void hover(Integer xOffset, Integer yOffset) {
        doAction(ACTION_NAME.HOVER, EXPLICIT_TIMEOUT, getDefaultCondition(), xOffset, yOffset);
    }

    /**
     * Click on an element if it is present on the page<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and attempt to click an element will be carried out according to the rules of the {@link #click()} method
     *
     * @return true if the element was clicked successfully
     */
    public boolean clickIfPresent() {
        return clickIfPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Click on an element if it is present and visible on the page<br>
     *
     * @param timeout timeout during which an attempt will be made to check the presence and visibility of the element.
     *            And attempt to click an element will be carried out according to the rules of the {@link #click()} method
     * @return true if the element was clicked successfully
     */
    public boolean clickIfPresent(long timeout) {
        boolean present = isElementPresent(timeout);
        if (present) {
            click();
        }
        return present;
    }

    /**
     * Send keys to the element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param keys see {@link Keys}
     */
    public void sendKeys(Keys keys) {
        sendKeys(keys, EXPLICIT_TIMEOUT);
    }

    /**
     * Send keys to the element<br>
     * Checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param keys see {@link Keys}
     * @param timeout timeout in seconds
     */
    public void sendKeys(Keys keys, long timeout) {
        sendKeys(keys, timeout, getDefaultCondition());
    }

    /**
     * Send keys to the element
     *
     * @param keys see {@link Keys}
     * @param timeout timeout in seconds
     * @param waitCondition to check element condition before action
     */
    public void sendKeys(Keys keys, long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.SEND_KEYS, timeout, waitCondition, keys);
    }

    /**
     * Type text to the element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param text the text to enter into the element
     */
    public void type(String text) {
        type(text, EXPLICIT_TIMEOUT);
    }

    /**
     * Type text to the element<br>
     * Checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param text the text to enter to the element
     * @param timeout timeout in seconds
     */
    public void type(String text, long timeout) {
        type(text, timeout, getDefaultCondition());
    }

    /**
     * Type text to the element
     *
     * @param text the text to enter to the element
     * @param timeout timeout in seconds
     * @param waitCondition to check element condition before action
     */
    public void type(String text, long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.TYPE, timeout, waitCondition, text);
    }

    /**
     * Scroll to the element (applied only for desktop).
     * Useful for desktop with React
     */
    public void scrollTo() {
        if (Configuration.getDriverType().equals(SpecialKeywords.MOBILE)) {
            LOGGER.debug("scrollTo javascript is unsupported for mobile devices!");
            return;
        }
        try {
            Locatable locatableElement = (Locatable) this.findElement();
            // [VD] onScreen should be updated onto onPage as only 2nd one
            // returns real coordinates without scrolling... read below material
            // for details
            // https://groups.google.com/d/msg/selenium-developers/nJR5VnL-3Qs/uqUkXFw4FSwJ

            // [CB] onPage -> inViewPort
            // https://code.google.com/p/selenium/source/browse/java/client/src/org/openqa/selenium/remote/RemoteWebElement.java?r=abc64b1df10d5f5d72d11fba37fabf5e85644081
            int y = locatableElement.getCoordinates().inViewPort().getY();
            int offset = R.CONFIG.getInt("scroll_to_element_y_offset");
            ((JavascriptExecutor) getDriver()).executeScript("window.scrollBy(0," + (y - offset) + ");");
        } catch (Exception e) {
        	//do nothing
        }
    }

    /**
     * Inputs file path to the specified element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param filePath path
     */
    public void attachFile(String filePath) {
        doAction(ACTION_NAME.ATTACH_FILE, EXPLICIT_TIMEOUT, getDefaultCondition(), filePath);
    }

    /**
     * Check checkbox (for checkbox element only)<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void check() {
        doAction(ACTION_NAME.CHECK, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Uncheck checkbox (for checkbox element only)<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    public void uncheck() {
        doAction(ACTION_NAME.UNCHECK, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Get checkbox state<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @return - true if checked, false otherwise
     */
    public boolean isChecked() {
        return (boolean) doAction(ACTION_NAME.IS_CHECKED, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Get selected elements from one-value select<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @return selected value
     */
    public String getSelectedValue() {
        return (String) doAction(ACTION_NAME.GET_SELECTED_VALUE, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Get selected elements from multi-value select<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @return selected values
     */
    @SuppressWarnings("unchecked")
    public List<String> getSelectedValues() {
        return (List<String>) doAction(ACTION_NAME.GET_SELECTED_VALUES, EXPLICIT_TIMEOUT, getDefaultCondition());
    }

    /**
     * Select text in specified select element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param selectText select text
     * @return true if item selected, false otherwise
     */
    public boolean select(final String selectText) {
        return (boolean) doAction(ACTION_NAME.SELECT, EXPLICIT_TIMEOUT, getDefaultCondition(), selectText);
    }

    /**
     * Select multiple text values in specified select element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param values final String[]
     * @return boolean.
     */
    public boolean select(final String[] values) {
        return (boolean) doAction(ACTION_NAME.SELECT_VALUES, EXPLICIT_TIMEOUT, getDefaultCondition(), values);
    }

    /**
     * Select value according to text value matcher<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param matcher {@link} BaseMatcher
     * @return true if item selected, otherwise false.
     *         <p>
     *         Usage example: BaseMatcher&lt;String&gt; match=new
     *         BaseMatcher&lt;String&gt;() { {@literal @}Override public boolean
     *         matches(Object actual) { return actual.toString().contains(RequiredText);
     *         } {@literal @}Override public void describeTo(Description description) {
     *         } };
     */
    public boolean selectByMatcher(final BaseMatcher<String> matcher) {
        return (boolean) doAction(ACTION_NAME.SELECT_BY_MATCHER, EXPLICIT_TIMEOUT, getDefaultCondition(), matcher);
    }

    /**
     * Select first value according to partial text value<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param partialSelectText select by partial text
     * @return true if item selected, false otherwise
     */
    public boolean selectByPartialText(final String partialSelectText) {
        return (boolean) doAction(ACTION_NAME.SELECT_BY_PARTIAL_TEXT, EXPLICIT_TIMEOUT, getDefaultCondition(),
                partialSelectText);
    }

    /**
     * Select item by index in specified select element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and checking the state of an element before action is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param index to select by
     * @return true if item selected, false otherwise
     */
    public boolean select(final int index) {
        return (boolean) doAction(ACTION_NAME.SELECT_BY_INDEX, EXPLICIT_TIMEOUT, getDefaultCondition(), index);
    }

    // --------------------------------------------------------------------------
    // Base UI validations
    // --------------------------------------------------------------------------
    /**
     * Check that element present and visible<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * 
     * @return true if element present and visible, false otherwise
     */
    public boolean isElementPresent() {
        return isElementPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element present and visible
     *
     * @param timeout timeout in seconds
     * @return true if element present and visible, false otherwise
     */
    public boolean isElementPresent(long timeout) {
        // perform at once super-fast single selenium call and only if nothing found move to waitAction
        if (this.element != null) {
            try {
                if (this.element.isDisplayed()) {
                    return true;
                }
            } catch (Exception e) {
                // do nothing as element is not found as expected here
            }
        }

        // [VD] replace presenceOfElementLocated and visibilityOf conditions by single "visibilityOfElementLocated"
        // visibilityOf: Does not check for presence of the element as the error explains it
        // visibilityOfElementLocated: Checks to see if the element is present and also visible. To check visibility, it makes sure that the element
        // has a height and width greater than 0.
        // [AS] visibilityOf do the same as visibilityOfElementLocated !

        ExpectedCondition<?> visibilityCondition = this.searchContext instanceof WebElement
                ? ExpectedConditions.visibilityOfNestedElementsLocatedBy((WebElement) this.searchContext, getBy())
                : ExpectedConditions.visibilityOfElementLocated(getBy());

        return waitUntil(visibilityCondition, timeout);
    }

    /**
     * Check that element is not present and not visible
     *
     * @param timeout timeout in seconds
     * @return true if element is not present and not visible, false otherwise
     */
    public boolean isElementNotPresent(long timeout) {
        return !isElementPresent(timeout);
    }

    /**
     * Check if an element is clickable<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * 
     * @return true if an element is clickable, false otherwise
     */
    public boolean isClickable() {
        return isClickable(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element clickable within specified timeout
     *
     * @param timeout timeout in seconds
     * @return true if an element is clickable, false otherwise
     */
    public boolean isClickable(long timeout) {
        ExpectedCondition<?> condition = null;
        if (element != null) {
            condition = ExpectedConditions.elementToBeClickable(this.element);
        } else {
            if (this.isSearchContextWebElement) {
                condition = new ExpectedCondition<WebElement>() {
                    @Override
                    public WebElement apply(WebDriver driver) {
                        List<WebElement> elements = ExpectedConditions.visibilityOfNestedElementsLocatedBy(((WebElement) searchContext), getBy())
                                .apply(getDriver());
                        try {
                            if (!elements.isEmpty() && elements.get(0) != null && elements.get(0).isEnabled()) {
                                return elements.get(0);
                            }
                            return null;
                        } catch (StaleElementReferenceException e) {
                            return null;
                        }
                    }

                    @Override
                    public String toString() {
                        return "element to be clickable: " + getBy();
                    }
                };
            } else {
                condition = ExpectedConditions.elementToBeClickable(getBy());
            }

        }
        return waitUntil(condition, timeout);
    }

    /**
     * Checks that element is visible<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * 
     * @return true if an element is visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element is visible
     *
     * @param timeout timeout in seconds
     * @return true if an element is visible, false otherwise
     */
    public boolean isVisible(long timeout) {
        ExpectedCondition<?> condition = null;

        if (this.element != null) {
            condition = ExpectedConditions.visibilityOf(this.element);
        } else {
            condition = this.isSearchContextWebElement ? ExpectedConditions.visibilityOfNestedElementsLocatedBy((WebElement) this.searchContext, getBy())
                    : ExpectedConditions.visibilityOfElementLocated(getBy());
        }

        boolean res = false;
        try {
            res = waitUntil(condition, timeout);
        } catch (StaleElementReferenceException e) {
            // there is no sense to continue as StaleElementReferenceException captured
            LOGGER.debug("waitUntil: StaleElementReferenceException", e);
        }
        return res;
    }

    /**
     * Check that element with text is present<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * 
     * @param text the text by which the element will be searched
     * @return true if the element is present, false otherwise
     */
    public boolean isElementWithTextPresent(final String text) {
        return isElementWithTextPresent(text, EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element with text is present<br>
     * If the text is encrypted, an attempt will be made to decrypt it
     *
     * @param text the text by which the element will be searched
     * @param timeout timeout in seconds
     * @return true if the element is present, false otherwise
     */
    public boolean isElementWithTextPresent(final String text, long timeout) {
        final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);

        ExpectedCondition<Boolean> textCondition = null;
        if (this.element != null) {
            textCondition = ExpectedConditions.textToBePresentInElement(this.element, decryptedText);
        } else {
            if (this.isSearchContextWebElement) {
                textCondition = new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver driver) {
                        try {
                            String elementText = searchContext.findElement(getBy()).getText();
                            return elementText.contains(decryptedText);
                        } catch (StaleElementReferenceException e) {
                            return null;
                        }
                    }

                    @Override
                    public String toString() {
                        return String.format("text ('%s') to be present in element found by %s",
                                text, getBy());
                    }
                };
            } else {
                textCondition = ExpectedConditions.textToBePresentInElementLocated(getBy(), decryptedText);
            }
        }

        return waitUntil(textCondition, timeout);
        // TODO: restore below code as only projects are migrated to "isElementWithContainTextPresent"
        // return waitUntil(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(getBy()),
        // ExpectedConditions.textToBe(getBy(), decryptedText)), timeout);
    }

    /**
     * Assert if the element with the specified text is not present<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * 
     * @param text the text by which the element will be searched
     */
    public void assertElementWithTextPresent(final String text) {
        assertElementWithTextPresent(text, EXPLICIT_TIMEOUT);
    }

    /**
     * Assert if the element with the specified text is not present
     *
     * @param timeout timeout in seconds
     * @param text the text by which the element will be searched
     */
    public void assertElementWithTextPresent(final String text, long timeout) {
        if (!isElementWithTextPresent(text, timeout)) {
            Assert.fail(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.getMessage(getNameWithLocator(), text));
        }
    }

    /**
     * Assert if the element with the specified text is not present<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     */
    public void assertElementPresent() {
        assertElementPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Assert if the element with the specified text is not present
     *
     * @param timeout timeout in seconds
     */
    public void assertElementPresent(long timeout) {
        if (!isPresent(timeout)) {
            Assert.fail(Messager.ELEMENT_NOT_PRESENT.getMessage(getNameWithLocator()));
        }
    }

    /**
     * Find ExtendedWebElement using By starting search from this element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and the search of an element is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param by see {@link By}
     * @return {@link ExtendedWebElement} if element exists, null otherwise
     */
    public ExtendedWebElement findExtendedWebElement(By by) {
        return findExtendedWebElement(by, by.toString(), EXPLICIT_TIMEOUT);
    }

    /**
     * Find ExtendedWebElement using By starting search from this element<br>
     * The search of an element is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param by see {@link By}
     * @param timeout timeout during which the attempt to find the element will be made, in seconds
     * @return {@link ExtendedWebElement} if element exists, null otherwise
     */
    public ExtendedWebElement findExtendedWebElement(By by, long timeout) {
        return findExtendedWebElement(by, by.toString(), timeout);
    }

    /**
     * Find ExtendedWebElement using By starting search from this element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and the search of elements is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param by see {@link By}
     * @param name the name that will be given to the found element
     * @return {@link ExtendedWebElement} if element exists, null otherwise
     */
    public ExtendedWebElement findExtendedWebElement(final By by, String name) {
        return findExtendedWebElement(by, name, EXPLICIT_TIMEOUT);
    }

    /**
     * Find ExtendedWebElement using By starting search from this element<br>
     * The search of an element is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     * 
     * @param by see {@link By}
     * @param name the name that will be given to the found element
     * @param timeout timeout during which the attempt to find the element will be made, in seconds
     * @return {@link ExtendedWebElement}
     * @throws NoSuchElementException if the element is not present on the page
     */
    public ExtendedWebElement findExtendedWebElement(final By by, String name, long timeout) {
        ExtendedWebElement foundElement = new ExtendedWebElement(by, name, this.driver, getElement());
        if (!foundElement.isPresent(timeout)) {
            throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by);
        }
        return foundElement;
    }

    /**
     * Find list of ExtendedWebElement using By starting search from this element<br>
     * Action timeout is determined by {@link Parameter#EXPLICIT_TIMEOUT}
     * and the search of elements is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param by see {@link By}
     * @return {@link ExtendedWebElement}
     * @throws NoSuchElementException if the element is not present on the page
     */
    public List<ExtendedWebElement> findExtendedWebElements(By by) {
        return findExtendedWebElements(by, EXPLICIT_TIMEOUT);
    }

    /**
     * Find list of ExtendedWebElement using By starting search from this element<br>
     * The search of an element is based on {@link Parameter#ELEMENT_LOADING_STRATEGY}
     *
     * @param by see {@link By}
     * @param timeout timeout in seconds
     * @return {@link List} of {@link ExtendedWebElement}
     * @throws NoSuchElementException if at least one element is not present on the page
     */
    public List<ExtendedWebElement> findExtendedWebElements(final By by, long timeout) {
        ExtendedWebElement anyElement = new ExtendedWebElement(by, "any element", this.driver, getElement());

        if (!anyElement.isPresent(timeout)) {
            throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by);
        }

        List<ExtendedWebElement> extendedWebElements = new ArrayList<>();

        int i = 1;
        for (WebElement el : getElement().findElements(by)) {
            String name = "undefined";
            try {
                name = el.getText();
            } catch (Exception e) {
                /* do nothing */
                LOGGER.debug("Error while getting text from element.", e);
            }
            // we can't initiate ExtendedWebElement using by as it belongs to the list of elements
            extendedWebElements.add(new ExtendedWebElement(generateByForList(by, i), name, this.driver, getElement()));
            i++;
        }
        return extendedWebElements;
    }

    /**
     * Wait until element disappear
     *
     * @param timeout timeout in seconds
     * @return true if element disappeared, false otherwise
     */
    public boolean waitUntilElementDisappear(final long timeout) {
        boolean res = false;
        try {
            if (this.element == null) {
                // if element not found it will cause NoSuchElementException
                refresh();
            }

            // if element is stale, it will cause StaleElementReferenceException
            if (this.element.isDisplayed()) {
                LOGGER.info("Element {} detected. Waiting until disappear...", this.element.getTagName());
            } else {
                LOGGER.info("Element {} is not detected, i.e. disappeared", this.element.getTagName());
                // no sense to continue as element is not displayed so return asap
                return true;
            }

            res = waitUntil(ExpectedConditions.or(ExpectedConditions.stalenessOf(this.element),
                    ExpectedConditions.invisibilityOf(this.element)), timeout);
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            // element not present so means disappear
            LOGGER.debug("Element disappeared as exception catched: {}", e.getMessage());
            res = true;
        }
        return res;
    }

    /**
     * Used to format locator
     * 
     * @return ExtendedWebElement
     */
    public ExtendedWebElement format(Object... objects) {
        String locator = this.by.toString();
        By resultBy = null;

        if (locator.startsWith(LocatorType.ID.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                resultBy = caseInsensitiveConverter.convert(resultBy);
            } else {
                resultBy = By.id(String.format(StringUtils.remove(locator, LocatorType.ID.getStartsWith()), objects));
            }
        }

        if (locator.startsWith(LocatorType.NAME.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                resultBy = caseInsensitiveConverter.convert(resultBy);
            } else {
                resultBy = By.id(String.format(StringUtils.remove(locator, LocatorType.NAME.getStartsWith()), objects));
            }
        }

        if (locator.startsWith(LocatorType.XPATH.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                resultBy = caseInsensitiveConverter.convert(resultBy);
            } else {
                resultBy = By.xpath(String.format(StringUtils.remove(locator, LocatorType.XPATH.getStartsWith()), objects));
            }
        }

        if (locator.startsWith(LocatorType.LINKTEXT.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                resultBy = caseInsensitiveConverter.convert(resultBy);
            } else {
                resultBy = By.xpath(String.format(StringUtils.remove(locator, LocatorType.LINKTEXT.getStartsWith()), objects));
            }
        }

        if (locator.startsWith(LocatorType.PARTIAL_LINK_TEXT.getStartsWith())) {
            resultBy = By.partialLinkText(String.format(StringUtils.remove(locator, LocatorType.PARTIAL_LINK_TEXT.getStartsWith()), objects));
        }

        if (locator.startsWith(LocatorType.CSS.getStartsWith())) {
            resultBy = By.cssSelector(String.format(StringUtils.remove(locator, LocatorType.CSS.getStartsWith()), objects));
        }

        if (locator.startsWith(LocatorType.TAG_NAME.getStartsWith())) {
            resultBy = By.tagName(String.format(StringUtils.remove(locator, LocatorType.TAG_NAME.getStartsWith()), objects));
        }

        /*
         * All ClassChain locators start from **. e.g FindBy(xpath = "**'/XCUIElementTypeStaticText[`name CONTAINS[cd] '%s'`]")
         */
        if (locator.startsWith("By.IosClassChain: **")) {
            resultBy = MobileBy.iOSClassChain(String.format(StringUtils.remove(locator, "By.IosClassChain: "), objects));
        }

        if (locator.startsWith("By.IosNsPredicate: **")) {
            resultBy = MobileBy.iOSNsPredicateString(String.format(StringUtils.remove(locator, "By.IosNsPredicate: "), objects));
        }

        if (locator.startsWith(LocatorType.ACCESSIBILITY_ID.getStartsWith())) {
            resultBy = MobileBy.AccessibilityId(String.format(StringUtils.remove(locator, LocatorType.ACCESSIBILITY_ID.getStartsWith()), objects));
        }

        if (locator.startsWith(LocatorType.IMAGE.getStartsWith())) {
            String formattedLocator = String.format(StringUtils.remove(locator, LocatorType.IMAGE.getStartsWith()), objects);
            Path path = Paths.get(formattedLocator);
            LOGGER.debug("Formatted locator is : {}", formattedLocator);
            String base64image;
            try {
                base64image = new String(Base64.encode(Files.readAllBytes(path)));
            } catch (IOException e) {
                throw new RuntimeException(
                        "Error while reading image file after formatting. Formatted locator : " + formattedLocator, e);
            }
            LOGGER.debug("Base64 image representation has benn successfully obtained after formatting.");
            resultBy = MobileBy.image(base64image);
        }

        if (locator.startsWith(LocatorType.ANDROID_UI_AUTOMATOR.getStartsWith())) {
            resultBy = MobileBy
                    .AndroidUIAutomator(String.format(StringUtils.remove(locator, LocatorType.ANDROID_UI_AUTOMATOR.getStartsWith()), objects));
            LOGGER.debug("Formatted locator is : {}", resultBy);
        }

        if (resultBy == null) {
            throw new RuntimeException("Locator formatting failed - no suitable locator type found for formatting");
        }

        return new ExtendedWebElement(resultBy, this.name, this.driver, this.searchContext, objects);
    }

    /**
     * Pause for specified timeout
     * 
     * @param timeout in seconds
     */
    public void pause(long timeout) {
        CommonUtils.pause(timeout);
    }

    public void pause(double timeout) {
        CommonUtils.pause(timeout);
    }

    
	public interface ActionSteps {
		void doClick();
		
		void doClickByJs();
		
		void doClickByActions();
		
		void doDoubleClick();

		void doRightClick();
		
		void doHover(Integer xOffset, Integer yOffset);

		void doType(String text);

		void doSendKeys(Keys keys);

		void doAttachFile(String filePath);

		void doCheck();

		void doUncheck();
		
		boolean doIsChecked();
		
		String doGetText();

		Point doGetLocation();

		Dimension doGetSize();

		String doGetAttribute(String name);

		boolean doSelect(String text);

		boolean doSelectValues(final String[] values);

		boolean doSelectByMatcher(final BaseMatcher<String> matcher);

		boolean doSelectByPartialText(final String partialSelectText);

		boolean doSelectByIndex(final int index);
		
		String doGetSelectedValue();
		
		List<String> doGetSelectedValues();
	}

    private Object executeAction(ACTION_NAME actionName, ActionSteps actionSteps, Object... inputArgs) {
        Object result = null;
        switch (actionName) {
        case CLICK:
            actionSteps.doClick();
            break;
        case CLICK_BY_JS:
            actionSteps.doClickByJs();
            break;
        case CLICK_BY_ACTIONS:
            actionSteps.doClickByActions();
            break;
        case DOUBLE_CLICK:
            actionSteps.doDoubleClick();
            break;
        case HOVER:
            actionSteps.doHover((Integer) inputArgs[0], (Integer) inputArgs[1]);
            break;
        case RIGHT_CLICK:
            actionSteps.doRightClick();
            break;
        case GET_TEXT:
            result = actionSteps.doGetText();
            break;
        case GET_LOCATION:
            result = actionSteps.doGetLocation();
            break;
        case GET_SIZE:
            result = actionSteps.doGetSize();
            break;
        case GET_ATTRIBUTE:
            result = actionSteps.doGetAttribute((String) inputArgs[0]);
            break;
        case SEND_KEYS:
            actionSteps.doSendKeys((Keys) inputArgs[0]);
            break;
        case TYPE:
            actionSteps.doType((String) inputArgs[0]);
            break;
        case ATTACH_FILE:
            actionSteps.doAttachFile((String) inputArgs[0]);
            break;
        case CHECK:
            actionSteps.doCheck();
            break;
        case UNCHECK:
            actionSteps.doUncheck();
            break;
        case IS_CHECKED:
            result = actionSteps.doIsChecked();
            break;
        case SELECT:
            result = actionSteps.doSelect((String) inputArgs[0]);
            break;
        case SELECT_VALUES:
            result = actionSteps.doSelectValues((String[]) inputArgs);
            break;
        case SELECT_BY_MATCHER:
            result = actionSteps.doSelectByMatcher((BaseMatcher<String>) inputArgs[0]);
            break;
        case SELECT_BY_PARTIAL_TEXT:
            result = actionSteps.doSelectByPartialText((String) inputArgs[0]);
            break;
        case SELECT_BY_INDEX:
            result = actionSteps.doSelectByIndex((int) inputArgs[0]);
            break;
        case GET_SELECTED_VALUE:
            result = actionSteps.doGetSelectedValue();
            break;
        case GET_SELECTED_VALUES:
            result = actionSteps.doGetSelectedValues();
            break;
        default:
            Assert.fail("Unsupported UI action name" + actionName.toString());
            break;
        }
        return result;
    }

	/**
	 * doAction on element.
     *
	 * @param actionName
     *            ACTION_NAME
	 * @param timeout
     *            long
	 * @param waitCondition
	 *            to check element conditions before action
     * @return
     *            Object
	 */
	private Object doAction(ACTION_NAME actionName, long timeout, ExpectedCondition<?> waitCondition) {
		// [VD] do not remove null args otherwise all actions without arguments will be broken!
		Object nullArgs = null;
		return doAction(actionName, timeout, waitCondition, nullArgs);
	}

    private Object doAction(ACTION_NAME actionName, long timeout, ExpectedCondition<?> waitCondition,
            Object... inputArgs) {

        if (waitCondition != null) {
            // do verification only if waitCondition is not null
            if (!waitUntil(waitCondition, timeout)) {
                // TODO: think about raising exception otherwise we do extra call and might wait and hangs especially for mobile/appium
                LOGGER.error(Messager.ELEMENT_CONDITION_NOT_VERIFIED.getMessage(actionName.getKey(), getNameWithLocator()));
            }
        }

        if (isLocalized) {
            isLocalized = false; // single verification is enough for this particular element
            L10N.verify(this);
        }

        Object output = null;

        try {
            this.element = getElement();
            output = overrideAction(actionName, inputArgs);
        } catch (StaleElementReferenceException e) {
            //TODO: analyze mobile testing for staled elements. Potentially it should be fixed by appium java client already
            // sometime Appium instead printing valid StaleElementException generate java.lang.ClassCastException:
            // com.google.common.collect.Maps$TransformedEntriesMap cannot be cast to java.lang.String
            LOGGER.debug("catched StaleElementReferenceException: ", e);
            // try to find again using driver context and do action
            element = this.findElement();
            output = overrideAction(actionName, inputArgs);
        }

        return output;
	}

	// single place for all supported UI actions in carina core
	private Object overrideAction(ACTION_NAME actionName, Object...inputArgs) {
		return executeAction(actionName, new ActionSteps() {
			@Override
			public void doClick() {
                DriverListener.setMessages(Messager.ELEMENT_CLICKED.getMessage(getName()),
                        Messager.ELEMENT_NOT_CLICKED.getMessage(getNameWithLocator()));
                element.click();
			}
			
            @Override
            public void doClickByJs() {
                DriverListener.setMessages(Messager.ELEMENT_CLICKED.getMessage(getName()),
                        Messager.ELEMENT_NOT_CLICKED.getMessage(getNameWithLocator()));

                LOGGER.info("Do click by JavascriptExecutor for element: " + getNameWithLocator());
                JavascriptExecutor executor = (JavascriptExecutor) getDriver();
                executor.executeScript("arguments[0].click();", element);
            }
            
            @Override
            public void doClickByActions() {
                DriverListener.setMessages(Messager.ELEMENT_CLICKED.getMessage(getName()),
                        Messager.ELEMENT_NOT_CLICKED.getMessage(getNameWithLocator()));

                LOGGER.info("Do click by Actions for element: " + getNameWithLocator());
                Actions actions = new Actions(getDriver());
                actions.moveToElement(element).click().perform();
            }     
			
			@Override
			public void doDoubleClick() {
				DriverListener.setMessages(Messager.ELEMENT_DOUBLE_CLICKED.getMessage(getName()),
						Messager.ELEMENT_NOT_DOUBLE_CLICKED.getMessage(getNameWithLocator()));
				
				WebDriver drv = getDriver();
				Actions action = new Actions(drv);
				action.moveToElement(element).doubleClick(element).build().perform();
			}
			
			@Override
			public void doHover(Integer xOffset, Integer yOffset) {
				DriverListener.setMessages(Messager.ELEMENT_HOVERED.getMessage(getName()),
						Messager.ELEMENT_NOT_HOVERED.getMessage(getNameWithLocator()));
				
				WebDriver drv = getDriver();
				Actions action = new Actions(drv);
				if (xOffset != null && yOffset!= null) {
					action.moveToElement(element, xOffset, yOffset).build().perform();
				} else {
					action.moveToElement(element).build().perform();
				}
			}
			
			@Override
			public void doSendKeys(Keys keys) {
				DriverListener.setMessages(Messager.KEYS_SEND_TO_ELEMENT.getMessage(keys.toString(), getName()),
						Messager.KEYS_NOT_SEND_TO_ELEMENT.getMessage(keys.toString(), getNameWithLocator()));
				element.sendKeys(keys);
			}

			@Override
			public void doType(String text) {
				final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);

/*				if (!element.getText().isEmpty()) {
    				DriverListener.setMessages(Messager.KEYS_CLEARED_IN_ELEMENT.getMessage(getName()),
    						Messager.KEYS_NOT_CLEARED_IN_ELEMENT.getMessage(getNameWithLocator()));
    				element.clear();
				}
*/
                DriverListener.setMessages(Messager.KEYS_CLEARED_IN_ELEMENT.getMessage(getName()),
                        Messager.KEYS_NOT_CLEARED_IN_ELEMENT.getMessage(getNameWithLocator()));
                element.clear();

				String textLog = (!decryptedText.equals(text) ? "********" : text);

				DriverListener.setMessages(Messager.KEYS_SEND_TO_ELEMENT.getMessage(textLog, getName()),
						Messager.KEYS_NOT_SEND_TO_ELEMENT.getMessage(textLog, getNameWithLocator()));

				element.sendKeys(decryptedText);
			}


			@Override
			public void doAttachFile(String filePath) {
				final String decryptedText = cryptoTool.decryptByPattern(filePath, CRYPTO_PATTERN);

				String textLog = (!decryptedText.equals(filePath) ? "********" : filePath);

				DriverListener.setMessages(Messager.FILE_ATTACHED.getMessage(textLog, getName()),
						Messager.FILE_NOT_ATTACHED.getMessage(textLog, getNameWithLocator()));

				((JavascriptExecutor) getDriver()).executeScript("arguments[0].style.display = 'block';", element);
				((RemoteWebDriver) castDriver(getDriver())).setFileDetector(new LocalFileDetector());
				element.sendKeys(decryptedText);
			}

			@Override
			public String doGetText() {
				String text = element.getText();
				LOGGER.debug(Messager.ELEMENT_ATTRIBUTE_FOUND.getMessage("Text", text, getName()));
				return text;
			}

			@Override
			public Point doGetLocation() {
				Point point = element.getLocation();
				LOGGER.debug(Messager.ELEMENT_ATTRIBUTE_FOUND.getMessage("Location", point.toString(), getName()));
				return point;
			}

			@Override
			public Dimension doGetSize() {
				Dimension dim = element.getSize();
				LOGGER.debug(Messager.ELEMENT_ATTRIBUTE_FOUND.getMessage("Size", dim.toString(), getName()));
				return dim;
			}

			@Override
			public String doGetAttribute(String name) {
				String attribute = element.getAttribute(name);
				LOGGER.debug(Messager.ELEMENT_ATTRIBUTE_FOUND.getMessage(name, attribute, getName()));
				return attribute;
			}

			@Override
			public void doRightClick() {
				DriverListener.setMessages(Messager.ELEMENT_RIGHT_CLICKED.getMessage(getName()),
						Messager.ELEMENT_NOT_RIGHT_CLICKED.getMessage(getNameWithLocator()));
				
				WebDriver drv = getDriver();
				Actions action = new Actions(drv);
				action.moveToElement(element).contextClick(element).build().perform();
			}

			@Override
			public void doCheck() {
				DriverListener.setMessages(Messager.CHECKBOX_CHECKED.getMessage(getName()), null);
				
                boolean isSelected = element.isSelected();
                if (element.getAttribute("checked") != null) {
                    isSelected |= element.getAttribute("checked").equalsIgnoreCase("true");
                }
                
				if (!isSelected) {
					click();
				}
			}

			@Override
			public void doUncheck() {
				DriverListener.setMessages(Messager.CHECKBOX_UNCHECKED.getMessage(getName()), null);
				
                boolean isSelected = element.isSelected();
                if (element.getAttribute("checked") != null) {
                    isSelected |= element.getAttribute("checked").equalsIgnoreCase("true");
                }
                
				if (isSelected) {
					click();
				}
			}
			
			@Override
			public boolean doIsChecked() {
				
		        boolean res = element.isSelected();
		        if (element.getAttribute("checked") != null) {
		            res |= element.getAttribute("checked").equalsIgnoreCase("true");
		        }
		        
		        return res;
			}
			
			@Override
			public boolean doSelect(String text) {
				final String decryptedSelectText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
				
				String textLog = (!decryptedSelectText.equals(text) ? "********" : text);
				
				DriverListener.setMessages(Messager.SELECT_BY_TEXT_PERFORMED.getMessage(textLog, getName()),
						Messager.SELECT_BY_TEXT_NOT_PERFORMED.getMessage(textLog, getNameWithLocator()));

				
				final Select s = new Select(getElement());
				// [VD] do not use selectByValue as modern controls could have only visible value without value
				s.selectByVisibleText(decryptedSelectText);
				return true;
			}

			@Override
			public boolean doSelectValues(String[] values) {
				boolean result = true;
				for (String value : values) {
					if (!select(value)) {
						result = false;
					}
				}
				return result;
			}

			@Override
			public boolean doSelectByMatcher(BaseMatcher<String> matcher) {
				
				DriverListener.setMessages(Messager.SELECT_BY_MATCHER_TEXT_PERFORMED.getMessage(matcher.toString(), getName()),
						Messager.SELECT_BY_MATCHER_TEXT_NOT_PERFORMED.getMessage(matcher.toString(), getNameWithLocator()));

				
				final Select s = new Select(getElement());
				String fullTextValue = null;
				for (WebElement option : s.getOptions()) {
					if (matcher.matches(option.getText())) {
						fullTextValue = option.getText();
						break;
					}
				}
				s.selectByVisibleText(fullTextValue);
				return true;
			}

			@Override
			public boolean doSelectByPartialText(String partialSelectText) {
				
				DriverListener.setMessages(
						Messager.SELECT_BY_TEXT_PERFORMED.getMessage(partialSelectText, getName()),
						Messager.SELECT_BY_TEXT_NOT_PERFORMED.getMessage(partialSelectText, getNameWithLocator()));
				
				final Select s = new Select(getElement());
				String fullTextValue = null;
				for (WebElement option : s.getOptions()) {
					if (option.getText().contains(partialSelectText)) {
						fullTextValue = option.getText();
						break;
					}
				}
				s.selectByVisibleText(fullTextValue);
				return true;
			}

			@Override
			public boolean doSelectByIndex(int index) {
				DriverListener.setMessages(
						Messager.SELECT_BY_INDEX_PERFORMED.getMessage(String.valueOf(index), getName()),
						Messager.SELECT_BY_INDEX_NOT_PERFORMED.getMessage(String.valueOf(index), getNameWithLocator()));
				
				
				final Select s = new Select(getElement());
				s.selectByIndex(index);
				return true;
			}

			@Override
			public String doGetSelectedValue() {
				final Select s = new Select(getElement());
				return s.getAllSelectedOptions().get(0).getText();
			}

			@Override
			public List<String> doGetSelectedValues() {
		        final Select s = new Select(getElement());
		        List<String> values = new ArrayList<String>();
		        for (WebElement we : s.getAllSelectedOptions()) {
		            values.add(we.getText());
		        }
		        return values;
			}
			
		}, inputArgs);
	}

    public WebDriver getDriver() {
		if (this.driver == null) {
			LOGGER.error("There is no any initialized driver for ExtendedWebElement: {}", getNameWithLocator());
			throw new RuntimeException(
					"Driver isn't initialized. Review stacktrace to analyze why driver is not populated correctly via reflection!");
		}
		return this.driver;
    }
    
    private WebDriver castDriver(WebDriver drv) {
        if (drv instanceof EventFiringWebDriver) {
            drv = ((EventFiringWebDriver) drv).getWrappedDriver();
        }
        return drv;
    }
    
	//TODO: investigate how can we merge the similar functionality in ExtendedWebElement, DriverHelper and LocalizedAnnotations
    public By generateByForList(By by, int index) {
        String locator = by.toString();
        By resBy = null;

        if (locator.startsWith(LocatorType.ID.getStartsWith())) {
            resBy = By.id(StringUtils.remove(locator, LocatorType.ID.getStartsWith()) + "[" + index + "]");
        }

        if (locator.startsWith(LocatorType.NAME.getStartsWith())) {
            resBy = By.name(StringUtils.remove(locator, LocatorType.NAME.getStartsWith()) + "[" + index + "]");
        }

        if (locator.startsWith(LocatorType.XPATH.getStartsWith())) {
            resBy = By.xpath(StringUtils.remove(locator, LocatorType.XPATH.getStartsWith()) + "[" + index + "]");
        }
        if (locator.startsWith(LocatorType.LINKTEXT.getStartsWith())) {
            resBy = By.linkText(StringUtils.remove(locator, LocatorType.LINKTEXT.getStartsWith()) + "[" + index + "]");
        }

        if (locator.startsWith(LocatorType.CLASSNAME.getStartsWith())) {
            resBy = By.className(StringUtils.remove(locator, LocatorType.CLASSNAME.getStartsWith()) + "[" + index + "]");
        }

        if (locator.startsWith(LocatorType.PARTIAL_LINK_TEXT.getStartsWith())) {
            resBy = By.partialLinkText(StringUtils.remove(locator, LocatorType.PARTIAL_LINK_TEXT.getStartsWith()) + "[" + index + "]");
        }

        if (locator.startsWith(LocatorType.CSS.getStartsWith())) {
            resBy = By.cssSelector(StringUtils.remove(locator, LocatorType.CSS.getStartsWith()) + ":nth-child(" + index + ")");
        }

        if (locator.startsWith(LocatorType.TAG_NAME.getStartsWith())) {
            resBy = By.tagName(StringUtils.remove(locator, LocatorType.TAG_NAME.getStartsWith()) + "[" + index + "]");
        }

        /*
         * All ClassChain locators start from **. e.g FindBy(xpath = "**'/XCUIElementTypeStaticText[`name CONTAINS[cd] '%s'`]")
         */
        if (locator.startsWith("By.IosClassChain: **")) {
            resBy = MobileBy.iOSClassChain(StringUtils.remove(locator, "By.IosClassChain: ") + "[" + index + "]");
        }

        if (locator.startsWith("By.IosNsPredicate: **")) {
            resBy = MobileBy.iOSNsPredicateString(StringUtils.remove(locator, "By.IosNsPredicate: ") + "[" + index + "]");
        }

        if (locator.startsWith(LocatorType.ACCESSIBILITY_ID.getStartsWith())) {
            resBy = MobileBy.AccessibilityId(StringUtils.remove(locator, LocatorType.ACCESSIBILITY_ID.getStartsWith()) + "[" + index + "]");
        }

        if (resBy == null) {
            throw new RuntimeException("Locator formatting failed - no suitable locator type found for generating by for element of list");
        }

        return resBy;
    }

    /**
     * Get condition to check presence/visibility/visibility-presence of element on the page<br>
     * This method must be context-sensitive
     * 
     * @return {@link ExpectedCondition} depends on the strategy, defined by the {@link Parameter#ELEMENT_LOADING_STRATEGY}
     */
    private ExpectedCondition<?> getDefaultCondition() {
        ExpectedCondition<?> condition = null;

        ExpectedCondition<?> presenceCondition = this.isSearchContextWebElement
                ? ExpectedConditions.presenceOfNestedElementLocatedBy((WebElement) this.searchContext, getBy())
                : ExpectedConditions.presenceOfElementLocated(getBy());

        ExpectedCondition<?> visibilityCondition = this.isSearchContextWebElement
                ? ExpectedConditions.visibilityOfNestedElementsLocatedBy((WebElement) this.searchContext, getBy())
                : ExpectedConditions.visibilityOfElementLocated(getBy());

        ExpectedCondition<?> visibilityElementCondition = ExpectedConditions.visibilityOf(this.element);

        switch (loadingStrategy) {
        case BY_PRESENCE: {
            condition = (this.element != null) ? ExpectedConditions.or(visibilityElementCondition, presenceCondition) : presenceCondition;
            break;
        }
        case BY_VISIBILITY: {
            condition = (this.element != null) ? ExpectedConditions.or(visibilityElementCondition, visibilityCondition) : visibilityCondition;
            break;
        }
        case BY_PRESENCE_OR_VISIBILITY:
            condition = (this.element != null) ? ExpectedConditions.or(visibilityElementCondition, visibilityCondition, presenceCondition)
                    : ExpectedConditions.or(visibilityCondition, presenceCondition);
            break;
        }
        return condition;
    }

    private long getRetryInterval(long timeout) {
        long retryInterval = RETRY_TIME;
        if (timeout >= 3 && timeout <= 10) {
            retryInterval = 500;
        }
        if (timeout > 10) {
            retryInterval = 1000;
        }
        return retryInterval;
    }
}
