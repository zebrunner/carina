/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.webdriver.decorator;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.log.TestLogHelper;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;

public class ExtendedWebElement
{
	private static final Logger LOGGER = Logger.getLogger(ExtendedWebElement.class);
	
	private static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);
	
	private static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

	private static final long RETRY_TIME = Configuration.getLong(Parameter.RETRY_INTERVAL);

	private static Wait<WebDriver> wait;

	private long timer;

	private TestLogHelper summary;

	private CryptoTool cryptoTool;

	private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

	
	
	private WebElement element;
	private String name;
	private By by;

	public ExtendedWebElement(WebElement element)
	{
		this.element = element;
		summary = new TestLogHelper(getDriver());
		try
		{
			cryptoTool = new CryptoTool();
		}
		catch (Exception e)
		{
			throw new RuntimeException("CryptoTool not initialized, check arg 'crypto_key_path'!");
		}
	}

	public ExtendedWebElement(WebElement element, String name)
	{
		this(element);
		this.name = name;
	}
	
	public ExtendedWebElement(WebElement element, String name, By by)
	{
		this(element, name);
		this.by = by;
	}
	
	public WebElement getElement()
	{
		return element;
	}

	public void setElement(WebElement element)
	{
		this.element = element;
	}

	public String getName()
	{
		return name !=null ? name : String.format(" (%s)", by);
	}
	
	public String getNameWithLocator()
	{
		return by != null ? name + String.format(" (%s)", by) : name + " (n/a)";
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getText()
	{
		return element != null ? element.getText() : null;
	}

	public String getAttribute(String attributeName)
	{
		return element != null ? element.getAttribute(attributeName) : null;
	}
	
	public By getBy()
	{
		return by;
	}

	public void setBy(By by)
	{
		this.by = by;
	}

	@Override
	public String toString()
	{
		return name;
	}
	
	
/*	public boolean isPresent() {
		WebDriver drv = DriverPool.getDriverByThread(); 
		
		if (!isPredicate) {
			return !drv.findElements(by).isEmpty() && drv.findElement(by).isDisplayed();
		} else {
			if (drv instanceof IOSDriver) {
				return !((IOSDriver) drv).findElementsByIosUIAutomation(getLocator(by)).isEmpty();	
			} else if (drv instanceof AndroidDriver) {
				return !((AndroidDriver) drv).findElementsByAndroidUIAutomator(getLocator(by)).isEmpty();
			} else {
				throw new RuntimeException("Unable to to detect valid driver for searching " + by.toString());
			}			
		}
	}*/

	/**
	 * Clicks on element.
	 *
     */
	public void click()
	{
		click(EXPLICIT_TIMEOUT);
	}
	
	/**
	 * Clicks on element.
	 *
     */
	public void click(long timeout)
	{
		clickSafe(timeout, true);
		String msg = Messager.ELEMENT_CLICKED.info(getName());
		summary.log(msg);
		try
		{
			TestLogCollector.addScreenshotComment(Screenshot.capture(getDriver()), msg);
		}
		catch (Exception e)
		{
			LOGGER.info(e.getMessage());
		}
	}	
	

	private WebElement find() {
		//TODO: Need to implement!
		throw new RuntimeException("Need to implement!");
	}
	
	/**
	 * Check that element present.
	 * 
	 * @param extWebElement ExtendedWebElement
	 * @return element existence status.
	 */
	public boolean isElementPresent()
	{
		return isElementPresent(EXPLICIT_TIMEOUT);
	}
	
	/**
	 * Check that element present within specified timeout.
	 * 
	 * @param timeout
	 *            - timeout.
	 * @return element existence status.
	 */
	public boolean isElementPresent(long timeout)
	{
		boolean result;
		final WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver drv)
				{
					return element.isDisplayed();
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
		}
		drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return result;
	}
	
