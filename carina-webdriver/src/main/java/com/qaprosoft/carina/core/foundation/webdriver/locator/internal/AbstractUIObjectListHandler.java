/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.webdriver.locator.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qaprosoft.carina.core.foundation.performance.ACTION_NAME;
import com.qaprosoft.carina.core.foundation.performance.Timer;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedFieldDecorator;
import com.qaprosoft.carina.core.gui.AbstractUIObject;

public class AbstractUIObjectListHandler<T extends AbstractUIObject> implements InvocationHandler {
    private Class<?> clazz;
    private WebDriver webDriver;
    private final ElementLocator locator;
    private String name;

    private By locatorBy;
    private Logger LOGGER = Logger.getLogger(ExtendedFieldDecorator.class);

    public AbstractUIObjectListHandler(Class<?> clazz, WebDriver webDriver, ElementLocator locator, String name) {
        this.clazz = clazz;
        this.webDriver = webDriver;
        this.locator = locator;
        this.name = name;
        this.locatorBy = getLocatorBy(locator);
    }

    @SuppressWarnings("unchecked")
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
    	
		// Hotfix for huge and expected regression in carina: we lost managed
		// time delays with lists manipulations
		// Temporary we are going to restore explicit waiter here with hardcoded
		// timeout before we find better solution
		// Pros: super fast regression issue which block UI execution
		// Cons: there is no way to manage timeouts in this places

    	waitUntil(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locatorBy),
    			ExpectedConditions.visibilityOfElementLocated(locatorBy)));

    	List<WebElement> elements = null;
    	try {
    		elements = locator.findElements();
		} catch (StaleElementReferenceException | InvalidElementStateException e) {
			LOGGER.debug("catched StaleElementReferenceException: ", e);
			elements = webDriver.findElements(locatorBy);
		}

        List<T> uIObjects = new ArrayList<T>();
        int index = 0;
        if (elements != null) {
            for (WebElement element : elements) {
                T uiObject;
                try {
                    uiObject = (T) clazz.getConstructor(WebDriver.class, SearchContext.class)
                            .newInstance(
                                    webDriver, element);
                } catch (NoSuchMethodException e) {
                    LOGGER.error("Implement appropriate AbstractUIObject constructor for auto-initialization: "
                            + e.getMessage());
                    throw new RuntimeException(
                            "Implement appropriate AbstractUIObject constructor for auto-initialization: "
                                    + e.getMessage(),
                            e);
                }
                uiObject.setName(String.format("%s - %d", name, index++));
                uiObject.setRootElement(element);
                uiObject.setRootBy(locatorBy);
                uIObjects.add(uiObject);
            }
        }

        try {
            return method.invoke(uIObjects, objects);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    private By getLocatorBy(ElementLocator locator) {
    	By rootBy = null;
    	
        //TODO: get root by annotation from ElementLocator to be able to append by for those elements and reuse fluent waits
		try {
			Field byContextField = null;

			byContextField = locator.getClass().getDeclaredField("by");
			byContextField.setAccessible(true);
			rootBy = (By) byContextField.get(locator);

		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (Throwable thr) {
			thr.printStackTrace();
			LOGGER.error("Unable to get rootBy via reflection!", thr);
		}
    	
    	return rootBy;
    }
    
    /**
     * Wait until any condition happens.
     *
     * @param condition - ExpectedCondition.
     * @param timeout - timeout.
     * @return true if condition happen.
     */
	@SuppressWarnings("unchecked")
	private boolean waitUntil(ExpectedCondition<?> condition) {
		boolean result;
		
		long timeout = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);
		long RETRY_TIME = Configuration.getLong(Parameter.RETRY_INTERVAL);
		
		Timer.start(ACTION_NAME.WAIT);
		@SuppressWarnings("rawtypes")
		Wait wait = new WebDriverWait(webDriver, timeout, RETRY_TIME).ignoring(WebDriverException.class)
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
}