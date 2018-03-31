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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebDriver;
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
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.metadata.MetadataCollector;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.ElementInfo;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.ElementsInfo;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.Rect;
import com.qaprosoft.carina.core.foundation.utils.metadata.model.ScreenShootInfo;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;

import io.appium.java_client.MobileBy;

// TODO: [VD] removed deprecated constructor and DriverPool import
// Also refactor screenshots capturing using listener approach to be able to remove it as well
public class ExtendedWebElement {
    private static final Logger LOGGER = Logger.getLogger(ExtendedWebElement.class);

    private static final long IMPLICIT_TIMEOUT = Configuration.getLong(Parameter.IMPLICIT_TIMEOUT);

    private static final long EXPLICIT_TIMEOUT = Configuration.getLong(Parameter.EXPLICIT_TIMEOUT);

    private static final long RETRY_TIME = Configuration.getLong(Parameter.RETRY_INTERVAL);

    private static final String ATTRIBUTE_JS = "var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;";

    private static Wait<WebDriver> wait;

    private CryptoTool cryptoTool;

    private static Pattern CRYPTO_PATTERN = Pattern.compile(SpecialKeywords.CRYPT);

    private WebElement element;
    private String name;
    private By by;
    private WebDriver driver;

    public ExtendedWebElement(WebElement element, String name, WebDriver driver) {
        this(element, driver);
        this.name = name;
    }

    public ExtendedWebElement(WebElement element, String name, By by, WebDriver driver) {
        this(element, name, driver);
        this.by = by;
    }

    public ExtendedWebElement(WebElement element, WebDriver driver) {
        this.element = element;
        this.driver = driver;
        cryptoTool = new CryptoTool(Configuration.get(Parameter.CRYPTO_KEY_PATH));
    }

    @Deprecated
    public ExtendedWebElement(WebElement element, String name) {
        // TODO: remove usage with default river!
        this(element, name, DriverPool.getDriver());
    }

    @Deprecated
    public ExtendedWebElement(WebElement element, String name, By by) {
        this(element, name, by, DriverPool.getDriver());
    }

    @Deprecated
    public ExtendedWebElement(WebElement element) {
        this(element, DriverPool.getDriver());
    }

    public WebElement getElement() {
        if (element == null) {
        	//TODO: why 1 sec?
            element = findElement(1);
        }
        return element;
    }

    private boolean isPresent(long timeout) {
    	return isPresent(getBy(), timeout);
    }
    
