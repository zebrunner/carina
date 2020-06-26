/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.Messager;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ElementLoadingStrategy;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedFieldDecorator;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocatorFactory;

public abstract class AbstractUIObject extends DriverHelper {
    protected String name;

    protected WebElement rootElement;
    protected By rootBy;

    protected ExtendedWebElement uiLoadedMarker;

    private ElementLoadingStrategy loadingStrategy = ElementLoadingStrategy.valueOf(Configuration.get(Parameter.ELEMENT_LOADING_STRATEGY));

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
        ExtendedElementLocatorFactory factory = new ExtendedElementLocatorFactory(searchContext, (driver != searchContext) ? true : false);
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
    public boolean isUIObjectPresent(long timeout) {
        switch (loadingStrategy) {
        case BY_PRESENCE:
            return waitUntil(ExpectedConditions.presenceOfElementLocated(rootBy), timeout);
        case BY_VISIBILITY:
            return waitUntil(ExpectedConditions.visibilityOfElementLocated(rootBy), timeout);
        default:
            return waitUntil(ExpectedConditions.presenceOfElementLocated(rootBy), timeout);
        }
    }

    public boolean isUIObjectPresent() {
        return isUIObjectPresent(Configuration.getInt(Parameter.EXPLICIT_TIMEOUT));
    }

    public ExtendedWebElement getUiLoadedMarker() {
        return uiLoadedMarker;
    }

    public void setUiLoadedMarker(ExtendedWebElement uiLoadedMarker) {
        this.uiLoadedMarker = uiLoadedMarker;
    }

    public ElementLoadingStrategy getLoadingStrategy() {
        return loadingStrategy;
    }

    public void setLoadingStrategy(ElementLoadingStrategy loadingStrategy) {
        this.loadingStrategy = loadingStrategy;
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

    public void setRootElement(WebElement element) {
        this.rootElement = element;
    }

    public By getRootBy() {
        return rootBy;
    }

    public void setRootBy(By rootBy) {
        this.rootBy = rootBy;
    }

    /**
     * Checks presence of UIObject root element on the page and throws Assertion error in case if it's missing
     */
    public void assertUIObjectPresent() {
        assertUIObjectPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Checks presence of UIObject root element on the page and throws Assertion error in case if it's missing
     * 
     * @param timeout long
     */
    public void assertUIObjectPresent(long timeout) {
        if (!isUIObjectPresent(timeout)) {
            Assert.fail(Messager.UI_OBJECT_NOT_PRESENT.getMessage(getNameWithLocator()));
        }
    }

    /**
     * Checks missing of UIObject root element on the page and throws Assertion error in case if it presents
     */
    public void assertUIObjectNotPresent() {
        assertUIObjectNotPresent(EXPLICIT_TIMEOUT);
    }

    /**
     * Checks missing of UIObject root element on the page and throws Assertion error in case if it presents
     * 
     * @param timeout long
     */
    public void assertUIObjectNotPresent(long timeout) {
        if (isUIObjectPresent(timeout)) {
            Assert.fail(Messager.UI_OBJECT_PRESENT.getMessage(getNameWithLocator()));
        }
    }

    private String getNameWithLocator() {
        return rootBy != null ? name + String.format(" (%s)", rootBy) : name + " (n/a)";
    }

}