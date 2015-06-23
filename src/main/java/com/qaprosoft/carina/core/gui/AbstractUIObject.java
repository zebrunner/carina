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
package com.qaprosoft.carina.core.gui;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedFieldDecorator;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocatorFactory;

public abstract class AbstractUIObject extends DriverHelper
{
	protected String name;

	protected WebElement rootElement;

	/**
	 * Initializes UI object using {@link PageFactory}. Whole browser window is used as search context
	 * 
	 * @param driver WebDriver
     */
	public AbstractUIObject(WebDriver driver)
	{
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
	 * @param locale Locale
	 */
	public AbstractUIObject(WebDriver driver, SearchContext searchContext)
	{
		super(driver);
		ExtendedElementLocatorFactory factory = new ExtendedElementLocatorFactory(searchContext);
		PageFactory.initElements(new ExtendedFieldDecorator(factory, driver), this);
		summary.setPrefix(this.getClass().getSimpleName());
	}

	/**
	 *  Verifies if root {@link WebElement} presents on page.
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
	public boolean isUIObjectPresent(int timeout)
	{
		return isElementPresent(name, rootElement, timeout);
	}

	public boolean isUIObjectPresent()
	{
		return isElementPresent(name, rootElement);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public WebElement getRootElement()
	{
		return rootElement;
	}

	public void setRootElement(WebElement rootElement)
	{
		this.rootElement = rootElement;
	}

}
