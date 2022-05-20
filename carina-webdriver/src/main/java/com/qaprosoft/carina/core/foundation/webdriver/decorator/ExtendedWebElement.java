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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
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

    public ExtendedWebElement(WebElement element, String name, By by) {
        this(element, name);
        this.by = by;
    }

    public ExtendedWebElement(By by, String name) {
    	this.by = by;
    	this.name = name;
    }
    
    public ExtendedWebElement(By by, String name, WebDriver driver) {
    	this.by = by;
    	this.name = name;
    	this.driver = driver;
    }
    
    public ExtendedWebElement(By by, String name, WebDriver driver, SearchContext searchContext) {
        this.by = by;
        this.name = name;
        this.driver = driver;
        this.searchContext = searchContext;
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
					this.isLocalized = locator.isLocalized();

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
		}
		
    	if (this.searchContext == null) {
			try {
				throw new RuntimeException("review stacktrace to analyze why searchContext is not populated correctly via reflection!");
			} catch (Throwable thr) {
			    LOGGER.warn("this.searchContext is null!", thr);
			}
    	}
    }

    

    public ExtendedWebElement(By by, String name, WebDriver driver, SearchContext searchContext, Object[] formatValues) {
        this(by, name, driver, searchContext);
        this.formatValues = Arrays.toString(formatValues);
    }

    public WebElement getElement() {
        if (this.element == null) {
            this.element = this.findElement();
        }
        
        return this.element;
    }
    
    /**
     * Check that element present or visible.
     *
     * @return element presence status.
     */
    public boolean isPresent() {
    	return isPresent(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Check that element present or visible within specified timeout.
     *
     * @param timeout - timeout.
     * @return element existence status.
     */
    public boolean isPresent(long timeout) {
    	return isPresent(getBy(), timeout);
    }
    
	/**
	 * Check that element with By present within specified timeout.
	 *
	 * @param by
	 *            - By.
	 * @param timeout
	 *            - timeout.
	 * @return element existence status.
	 */
	public boolean isPresent(By by, long timeout) {
		return waitUntil(getDefaultCondition(by), timeout);
	}
	
	
    /**
     * Wait until any condition happens.
     *
     * @param condition - ExpectedCondition.
     * @param timeout - timeout.
     * @return true if condition happen.
     */
    private boolean waitUntil(ExpectedCondition<?> condition, long timeout) {
        if (timeout <= 0) {
            LOGGER.error("Fluent wait with 0 and less timeout might hangs! Updating to 1 sec.");
            timeout = 1;
        }
        boolean result = false;

//      Wait<WebDriver> wait = new WebDriverWait(getDriver(), timeout, RETRY_TIME)
        //try to use better tickMillis clock 
        Wait<WebDriver> wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeout), Duration.ofSeconds(RETRY_TIME),  java.time.Clock.tickMillis(java.time.ZoneId.systemDefault()), Sleeper.SYSTEM_SLEEPER)
                .ignoring(WebDriverException.class)
                .ignoring(NoSuchElementException.class);

        // [VD] Notes:
        // do not ignore TimeoutException or NoSuchSessionException otherwise you can wait for minutes instead of timeout!
        // [VD] note about NoSuchSessionException is pretty strange. Let's ignore here and return false only in case of
        // TimeoutException putting details into the debug log message. All the rest shouldn't be ignored
        
        // 7.3.17-SNAPSHOT. Removed NoSuchSessionException (Mar-11-2022)
        //.ignoring(NoSuchSessionException.class) // why do we ignore noSuchSession? Just to minimize errors?

        LOGGER.debug("waitUntil: starting... timeout: " + timeout);
        
        Future<?> future = Executors.newSingleThreadExecutor().submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                boolean res = false;
                try {
                    wait.until(condition);
                    res = true;
                } catch (TimeoutException e) {
                    LOGGER.debug("waitUntil: org.openqa.selenium.TimeoutException", e);
                }
                return res;
            }
        });
        
     // make future process timeout 10s longer
        long processTimeout = timeout + 10;
        try {
            result = (boolean) future.get(processTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (java.util.concurrent.TimeoutException e) {
            LOGGER.error("waitUntil: java.util.concurrent.TimeoutException: " + processTimeout + "s", e);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        return result;
    }

    private WebElement findElement() {
        // as we still provide several ways to init ExtendedWebElement without searchContext we have to use "if" operator and getDriver()
        // to use only searchContext we must remove all findExtendedWebElement(s) methods in DriverHelper which is not so simple
        if (searchContext != null) {
            //element = searchContext.findElement(by);
            List<WebElement> elements = searchContext.findElements(by);

            if (elements.isEmpty()) {
                throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by.toString());
            }
            this.element = elements.get(0);
        } else {
            // No way to remove this else piece as several projects play with this part of finder
            //LOGGER.warn("Please, inform Carina team about this warn providing logs...");
            List<WebElement> elements = getDriver().findElements(by);

            if (elements.isEmpty()) {
                throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by.toString());
            }

            this.element = elements.get(0);
        }

        return element;
    }
    
    public void setElement(WebElement element) {
        this.element = element;
    }

    public String getName() {
        return this.name + this.formatValues;
    }

    public String getNameWithLocator() {
        if (this.by != null) {
            return this.name + this.formatValues + String.format(" (%s)", by);
        } else {
            return this.name + this.formatValues + " (n/a)";
        }
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get element By.
     *
     * @return By by
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
        return name;
    }


    /**
     * Get element text.
     *
     * @return String text
     */
    public String getText() {
        return (String) doAction(ACTION_NAME.GET_TEXT, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Get element location.
     *
     * @return Point location
     */
    public Point getLocation() {
        return (Point) doAction(ACTION_NAME.GET_LOCATION, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Get element size.
     *
     * @return Dimension size
     */
    public Dimension getSize() {
        return (Dimension) doAction(ACTION_NAME.GET_SIZE, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Get element attribute.
     *
     * @param name of attribute
     * @return String attribute value
     */
    public String getAttribute(String name) {
        return (String) doAction(ACTION_NAME.GET_ATTRIBUTE, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), name);
    }

    /**
     * Click on element.
     */
    public void click() {
        click(EXPLICIT_TIMEOUT);
    }

    /**
     * Click on element.
     *
     * @param timeout to wait
     */
    public void click(long timeout) {
        click(timeout, getDefaultCondition(getBy()));
    }
    
	/**
	 * Click on element.
	 *
	 * @param timeout to wait
	 * @param waitCondition
	 *            to check element conditions before action
	 */
    public void click(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.CLICK, timeout, waitCondition);
    }
    
    /**
     * Click on element by javascript.
     */
    public void clickByJs() {
        clickByJs(EXPLICIT_TIMEOUT);
    }

    /**
     * Click on element by javascript.
     *
     * @param timeout to wait
     */
    public void clickByJs(long timeout) {
        clickByJs(timeout, getDefaultCondition(getBy()));
    }
    
    /**
     * Click on element by javascript.
     *
     * @param timeout to wait
     * @param waitCondition
     *            to check element conditions before action
     */
    public void clickByJs(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.CLICK_BY_JS, timeout, waitCondition);
    }
    
    /**
     * Click on element by Actions.
     */
    public void clickByActions() {
        clickByActions(EXPLICIT_TIMEOUT);
    }

    /**
     * Click on element by Actions.
     *
     * @param timeout to wait
     */
    public void clickByActions(long timeout) {
        clickByActions(timeout, getDefaultCondition(getBy()));
    }
    
    /**
     * Click on element by Actions.
     *
     * @param timeout to wait
     * @param waitCondition
     *            to check element conditions before action
     */
    public void clickByActions(long timeout, ExpectedCondition<?> waitCondition) {
        doAction(ACTION_NAME.CLICK_BY_ACTIONS, timeout, waitCondition);
    }
    
    /**
     * Double Click on element.
     */
    public void doubleClick() {
    	doubleClick(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Double Click on element.
     *
     * @param timeout to wait
     */
    public void doubleClick(long timeout) {
        doubleClick(timeout, getDefaultCondition(getBy()));
    }
    /**
     * Double Click on element.
     *
     * @param timeout to wait
	 * @param waitCondition
	 *            to check element conditions before action
     */
    public void doubleClick(long timeout, ExpectedCondition<?> waitCondition) {
    	doAction(ACTION_NAME.DOUBLE_CLICK, timeout, waitCondition);
    }

    
    /**
     * Mouse RightClick on element.
     */
    public void rightClick() {
    	rightClick(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Mouse RightClick on element.
     *
     * @param timeout to wait
     */
    public void rightClick(long timeout) {
        rightClick(timeout, getDefaultCondition(getBy()));
    }
    
    /**
     * Mouse RightClick on element.
     *
     * @param timeout to wait
	 * @param waitCondition
	 *            to check element conditions before action
     */
    public void rightClick(long timeout, ExpectedCondition<?> waitCondition) {
    	doAction(ACTION_NAME.RIGHT_CLICK, timeout, waitCondition);
    }
    

    /**
     * MouseOver (Hover) an element.
     */
    public void hover() {
        hover(null, null);
    }

    /**
     * MouseOver (Hover) an element.
	 * @param xOffset x offset for moving
	 * @param yOffset y offset for moving
     */
    public void hover(Integer xOffset, Integer yOffset) {
        doAction(ACTION_NAME.HOVER, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), xOffset, yOffset);
    }
    
    /**
     * Click onto element if it present.
     *
     * @return boolean return true if clicked
     */
    public boolean clickIfPresent() {
        return clickIfPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Click onto element if present.
     *
     * @param timeout - timeout
     * @return boolean return true if clicked
     */
    public boolean clickIfPresent(long timeout) {
        boolean present = isElementPresent(timeout);
        if (present) {
            click();
        }

        return present;
    }

    
    /**
     * Send Keys to element.
     * 
	 * @param keys Keys
     */
    public void sendKeys(Keys keys) {
    	sendKeys(keys, EXPLICIT_TIMEOUT);
    }

    /**
     * Send Keys to element.
     *
	 * @param keys Keys
     * @param timeout to wait
     */
    public void sendKeys(Keys keys, long timeout) {
        sendKeys(keys, timeout, getDefaultCondition(getBy()));
    }
    
	/**
	 * Send Keys to element.
	 *
	 * @param keys Keys
	 * @param timeout to wait
	 * @param waitCondition
	 *            to check element conditions before action
	 */
    public void sendKeys(Keys keys, long timeout, ExpectedCondition<?> waitCondition) {
    	doAction(ACTION_NAME.SEND_KEYS, timeout, waitCondition, keys);
    }
    
    
    /**
     * Type text to element.
     * 
	 * @param text String
     */
    public void type(String text) {
    	type(text, EXPLICIT_TIMEOUT);
    }

    /**
     * Type text to element.
     *
	 * @param text String
     * @param timeout to wait
     */
    public void type(String text, long timeout) {
        type(text, timeout, getDefaultCondition(getBy()));
    }
    
	/**
	 * Type text to element.
	 *
	 * @param text String
	 * @param timeout to wait
	 * @param waitCondition
	 *            to check element conditions before action
	 */
    public void type(String text, long timeout, ExpectedCondition<?> waitCondition) {
    	doAction(ACTION_NAME.TYPE, timeout, waitCondition, text);
    }
    
    /**
    /**
     * Scroll to element (applied only for desktop).
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
     
    /* Inputs file path to specified element.
     *
     * @param filePath path
     */
    public void attachFile(String filePath) {
        doAction(ACTION_NAME.ATTACH_FILE, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), filePath);
    }

    /**
     * Check checkbox
     * <p>
     * for checkbox Element
     */
    public void check() {
        doAction(ACTION_NAME.CHECK, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Uncheck checkbox
     * <p>
     * for checkbox Element
     */
    public void uncheck() {
        doAction(ACTION_NAME.UNCHECK, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Get checkbox state.
     *
     * @return - current state
     */
    public boolean isChecked() {
        return (boolean) doAction(ACTION_NAME.IS_CHECKED, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Get selected elements from one-value select.
     *
     * @return selected value
     */
    public String getSelectedValue() {
        return (String) doAction(ACTION_NAME.GET_SELECTED_VALUE, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Get selected elements from multi-value select.
     *
     * @return selected values
     */
    @SuppressWarnings("unchecked")
	public List<String> getSelectedValues() {
        return (List<String>) doAction(ACTION_NAME.GET_SELECTED_VALUES, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()));
    }

    /**
     * Selects text in specified select element.
     *
     * @param selectText select text
     * @return true if item selected, otherwise false.
     */
    public boolean select(final String selectText) {
        return (boolean) doAction(ACTION_NAME.SELECT, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), selectText);
    }

    /**
     * Select multiple text values in specified select element.
     *
     * @param values final String[]
     * @return boolean.
     */
    public boolean select(final String[] values) {
        return (boolean) doAction(ACTION_NAME.SELECT_VALUES, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), values);
    }

    /**
     * Selects value according to text value matcher.
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
        return (boolean) doAction(ACTION_NAME.SELECT_BY_MATCHER, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), matcher);
    }

    /**
     * Selects first value according to partial text value.
     *
     * @param partialSelectText select by partial text
     * @return true if item selected, otherwise false.
     */
    public boolean selectByPartialText(final String partialSelectText) {
        return (boolean) doAction(ACTION_NAME.SELECT_BY_PARTIAL_TEXT, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), partialSelectText);
    }

    /**
     * Selects item by index in specified select element.
     *
     * @param index to select by
     * @return true if item selected, otherwise false.
     */
    public boolean select(final int index) {
        return (boolean) doAction(ACTION_NAME.SELECT_BY_INDEX, EXPLICIT_TIMEOUT, getDefaultCondition(getBy()), index);
    }

    // --------------------------------------------------------------------------
    // Base UI validations
    // --------------------------------------------------------------------------
    /**
     * Check that element present and visible.
     *
     * @return element existence status.
     */
    public boolean isElementPresent() {
    	return isElementPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element present and visible within specified timeout.
     *
     * @param timeout - timeout.
     * @return element existence status.
     */
    public boolean isElementPresent(long timeout) {
		// perform at once super-fast single selenium call and only if nothing found move to waitAction
		if (element != null) {
			try {
				if (element.isDisplayed()) {
					return true;
				}
			} catch (Exception e) {
				//do nothing as element is not found as expected here
			}
		}

    	ExpectedCondition<?> waitCondition;
    	
        // [VD] replace presenceOfElementLocated and visibilityOf conditions by single "visibilityOfElementLocated"
        // visibilityOf: Does not check for presence of the element as the error explains it.
        // visibilityOfElementLocated: Checks to see if the element is present and also visible. To check visibility, it makes sure that the element
        // has a height and width greater than 0.
    	
        waitCondition = ExpectedConditions.visibilityOfElementLocated(getBy());
    	return waitUntil(waitCondition, timeout);
    }

    /**
     * Check that element not present and not visible within specified timeout.
     *
     * @param timeout - timeout.
     * @return element existence status.
     */
    public boolean isElementNotPresent(long timeout) {
        return !isElementPresent(timeout);
    }

    /**
     * Checks that element clickable.
     *
     * @return element clickability status.
     */
    public boolean isClickable() {
        return isClickable(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element clickable within specified timeout.
     *
     * @param timeout - timeout.
     * @return element clickability status.
     */
    public boolean isClickable(long timeout) {
    	ExpectedCondition<?> waitCondition;
    	
		if (element != null) {
			waitCondition = ExpectedConditions.elementToBeClickable(element);
		} else {
            waitCondition = ExpectedConditions.elementToBeClickable(getBy());
		}
		
    	return waitUntil(waitCondition, timeout);
    }

    /**
     * Checks that element visible.
     *
     * @return element visibility status.
     */
    public boolean isVisible() {
        return isVisible(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element visible within specified timeout.
     *
     * @param timeout - timeout.
     * @return element visibility status.
     */
	public boolean isVisible(long timeout) {
		ExpectedCondition<?> waitCondition;

        if (element != null) {
            waitCondition = ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(getBy()),
                    ExpectedConditions.visibilityOf(element));
        } else {
            waitCondition = ExpectedConditions.visibilityOfElementLocated(getBy());
        }

        return waitUntil(waitCondition, timeout);
    }

	
    /**
     * Check that element with text present.
     *
     * @param text of element to check.
     * @return element with text existence status.
     */
    public boolean isElementWithTextPresent(final String text) {
        return isElementWithTextPresent(text, EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element with text present.
     *
     * @param text of element to check.
     * @param timeout - timeout.
     * @return element with text existence status.
     */
    public boolean isElementWithTextPresent(final String text, long timeout) {
    	final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		ExpectedCondition<Boolean> textCondition;
		if (element != null) {
			textCondition = ExpectedConditions.textToBePresentInElement(element, decryptedText);
		} else {
            textCondition = ExpectedConditions.textToBePresentInElementLocated(getBy(), decryptedText);
		}
		return waitUntil(textCondition, timeout);
    	//TODO: restore below code as only projects are migrated to "isElementWithContainTextPresent"
//    	return waitUntil(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(getBy()),
//				ExpectedConditions.textToBe(getBy(), decryptedText)), timeout);

    }
    
    public void assertElementWithTextPresent(final String text) {
        assertElementWithTextPresent(text, EXPLICIT_TIMEOUT);
    }

    public void assertElementWithTextPresent(final String text, long timeout) {
        if (!isElementWithTextPresent(text, timeout)) {
            Assert.fail(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.getMessage(getNameWithLocator(), text));
        }
    }
    
    public void assertElementPresent() {
        assertElementPresent(EXPLICIT_TIMEOUT);
    }

    public void assertElementPresent(long timeout) {
		if (!isPresent(timeout)) {
			Assert.fail(Messager.ELEMENT_NOT_PRESENT.getMessage(getNameWithLocator()));
		}
    }



    /**
     * Find Extended Web Element on page using By starting search from this
     * object.
     *
     * @param by Selenium By locator
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(By by) {
        return findExtendedWebElement(by, by.toString(), EXPLICIT_TIMEOUT);
    }

    /**
     * Find Extended Web Element on page using By starting search from this
     * object.
     *
     * @param by Selenium By locator
     * @param timeout to wait
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(By by, long timeout) {
        return findExtendedWebElement(by, by.toString(), timeout);
    }

    /**
     * Find Extended Web Element on page using By starting search from this
     * object.
     *
     * @param by Selenium By locator
     * @param name Element name
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(final By by, String name) {
        return findExtendedWebElement(by, name, EXPLICIT_TIMEOUT);
    }

    /**
     * Find Extended Web Element on page using By starting search from this
     * object.
     *
     * @param by Selenium By locator
     * @param name Element name
     * @param timeout Timeout to find
     * @return ExtendedWebElement if exists otherwise null.
     */
    public ExtendedWebElement findExtendedWebElement(final By by, String name, long timeout) {
        if (isPresent(by, timeout)) {
            return new ExtendedWebElement(by, name, this.driver, this.searchContext);
        } else {
        	throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by.toString());
        }
    }

    public List<ExtendedWebElement> findExtendedWebElements(By by) {
        return findExtendedWebElements(by, EXPLICIT_TIMEOUT);
    }

    public List<ExtendedWebElement> findExtendedWebElements(final By by, long timeout) {
        List<ExtendedWebElement> extendedWebElements = new ArrayList<ExtendedWebElement>();
        List<WebElement> webElements = new ArrayList<WebElement>();
        
        if (isPresent(by, timeout)) {
            webElements = getElement().findElements(by);
        } else {
        	throw new NoSuchElementException(SpecialKeywords.NO_SUCH_ELEMENT_ERROR + by.toString());
        }

        int i = 1;
        for (WebElement element : webElements) {
            String name = "undefined";
            try {
                name = element.getText();
            } catch (Exception e) {
                /* do nothing */
                LOGGER.debug("Error while getting text from element.", e);
            }

            // we can't initiate ExtendedWebElement using by as it belongs to the list of elements
            extendedWebElements.add(new ExtendedWebElement(generateByForList(by, i), name, this.driver, this.searchContext));
            i++;
        }
        return extendedWebElements;
    }

    public boolean waitUntilElementDisappear(final long timeout) {
    	try {
        	//TODO: investigate maybe searchContext better to use here!
    		//do direct selenium/appium search without any extra validations
            if (searchContext != null) {
                //TODO: use-case when format method is used. Need investigate howto init context in this case as well
                element = searchContext.findElement(by);
            } else {
                LOGGER.debug("waitUntilElementDisappear: searchContext is null for " + getNameWithLocator());
                element = getDriver().findElement(by);  
            }
    	} catch (NoSuchElementException e) {
    		//element not present so means disappear
    		return true;
    	} catch (Exception e) {
    		//element not present so means disappear
    		LOGGER.error("Investigate use-case with disappeared element later!", e);
    		return true;
    	}

        return waitUntil(ExpectedConditions.or(ExpectedConditions.invisibilityOfElementLocated(getBy()),
                ExpectedConditions.stalenessOf(element),
                ExpectedConditions.invisibilityOf(element)), timeout);
    }

    public ExtendedWebElement format(Object... objects) {
        String locator = by.toString();
        By by = null;

        if (locator.startsWith(LocatorType.ID.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                by = caseInsensitiveConverter.convert(by);
            } else {
                by = By.id(String.format(StringUtils.remove(locator, LocatorType.ID.getStartsWith()), objects));
            }
        }

        if (locator.startsWith(LocatorType.NAME.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                by = caseInsensitiveConverter.convert(by);
            } else {
                by = By.id(String.format(StringUtils.remove(locator, LocatorType.NAME.getStartsWith()), objects));
            }
        }

        if (locator.startsWith(LocatorType.XPATH.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                by = caseInsensitiveConverter.convert(by);
            } else {
                by = By.xpath(String.format(StringUtils.remove(locator, LocatorType.XPATH.getStartsWith()), objects));
            }
        }

        if (locator.startsWith(LocatorType.LINKTEXT.getStartsWith())) {
            if (caseInsensitiveConverter != null) {
                by = caseInsensitiveConverter.convert(by);
            } else {
                by = By.xpath(String.format(StringUtils.remove(locator, LocatorType.LINKTEXT.getStartsWith()), objects));
            }
        }

        if (locator.startsWith("partialLinkText: ")) {
            by = By.partialLinkText(String.format(StringUtils.remove(locator, "partialLinkText: "), objects));
        }

        if (locator.startsWith("css: ")) {
            by = By.cssSelector(String.format(StringUtils.remove(locator, "css: "), objects));
        }

        if (locator.startsWith("tagName: ")) {
            by = By.tagName(String.format(StringUtils.remove(locator, "tagName: "), objects));
        }

        /*
         * All ClassChain locators start from **. e.g FindBy(xpath = "**'/XCUIElementTypeStaticText[`name CONTAINS[cd] '%s'`]")
         */
        if (locator.startsWith("By.IosClassChain: **")) {
            by = MobileBy.iOSClassChain(String.format(StringUtils.remove(locator, "By.IosClassChain: "), objects));
        }

        if (locator.startsWith("By.IosNsPredicate: **")) {
            by = MobileBy.iOSNsPredicateString(String.format(StringUtils.remove(locator, "By.IosNsPredicate: "), objects));
        }

        if (locator.startsWith("By.AccessibilityId: ")) {
            by = MobileBy.AccessibilityId(String.format(StringUtils.remove(locator, "By.AccessibilityId: "), objects));
        }

        if (locator.startsWith("By.Image: ")) {
            String formattedLocator = String.format(StringUtils.remove(locator, "By.Image: "), objects);
            Path path = Paths.get(formattedLocator);
            LOGGER.debug("Formatted locator is : " + formattedLocator);
            String base64image;
            try {
                base64image = new String(Base64.encode(Files.readAllBytes(path)));
            } catch (IOException e) {
                throw new RuntimeException(
                        "Error while reading image file after formatting. Formatted locator : " + formattedLocator, e);
            }
            LOGGER.debug("Base64 image representation has benn successfully obtained after formatting.");
            by = MobileBy.image(base64image);
        }

        if (locator.startsWith("By.AndroidUIAutomator: ")) {
            by = MobileBy.AndroidUIAutomator(String.format(StringUtils.remove(locator, "By.AndroidUIAutomator: "), objects));
            LOGGER.debug("Formatted locator is : " + by.toString());
        }

        return new ExtendedWebElement(by, name, this.driver, this.searchContext, objects);
    }


    /**
     * Pause for specified timeout.
     * 
     * @param timeout in seconds.
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
			Object...inputArgs) {
		
		if (waitCondition != null) {
			//do verification only if waitCondition is not null
			if (!waitUntil(waitCondition, timeout)) {
				//TODO: think about raising exception otherwise we do extra call and might wait and hangs especially for mobile/appium
				LOGGER.error(Messager.ELEMENT_CONDITION_NOT_VERIFIED.getMessage(actionName.getKey(), getNameWithLocator()));
			}
		}
		
        if (isLocalized) {
            isLocalized = false; // single verification is enough for this particular element
            L10N.verify(this);
        }

        this.element = getElement();
        return overrideAction(actionName, inputArgs);
	}

	// single place for all supported UI actions in carina core
	private Object overrideAction(ACTION_NAME actionName, Object...inputArgs) {
		Object output = executeAction(actionName, new ActionSteps() {
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
		return output;
	}

    public WebDriver getDriver() {
		if (driver == null) {
			LOGGER.error("There is no any initialized driver for ExtendedWebElement: " + getNameWithLocator());
			throw new RuntimeException(
					"Driver isn't initialized. Review stacktrace to analyze why driver is not populated correctly via reflection!");
		}
		return driver;
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

        if (locator.startsWith("partialLinkText: ")) {
        	resBy = By.partialLinkText(StringUtils.remove(locator, "partialLinkText: ") + "[" + index + "]");
        }

        if (locator.startsWith("css: ")) {
        	resBy = By.cssSelector(StringUtils.remove(locator, "css: ") + ":nth-child(" + index + ")");
        }
        
        if (locator.startsWith("By.cssSelector: ")) {
        	resBy = By.cssSelector(StringUtils.remove(locator, "By.cssSelector: ") + ":nth-child(" + index + ")");
        }

        if (locator.startsWith("tagName: ")) {
        	resBy = By.tagName(StringUtils.remove(locator, "tagName: ") + "[" + index + "]");
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

        if (locator.startsWith("By.AccessibilityId: ")) {
            resBy = MobileBy.AccessibilityId(StringUtils.remove(locator, "By.AccessibilityId: ") + "[" + index + "]");
        }
        return resBy;
    }
    
/*	private ExpectedCondition<?> getDefaultCondition(By myBy) {
        // generate the most popular wiatCondition to check if element visible or present
        return ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(myBy),
                ExpectedConditions.visibilityOfElementLocated(myBy));
    }*/

    // old functionality to remove completely after successfull testing
    private ExpectedCondition<?> getDefaultCondition(By myBy) {
        // generate the most popular waitCondition to check if element visible or present
        ExpectedCondition<?> waitCondition = null;
        switch (loadingStrategy) {
        case BY_PRESENCE: {
            if (element != null) {
                waitCondition = ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(myBy), ExpectedConditions.visibilityOf(element));
            } else {
                waitCondition = ExpectedConditions.presenceOfElementLocated(myBy);
            }
            break;
        }
        case BY_VISIBILITY: {
            if (element != null) {
                waitCondition = ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(myBy), ExpectedConditions.visibilityOf(element));
            } else {
                waitCondition = ExpectedConditions.visibilityOfElementLocated(myBy);
            }
            break;
        }
        case BY_PRESENCE_OR_VISIBILITY:
            if (element != null) {
                waitCondition = ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(myBy),
                        ExpectedConditions.visibilityOfElementLocated(myBy),
                        ExpectedConditions.visibilityOf(element));
            } else {
                waitCondition = ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(myBy),
                        ExpectedConditions.visibilityOfElementLocated(myBy));
            }
            break;
        }
        return waitCondition;
    }
}
