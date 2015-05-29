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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hamcrest.BaseMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

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
	
	/**
	 * Double Clicks on element.
	 *
     */
	
	public void doubleClick() {
		doubleClickSafe(true);
		String msg = Messager.ELEMENT_DOUBLE_CLICKED.info(getName());
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
	
	/**
	 * Safe doubleClick on element, used to reduce any problems with that action.
	 * 
	 * @param startTimer Start time
	 */
	private void doubleClickSafe(boolean startTimer)
	{
		WebDriver drv = getDriver();
		Actions action = new Actions(drv);

		if (startTimer)
		{
			timer = System.currentTimeMillis();
		}
		try
		{
			Thread.sleep(RETRY_TIME);
			action.moveToElement(getElement()).doubleClick(getElement()).build().perform();
		}
		catch (UnhandledAlertException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			drv.switchTo().alert().accept();
		}
		catch(StaleElementReferenceException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			if (!drv.findElements(by).isEmpty()) {
				element = drv.findElement(by);
				LOGGER.debug("Element was idenfified using By: " + by.toString());
			} else {
				throw new RuntimeException("Unable to identify element using By: " + by.toString());
			}
		}
		catch (Exception e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			if (e.getMessage().contains("Element is not clickable"))
			{
				scrollTo();
			}

			if (System.currentTimeMillis() - timer < EXPLICIT_TIMEOUT * 1000)
			{
				doubleClickSafe(false);
			}
			else
			{
				String msg = Messager.ELEMENT_NOT_DOUBLE_CLICKED.error(getNameWithLocator());
				summary.log(msg);			
				throw new RuntimeException(msg, e); 			
			}
		}
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
	
	/**
	 * Check that element not present within specified timeout.
	 * 
	 * @param timeout
	 *            - timeout.
	 * @return element existence status.
	 */
	public boolean isElementNotPresent(long timeout) {
		return !isElementPresent(timeout);
		
/*		boolean result;
		final WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return element.isDisplayed();
				}
			});
			result = false;
			summary.log(Messager.UNEXPECTED_ELEMENT_PRESENT.error(getNameWithLocator()));
		}
		catch (Exception e)
		{
			result = true;
		}
		drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return result;*/
	}

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

/*	public ExtendedWebElement format(Object...objects) {
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
		
		return new ExtendedWebElement(findWebElement(by, getName(), timeout), getName(), by);
		
	}

	
	private WebElement findWebElement(final By by, String name, long timeout) {
		WebElement element;
		final WebDriver drv = getDriver();
		setImplicitTimeout(0);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return !drv.findElements(by).isEmpty();
				}
			});
			element = drv.findElement(by);
			summary.log(Messager.ELEMENT_FOUND.info(name));
		}
		catch (Exception e)
		{
			element = null;
			summary.log(Messager.ELEMENT_NOT_FOUND.error(name));
			setImplicitTimeout(IMPLICIT_TIMEOUT);
			throw new RuntimeException(e);
		}
		setImplicitTimeout(IMPLICIT_TIMEOUT);
		return element;
	}	*/
	
	public void setImplicitTimeout(long implicit_wait){
		getDriver().manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
	}

	
	/**
	 * Safe click on element, used to reduce any problems with that action.
	 * 
	 * @param startTimer Start time
	 */
	private void clickSafe(long timeout, boolean startTimer)
	{
		WebDriver drv = getDriver();
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
				LOGGER.debug("Click operation is executed against nul WebElement object. Trying to find element...");
				if (!drv.findElements(by).isEmpty()) {
					element = drv.findElement(by);
					LOGGER.debug("Element was idenfified using By: " + by.toString());
				} else {
					throw new RuntimeException("Unable to identify element using By: " + by.toString());
				}
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
			if (!drv.findElements(by).isEmpty()) {
				element = drv.findElement(by);
				LOGGER.debug("Element was idenfified using By: " + by.toString());
			} else {
				throw new RuntimeException("Unable to identify element using By: " + by.toString());
			}
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

	public void scrollTo()
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
	
	/**
	 * Inputs file path to specified element.
	 *
	 * @param filePath path
	 */
	public void attachFile(String filePath)
	{
		String msg;
		final String decryptedFilePath = cryptoTool.decryptByPattern(filePath, CRYPTO_PATTERN);
		WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return getElement().isDisplayed();
				}
			});
			getElement().sendKeys(decryptedFilePath);
			msg = Messager.FILE_ATTACHED.info(filePath);
			summary.log(msg);
		}
		catch (Exception e)
		{
			msg = Messager.FILE_NOT_ATTACHED.error(filePath);
			summary.log(msg);
			throw new RuntimeException(msg, e); 			
		}
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
	}
	
	/**
	 * Check checkbox
	 * 
	 * @param checkbox Element
	 */
	public void check()
	{
		if (isElementPresent() && !getElement().isSelected())
		{
			click();
			String msg = Messager.CHECKBOX_CHECKED.info(getName());
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(getDriver()), msg);
		}
	}

	/**
	 * Uncheck checkbox
	 * 
	 * @param checkbox Element
	 */
	public void uncheck()
	{
		if (isElementPresent() && getElement().isSelected())
		{
			click();
			String msg = Messager.CHECKBOX_UNCHECKED.info(getName());
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(getDriver()), msg);
		}
	}
	
	/**
	 * Get checkbox state.
	 * 
	 * @return - current state
	 */
	public boolean isChecked()
	{
		assertElementPresent();
		boolean res = getElement().isSelected();
		if (getElement().getAttribute("checked") != null ) {
			res |= getElement().getAttribute("checked").equalsIgnoreCase("true");
		}
		return res;
	}

	
	/**
	 * Get selected elements from one-value select.
	 * 
	 * @return selected value
	 */
	public String getSelectedValue()
	{
		assertElementPresent();
		return new Select(getElement()).getAllSelectedOptions().get(0).getText();
	}

	/**
	 * Get selected elements from multi-value select.
	 * 
	 * @return selected values
	 */
	public List<String> getSelectedValues()
	{
		assertElementPresent();
		Select s = new Select(getElement());
		List<String> values = new ArrayList<String>();
		for (WebElement we : s.getAllSelectedOptions())
		{
			values.add(we.getText());
		}
		return values;
	}
	
	private WebDriver getDriver() {
		return DriverPool.getDriverByThread();
	}
	
