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
package com.qaprosoft.carina.core.gui;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qaprosoft.carina.core.foundation.performance.ACTION_NAME;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedFieldDecorator;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocatorFactory;

public abstract class AbstractUIObject extends DriverHelper {
    protected String name;

    protected WebElement rootElement;

    /**
     * Initializes UI object using {@link PageFactory}. Whole browser window is used as search context
     * 
     * @param driver WebDriver
     */
    public AbstractUIObject(WebDriver driver) {
        this(driver, driver);
    }

    /**
     * Initializes UI object using {@link PageFactory}. Browser area for internal elements initialization is bordered by
     * SearchContext instance.
     * If {@link WebDriver} object is used as search context then whole browser window will be used for initialization
     * of {@link ExtendedWebElement} fields inside.
     * 
     * Note: implement this constructor if you want your {@link AbstractUIObject} instances marked with {@link FindBy}
     * to be auto-initialized on {@link AbstractPage} inheritors
     * 
     * @param driver WebDriver instance to initialize UI Object fields using PageFactory
     * @param searchContext Window area that will be used for locating of internal elements
     */
    public AbstractUIObject(WebDriver driver, SearchContext searchContext) {
        super(driver);
        ExtendedElementLocatorFactory factory = new ExtendedElementLocatorFactory(searchContext);
        PageFactory.initElements(new ExtendedFieldDecorator(factory, driver), this);
    }

    /**
     * Verifies if root {@link WebElement} presents on page.
     *
     * If {@link AbstractUIObject} field on {@link AbstractPage} is marked with {@link FindBy} annotation then this
     * locator will be used to instantiate rootElement
     * 
     * @param timeout
     *            - max timeout for waiting until rootElement appear
     * 
     * @return true - if rootElement is enabled and visible on browser's screen;
     *
     *         false - otherwise
     */
    public boolean isUIObjectPresent(int timeout) {
    	return waitUntil(ExpectedConditions.visibilityOf(rootElement), timeout);
    }

    public boolean isUIObjectPresent() {
    	return isUIObjectPresent(Configuration.getInt(Parameter.EXPLICIT_TIMEOUT));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WebElement getRootElement() {
        return rootElement;
    }

    public void setRootElement(WebElement rootElement) {
        this.rootElement = rootElement;
    }
    
    
    /**
     * Wait until any condition happens.
     *
     * @param condition - ExpectedCondition.
     * @param timeout - timeout.
     * @return true if condition happen.
     */
    //TODO: replace with extendedWebElement as only deliver functionality for getting driver and by from WebElement object only
	private boolean waitUntil(ExpectedCondition<?> condition, long timeout) {
		boolean result;
		final WebDriver drv = getDriver();
		Timer.start(ACTION_NAME.WAIT);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try {
			LOGGER.debug("waitUntil: starting..." + getName() + "; condition: " + condition.toString());
			wait.until(condition);
			result = true;
			LOGGER.debug("waitUntil: finished true..." + getName());
		} catch (NoSuchElementException | TimeoutException e) {
			// don't write exception even in debug mode
			LOGGER.debug("waitUntil: NoSuchElementException | TimeoutException e..." + getName());
			result = false;
		} catch (Exception e) {
			LOGGER.error("waitUntil: " + getName(), e);
			result = false;
		}
		Timer.stop(ACTION_NAME.WAIT);
		return result;
	}
	
}