	private boolean isPresent(By by, long timeout) {
		boolean result;
		final WebDriver drv = getDriver();
		wait = new WebDriverWait(drv, timeout, RETRY_TIME);
		try {
			LOGGER.info("isPresent: starting..." + getNameWithLocator());
			wait.until(ExpectedConditions.presenceOfElementLocated(getBy()));
			result = true;
			LOGGER.info("isPresent: finished true..." + getNameWithLocator());
		} catch (NoSuchElementException | TimeoutException e) {
			// don't write exception even in debug mode
			LOGGER.info("isPresent: NoSuchElementException | TimeoutException e..." + getNameWithLocator());
			result = false;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e.getCause());
			LOGGER.error("isPresent: Exception e..." + getNameWithLocator(), e);
			result = false;
		} finally {
			LOGGER.debug("isPresent: finally..." + getNameWithLocator());
		}
		return result;
	}
	
    private WebElement findElement(long timeout) {
        if (element != null) {
            return element;
        }
        
        if (isPresent(timeout)) {
        	element = getDriver().findElement(by);
        } else {
        	throw new RuntimeException("Unable to find dynamic element using By: " + by.toString());
        }

        return element;
    }
    
    private WebElement findStaleElement(By by, long timeout) {
        LOGGER.info("explicitly find element using by annotation: " + by);
        if (isPresent(by, timeout)) {
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

    /**
     * Get element text.
     *
     * @return String text
     */
    public String getText() {
    	String text = "";
    	try {
    		text = findElement(EXPLICIT_TIMEOUT).getText();
    	} catch (Exception e) {
        	if (e != null && e.getMessage().contains("StaleObjectException")) {
        		element = findStaleElement(getBy(), 1);
        		text = element.getText();
        		//reset exception info and everything is fine after refind
        	}

    	}
    	return text;
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
    	return findElement(EXPLICIT_TIMEOUT).getSize();
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
    	assertElementPresent(timeout);
    	captureElements();
    	
    	
        try {
            getElement().click();
        } catch (UnhandledAlertException e) {
        	//TODO: think about removal for this alert as not very popular anymore
            LOGGER.debug(e.getMessage(), e.getCause());
            getDriver().switchTo().alert().accept();
            getElement().click();
        } catch (StaleElementReferenceException e) {
        	LOGGER.info("catched StaleElementReferenceException: ", e);
        	// analyze if it StaleObjectException and try to find again using driver
        	element = findStaleElement(getBy(), 1);
    		element.click();
        } catch (Throwable e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.ELEMENT_NOT_CLICKED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;
        }
        
        String msg = Messager.ELEMENT_CLICKED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
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
    	assertElementPresent(timeout);
    	captureElements();

    	WebDriver drv = getDriver();
        Actions action = new Actions(drv);

        try {
        	element = getElement();
            action.moveToElement(element).doubleClick(element).build().perform();
        } catch (UnhandledAlertException e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            getDriver().switchTo().alert().accept();
            
        	element = getElement();
            action.moveToElement(element).doubleClick(element).build().perform();
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.ELEMENT_NOT_DOUBLE_CLICKED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.ELEMENT_DOUBLE_CLICKED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
    }

    /**
     * Mouse Right click on element.
     */
    public void rightClick() {
    	rightClick(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Mouse Right click on element.
     *
     * @param timeout to wait
     */
    public void rightClick(long timeout) {
    	assertElementPresent(timeout);
    	captureElements();

    	WebDriver drv = getDriver();
        Actions action = new Actions(drv);

        try {
        	element = getElement();
        	action.moveToElement(element).contextClick(element).build().perform();
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.ELEMENT_NOT_RIGHT_CLICKED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.ELEMENT_RIGHT_CLICKED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
    }
    
    
    /**
     * Click Hidden Element. useful when element present in DOM but actually is
     * not visible. And can't be clicked by standard click.
     */
    public void clickHiddenElement() {
    	clickHiddenElement(EXPLICIT_TIMEOUT);
    }
    
    /**
     * Click Hidden Element. useful when element present in DOM but actually is
     * not visible. And can't be clicked by standard click.
     *
     * @param timeout to wait
     */
    public void clickHiddenElement(long timeout) {
    	assertElementPresent(timeout);
    	captureElements();

        try {
        	element = getElement();
        	JavascriptExecutor executor = (JavascriptExecutor) getDriver();
            executor.executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.HIDDEN_ELEMENT_NOT_CLICKED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.HIDDEN_ELEMENT_CLICKED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
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
        String msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, getName());
        final String decryptedText = cryptoTool.decryptByPattern(text, CRYPTO_PATTERN);
        
        try {
            getElement().clear();
            getElement().sendKeys(decryptedText);
        } catch (UnhandledAlertException e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            getDriver().switchTo().alert().accept();
            getElement().clear();
            getElement().sendKeys(decryptedText);
        } catch (StaleElementReferenceException e) {
        	LOGGER.info("catched StaleElementReferenceException: ", e);
        	// analyze if it StaleObjectException and try to find again using driver
        	element = findStaleElement(getBy(), 1);
        	element.clear();
        	element.sendKeys(decryptedText);
        } catch (Throwable e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            msg = Messager.KEYS_NOT_SEND_TO_ELEMENT.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
  
    }

    /**
     * Set implicit timeout to default IMPLICIT_TIMEOUT value.
     */
    public void setImplicitTimeout() {
        setImplicitTimeout(IMPLICIT_TIMEOUT);
    }

    /**
     * Set implicit timeout.
     *
     * @param timeout in seconds. Minimal value - 1 second
     */
    public void setImplicitTimeout(long timeout) {
        if (timeout < 1) {
            timeout = 1;
        }

        try {
            LOGGER.info("setImplicitTimeout: starting... value: " + timeout);
            getDriver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
            LOGGER.info("setImplicitTimeout: finished. " + timeout);
        } catch (Exception e) {
            LOGGER.error("Unable to set implicit timeout to " + timeout, e);
        }
    }

    /**
     * Scroll to element (applied only for desktop).
     */
    @Deprecated
    public void scrollTo() {
        if (Configuration.getDriverType().equals(SpecialKeywords.MOBILE)) {
            LOGGER.debug("scrollTo javascript is unsupported for mobile devices!");
            return;
        }
        try {
            Locatable locatableElement = (Locatable) findElement(EXPLICIT_TIMEOUT);
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
            // TODO: calm error logging as it is too noisy
            // LOGGER.debug("Scroll to element: " + getName() + " not
            // performed!" + e.getMessage());
        }
    }

    /**
     * Inputs file path to specified element.
     *
     * @param filePath path
     */
    public void attachFile(String filePath) {
    	assertElementPresent(EXPLICIT_TIMEOUT);
    	
    	final String decryptedFilePath = cryptoTool.decryptByPattern(filePath, CRYPTO_PATTERN);
    	
        try {
        	getElement().sendKeys(decryptedFilePath);
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.FILE_NOT_ATTACHED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.FILE_ATTACHED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
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
        element = getElement();
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
			LOGGER.error("Unable to detect driver by sessionId! Looking for default one from pool.");
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
    	assertElementPresent(EXPLICIT_TIMEOUT);
    	
        boolean isSelected = false;
        final String decryptedSelectText = cryptoTool.decryptByPattern(selectText, CRYPTO_PATTERN);
        
        try {
        	final Select s = new Select(element);
        	s.selectByVisibleText(decryptedSelectText);
        	isSelected = true;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.SELECT_BY_TEXT_NOT_PERFORMED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.SELECT_BY_TEXT_PERFORMED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
        
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
    	assertElementPresent(EXPLICIT_TIMEOUT);
    	
        boolean isSelected = false;
        
        try {
        	final Select s = new Select(element);
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
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.SELECT_BY_MATCHER_TEXT_NOT_PERFORMED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.SELECT_BY_MATCHER_TEXT_PERFORMED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
        
        return isSelected;
    }

    /**
     * Selects first value according to partial text value.
     *
     * @param partialSelectText select by partial text
     * @return true if item selected, otherwise false.
     */
    public boolean selectByPartialText(final String partialSelectText) {
    	assertElementPresent(EXPLICIT_TIMEOUT);
    	
        boolean isSelected = false;
        
        try {
        	final Select s = new Select(element);
            String fullTextValue = null;
            for (WebElement option : s.getOptions()) {
                if (option.getText().contains(partialSelectText)) {
                    fullTextValue = option.getText();
                    break;
                }
            }
            s.selectByVisibleText(fullTextValue);
        	isSelected = true;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.SELECT_BY_TEXT_NOT_PERFORMED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.SELECT_BY_TEXT_PERFORMED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
        
        return isSelected;
    }

    /**
     * Selects item by index in specified select element.
     *
     * @param index to select by
     * @return true if item selected, otherwise false.
     */
    public boolean select(final int index) {
    	assertElementPresent(EXPLICIT_TIMEOUT);
    	
        boolean isSelected = false;
        
        try {
        	final Select s = new Select(element);
        	s.selectByIndex(index);
        	isSelected = true;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e.getCause());
            String msg = Messager.SELECT_BY_INDEX_NOT_PERFORMED.error(getNameWithLocator());
            Screenshot.capture(getDriver(), msg);
            throw e;  
        }
        
        String msg = Messager.SELECT_BY_INDEX_PERFORMED.info(getName());
        //TODO: move screenshoting outside of class
        Screenshot.capture(getDriver(), msg);
        
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
        if (isElementWithTextPresent(text, timeout)) {
            Screenshot.capture(getDriver(), Messager.ELEMENT_WITH_TEXT_PRESENT.getMessage(getName(), text));
        } else {
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
        if (isPresent(by, timeout)) {
        	return new ExtendedWebElement(getElement().findElement(by), name, by, driver);
        } else {
        	throw new RuntimeException("Unable to find dynamic element using By: " + by.toString());
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
        	throw new RuntimeException("Unable to find dynamic elements using By: " + by.toString());
        }

        for (WebElement element : webElements) {
            String name = "undefined";
            try {
                name = element.getText();
            } catch (Exception e) {
                /* do nothing */
                LOGGER.debug(e.getMessage(), e.getCause());
            }

            extendedWebElements.add(new ExtendedWebElement(element, name, driver));
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

    //TODO: refactor to fluent waits
    public void waitUntilElementNotPresent(final long timeout) {
        final ExtendedWebElement element = this;

        LOGGER.info(String.format("Wait until element %s disappear", element.getName()));

        final WebDriver drv = getDriver();

        wait = new WebDriverWait(drv, timeout, RETRY_TIME);
        try {
            setImplicitTimeout(0);
            wait.until((Function<WebDriver, Object>) dr -> {
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
        } catch (TimeoutException ex) {
            msg = Messager.ELEMENT_NOT_BECOME_CLICKABLE.info(getName());
            LOGGER.error(ex);
            res = false;
        } catch (Exception e) {
            msg = Messager.ELEMENT_NOT_BECOME_CLICKABLE.info(getName());
            LOGGER.error(e);
            res = false;
        }
        try {
            Screenshot.capture(getDriver(), msg);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
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
        } catch (TimeoutException ex) {
            msg = Messager.ELEMENT_NOT_BECOME_VISIBLE.info(getName());
            LOGGER.error(ex);
            res = false;
        } catch (Exception e) {
            msg = Messager.ELEMENT_NOT_BECOME_VISIBLE.info(getName());
            LOGGER.error(e);
            res = false;
        }
        try {
            Screenshot.capture(getDriver(), msg);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
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

        return new ExtendedWebElement(null, name, by, driver);
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
            currentUrl = driver.getCurrentUrl();
        } else {
            // change for XBox and looks like mobile part
            currentUrl = driver.getTitle();
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

                List<WebElement> all = driver.findElements(By.xpath("//*"));

                List<WebElement> control = driver.findElements(
                        By.xpath("//input[not(contains(@type,'hidden'))] | //button | .//*[contains(@class, 'btn') and not(self::span)] | //select"));

                for (WebElement webElement : control) {
                    ElementInfo elementInfo = getElementInfo(new ExtendedWebElement(webElement, driver));

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
                            elementInfo.setTextInfo(getElementInfo(new ExtendedWebElement(all.get(elementPosition - i), driver)));
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
                    (Map<String, String>) ((RemoteWebDriver) driver).executeScript(ATTRIBUTE_JS, extendedWebElement.getElement()));

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
