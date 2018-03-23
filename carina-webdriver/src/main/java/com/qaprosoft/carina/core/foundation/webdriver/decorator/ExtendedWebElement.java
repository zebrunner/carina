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
package com.qaprosoft.carina.core.foundation.webdriver.decorator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hamcrest.BaseMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.crypto.CryptoTool;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.metadata.MetadataCollector;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.ElementInfo;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.ElementsInfo;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.Rect;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.ScreenShootInfo;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocator;

import io.appium.java_client.MobileBy;

// TODO: [VD] Also refactor screenshots capturing using listener approach to be able to remove it as well
public class ExtendedWebElement {
    private static final Logger LOGGER = Logger.getLogger(ExtendedWebElement.class);

    private static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);

    private static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    private static final long RETRY_TIME = Configuration.getLong(Parameter.RETRY_INTERVAL);

    private static final String ATTRIBUTE_JS = "var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;";

    private static Wait<WebDriver> wait;
    private WebDriver driver;

    private CryptoTool cryptoTool;

    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    private WebElement element;
    private By by;
    
    private String name;

    public ExtendedWebElement(WebElement element, String name) {
        this(element);
        this.name = name;
    }

    public ExtendedWebElement(WebElement element, String name, By by) {
        this(element, name);
        this.by = by;
    }

    public ExtendedWebElement(WebElement element) {
        this.element = element;
        cryptoTool = new CryptoTool(Configuration.get(Parameter.CRYPTO_KEY_PATH));
        
        //read searchContext from not null element
        if (element == null) {
        	try {
        		throw new RuntimeException("to see stacktrace!");
        	} catch (Throwable thr) {
        		thr.printStackTrace();
        	}
        	//TODO: we should refactor and remove possibility to declare ExtendedWebElement with null element
        	return;
        }
        
		try {
            
			Field locatorField, searchContextField = null;
			SearchContext searchContext = null;
			
			if (element instanceof RemoteWebElement) {
				searchContext = ((RemoteWebElement) element).getWrappedDriver();
			} else if (element instanceof Proxy) { 
				InvocationHandler innerProxy = Proxy.getInvocationHandler(((Proxy) element));
				
				locatorField = innerProxy.getClass().getDeclaredField("locator");
				locatorField.setAccessible(true);
				
				ExtendedElementLocator locator = (ExtendedElementLocator) locatorField.get(innerProxy);
				
				searchContextField = locator.getClass().getDeclaredField("searchContext");
				searchContextField.setAccessible(true);
				searchContext = (SearchContext) searchContextField.get(locator);
				
				if (searchContext instanceof Proxy) {
					innerProxy = Proxy.getInvocationHandler(((Proxy) searchContext));
					
					locatorField = innerProxy.getClass().getDeclaredField("locator");
					locatorField.setAccessible(true);
					
					locator = (ExtendedElementLocator) locatorField.get(innerProxy);
					
					searchContextField = locator.getClass().getDeclaredField("searchContext");
					searchContextField.setAccessible(true);
					searchContext = (SearchContext) searchContextField.get(locator);
				}
			}
			
			if (searchContext instanceof RemoteWebElement) {
				searchContext = ((RemoteWebElement) searchContext).getWrappedDriver();
			}
			if (searchContext != null && searchContext instanceof RemoteWebDriver) {
				SessionId sessionId = ((RemoteWebDriver)searchContext).getSessionId();
				driver = DriverPool.getDriver(sessionId);
			} else {
				LOGGER.error(searchContext);
			}
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) { // TODO: refactor: why? somehow
											// HtmlElement objects can't be cast
											// to Proxy...
			e.printStackTrace();
		}
		if (driver == null) {
			try {
				throw new RuntimeException("review stacktrace!");
			} catch (Throwable thr) {
				thr.printStackTrace();
			}
		} else  {
			LOGGER.info("instance of driver");
		}

    }
    
    public WebElement getElement() {
        if (element == null) {
        	//TODO: why 1 sec?
            element = findElement(1);
        }
        return element;
    }

    private WebElement findElement(long timeout) {
//        if (element != null) {
//            return element;
//        }

        LOGGER.debug("There is null WebElement object. Trying to find dynamic element using By: " + by.toString());
        
        if (isPresent(timeout)) {
        	element = getDriver().findElement(by);
        } else {
            throw new RuntimeException("Unable to find dynamic element using By: " + by.toString());
        }

        return element;
    }

    public void setElement(WebElement element) {
        this.element = element;
    }

    public String getName() {
        return name != null ? name : String.format(" (%s)", by);
    }

    public String getNameWithLocator() {
        return by != null ? name + String.format(" (%s)", by) : name + " (n/a)";
    }

    public void setName(String name) {
        this.name = name;
    }

    //TODO: getText/Location etc - need methods with timeouts?
    /**
     * Get element text.
     *
     * @return String text
     */
    public String getText() {
       	return findElement(EXPLICIT_TIMEOUT).getText();
    }

    /**
     * Get element location.
     *
     * @return Point location
     */
    public Point getLocation() {
		return findElement(EXPLICIT_TIMEOUT).getLocation();
    }

    /**
     * Get element size.
     *
     * @return Dimension size
     */
    public Dimension getSize() {
    	return  findElement(EXPLICIT_TIMEOUT).getSize();
    }

    /**
     * Get element attribute.
     *
     * @param name of attribute
     * @return String text
     */
    public String getAttribute(String name) {
		return findElement(EXPLICIT_TIMEOUT).getAttribute(name);
    }

    /**
     * Get element By.
     *
     * @return By by
     */
    public By getBy() {
        return by;
    }

    public void setBy(By by) {
        this.by = by;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Clicks on element.
     */
    public void click() {
        click(EXPLICIT_TIMEOUT);
    }

    /**
     * Clicks on element.
     *
     * @param timeout to wait
     */
    public void click(long timeout) {
    	assertElementPresent(timeout);
        
        captureElements();
        
        try {
            getElement().click();
        } catch (StaleElementReferenceException | InvalidElementStateException e) {
        	getDriver().findElement(by).click();
        } catch (UnhandledAlertException e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            getDriver().switchTo().alert().accept();
            getElement().click();
        }
        catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.ELEMENT_NOT_CLICKED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.ELEMENT_CLICKED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
    }

    
    
    public void doubleClick() {
    	doubleClick(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Double Clicks on element.
     * 
     * @param timeout - time to wait until element is displayed
     */
    public void doubleClick(long timeout) {
    	assertElementPresent(timeout);

    	WebDriver drv = getDriver();
        Actions action = new Actions(drv);
        
        element = getElement();
        try {
        	action.moveToElement(element).doubleClick(element).build().perform();
        } catch (StaleElementReferenceException | InvalidElementStateException e) {
        	element = getDriver().findElement(by);
        	action.moveToElement(element).doubleClick(element).build().perform();
        } catch (UnhandledAlertException e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            getDriver().switchTo().alert().accept();
            action.moveToElement(element).doubleClick(element).build().perform();
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.ELEMENT_NOT_DOUBLE_CLICKED.error(getNameWithLocator());
            throw new RuntimeException(msg, e);
        }
        
        String msg = Messager.ELEMENT_DOUBLE_CLICKED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
    }

    public void rightClick() {
    	rightClick(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Mouse Right click to element.
     * 
     * @param timeout - time to wait until element is displayed
     *
     */
    public void rightClick(long timeout) {
    	assertElementPresent(timeout);

        WebDriver drv = getDriver();
        Actions action = new Actions(drv);
        
        String msg = "Right Click";
        try {
            element = getElement();
            action.moveToElement(element).contextClick(element).build().perform();
        } catch (StaleElementReferenceException | InvalidElementStateException e) {
        	element = getDriver().findElement(by);
            action.moveToElement(element).contextClick(element).build().perform();
        } catch (Exception e) {
            msg = Messager.ELEMENT_NOT_RIGHT_CLICKED.info(getNameWithLocator());
            throw new RuntimeException(msg, e);
        }

        msg = Messager.ELEMENT_RIGHT_CLICKED.info(getName());
        //TODO: move outside of class
        Screenshot.capture(drv, msg);
    }

    public void clickHiddenElement() {
    		clickHiddenElement(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Click Hidden Element. useful when element present in DOM but actually is
     * not visible. And can't be clicked by standard click.
     * 
     * @param timeout - time to wait until element is displayed
     */
    public void clickHiddenElement(long timeout) {
   		assertElementPresent(timeout);
        
        String msg = "Hidden Element Click";
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();

        try {
            executor.executeScript("arguments[0].click();", element);
        } catch (StaleElementReferenceException | InvalidElementStateException e) {
        	element = getDriver().findElement(by);
        	executor.executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            msg = Messager.HIDDEN_ELEMENT_NOT_CLICKED.info(getName());
            throw new RuntimeException(msg, e);
        }

        msg = Messager.HIDDEN_ELEMENT_CLICKED.info(getName());
        //TODO: move outside of class
        Screenshot.capture(getDriver(), msg);
    }

    
    //TODO: create isPresent with any ExpectedConditions as argument to be able to check isClickable, isVisible etc using the same method
    /**
     * Check that element present.
     *
     * @return element existence status.
     */
    private boolean isPresent(long timeout) {
        boolean result;
/*        if (timeout < 1) {
            LOGGER.warn("Timeout should be bigger than 0.");
            timeout = 1;
        }
*/
        final WebDriver drv = getDriver();
        wait = new WebDriverWait(drv, timeout, RETRY_TIME);
        try {
            LOGGER.debug("isElementPresent: starting..." + getNameWithLocator());
            wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
            result = true;
            LOGGER.debug("isElementPresent: finished true..." + getNameWithLocator());
        } catch (NoSuchElementException | TimeoutException e) {
            // don't write exception even in debug mode
            LOGGER.debug("isElementPresent: NoSuchElementException | TimeoutException e..." + getNameWithLocator());
            result = false;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            LOGGER.debug("isElementPresent: Exception e..." + getNameWithLocator(), e);
            result = false;
        } finally {
            LOGGER.debug("isElementPresent: finally..." + getNameWithLocator());
        }
        return result;
    }
    
    /**
     * Check that element present.
     *
     * @return element existence status.
     */
    public boolean isElementPresent() {
    	return isPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Check that element present within specified timeout.
     *
     * @param timeout - timeout.
     * @return element existence status.
     */
    public boolean isElementPresent(long timeout) {
    	return isPresent(timeout);
    }

    /**
     * Check that element not present within specified timeout.
     *
     * @param timeout - timeout.
     * @return element existence status.
     */
    public boolean isElementNotPresent(long timeout) {
        return !isElementPresent(timeout);
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
    	assertElementPresent(timeout);
    	
        final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
        return element.getText().contains(decryptedText);
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
        boolean present = isPresent(timeout);
        if (present) {
            captureElements();
            click();
            Messager.ELEMENT_CLICKED.info(getName());
        }

        return present;
    }

    /**
     * Types text to specified element.
     *
     * @param text to type.
     */
    public void type(String text) {
        type(text, EXPLICIT_TIMEOUT);
    }

    /**
     * Types text to specified element.
     *
     * @param text to type.
     * @param timeout long
     */
    public void type(String text, long timeout) {
    	assertElementPresent(timeout);
        
        captureElements();
        String msg;
        final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
        
		try {
			element = getElement();
			element.clear();
			element.sendKeys(decryptedText);
		} catch (InvalidElementStateException | StaleElementReferenceException e) {
			element = getDriver().findElement(by);
			element.clear();
			element.sendKeys(decryptedText);
		} catch (Exception e) {
			msg = Messager.KEYS_NOT_SEND_TO_ELEMENT.error(text, getNameWithLocator());
			throw new RuntimeException(msg, e);
		}

		msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, getName());
		//TODO: move outside of class
        WebDriver drv = getDriver();
        Screenshot.capture(drv, msg);
    }

    /**
     * Set implicit timeout to default IMPLICIT_TIMEOUT value.
     */
    //TODO: remove this functionality at all after migration to FluentWaits
    public void setImplicitTimeout() {
        setImplicitTimeout(IMPLICIT_TIMEOUT);
    }

    /**
     * Set implicit timeout.
     *
     * @param timeout in seconds. Minimal value - 1 second
     */
    public void setImplicitTimeout(long timeout) {
        try {
            LOGGER.debug("setImplicitTimeout: starting... value: " + timeout);
            getDriver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
            LOGGER.debug("setImplicitTimeout: finished. " + timeout);
        } catch (Exception e) {
            LOGGER.error("Unable to set implicit timeout to " + timeout, e);
            // getDriver().manage().timeouts().implicitlyWait(timeout,
            // TimeUnit.SECONDS);
        }
    }

    /**
     * Inputs file path to specified element.
     *
     * @param filePath path
     */
    public void attachFile(String filePath) {
    	assertElementPresent();
        
        String msg;
        final String decryptedFilePath = cryptoTool.decryptByPattern(filePath, CRYPTO_PATTERN);
        
        
        try {
            element.sendKeys(decryptedFilePath);
        } catch (InvalidElementStateException | StaleElementReferenceException e) {
			element = getDriver().findElement(by);
			element.sendKeys(decryptedFilePath);
		} catch (Exception e) {
            msg = Messager.FILE_NOT_ATTACHED.error(filePath);
            throw new RuntimeException(msg, e);
        }
        
        msg = Messager.FILE_ATTACHED.info(filePath);
        //TODO: move outside of class
        WebDriver drv = getDriver();
        Screenshot.capture(drv, msg);
    }

    /**
     * Check checkbox
     * <p>
     * for checkbox Element
     */
    public void check() {
    	assertElementPresent();
        
        if (!getElement().isSelected()) {
            click();
            String msg = Messager.CHECKBOX_CHECKED.info(getName());
            Screenshot.capture(getDriver(), msg);
        }
    }

    /**
     * Uncheck checkbox
     * <p>
     * for checkbox Element
     */
    public void uncheck() {
    	assertElementPresent();
        
        if (getElement().isSelected()) {
            click();
            String msg = Messager.CHECKBOX_UNCHECKED.info(getName());
            Screenshot.capture(getDriver(), msg);
        }
    }

    /**
     * Get checkbox state.
     *
     * @return - current state
     */
    public boolean isChecked() {
        assertElementPresent();
        
        element = findElement(EXPLICIT_TIMEOUT);
        boolean res = element.isSelected();
        if (element.getAttribute("checked") != null) {
            res |= element.getAttribute("checked").equalsIgnoreCase("true");
        }
        return res;
    }

    /**
     * Get selected elements from one-value select.
     *
     * @return selected value
     */
    public String getSelectedValue() {
        assertElementPresent();
        
        return new Select(getElement()).getAllSelectedOptions().get(0).getText();
    }

    /**
     * Get selected elements from multi-value select.
     *
     * @return selected values
     */
    public List<String> getSelectedValues() {
        assertElementPresent();
        
        Select s = new Select(getElement());
        List<String> values = new ArrayList<String>();
        for (WebElement we : s.getAllSelectedOptions()) {
            values.add(we.getText());
        }
        return values;
    }

    private WebDriver getDriver() {
    	if (driver != null) {
    		return driver;
    	} else {
    		LOGGER.warn("Unable to detect driver by sessionId! Looking for default one from pool.");
    		return DriverPool.getDriver();
    	}
    }

    /**
     * Selects text in specified select element.
     *
     * @param selectText select text
     * @return true if item selected, otherwise false.
     */
    public boolean select(final String selectText) {
    	assertElementPresent();
    	
        boolean isSelected = false;
        final String decryptedSelectText = cryptoTool.decryptByPattern(selectText, CRYPTO_PATTERN);

        String msg = null;

        try {
            final Select s = new Select(element);
            s.selectByVisibleText(decryptedSelectText);
        } catch (InvalidElementStateException | StaleElementReferenceException e) {
			element = getDriver().findElement(by);
            final Select s = new Select(element);
            s.selectByVisibleText(decryptedSelectText);
		} catch (Exception e) {
            msg = Messager.SELECT_BY_TEXT_NOT_PERFORMED.error(selectText, getNameWithLocator());
            throw new RuntimeException(msg, e);
        }
        
        msg = Messager.SELECT_BY_TEXT_PERFORMED.info(selectText, getName());
        WebDriver drv = getDriver();
        Screenshot.capture(drv, msg);
        return isSelected;
    }

    /**
     * Select multiple text values in specified select element.
     *
     * @param values final String[]
     * @return boolean.
     */
    public boolean select(final String[] values) {
        boolean result = true;
        for (String value : values) {
            if (!select(value)) {
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
     *         <p>
     *         Usage example: BaseMatcher&lt;String&gt; match=new
     *         BaseMatcher&lt;String&gt;() { {@literal @}Override public boolean
     *         matches(Object actual) { return actual.toString().contains(RequiredText);
     *         } {@literal @}Override public void describeTo(Description description) {
     *         } };
     */
    public boolean selectByMatcher(final BaseMatcher<String> matcher) {
    	assertElementPresent();
    	
        boolean isSelected = false;
        
        String msg = null;

		try {
			Select s = new Select(element);
			String fullTextValue = null;
			for (WebElement option : s.getOptions()) {
				if (matcher.matches(option.getText())) {
					fullTextValue = option.getText();
					break;
				}
			}
			s.selectByVisibleText(fullTextValue);
			isSelected = true;
		} catch (InvalidElementStateException | StaleElementReferenceException e) {
			element = getDriver().findElement(by);
			Select s = new Select(element);
			String fullTextValue = null;
			for (WebElement option : s.getOptions()) {
				if (matcher.matches(option.getText())) {
					fullTextValue = option.getText();
					break;
				}
			}
			s.selectByVisibleText(fullTextValue);
			isSelected = true;
		} catch (Exception e) {
			msg = Messager.SELECT_BY_MATCHER_TEXT_NOT_PERFORMED.error(matcher.toString(), getNameWithLocator());
			throw new RuntimeException(msg, e);
		}
		
		msg = Messager.SELECT_BY_MATCHER_TEXT_PERFORMED.info(matcher.toString(), getName());
		WebDriver drv = getDriver();
        Screenshot.capture(drv, msg);
        return isSelected;
    }

    /**
     * Selects first value according to partial text value.
     *
     * @param partialSelectText select by partial text
     * @return true if item selected, otherwise false.
     */
    public boolean selectByPartialText(final String partialSelectText) {
    	assertElementPresent();
    	
        boolean isSelected = false;

        String msg = null;
        String fullTextValue = null;
        
        try {
        	Select s = new Select(element);
            for (WebElement option : s.getOptions()) {
                if (option.getText().contains(partialSelectText)) {
                    fullTextValue = option.getText();
                    break;
                }
            }
            s.selectByVisibleText(fullTextValue);
            isSelected = true;
        } catch (InvalidElementStateException | StaleElementReferenceException e) {
			element = getDriver().findElement(by);
			Select s = new Select(element);
			fullTextValue = null;
            for (WebElement option : s.getOptions()) {
                if (option.getText().contains(partialSelectText)) {
                    fullTextValue = option.getText();
                    break;
                }
            }
            s.selectByVisibleText(fullTextValue);
            isSelected = true;
		} catch (Exception e) {
        	msg = Messager.SELECT_BY_TEXT_NOT_PERFORMED.error(partialSelectText, getNameWithLocator());
        	throw new RuntimeException(msg, e);
        }

        msg = Messager.SELECT_BY_TEXT_PERFORMED.info(partialSelectText, getName());
        WebDriver drv = getDriver();
        Screenshot.capture(drv, msg);

        return isSelected;
    }

    /**
     * Selects item by index in specified select element.
     *
     * @param index to select by
     * @return true if item selected, otherwise false.
     */
    public boolean select(final int index) {
    	assertElementPresent();
    	
        boolean isSelected = false;

        String msg = null;

        try {
        	Select s = new Select(element);
            s.selectByIndex(index);
            isSelected = true;
        } catch (InvalidElementStateException | StaleElementReferenceException e) {
			element = getDriver().findElement(by);
        	Select s = new Select(element);
            s.selectByIndex(index);
            isSelected = true;
		} catch (Exception e) {
        	msg = Messager.SELECT_BY_INDEX_NOT_PERFORMED.error(String.valueOf(index), getNameWithLocator());
        	throw new RuntimeException(msg, e);
        }
        
        msg = Messager.SELECT_BY_INDEX_PERFORMED.info(String.valueOf(index), getName());
        WebDriver drv = getDriver();
        Screenshot.capture(drv, msg);

        return isSelected;
    }

    // --------------------------------------------------------------------------
    // Base UI validations
    // --------------------------------------------------------------------------
    public void assertElementPresent() {
        assertElementPresent(EXPLICIT_TIMEOUT);
    }

    public void assertElementPresent(long timeout) {
        if (!isPresent(timeout)) {
        	Assert.fail(Messager.ELEMENT_NOT_PRESENT.getMessage(getNameWithLocator()));
        }
        
    }

    public void assertElementWithTextPresent(final String text) {
        assertElementWithTextPresent(text, EXPLICIT_TIMEOUT);
    }

    public void assertElementWithTextPresent(final String text, long timeout) {
        if (!isElementWithTextPresent(text, timeout)) {
            Assert.fail(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.getMessage(getNameWithLocator(), text));
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
        ExtendedWebElement element;
        final WebDriver drv = getDriver();
        wait = new WebDriverWait(drv, timeout, RETRY_TIME);
        try {
            setImplicitTimeout(0);
            wait.until((Function<WebDriver, Object>) dr -> {
                // try to search starting from existing webElement and using
                // driver directly
                if (!drv.findElements(by).isEmpty()) {
                    return true;
                } else if (getElement() != null) {
                    return !getElement().findElements(by).isEmpty();
                }
                return false;
            });
            element = new ExtendedWebElement(this.getElement().findElement(by), name, by);
            // summary.log(Messager.ELEMENT_FOUND.info(name));
        } catch (Exception e) {
            element = null;
            // summary.log(Messager.ELEMENT_NOT_FOUND.error(name));
            throw new RuntimeException(e);
        } finally {
            setImplicitTimeout(IMPLICIT_TIMEOUT);
        }

        return element;
    }

    public List<ExtendedWebElement> findExtendedWebElements(By by) {
        return findExtendedWebElements(by, EXPLICIT_TIMEOUT);
    }

    public List<ExtendedWebElement> findExtendedWebElements(final By by, long timeout) {
        List<ExtendedWebElement> extendedWebElements = new ArrayList<ExtendedWebElement>();
        List<WebElement> webElements = new ArrayList<WebElement>();

        final WebDriver drv = getDriver();
        wait = new WebDriverWait(drv, 10, RETRY_TIME);
        try {
            setImplicitTimeout(0);
            wait.until((Function<WebDriver, Object>) dr -> {
                // try to search starting from existing webElement and using
                // driver directly
                if (!drv.findElements(by).isEmpty()) {
                    return true;
                } else if (getElement() != null) {
                    return !getElement().findElements(by).isEmpty();
                }
                return false;

            });
            webElements = this.getElement().findElements(by);
        } catch (NoSuchElementException | TimeoutException e) {
            // do nothing
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            // do nothing
        } finally {
            setImplicitTimeout(IMPLICIT_TIMEOUT);
        }

        for (WebElement element : webElements) {
            String name = "undefined";
            try {
                name = element.getText();
            } catch (Exception e) {
                /* do nothing */
                LOGGER.debug(e.getMessage(), e.getCause());
            }

            extendedWebElements.add(new ExtendedWebElement(element, name));
        }
        return extendedWebElements;
    }

    @Deprecated
    public void tapWithCoordinates(double x, double y) {
        HashMap<String, Double> tapObject = new HashMap<String, Double>();
        tapObject.put("x", x);
        tapObject.put("y", y);
        final WebDriver drv = getDriver();
        JavascriptExecutor js = (JavascriptExecutor) drv;
        js.executeScript("mobile: tap", tapObject);
    }
    
    public void waitUntilElementNotPresent(final long timeout) {
        final ExtendedWebElement element = this;

        LOGGER.info(String.format("Wait until element %s disappear", element.getName()));
        
        final WebDriver drv = getDriver();

        wait = new WebDriverWait(drv, timeout, RETRY_TIME);
        try {
            setImplicitTimeout(0);
            wait.until((Function<WebDriver, Object>) dr -> {
            	// ne nayden then true
                boolean result = drv.findElements(element.getBy()).size() == 0;
                if (!result) {
                    LOGGER.debug(drv.getPageSource());
                    LOGGER.info(String.format("Element %s is still present. Wait until it disappear.", element.getName()));
                }
                return result;

            });
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            // do nothing
        } finally {
            setImplicitTimeout(IMPLICIT_TIMEOUT);
        }

    }

    /**
     * is Element Not Present After Wait
     *
     * @param timeout in seconds
     * @return boolean - false if element still present after wait - otherwise
     *         true if it disappear
     */
    public boolean isElementNotPresentAfterWait(final long timeout) {
        final ExtendedWebElement element = this;

        LOGGER.info(String.format("Check element %s not presence after wait.", element.getName()));

        Wait<WebDriver> wait = new FluentWait<WebDriver>(getDriver()).withTimeout(timeout, TimeUnit.SECONDS).pollingEvery(1, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class);
        try {
            return wait.until(driver -> {
                boolean result = driver.findElements(element.getBy()).isEmpty();
                if (!result) {
                    LOGGER.info(String.format("Element '%s' is still present. Wait until it disappear.", element.getNameWithLocator()));
                }
                return result;
            });
        } catch (Exception e) {
            LOGGER.error("Error happened: " + e.getMessage(), e.getCause());
            LOGGER.warn("Return standard element not presence method");
            return !element.isElementPresent();
        }
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
        final WebDriver drv = getDriver();
        By locator = getBy();
        boolean res = true;
        String msg = "";
        try {
            ExpectedConditions.elementToBeClickable(locator);
            (new WebDriverWait(drv, timeout)).until(ExpectedConditions.elementToBeClickable(locator));
            msg = Messager.ELEMENT_BECOME_CLICKABLE.info(getName());
            LOGGER.info(msg);
        } catch (TimeoutException ex) {
            msg = Messager.ELEMENT_NOT_BECOME_CLICKABLE.info(getName());
            LOGGER.error(msg, ex);
            res = false;
        } catch (Exception e) {
            msg = Messager.ELEMENT_NOT_BECOME_CLICKABLE.info(getName());
            LOGGER.error(msg, e);
            res = false;
        }

        return res;
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
        final WebDriver drv = getDriver();
        By locator = getBy();
        boolean res = true;
        String msg = "";
        try {
            ExpectedConditions.elementToBeClickable(locator);
            (new WebDriverWait(drv, timeout)).until(ExpectedConditions.visibilityOfElementLocated(locator));
            msg = Messager.ELEMENT_BECOME_VISIBLE.info(getName());
            LOGGER.info(msg);
        } catch (TimeoutException ex) {
            msg = Messager.ELEMENT_NOT_BECOME_VISIBLE.info(getName());
            LOGGER.error(msg, ex);
            res = false;
        } catch (Exception e) {
            msg = Messager.ELEMENT_NOT_BECOME_VISIBLE.info(getName());
            LOGGER.error(msg, e);
            res = false;
        }
        return res;
    }

    public ExtendedWebElement format(Object... objects) {
        String locator = by.toString();
        By by = null;

        if (locator.startsWith("By.id: ")) {
            by = By.id(String.format(StringUtils.remove(locator, "By.id: "), objects));
        }

        if (locator.startsWith("By.name: ")) {
            by = By.name(String.format(StringUtils.remove(locator, "By.name: "), objects));
        }

        if (locator.startsWith("By.xpath: ")) {
            by = By.xpath(String.format(StringUtils.remove(locator, "By.xpath: "), objects));
        }
        if (locator.startsWith("linkText: ")) {
            by = By.linkText(String.format(StringUtils.remove(locator, "linkText: "), objects));
        }

        if (locator.startsWith("partialLinkText: ")) {
            by = By.linkText(String.format(StringUtils.remove(locator, "partialLinkText: "), objects));
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

        if (locator.startsWith("By.xpath: **")) {
            by = MobileBy.iOSClassChain(String.format(StringUtils.remove(locator, "By.xpath: "), objects));
        }

        //TODO: find a way to generate valid WebElement here or reuse getElement() with invalid by
        
/*		try {
			Field locatorField, byField = null;
			InvocationHandler innerProxy = Proxy.getInvocationHandler(((Proxy) element));
			locatorField = innerProxy.getClass().getDeclaredField("locator");
			locatorField.setAccessible(true);
			
			ExtendedElementLocator elementLocator =  (ExtendedElementLocator) locatorField.get(innerProxy);
			
			byField = elementLocator.getClass().getDeclaredField("by");
			byField.setAccessible(true);
			byField.set(byField, by);
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) { // TODO: refactor: why? somehow
											// HtmlElement objects can't be cast
											// to Proxy...
			e.printStackTrace();
		}*/

        return new ExtendedWebElement(null, name, by);
    }

    private void captureElements() {

        if (!Configuration.getBoolean(Parameter.SMART_SCREENSHOT)) {
            return;
        }

        if (!BrowserType.CHROME.equalsIgnoreCase(Configuration.get(Parameter.BROWSER))) {
            return;
        }

        String currentUrl;
        if (!Configuration.get(Parameter.BROWSER).isEmpty()) {
            currentUrl = getDriver().getCurrentUrl();
        } else {
            // change for XBox and looks like mobile part
            currentUrl = getDriver().getTitle();
        }

        String cache = getUrlWithoutParameters(currentUrl);
        if (!MetadataCollector.getAllCollectedData().containsKey(cache)) {
            try {

                ElementsInfo elementsInfo = new ElementsInfo();
                elementsInfo.setCurrentURL(currentUrl);

                String metadataScreenPath = Screenshot.captureMetadata(getDriver(), String.valueOf(cache.hashCode()));
                // TODO: double check that file exist because due to the
                // different reason screenshot can miss
                File newPlace = new File(metadataScreenPath);

                ScreenShootInfo screenShootInfo = new ScreenShootInfo();
                screenShootInfo.setScreenshotPath(newPlace.getAbsolutePath());
                BufferedImage bimg = ImageIO.read(newPlace);

                screenShootInfo.setWidth(bimg.getWidth());
                screenShootInfo.setHeight(bimg.getHeight());
                elementsInfo.setScreenshot(screenShootInfo);

                List<WebElement> all = getDriver().findElements(By.xpath("//*"));

                List<WebElement> control = getDriver().findElements(
                        By.xpath("//input[not(contains(@type,'hidden'))] | //button | .//*[contains(@class, 'btn') and not(self::span)] | //select"));

                for (WebElement webElement : control) {
                    ElementInfo elementInfo = getElementInfo(new ExtendedWebElement(webElement));

                    int elementPosition = all.indexOf(webElement);
                    for (int i = 1; i < 5; i++) {
                        if (elementPosition - i < 0) {
                            break;
                        }

                        if (control.indexOf(all.get(elementPosition - i)) > 0) {
                            break;
                        }
                        if (!all.get(elementPosition - i).isDisplayed()) {
                            continue;
                        }
                        String sti = all.get(elementPosition - i).getText();
                        if (sti == null || sti.isEmpty() || control.get(0).getText().equals(sti)) {
                            continue;
                        } else {
                            elementInfo.setTextInfo(getElementInfo(new ExtendedWebElement(all.get(elementPosition - i))));
                            break;
                        }
                    }
                    elementsInfo.addElement(elementInfo);
                    MetadataCollector.putPageInfo(cache, elementsInfo);
                }

            } catch (IOException e) {
                LOGGER.error("Unable to capture elements metadata!", e);
            } catch (Exception e) {
                LOGGER.error("Unable to capture elements metadata!", e);
            } catch (Throwable thr) {
                LOGGER.error("Unable to capture elements metadata!", thr);
            }
        }
    }

    private String getUrlWithoutParameters(String url) {

        try {
            URI uri = new URI(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }

    @SuppressWarnings("unchecked")
    private ElementInfo getElementInfo(ExtendedWebElement extendedWebElement) {
        ElementInfo elementInfo = new ElementInfo();
        if (extendedWebElement.isElementPresent(1)) {
            Point location = extendedWebElement.getElement().getLocation();
            Dimension size = extendedWebElement.getElement().getSize();
            elementInfo.setRect(new Rect(location.getX(), location.getY(), size.getWidth(), size.getHeight()));
            elementInfo.setElementsAttributes(
                    (Map<String, String>) ((RemoteWebDriver) getDriver()).executeScript(ATTRIBUTE_JS, extendedWebElement.getElement()));

            try {
                elementInfo.setText(extendedWebElement.getText());
            } catch (Exception e) {
                elementInfo.setText("");
            }

            return elementInfo;
        } else {
            return null;
        }

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

}