/*	*//**
	 * Hovers over element.
	 *
     *//*
	public void hover() {
		hover(null, null);
	}
	

	
	public void hover(Integer xOffset, Integer  yOffset)
	{
		WebDriver drv = getDriver();
		if (isElementPresent())
		{
			
			if (!drv.toString().contains("safari")) {
				Actions action = new Actions(drv);
				if (xOffset != null && yOffset != null) {
					action.moveToElement(getElement(), xOffset, yOffset);
				}
				else {
					action.moveToElement(getElement());
				}

				action.perform();				
			}
			else {
				//https://code.google.com/p/selenium/issues/detail?id=4136
				JavascriptExecutor js = (JavascriptExecutor) drv;
				String locatorType = getBy().toString().substring(3);
				String elem = "var elem = document;";
				if (locatorType.startsWith("id")) {
					elem = "var elem = document.getElementById(\""+locatorType.substring(4)+"\");";
				}
				else if (locatorType.startsWith("xpath")) {
					String snippet = "document.getElementByXPath = function(sValue) { var a = this.evaluate(sValue, this, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null); if (a.snapshotLength > 0) { return a.snapshotItem(0); } }; ";
					js.executeScript(snippet);
					elem = "var elem = document.getElementByXPath(\""+locatorType.substring(7)+"\");";
				}
				else if (locatorType.startsWith("className")) {
					elem = "var elem = document.getElementsByClassName(\""+locatorType.substring(14)+"\")[0];";
				}
				String mouseOverScript = elem + " if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false);" +
						" elem.dispatchEvent(evObj);} else if(document.createEventObject) { elem.fireEvent('onmouseover');}";
				js.executeScript(mouseOverScript);
			}

			String msg = Messager.HOVER_IMG.info(getName());
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
		}
		else
		{
			summary.log(Messager.ELEMENT_NOT_HOVERED.error(getNameWithLocator()));
		}
	}*/
	
	/**
	 * Selects text in specified select element.
	 *
	 * @param selectText select text
	 * @return true if item selected, otherwise false.
	 */
	public boolean select(final String selectText)
	{
		boolean isSelected = false;
		final String decryptedSelectText = cryptoTool.decryptByPattern(selectText, CRYPTO_PATTERN);
		final Select s = new Select(getElement());
		WebDriver drv = getDriver();
		String msg = null;
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{

					try
					{
						s.selectByVisibleText(decryptedSelectText);
						return true;
					}
					catch (Exception e)
					{
					}
					return false;
				}
			});
			isSelected = true;
			msg = Messager.SELECT_BY_TEXT_PERFORMED.info(selectText, getName());
		}
		catch (Exception e)
		{
			msg = Messager.SELECT_BY_TEXT_NOT_PERFORMED.error(selectText, getNameWithLocator());
			e.printStackTrace();
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);

		return isSelected;
	}
	
	/**
	 * Select multiple text values in specified select element.
	 */
	public boolean select(final String[] values)
	{
		boolean result = true;
		for (String value : values)
		{
			if (!select(value))
			{
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * Selects value according to text value matcher.
	 *
	 * @param matcher {@link} BaseMatcher
	 * @return true if item selected, otherwise false.
	 */
	public boolean selectByMatcher(final BaseMatcher<String> matcher)
	{
		boolean isSelected = false;
		final Select s = new Select(getElement());
		WebDriver drv = getDriver();
		String msg = null;
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					try
					{
						String fullTextValue = null;
						for (WebElement option : s.getOptions())
						{
							if (matcher.matches(option.getText()))
							{
								fullTextValue = option.getText();
								break;
							}
						}
						s.selectByVisibleText(fullTextValue);
						return true;
					}
					catch (Exception e)
					{
					}
					return false;
				}
			});
			isSelected = true;
			msg = Messager.SELECT_BY_MATCHER_TEXT_PERFORMED.info(matcher.toString(), getName());
		}
		catch (Exception e)
		{
			msg = Messager.SELECT_BY_MATCHER_TEXT_NOT_PERFORMED.error(matcher.toString(), getNameWithLocator());
			e.printStackTrace();
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);

		return isSelected;
	}
	
	/**
	 * Selects item by index in specified select element.
	 * 
	 * @param index
	 * @return true if item selected, otherwise false.
	 */
	public boolean select(final int index)
	{
		boolean isSelected = false;
		final Select s = new Select(getElement());
		WebDriver drv = getDriver();
		String msg = null;
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					try
					{
						s.selectByIndex(index);
						return true;
					}
					catch (Exception e)
					{
					}
					return false;
				}
			});
			isSelected = true;
			msg = Messager.SELECT_BY_INDEX_PERFORMED.info(String.valueOf(index), getName());
		}
		catch (Exception e)
		{
			msg = Messager.SELECT_BY_INDEX_NOT_PERFORMED.error(String.valueOf(index), getNameWithLocator());
			e.printStackTrace();
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);

		return isSelected;
	}

	// --------------------------------------------------------------------------
	// Base UI validations
	// --------------------------------------------------------------------------
	public void assertElementPresent()
	{
		assertElementPresent(EXPLICIT_TIMEOUT);
	}

	public void assertElementPresent(long timeout)
	{
		if (isElementPresent(timeout))
		{
			TestLogCollector
					.addScreenshotComment(Screenshot.capture(getDriver()), Messager.ELEMENT_PRESENT.getMessage(getName()));
		}
		else
		{
			Assert.fail(Messager.ELEMENT_NOT_PRESENT.getMessage(getNameWithLocator()));
		}
	}

	public void assertElementWithTextPresent(final String text)
	{
		assertElementWithTextPresent(text, EXPLICIT_TIMEOUT);
	}

	public void assertElementWithTextPresent(final String text, long timeout)
	{
		if (isElementWithTextPresent(text, timeout))
		{
			TestLogCollector.addScreenshotComment(Screenshot.capture(getDriver()),
					Messager.ELEMENT_WITH_TEXT_PRESENT.getMessage(getName(), text));
		}
		else
		{
			Assert.fail(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.getMessage(getNameWithLocator(), text));
		}
	}
}