/*	*//**
	 * Check that element not present within specified timeout.
	 * 
	 * @param timeout
	 *            - timeout.
	 * @return element existence status.
	 *//*
	public boolean isElementNotPresent(long timeout)
	{
		//TODO: test carefully especially result = true/false lines!!!
		boolean result;
		final WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver drv)
				{
					return !element.isDisplayed();
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
		}
		drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return result;
	}*/
	
	/**
	 * Check that element with text present.
	 * 
	 * @param text
	 *            of element to check.
	 * @return element with text existence status.
	 */
	public boolean isElementWithTextPresent(final String text)
	{
		return isElementWithTextPresent(text, EXPLICIT_TIMEOUT);
	}

	public boolean isElementWithTextPresent(final String text, long timeout)
	{
		boolean result;
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		wait = new WebDriverWait(getDriver(), timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					try
					{
						return element.isDisplayed() && element.getText().contains(decryptedText);
					}
					catch (Exception e)
					{
						return false;
					}
				}
			});
			result = true;
			summary.log(Messager.ELEMENT_WITH_TEXT_PRESENT.info(getName(), text));
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.error(getNameWithLocator(), text));
		}
		return result;
	}
	
	public boolean clickIfPresent()
	{
		return clickIfPresent(EXPLICIT_TIMEOUT);
	}
	
	public boolean clickIfPresent(long timeout)
	{
		boolean result;
		WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver drv)
				{
					return element.isDisplayed();
				}
			});
			element.click();
			String msg = Messager.ELEMENT_CLICKED.info(getName());
			summary.log(msg);
			result = true;
		}
		catch (Exception e)
		{
			result = false;
		}
		drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return result;
	}	
	
	
	/**
	 * Types text to specified element.
	 * 
	 * @param text
	 *            to type.
	 */
	public void type(String text)
	{
		String msg;
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver drv)
				{
					return element.isDisplayed();
				}
			});
			scrollTo();
			element.clear();
			element.sendKeys(decryptedText);
			msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, getName());
			summary.log(msg);
		}
		catch (Exception e)
		{
			msg = Messager.KEYS_NOT_SEND_TO_ELEMENT.error(text, getNameWithLocator());
			summary.log(msg);			
			
			throw new RuntimeException(msg, e); 			
		}
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
	}

	

	//TODO: format methos are untested at all yet!
	public ExtendedWebElement format(Object...objects) {
		return format(IMPLICIT_TIMEOUT, objects);
	}
	public ExtendedWebElement format(long timeout, Object...objects) {
		String locator = getBy().toString();
		By by = null;
		if (locator.startsWith("By.id: "))
		{
			by =  By.id(String.format(StringUtils.remove(locator, "By.id: "), objects));
		}
		if (locator.startsWith("By.name: "))
		{
			by =  By.name(String.format(StringUtils.remove(locator, "By.name: "), objects));
		}
		if (locator.startsWith("By.xpath: "))
		{
			by =  By.xpath(String.format(StringUtils.remove(locator, "By.xpath: "), objects));
		}
		if (locator.startsWith("linkText: "))
		{
			by =  By.linkText(String.format(StringUtils.remove(locator, "linkText: "), objects));
		}
		if (locator.startsWith("css: "))
		{
			by =  By.cssSelector(String.format(StringUtils.remove(locator, "css: "), objects));
		}
		if (locator.startsWith("tagName: "))
		{
			by =  By.tagName(String.format(StringUtils.remove(locator, "tagName: "), objects));
		}
		
		return new ExtendedWebElement(null, getName(), by);
		
	}

	
	/**
	 * Safe click on element, used to reduce any problems with that action.
	 * 
	 * @param startTimer Start time
	 */
	private void clickSafe(long timeout, boolean startTimer)
	{
		boolean clicked = false;
		Exception reason = null;
		if (startTimer)
		{
			timer = System.currentTimeMillis();
		}
		try
		{
			Thread.sleep(RETRY_TIME);
			if (element == null) {
				element = find();
			}
			element.click();
			clicked = true;
		}
		catch (UnhandledAlertException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			getDriver().switchTo().alert().accept();
		}
		catch(StaleElementReferenceException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			element = find();
		}
		catch (Exception e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			scrollTo();
			reason = e;
		}
		
		if (!clicked) {
			//repeat again until timeout achieved
			if (System.currentTimeMillis() - timer < timeout * 1000)
			{
				clickSafe(timeout, false);
			}
			else
			{
				String msg = Messager.ELEMENT_NOT_CLICKED.error(getNameWithLocator());
				summary.log(msg);			
				throw new RuntimeException(msg, reason); 			
			}
		}		
	}

	private void scrollTo()
	{
		if (Configuration.get(Parameter.BROWSER).toLowerCase().contains("mobile")) {
			LOGGER.debug("scrollTo javascript is unsupported for mobile devices!");
			return;
		}
		try
		{
			Locatable locatableElement = (Locatable) getElement();
			//[VD] onScreen should be updated onto onPage as only 2nd one returns real coordinates without scrolling... read below material for details
			//https://groups.google.com/d/msg/selenium-developers/nJR5VnL-3Qs/uqUkXFw4FSwJ
			
			int y = locatableElement.getCoordinates().onScreen().getY();
			if (y > 120) {
				((JavascriptExecutor) getDriver()).executeScript("window.scrollBy(0," + (y - 120) + ");");
			}
		}
		catch (Exception e)
		{
			// TODO: calm error logging as it is too noisy
			LOGGER.debug("Scroll to element: " + getName() + " not performed!" + e.getMessage());
		}
	}
	
	private WebDriver getDriver() {
		return DriverPool.getDriverByThread();
	}

}
