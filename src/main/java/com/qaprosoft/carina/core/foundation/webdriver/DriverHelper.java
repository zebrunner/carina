/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
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
package com.qaprosoft.carina.core.foundation.webdriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hamcrest.BaseMatcher;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.log.TestLogHelper;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.DriverMode;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.LogicUtils;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.gui.AbstractPage;

/**
 * DriverHelper - WebDriver wrapper for logging and reporting features. Also it
 * contains some complex operations with UI.
 * 
 * @author Alex Khursevich
 */
public class DriverHelper
{
	protected static final Logger LOGGER = Logger.getLogger(DriverHelper.class);

	protected static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);
	
	protected static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

	protected static final long RETRY_TIME = Configuration.getLong(Parameter.RETRY_TIMEOUT);

	protected static Wait<WebDriver> wait;

	protected long timer;

	protected TestLogHelper summary;

	protected WebDriver driver;
	
	protected CryptoTool cryptoTool;

	protected static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);
	
	public DriverHelper()
	{
		try
		{
			cryptoTool = new CryptoTool();
		}
		catch (Exception e)
		{
			throw new RuntimeException("CryptoTool not initialized, check arg 'crypto_key_path'!");
		}
		summary = new TestLogHelper(UUID.randomUUID().toString());
	}

	public DriverHelper(WebDriver driver)
	{
		this();
		this.driver = driver;
		
		if (driver == null)
		{
			throw new RuntimeException("WebDriver not initialized, check log files for details!");
		}
		driver.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		initSummary(driver);
	}
	

	// --------------------------------------------------------------------------
	// Base UI interaction operations
	// --------------------------------------------------------------------------

	public void setImplicitTimeout(long implicit_wait){
		getDriver().manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
	}
	
	public long getImplicitTimeout(){
		return IMPLICIT_TIMEOUT;
	}	
	/**
	 * Initializes test log container dedicated to WebDriver instance.
	 * 
	 * @param driver
	 */
	protected void initSummary(WebDriver driver)
	{
		summary = new TestLogHelper(driver);
	}

	protected void initSummary(String sessionId)
	{
		summary = new TestLogHelper(sessionId);
	}


	/**
	 * Check that element present within specified timeout.
	 * 
	 * @param element
	 *            to find.
	 * @param timeout
	 *            - timeout.
	 * @return element existence status.
	 */
	public boolean isElementPresent(final ExtendedWebElement extWebElement, long timeout)
	{
		if (extWebElement == null)
			return false;
		
		return isElementPresent(extWebElement.getName(), extWebElement.getBy(), timeout);
	}
	/**
	 * Check that element present.
	 * 
	 * @param element
	 *            to find.
	 * @return element existence status.
	 */
	public boolean isElementPresent(final ExtendedWebElement extWebElement)
	{
		return isElementPresent(extWebElement, EXPLICIT_TIMEOUT);
	}

	public boolean isElementPresent(String controlInfo, final WebElement element)
	{
		return isElementPresent(new ExtendedWebElement(element, controlInfo));
	}

	
	/**
	 * Check that element present on page using By.
	 * 
	 * @param by
	 * @return element non-existence status.
	 */
	public boolean isElementPresent(String elementName, final By by, long timeout)
	{
		boolean result;
		final WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return !drv.findElements(by).isEmpty() && drv.findElement(by).isDisplayed();
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

	public boolean isElementPresent(String elementName, final By by)
	{
		return isElementPresent(elementName, by, EXPLICIT_TIMEOUT);
	}	

	public boolean isElementPresent(String controlInfo, final WebElement element, long timeout)
	{
		return isElementPresent(new ExtendedWebElement(element, controlInfo), timeout);
	}

	/**
	 * Check that element with text present.
	 * 
	 * @param element
	 *            to find.
	 * @param text
	 *            of element to check.
	 * @return element with text existence status.
	 */
	public boolean isElementWithTextPresent(final ExtendedWebElement extWebElement, final String text)
	{
		return isElementWithTextPresent(extWebElement, text, EXPLICIT_TIMEOUT);
	}

	public boolean isElementWithTextPresent(final ExtendedWebElement extWebElement, final String text, long timeout)
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
						return extWebElement.getElement().isDisplayed() && extWebElement.getElement().getText().contains(decryptedText);
					}
					catch (Exception e)
					{
						return false;
					}
				}
			});
			result = true;
			summary.log(Messager.ELEMENT_WITH_TEXT_PRESENT.info(extWebElement.getName(), text));
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.error(extWebElement.getNameWithLocator(), text));
		}
		return result;
	}

	/**
	 * Check that element not present on page.
	 * 
	 * @param element
	 * @return element non-existence status.
	 */
	public boolean isElementNotPresent(final ExtendedWebElement extWebElement) {
		return isElementNotPresent(extWebElement.getName(), extWebElement.getBy(), EXPLICIT_TIMEOUT);
	}
	public boolean isElementNotPresent(final ExtendedWebElement extWebElement, long timeout)
	{
		return isElementNotPresent(extWebElement.getName(), extWebElement.getBy(), timeout);
	}

	public boolean isElementNotPresent(String controlInfo, final WebElement element)
	{
		return isElementNotPresent(new ExtendedWebElement(element, controlInfo));
	}

	/**
	 * Check that element not present on page.
	 * 
	 * @param by
	 * @return element non-existence status.
	 */
	public boolean isElementNotPresent(String elementName, final By by) {
		return isElementNotPresent(elementName, by, EXPLICIT_TIMEOUT);
	}
	
	public boolean isElementNotPresent(String elementName, final By by, long timeout)
	{
		boolean result;
		final WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return drv.findElements(by).isEmpty();
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.UNEXPECTED_ELEMENT_PRESENT.error(elementName));
		}
		drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return result;
	}

	/**
	 * Types text to specified element.
	 * 
	 * @param element
	 *            in which the text should be typed.
	 * @param text
	 *            to type.
	 */
	public void type(final ExtendedWebElement extWebElement, String text)
	{
		String msg;
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return extWebElement.getElement().isDisplayed();
				}
			});
			scrollTo(extWebElement);
			extWebElement.getElement().clear();
			extWebElement.getElement().sendKeys(decryptedText);
			msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, extWebElement.getName());
			summary.log(msg);
		}
		catch (Exception e)
		{
			msg = Messager.KEYS_NOT_SEND_TO_ELEMENT.error(text, extWebElement.getNameWithLocator());
			summary.log(msg);			
			
			throw new RuntimeException(msg, e); 			
		}
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
	}

	public void type(String controlInfo, WebElement control, String text)
	{
		type(new ExtendedWebElement(control, controlInfo), text);
	}

	/**
	 * Clicks on element.
	 * 
	 * @param element
	 *            to click.
	 */
	public void click(final ExtendedWebElement extendedWebElement)
	{
		//isElementPresent(extendedWebElement, EXPLICIT_TIMEOUT); //just wait and try to link anyway
		click(extendedWebElement, EXPLICIT_TIMEOUT);
	}
	
	/**
	 * Clicks on element.
	 * 
	 * @param element
	 *            to click.
	 */
	public void click(final ExtendedWebElement extendedWebElement, long timeout)
	{
		//isElementPresent(extendedWebElement, timeout);
		
		clickSafe(extendedWebElement, timeout, true);
		String msg = Messager.ELEMENT_CLICKED.info(extendedWebElement.getName());
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

	public void click(String controlInfo, WebElement control)
	{
		click(new ExtendedWebElement(control, controlInfo));
	}
	
	public boolean clickIfPresent(final ExtendedWebElement extWebElement)
	{
		return clickIfPresent(extWebElement, EXPLICIT_TIMEOUT);
	}	

	public boolean clickIfPresent(final ExtendedWebElement extWebElement, long timeout)
	{
		boolean result;
		WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					if (extWebElement.getElement().isDisplayed()){
						String msg = Messager.ELEMENT_CLICKED.info(extWebElement.getName());
						summary.log(msg);
						extWebElement.getElement().click();
					}
					return true;
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
	 * Safe click on element, used to reduce any problems with that action.
	 * 
	 * @param elementName
	 * @param element
	 * @param startTimer
	 */
	private void clickSafe(ExtendedWebElement extendedWebElement, long timeout, boolean startTimer)
	{
		if (startTimer)
		{
			timer = System.currentTimeMillis();
/*			if (!isElementPresent(extendedWebElement, timeout/5)) {
				throw new RuntimeException("An element could not be located on the page using the given search parameters. " + extendedWebElement.getNameWithLocator());	
			}*/
		}
		try
		{
			Thread.sleep(RETRY_TIME);
			if (extendedWebElement.getElement() == null) {
				extendedWebElement = findExtendedWebElement(extendedWebElement.getBy());
			}
			extendedWebElement.getElement().click();
		}
		catch (UnhandledAlertException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			getDriver().switchTo().alert().accept();
		}
		catch(StaleElementReferenceException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			extendedWebElement = findExtendedWebElement(extendedWebElement.getBy());
		}
		catch (Exception e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			scrollTo(extendedWebElement);
			//repeat again until timeout achieved
			if (System.currentTimeMillis() - timer < timeout * 1000)
			{
				clickSafe(extendedWebElement, timeout, false);
			}
			else
			{
				String msg = Messager.ELEMENT_NOT_CLICKED.error(extendedWebElement.getNameWithLocator());
				summary.log(msg);			
				throw new RuntimeException(msg, e); 			
			}
		}
	}
	
	
	/**
	 * Double Clicks on element.
	 * 
	 * @param element
	 *            to click.
	 */
	
	public void doubleClick(final ExtendedWebElement extendedWebElement) {
		//isElementPresent(extendedWebElement);
		doubleClickSafe(extendedWebElement, true);
		String msg = Messager.ELEMENT_DOUBLE_CLICKED.info(extendedWebElement.getName());
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

	public void doubleClick(String controlInfo, WebElement control)
	{
		doubleClick(new ExtendedWebElement(control, controlInfo));
	}
	
	/**
	 * Safe doubleClick on element, used to reduce any problems with that action.
	 * 
	 * @param elementName
	 * @param element
	 * @param startTimer
	 */
	private void doubleClickSafe(ExtendedWebElement extendedWebElement, boolean startTimer)
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
			if (extendedWebElement.getElement() == null) {
				extendedWebElement = findExtendedWebElement(extendedWebElement.getBy());
			}
			action.moveToElement(extendedWebElement.getElement()).doubleClick(extendedWebElement.getElement()).build().perform();
		}
		catch (UnhandledAlertException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			drv.switchTo().alert().accept();
		}
		catch(StaleElementReferenceException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			extendedWebElement = findExtendedWebElement(extendedWebElement.getBy());
		}
		catch (Exception e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			if (e.getMessage().contains("Element is not clickable"))
			{
				scrollTo(extendedWebElement);
			}

			if (System.currentTimeMillis() - timer < EXPLICIT_TIMEOUT * 1000)
			{
				doubleClickSafe(extendedWebElement, false);
			}
			else
			{
				String msg = Messager.ELEMENT_NOT_DOUBLE_CLICKED.error(extendedWebElement.getNameWithLocator());
				summary.log(msg);			
				throw new RuntimeException(msg, e); 			
			}
		}
	}	

	/**
	 * Sends enter to element.
	 * 
	 * @param extendedWebElement
	 *            to send enter.
	 */
	public void pressEnter(final ExtendedWebElement extendedWebElement)
	{
		isElementPresent(extendedWebElement);
		pressEnterSafe(extendedWebElement, true);
		String msg = Messager.ELEMENT_CLICKED.info(extendedWebElement.getName());
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(getDriver()), msg);
	}

	/**
	 * Safe enter sending to specified element.
	 * 
	 * @param controlInfo
	 * @param control
	 */	
	public void pressEnter(String controlInfo, WebElement control)
	{
		pressEnter(new ExtendedWebElement(control, controlInfo));
	}


	private void pressEnterSafe(ExtendedWebElement extendedWebElement, boolean startTimer)
	{

		if (startTimer)
		{
			timer = System.currentTimeMillis();
		}
		try
		{
			Thread.sleep(RETRY_TIME);
			if (extendedWebElement.getElement() == null) {
				extendedWebElement = findExtendedWebElement(extendedWebElement.getBy());
			}
			extendedWebElement.getElement().sendKeys(Keys.ENTER);
		}
		catch (UnhandledAlertException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			getDriver().switchTo().alert().accept();
		}
		catch(StaleElementReferenceException e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			extendedWebElement = findExtendedWebElement(extendedWebElement.getBy());
		}		
		catch (Exception e)
		{
			LOGGER.debug(e.getMessage(), e.getCause());
			if (System.currentTimeMillis() - timer < EXPLICIT_TIMEOUT * 1000)
			{
				pressEnterSafe(extendedWebElement, false);
			}
			else
			{
				String msg = Messager.ELEMENT_NOT_CLICKED.error(extendedWebElement.getNameWithLocator());
				summary.log(msg);			
				throw new RuntimeException(msg, e); 			
			}
		}
	}

	/**
	 * Check checkbox
	 * 
	 * @param checkbox
	 */
	public void check(ExtendedWebElement checkbox)
	{
		if (isElementPresent(checkbox) && !checkbox.getElement().isSelected())
		{
			click(checkbox);
			logMakingScreen(Messager.CHECKBOX_CHECKED.info(checkbox.getName()));
		}
	}

	/**
	 * Uncheck checkbox
	 * 
	 * @param checkbox
	 */
	public void uncheck(ExtendedWebElement checkbox)
	{
		if (isElementPresent(checkbox) && checkbox.getElement().isSelected())
		{
			click(checkbox);
			logMakingScreen(Messager.CHECKBOX_UNCHECKED.info(checkbox.getName()));
		}
	}

	/**
	 * Get checkbox state.
	 * 
	 * @param checkbox
	 *            - checkbox to test
	 * @return - current state
	 */
	public boolean isChecked(final ExtendedWebElement checkbox)
	{
		assertElementPresent(checkbox);
		boolean res = checkbox.getElement().isSelected();
		if (checkbox.getElement().getAttribute("checked") != null ) {
			res |= checkbox.getElement().getAttribute("checked").equalsIgnoreCase("true");
		}
		return res;
	}

	
	/**
	 * Inputs file path to specified element.
	 * 
	 * @param element
	 * @param filePath
	 */
	public void attachFile(final ExtendedWebElement extendedWebElement, String filePath)
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
					return extendedWebElement.getElement().isDisplayed();
				}
			});
			extendedWebElement.getElement().sendKeys(decryptedFilePath);
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
	 * Opens full or relative URL.
	 * 
	 * @param url
	 *            to open.
	 */
	public void openURL(String url)
	{
		String decryptedURL = cryptoTool.decryptByPattern(url, CRYPTO_PATTERN);
		decryptedURL = decryptedURL.contains("http:") || decryptedURL.contains("https:") ? decryptedURL : Configuration.get(Parameter.URL)
				+ decryptedURL;
		WebDriver drv = getDriver();
		try
		{
			drv.get(decryptedURL);
		}
		catch (UnhandledAlertException e)
		{
			drv.switchTo().alert().accept();
		}
		//AUTO-250 tweak core to start browser in maximized mode - to prevent stability issues
		try
		{			
			drv.manage().window().maximize();				
		}
		catch (Exception e)
		{
			summary.log(e.getMessage());
		    //e.printStackTrace();			
		}		
		
		
		String msg = Messager.OPEN_URL.info(url);
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
	}

	/**
	 * Checks that current URL is as expected.
	 * 
	 * @param expectedURL
	 * @return validation result.
	 */
	public boolean isUrlAsExpected(String expectedURL)
	{
		String decryptedURL = cryptoTool.decryptByPattern(expectedURL, CRYPTO_PATTERN);
		decryptedURL = decryptedURL.startsWith("http") ? decryptedURL : Configuration.get(Parameter.URL) + decryptedURL;
		WebDriver drv = getDriver();
		if (LogicUtils.isURLEqual(decryptedURL, drv.getCurrentUrl()))
		{
			summary.log(Messager.EXPECTED_URL.info(drv.getCurrentUrl()));
			return true;
		}
		else
		{
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

	public void pause(long timeout)
	{
		try
		{
			Thread.sleep(timeout * 1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}	
	
	public void pause(Double timeout)
	{
		try
		{
			timeout = timeout * 1000;
			long miliSec = timeout.longValue();
			Thread.sleep(miliSec);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}	

	/**
	 * Checks that page title is as expected.
	 * 
	 * @param expectedTitle
	 * @return validation result.
	 */
	public boolean isTitleAsExpected(final String expectedTitle)
	{
		boolean result;
		final String decryptedExpectedTitle = cryptoTool.decryptByPattern(expectedTitle, CRYPTO_PATTERN);
		final WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return drv.getTitle().contains(decryptedExpectedTitle);
				}
			});
			result = true;
			summary.log(Messager.TITLE_CORERECT.info(drv.getCurrentUrl(), expectedTitle));
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.TITLE_NOT_CORERECT.error(drv.getCurrentUrl(), expectedTitle, drv.getTitle()));
		}
		return result;
	}

	/**
	 * Checks that page suites to expected pattern.
	 * 
	 * @param title
	 * @param expectedPattern
	 * @return validation result.
	 */
	public boolean isTitleAsExpectedPattern(String expectedPattern)
	{
		boolean result;
		final String decryptedExpectedPattern = cryptoTool.decryptByPattern(expectedPattern, CRYPTO_PATTERN);
		WebDriver drv = getDriver();
		String actual = drv.getTitle();
		Pattern p = Pattern.compile(decryptedExpectedPattern);
		Matcher m = p.matcher(actual);
		if (m.find())
		{
			summary.log(Messager.TITLE_CORERECT.info(drv.getCurrentUrl(), actual));
			result = true;
		}
		else
		{
			summary.log(Messager.TITLE_DOES_NOT_MATCH_TO_PATTERN.error(drv.getCurrentUrl(), expectedPattern, actual));
			result = false;
		}
		return result;
	}

	/**
	 * Go back in browser.
	 */
	public void navigateBack()
	{
		getDriver().navigate().back();
		summary.log(Messager.BACK.info());
	}

	/**
	 * Refresh browser.
	 */
	public void refresh()
	{
		getDriver().navigate().refresh();
		summary.log(Messager.REFRESH.info());
	}

	/**
	 * Refresh browser after timeout.
	 * 
	 * @param timeout
	 *            before refresh.
	 */
	public void refresh(long timeout)
	{
		pause(timeout);
		refresh();
	}

	/**
	 * Selects text in specified select element.
	 * 
	 * @param select
	 * @param selectText
	 * @return true if item selected, otherwise false.
	 */
	public boolean select(final ExtendedWebElement extendedWebElement, final String selectText)
	{
		boolean isSelected = false;
		final String decryptedSelectText = cryptoTool.decryptByPattern(selectText, CRYPTO_PATTERN);
		final Select s = new Select(extendedWebElement.getElement());
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
			msg = Messager.SELECT_BY_TEXT_PERFORMED.info(selectText, extendedWebElement.getName());
		}
		catch (Exception e)
		{
			msg = Messager.SELECT_BY_TEXT_NOT_PERFORMED.error(selectText, extendedWebElement.getNameWithLocator());
			e.printStackTrace();
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);

		return isSelected;
	}

	/**
	 * Select multiple text values in specified select element.
	 */
	public boolean select(final ExtendedWebElement extendedWebElement, final String[] values)
	{
		boolean result = true;
		for (String value : values)
		{
			if (!select(extendedWebElement, value))
			{
				result = false;
			}
		}
		return result;
	}

	/**
	 * Selects value according to text value matcher.
	 * 
	 * @param select
	 * @param matcher
	 * @return true if item selected, otherwise false.
	 */
	public boolean selectByMatcher(final ExtendedWebElement extendedWebElement, final BaseMatcher<String> matcher)
	{
		boolean isSelected = false;
		final Select s = new Select(extendedWebElement.getElement());
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
			msg = Messager.SELECT_BY_MATCHER_TEXT_PERFORMED.info(matcher.toString(), extendedWebElement.getName());
		}
		catch (Exception e)
		{
			msg = Messager.SELECT_BY_MATCHER_TEXT_NOT_PERFORMED.error(matcher.toString(), extendedWebElement.getNameWithLocator());
			e.printStackTrace();
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);

		return isSelected;
	}

	public void select(String controlInfo, WebElement control, String selectText)
	{
		select(new ExtendedWebElement(control, controlInfo), selectText);
	}

	/**
	 * Selects item by index in specified select element.
	 * 
	 * @param select
	 * @param selectText
	 * @return true if item selected, otherwise false.
	 */
	public boolean select(final ExtendedWebElement extendedWebElement, final int index)
	{
		boolean isSelected = false;
		final Select s = new Select(extendedWebElement.getElement());
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
			msg = Messager.SELECT_BY_INDEX_PERFORMED.info(String.valueOf(index), extendedWebElement.getName());
		}
		catch (Exception e)
		{
			msg = Messager.SELECT_BY_INDEX_NOT_PERFORMED.error(String.valueOf(index), extendedWebElement.getNameWithLocator());
			e.printStackTrace();
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);

		return isSelected;
	}

	public void select(String controlInfo, WebElement control, int index)
	{
		select(new ExtendedWebElement(control, controlInfo), index);
	}

	/**
	 * Hovers over element.
	 * 
	 * @param element
	 *            to be hovered.
	 */
	public void hover(final ExtendedWebElement extendedWebElement) {
		hover(extendedWebElement, null, null);
	}
	public void hover(final ExtendedWebElement extendedWebElement, Integer xOffset, Integer  yOffset)
	{
		WebDriver drv = getDriver();
		if (isElementPresent(extendedWebElement))
		{
			
			if (!drv.toString().contains("safari")) {
				Actions action = new Actions(drv);
				if (xOffset != null && yOffset != null) {
					action.moveToElement(extendedWebElement.getElement(), xOffset, yOffset);
				}
				else {
					action.moveToElement(extendedWebElement.getElement());
				}

				action.perform();				
			}
			else {
				//https://code.google.com/p/selenium/issues/detail?id=4136
				JavascriptExecutor js = (JavascriptExecutor) drv;
				String locatorType = extendedWebElement.getBy().toString().substring(3);
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

			String msg = Messager.HOVER_IMG.info(extendedWebElement.getName());
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
		}
		else
		{
			summary.log(Messager.ELEMENT_NOT_HOVERED.error(extendedWebElement.getNameWithLocator()));
		}
	}

	public void hover(String controlInfo, WebElement control)
	{
		hover(new ExtendedWebElement(control, controlInfo));
	}

	/**
	 * Hovers over element.
	 * 
	 * @param xpathLocator
	 * @param elementName
	 */
	public void hover(String elementName, String xpathLocator)
	{
		WebDriver drv = getDriver();
		Actions action = new Actions(drv);
		action.moveToElement(drv.findElement(By.xpath(xpathLocator))).perform();
		String msg = Messager.HOVER_IMG.info(elementName);
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
	}

	public void scrollTo(final ExtendedWebElement extendedWebElement)
	{
		try
		{
			Locatable locatableElement = (Locatable) extendedWebElement.getElement();
			//[VD] onScreen should be updated onto onPage as only 2nd one returns real coordinates without scrolling... read below material for details
			//https://groups.google.com/d/msg/selenium-developers/nJR5VnL-3Qs/uqUkXFw4FSwJ
			
			int y = locatableElement.getCoordinates().onScreen().getY();
			((JavascriptExecutor) getDriver()).executeScript("window.scrollBy(0," + (y - 120) + ");");
		}
		catch (Exception e)
		{
			// TODO: calm error logging as it is too noisy
			LOGGER.debug("Scroll to element: " + extendedWebElement.getName() + " not performed!" + e.getMessage());
		}
	}

	public void pressTab()
	{
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.TAB).perform();
	}

	public void sendKeys(String keys)
	{
		final String decryptedKeys = cryptoTool.decryptByPattern(keys, CRYPTO_PATTERN);
		Actions builder = new Actions(getDriver());
		builder.sendKeys(decryptedKeys).perform();
	}

	/**
	 * Close alert modal by JS.
	 */
	public void sielentAlert()
	{
		WebDriver drv = getDriver();
		if (!(drv instanceof HtmlUnitDriver))
		{
			((JavascriptExecutor) drv).executeScript("window.alert = function(msg) { return true; }");
			((JavascriptExecutor) drv).executeScript("window.confirm = function(msg) { return true; }");
			((JavascriptExecutor) drv).executeScript("window.prompt = function(msg) { return true; }");
		}
	}

	/**
	 * Drags and drops element to specified place.
	 * 
	 * @param from
	 *            - element to drag.
	 * @param to
	 *            - element to drop to.
	 */
	public void dragAndDrop(final ExtendedWebElement from, final ExtendedWebElement to)
	{

		if (isElementPresent(from) && isElementPresent(to))
		{
			WebDriver drv = getDriver();
			if (!drv.toString().contains("safari")) {			
				Actions builder = new Actions(drv);
				Action dragAndDrop = builder.clickAndHold(from.getElement()).moveToElement(to.getElement()).release(to.getElement()).build();
				dragAndDrop.perform();
			} else {			
				WebElement LocatorFrom = from.getElement();
				WebElement LocatorTo = to.getElement();
				String xto=Integer.toString(LocatorTo.getLocation().x);
				String yto=Integer.toString(LocatorTo.getLocation().y);
				((JavascriptExecutor)driver).executeScript("function simulate(f,c,d,e){var b,a=null;for(b in eventMatchers)if(eventMatchers[b].test(c)){a=b;break}if(!a)return!1;document.createEvent?(b=document.createEvent(a),a==\"HTMLEvents\"?b.initEvent(c,!0,!0):b.initMouseEvent(c,!0,!0,document.defaultView,0,d,e,d,e,!1,!1,!1,!1,0,null),f.dispatchEvent(b)):(a=document.createEventObject(),a.detail=0,a.screenX=d,a.screenY=e,a.clientX=d,a.clientY=e,a.ctrlKey=!1,a.altKey=!1,a.shiftKey=!1,a.metaKey=!1,a.button=1,f.fireEvent(\"on\"+c,a));return!0} var eventMatchers={HTMLEvents:/^(?:load|unload|abort|error|select|change|submit|reset|focus|blur|resize|scroll)$/,MouseEvents:/^(?:click|dblclick|mouse(?:down|up|over|move|out))$/}; " +
				"simulate(arguments[0],\"mousedown\",0,0); simulate(arguments[0],\"mousemove\",arguments[1],arguments[2]); simulate(arguments[0],\"mouseup\",arguments[1],arguments[2]); ",
				LocatorFrom,xto,yto);			
			}

			String msg = Messager.ELEMENTS_DRAGGED_AND_DROPPED.info(from.getName(), to.getName());
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
		}
		else
		{
			summary.log(Messager.ELEMENTS_NOT_DRAGGED_AND_DROPPED.error(from.getNameWithLocator(), to.getNameWithLocator()));
		}
	}

	/**
	 * Performs slider move for specified offset.
	 * 
	 * @param slider
	 * @param moveX
	 * @param moveY
	 */
	public void slide(ExtendedWebElement slider, int moveX, int moveY)
	{
		if (isElementPresent(slider))
		{
			WebDriver drv = getDriver();
			(new Actions(drv)).moveToElement(slider.getElement()).dragAndDropBy(slider.getElement(), moveX, moveY).build().perform();
			String msg = Messager.SLIDER_MOVED.info(slider.getNameWithLocator(), String.valueOf(moveX), String.valueOf(moveY));
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
		}
		else
		{
			summary.log(Messager.SLIDER_NOT_MOVED.error(slider.getNameWithLocator(), String.valueOf(moveX), String.valueOf(moveY)));
		}
	}

	/**
	 * Get selected elements from one-value select.
	 * 
	 * @param select
	 * @return selected value
	 */
	public String getSelectedValue(ExtendedWebElement select)
	{
		assertElementPresent(select);
		return new Select(select.getElement()).getAllSelectedOptions().get(0).getText();
	}

	/**
	 * Get selected elements from multi-value select.
	 * 
	 * @param select
	 * @return selected value
	 */
	public List<String> getSelectedValues(ExtendedWebElement select)
	{
		assertElementPresent(select);
		Select s = new Select(select.getElement());
		List<String> values = new ArrayList<String>();
		for (WebElement we : s.getAllSelectedOptions())
		{
			values.add(we.getText());
		}
		return values;
	}

	/**
	 * Accepts alert modal.
	 */
	public void acceptAlert()
	{
		WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return isAlertPresent();
				}
			});
			drv.switchTo().alert().accept();
			Messager.ALERT_ACCEPTED.info("");
		}
		catch (Exception e)
		{
			Messager.ALERT_NOT_ACCEPTED.error("");
		}
	}

	/**
	 * Cancels alert modal.
	 */
	public void cancelAlert()
	{
		WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return isAlertPresent();
				}
			});
			drv.switchTo().alert().dismiss();
			Messager.ALERT_CANCELED.info("");
		}
		catch (Exception e)
		{
			Messager.ALERT_NOT_CANCELED.error("");
		}
	}

	/**
	 * Checks that alert modal is shown.
	 * 
	 * @return whether the alert modal present.
	 */
	public boolean isAlertPresent()
	{
		try
		{
			getDriver().switchTo().alert();
			return true;
		}
		catch (NoAlertPresentException Ex)
		{
			return false;
		}
	}

	// --------------------------------------------------------------------------
	// Methods from v1.0
	// --------------------------------------------------------------------------

	public void setElementText(String controlInfo, String frame, String id, String text)
	{
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		WebDriver drv = getDriver();
		((JavascriptExecutor) drv).executeScript(String.format(
				"document.getElementById('%s').contentWindow.document.getElementById('%s').innerHTML='%s'", frame, id, decryptedText));
		String msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, controlInfo);
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
	}

	public void setElementText(String controlInfo, String text)
	{
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		WebDriver drv = getDriver();
		((JavascriptExecutor) drv)
				.executeScript(String
						.format("document.contentWindow.getElementsByTagName('ol')[0].getElementsByTagName('li')[1].getElementsByClassName('CodeMirror-lines')[0].getElementsByTagName('div')[0].getElementsByTagName('div')[2].innerHTML=<pre><span class='cm-plsql-word'>'%s'</span></pre>",
								decryptedText));
		String msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, controlInfo);
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(drv), msg);
	}

	public boolean isPageOpened(final AbstractPage page)
	{
		boolean result;
		final WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return LogicUtils.isURLEqual(page.getPageURL(), drv.getCurrentUrl());
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
		}
		return result;
	}

	public boolean isPageOpened(final AbstractPage page, long timeout)
	{
		boolean result;
		final WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return LogicUtils.isURLEqual(page.getPageURL(), drv.getCurrentUrl());
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
		}
		return result;
	}

	/**
	 * Executes a script on an element
	 * 
	 * @note Really should only be used when the web driver is sucking at
	 *       exposing functionality natively
	 * @param script
	 *            The script to execute
	 * @param element
	 *            The target of the script, referenced as arguments[0]
	 */
	public void trigger(String script, WebElement element)
	{
		((JavascriptExecutor) getDriver()).executeScript(script, element);
	}

	/**
	 * Executes a script
	 * 
	 * @note Really should only be used when the web driver is sucking at
	 *       exposing functionality natively
	 * @param script
	 *            The script to execute
	 */
	public Object trigger(String script)
	{
		return ((JavascriptExecutor) getDriver()).executeScript(script);
	}

	/**
	 * Opens a new tab for the given URL
	 * 
	 * @param url
	 *            The URL to
	 * @throws JavaScriptException
	 *             If unable to open tab
	 */
	public void openTab(String url)
	{
		final String decryptedURL = cryptoTool.decryptByPattern(url, CRYPTO_PATTERN);
		String script = "var d=document,a=d.createElement('a');a.target='_blank';a.href='%s';a.innerHTML='.';d.body.appendChild(a);return a";
		Object element = trigger(String.format(script, decryptedURL));
		if (element instanceof WebElement)
		{
			WebElement anchor = (WebElement) element;
			anchor.click();
			trigger("var a=arguments[0];a.parentNode.removeChild(a);", anchor);
		}
		else
		{
			throw new JavaScriptException(element, "Unable to open tab", 1);
		}
	}

	public void switchWindow() throws NoSuchWindowException, NoSuchWindowException
	{
		WebDriver drv = getDriver();
		Set<String> handles = drv.getWindowHandles();
		String current = drv.getWindowHandle();
		if (handles.size() > 1)
		{
			handles.remove(current);
		}
		String newTab = handles.iterator().next();
		drv.switchTo().window(newTab);
	}

	/**
	 * Swipes mobile screen by coordinates.
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param duration
	 */
	public void swipe(double startX, double startY, double endX, double endY, double duration)
	{
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		HashMap<String, Double> swipeObject = new HashMap<String, Double>();
		swipeObject.put("startX", startX);
		swipeObject.put("startY", startY);
		swipeObject.put("endX", endX);
		swipeObject.put("endY", endY);
		swipeObject.put("duration", duration);
		js.executeScript("mobile: swipe", swipeObject);
	}
	
    public void swipe(ExtendedWebElement element, Double startX, Double startY, Double endX, Double endY, Double duration) {
		LOGGER.info(String.format("Swipe on element %s. Start point (%s;%s), end point (%s;%s)", element.getNameWithLocator(), startX, startY, endX, endY));
		isElementPresent(element);

		
		final JavascriptExecutor js = (JavascriptExecutor) getDriver();
		final HashMap<String, String> swipeObject = new HashMap<String, String>();
		swipeObject.put("startX", startX.toString());
		swipeObject.put("startY", startY.toString());
		swipeObject.put("endX", endX.toString());
		swipeObject.put("endY", endY.toString());
		swipeObject.put("element", ((RemoteWebElement) element.getElement()).getId());
		swipeObject.put("duration", duration.toString());
		LOGGER.info(String.format("Swipe object: %s", swipeObject));
		js.executeScript("mobile: swipe", swipeObject);
		
    }	

	// --------------------------------------------------------------------------
	// Base UI validations
	// --------------------------------------------------------------------------
	public void assertElementPresent(final ExtendedWebElement extWebElement)
	{
		assertElementPresent(extWebElement, EXPLICIT_TIMEOUT);
	}

	public void assertElementPresent(final ExtendedWebElement extWebElement, long timeout)
	{
		if (isElementPresent(extWebElement, timeout))
		{
			TestLogCollector
					.addScreenshotComment(Screenshot.capture(getDriver()), Messager.ELEMENT_PRESENT.getMessage(extWebElement.toString()));
		}
		else
		{
			Assert.fail(Messager.ELEMENT_NOT_PRESENT.getMessage(extWebElement.getNameWithLocator()));
//			summary.log(Messager.ELEMENT_NOT_PRESENT.getMessage(extWebElement.getNameWithLocator()));
		}
	}

	public void assertElementWithTextPresent(final ExtendedWebElement extWebElement, final String text)
	{
		assertElementWithTextPresent(extWebElement, text, EXPLICIT_TIMEOUT);
	}

	public void assertElementWithTextPresent(final ExtendedWebElement extWebElement, final String text, long timeout)
	{
		if (isElementWithTextPresent(extWebElement, text, timeout))
		{
			TestLogCollector.addScreenshotComment(Screenshot.capture(getDriver()),
					Messager.ELEMENT_WITH_TEXT_PRESENT.getMessage(extWebElement.toString(), text));
		}
		else
		{
			Assert.fail(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.getMessage(extWebElement.toString(), text));
//			summary.log(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.getMessage(extWebElement.toString(), text));
		}
	}

	// --------------------------------------------------------------------------
	// Helpers
	// --------------------------------------------------------------------------
	private void logMakingScreen(String msg)
	{
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(getDriver()), msg);
	}
	

   
	/**
	 * Find Extended Web Element on page using By.
	 * 
	 * @param by
	 * @param name
	 * @return ExtendedWebElement if exists otherwise null.
	 */

	public ExtendedWebElement findExtendedWebElement(final By by, String name)
	{
		return findExtendedWebElement(by, name, EXPLICIT_TIMEOUT);
	}
	
	/**
	 * Find Extended Web Element on page using By.
	 * 
	 * @param by
	 * @param name
	 * @param timeout
	 * @return ExtendedWebElement if exists otherwise null.
	 */

	public ExtendedWebElement findExtendedWebElement(final By by, String name, long timeout)
	{
		ExtendedWebElement element;
		final WebDriver drv = getDriver();
		setImplicitTimeout(0);
		//drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
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
			element = new ExtendedWebElement(driver.findElement(by), name, by);
			summary.log(Messager.ELEMENT_FOUND.info(name));
		}
		catch (Exception e)
		{
			element = null;
			summary.log(Messager.ELEMENT_NOT_FOUND.error(name));
			//drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
			setImplicitTimeout(IMPLICIT_TIMEOUT);
			throw new RuntimeException(e);
		}
		//drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		setImplicitTimeout(IMPLICIT_TIMEOUT);
		return element;
	}	
    
    public ExtendedWebElement findExtendedWebElement(By by) {
    	return findExtendedWebElement(by, by.toString(), EXPLICIT_TIMEOUT);
    }
    
    public ExtendedWebElement findExtendedWebElement(By by, long timeout) {
    	return findExtendedWebElement(by, by.toString(), timeout);
    }
	
	public List<ExtendedWebElement> findExtendedWebElements(By by) {
		return findExtendedWebElements(by, EXPLICIT_TIMEOUT);
	}
	
	public List<ExtendedWebElement> findExtendedWebElements(final By by, long timeout)
	{
		List<ExtendedWebElement> extendedWebElements = new ArrayList<ExtendedWebElement> ();;
		List<WebElement> webElements = new ArrayList<WebElement> ();
		
		final WebDriver drv = getDriver();
		drv.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
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
			webElements = driver.findElements(by);
		}
		catch (Exception e)
		{
			//do nothing
		}
		
		for (WebElement element : webElements) {
			String name = "undefined";
			try {
				name = element.getText();
			} catch (Exception e) {/* do nothing*/}

			extendedWebElements.add(new ExtendedWebElement(element, name));
		}		
		drv.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return extendedWebElements;
	}	
     
	protected WebDriver getDriver() {
		if (driver == null || driver.toString().contains("null")) {
			if (Configuration.getDriverMode(Parameter.DRIVER_MODE) == DriverMode.SUITE_MODE)
		    {
				//duty hack to replace obsolete driver for all pages
				driver = DriverPool.getSingleDriver();
		    }
		}
		
		return driver;
	}

	public ExtendedWebElement format(ExtendedWebElement element, Object...objects) {
		return format(IMPLICIT_TIMEOUT, element, objects);
	}
	public ExtendedWebElement format(long timeout, ExtendedWebElement element, Object...objects) {
		String locator = element.getBy().toString();
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
		
		ExtendedWebElement res = null;
		try {
			res = findExtendedWebElement(by, timeout); 
		} catch (Exception e) {
			res = new ExtendedWebElement(null, element.getName(), by);
		}
		return res;
	}
}    