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

import org.apache.log4j.Logger;
import org.hamcrest.BaseMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Action;
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

	/**
	 * Initializes test log container dedicated to WebDriver instance.
	 * 
	 * @param driver
	 */
	protected void initSummary(WebDriver driver)
	{
		summary = new TestLogHelper(driver);
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
		boolean result;
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return extWebElement.getElement().isDisplayed();
				}
			});
			result = true;
			summary.log(Messager.ELEMENT_PRESENT.info(extWebElement.getName()));
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.ELEMENT_NOT_PRESENT.error(extWebElement.getNameWithLocator()));
		}
		return result;
	}

	public boolean isElementPresent(String controlInfo, final WebElement element)
	{
		return isElementPresent(new ExtendedWebElement(element, controlInfo));
	}

	/**
	 * Check that element present within specified timeout.
	 * 
	 * @param element
	 *            to find.
	 * @param maxWait
	 *            - timeout.
	 * @return element existence status.
	 */
	public boolean isElementPresent(final ExtendedWebElement extWebElement, long maxWait)
	{
		boolean result;
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, maxWait, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return extWebElement.getElement().isDisplayed();
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
		}
		driver.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		return result;
	}

	public boolean isElementPresent(String controlInfo, final WebElement element, long maxWait)
	{
		return isElementPresent(new ExtendedWebElement(element, controlInfo), maxWait);
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
		boolean result;
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
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

	public boolean isElementWithTextPresent(final ExtendedWebElement extWebElement, final String text, long maxWait)
	{
		boolean result;
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		wait = new WebDriverWait(driver, maxWait, RETRY_TIME);
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
	public boolean isElementNotPresent(final ExtendedWebElement extWebElement)
	{
		boolean result;
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return extWebElement.getElement() == null || !extWebElement.getElement().isDisplayed();
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.UNEXPECTED_ELEMENT_PRESENT.error(extWebElement.getNameWithLocator()));
		}
		return result;
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
	public boolean isElementNotPresent(String elementName, final By by)
	{
		boolean result;
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return driver.findElements(by).isEmpty();
				}
			});
			result = true;
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.UNEXPECTED_ELEMENT_PRESENT.error(elementName));
		}
		driver.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
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
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return extWebElement.getElement().isDisplayed();
				}
			});
			extWebElement.getElement().clear();
			extWebElement.getElement().sendKeys(decryptedText);
			msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, extWebElement.getName());
			summary.log(msg);
		}
		catch (Exception e)
		{
			msg = Messager.KEYS_NOT_SEND_TO_ELEMENT.error(text, extWebElement.getNameWithLocator());
			summary.log(msg);
			Assert.fail(msg);
		}
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
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
		isElementPresent(extendedWebElement);
		clickSafe(extendedWebElement, true);
		String msg = Messager.ELEMENT_CLICKED.info(extendedWebElement.getName());
		summary.log(msg);
		try
		{
			TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
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

	/**
	 * Safe click on element, used to reduce any problems with that action.
	 * 
	 * @param elementName
	 * @param element
	 * @param startTimer
	 */
	private void clickSafe(final ExtendedWebElement extendedWebElement, boolean startTimer)
	{
		if (startTimer)
		{
			timer = System.currentTimeMillis();
		}
		try
		{
			Thread.sleep(RETRY_TIME);
			extendedWebElement.getElement().click();
		}
		catch (UnhandledAlertException e)
		{
			driver.switchTo().alert().accept();
		}
		catch (Exception e)
		{
			if (e.getMessage().contains("Element is not clickable"))
			{
				scrollTo(extendedWebElement);
			}

			if (System.currentTimeMillis() - timer < EXPLICIT_TIMEOUT * 1000)
			{
				clickSafe(extendedWebElement, false);
			}
			else
			{
				Assert.fail(Messager.ELEMENT_NOT_CLICKED.error(extendedWebElement.getNameWithLocator()));
			}
		}
	}

	/**
	 * Sends enter to element.
	 * 
	 * @param element
	 *            to send enter.
	 */
	public void pressEnter(final ExtendedWebElement extendedWebElement)
	{
		isElementPresent(extendedWebElement);
		pressEnterSafe(extendedWebElement, true);
		String msg = Messager.ELEMENT_CLICKED.info(extendedWebElement.getName());
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
	}

	public void pressEnter(String controlInfo, WebElement control)
	{
		pressEnter(new ExtendedWebElement(control, controlInfo));
	}

	/**
	 * Safe enter sending to specified element.
	 * 
	 * @param elementName
	 * @param element
	 * @param startTimer
	 */
	private void pressEnterSafe(final ExtendedWebElement extendedWebElement, boolean startTimer)
	{

		if (startTimer)
		{
			timer = System.currentTimeMillis();
		}
		try
		{
			Thread.sleep(RETRY_TIME);
			extendedWebElement.getElement().sendKeys(Keys.ENTER);
		}
		catch (UnhandledAlertException e)
		{
			driver.switchTo().alert().accept();
		}
		catch (Exception e)
		{
			if (System.currentTimeMillis() - timer < EXPLICIT_TIMEOUT * 1000)
			{
				pressEnterSafe(extendedWebElement, false);
			}
			else
			{
				Assert.fail(Messager.ELEMENT_NOT_CLICKED.error(extendedWebElement.getNameWithLocator()));
			}
		}
	}

	/**
	 * Check checkbox
	 * 
	 * @param checkbox
	 */
	public void check(final ExtendedWebElement checkbox)
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
	public void uncheck(final ExtendedWebElement checkbox)
	{
		if (isElementPresent(checkbox) && checkbox.getElement().isSelected())
		{
			click(checkbox);
			logMakingScreen(Messager.CHECKBOX_UNCHECKED.info(checkbox.getName()));
		}
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
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
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
			Assert.fail(msg);
		}
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
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
		try
		{
			driver.get(decryptedURL);
		}
		catch (UnhandledAlertException e)
		{
			driver.switchTo().alert().accept();
		}
		//AUTO-250 tweak core to start browser in maximized mode - to prevent stability issues
		try
		{
			driver.manage().window().maximize();
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
		if (LogicUtils.isURLEqual(decryptedURL, driver.getCurrentUrl()))
		{
			summary.log(Messager.EXPECTED_URL.info(driver.getCurrentUrl()));
			return true;
		}
		else
		{
			Messager.UNEXPECTED_URL.error(expectedURL, driver.getCurrentUrl());
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
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return driver.getTitle().contains(decryptedExpectedTitle);
				}
			});
			result = true;
			summary.log(Messager.TITLE_CORERECT.info(driver.getCurrentUrl(), expectedTitle));
		}
		catch (Exception e)
		{
			result = false;
			summary.log(Messager.TITLE_NOT_CORERECT.error(driver.getCurrentUrl(), expectedTitle, driver.getTitle()));
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
		String actual = driver.getTitle();
		Pattern p = Pattern.compile(decryptedExpectedPattern);
		Matcher m = p.matcher(actual);
		if (m.find())
		{
			summary.log(Messager.TITLE_CORERECT.info(driver.getCurrentUrl(), actual));
			result = true;
		}
		else
		{
			summary.log(Messager.TITLE_DOES_NOT_MATCH_TO_PATTERN.error(driver.getCurrentUrl(), expectedPattern, actual));
			result = false;
		}
		return result;
	}

	/**
	 * Go back in browser.
	 */
	public void navigateBack()
	{
		driver.navigate().back();
		summary.log(Messager.BACK.info());
	}

	/**
	 * Refresh browser.
	 */
	public void refresh()
	{
		driver.navigate().refresh();
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
		String msg = null;
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
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
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);

		return isSelected;
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
		return checkbox.getElement().isSelected();
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
		String msg = null;
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
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
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);

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
		String msg = null;
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
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
		}
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);

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
	public void hover(final ExtendedWebElement extendedWebElement)
	{
		if (isElementPresent(extendedWebElement))
		{
			Actions action = new Actions(driver);
			action.moveToElement(extendedWebElement.getElement());
			action.perform();

			String msg = Messager.HOVER_IMG.info(extendedWebElement.getName());
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
		}
		else
		{
			Assert.fail(Messager.ELEMENT_NOT_HOVERED.error(extendedWebElement.getNameWithLocator()));
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
		Actions action = new Actions(driver);
		action.moveToElement(driver.findElement(By.xpath(xpathLocator))).perform();
		String msg = Messager.HOVER_IMG.info(elementName);
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
	}

	public void scrollTo(final ExtendedWebElement extendedWebElement)
	{
		try
		{
			Locatable locatableElement = (Locatable) extendedWebElement.getElement();
			int y = locatableElement.getCoordinates().onScreen().getY();
			((JavascriptExecutor) driver).executeScript("window.scrollBy(0," + (y - 120) + ");");
		}
		catch (Exception e)
		{
			// TODO: calm error logging as it is too noisy
			// LOGGER.error("Scroll to element: " + extendedWebElement.getName()
			// + " not performed, seems not clickable yet!");
		}
	}

	public void pressTab()
	{
		Actions builder = new Actions(driver);
		builder.sendKeys(Keys.TAB).perform();
	}

	public void sendKeys(String keys)
	{
		final String decryptedKeys = cryptoTool.decryptByPattern(keys, CRYPTO_PATTERN);
		Actions builder = new Actions(driver);
		builder.sendKeys(decryptedKeys).perform();
	}

	/**
	 * Close alert modal by JS.
	 */
	public void sielentAlert()
	{
		if (!(driver instanceof HtmlUnitDriver))
		{
			((JavascriptExecutor) driver).executeScript("window.alert = function(msg) { return true; }");
			((JavascriptExecutor) driver).executeScript("window.confirm = function(msg) { return true; }");
			((JavascriptExecutor) driver).executeScript("window.prompt = function(msg) { return true; }");
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
			Actions builder = new Actions(driver);
			Action dragAndDrop = builder.clickAndHold(from.getElement()).moveToElement(to.getElement()).release(to.getElement()).build();
			dragAndDrop.perform();

			String msg = Messager.ELEMENTS_DRAGGED_AND_DROPPED.info(from.getName(), to.getName());
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
		}
		else
		{
			Assert.fail(Messager.ELEMENTS_NOT_DRAGGED_AND_DROPPED.error(from.getNameWithLocator(), to.getNameWithLocator()));
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
			(new Actions(driver)).moveToElement(slider.getElement()).dragAndDropBy(slider.getElement(), moveX, moveY).build().perform();
			String msg = Messager.SLIDER_MOVED.info(slider.getNameWithLocator(), String.valueOf(moveX), String.valueOf(moveY));
			summary.log(msg);
			TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
		}
		else
		{
			Assert.fail(Messager.SLIDER_NOT_MOVED.error(slider.getNameWithLocator(), String.valueOf(moveX), String.valueOf(moveY)));
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
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return isAlertPresent();
				}
			});
			driver.switchTo().alert().accept();
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
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return isAlertPresent();
				}
			});
			driver.switchTo().alert().dismiss();
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
			driver.switchTo().alert();
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
		((JavascriptExecutor) driver).executeScript(String.format(
				"document.getElementById('%s').contentWindow.document.getElementById('%s').innerHTML='%s'", frame, id, decryptedText));
		String msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, controlInfo);
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
	}

	public void setElementText(String controlInfo, String text)
	{
		final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
		((JavascriptExecutor) driver)
				.executeScript(String
						.format("document.contentWindow.getElementsByTagName('ol')[0].getElementsByTagName('li')[1].getElementsByClassName('CodeMirror-lines')[0].getElementsByTagName('div')[0].getElementsByTagName('div')[2].innerHTML=<pre><span class='cm-plsql-word'>'%s'</span></pre>",
								decryptedText));
		String msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, controlInfo);
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
	}

	public boolean isPageOpened(final AbstractPage page)
	{
		boolean result;
		wait = new WebDriverWait(driver, EXPLICIT_TIMEOUT, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return LogicUtils.isURLEqual(page.getPageURL(), driver.getCurrentUrl());
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
		wait = new WebDriverWait(driver, timeout, RETRY_TIME);
		try
		{
			wait.until(new ExpectedCondition<Boolean>()
			{
				public Boolean apply(WebDriver dr)
				{
					return LogicUtils.isURLEqual(page.getPageURL(), driver.getCurrentUrl());
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
		((JavascriptExecutor) driver).executeScript(script, element);
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
		return ((JavascriptExecutor) driver).executeScript(script);
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
		Set<String> handles = driver.getWindowHandles();
		String current = driver.getWindowHandle();
		if (handles.size() > 1)
		{
			handles.remove(current);
		}
		String newTab = handles.iterator().next();
		driver.switchTo().window(newTab);
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
		JavascriptExecutor js = (JavascriptExecutor) driver;
		HashMap<String, Double> swipeObject = new HashMap<String, Double>();
		swipeObject.put("startX", startX);
		swipeObject.put("startY", startY);
		swipeObject.put("endX", endX);
		swipeObject.put("endY", endY);
		swipeObject.put("duration", duration);
		js.executeScript("mobile: swipe", swipeObject);
	}

	// --------------------------------------------------------------------------
	// Base UI validations
	// --------------------------------------------------------------------------
	public void assertElementPresent(final ExtendedWebElement extWebElement)
	{
		assertElementPresent(extWebElement, EXPLICIT_TIMEOUT);
	}

	public void assertElementPresent(final ExtendedWebElement extWebElement, long maxWait)
	{
		if (isElementPresent(extWebElement, maxWait))
		{
			TestLogCollector
					.addScreenshotComment(Screenshot.capture(driver), Messager.ELEMENT_PRESENT.getMessage(extWebElement.toString()));
		}
		else
		{
			Assert.fail(Messager.ELEMENT_NOT_PRESENT.getMessage(extWebElement.getNameWithLocator()));
		}
	}

	public void assertElementWithTextPresent(final ExtendedWebElement extWebElement, final String text)
	{
		assertElementWithTextPresent(extWebElement, text, EXPLICIT_TIMEOUT);
	}

	public void assertElementWithTextPresent(final ExtendedWebElement extWebElement, final String text, long maxWait)
	{
		if (isElementWithTextPresent(extWebElement, text, maxWait))
		{
			TestLogCollector.addScreenshotComment(Screenshot.capture(driver),
					Messager.ELEMENT_WITH_TEXT_PRESENT.getMessage(extWebElement.toString(), text));
		}
		else
		{
			Assert.fail(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.getMessage(extWebElement.toString(), text));
		}
	}

	// --------------------------------------------------------------------------
	// Helpers
	// --------------------------------------------------------------------------
	private void logMakingScreen(String msg)
	{
		summary.log(msg);
		TestLogCollector.addScreenshotComment(Screenshot.capture(driver), msg);
	}
}
