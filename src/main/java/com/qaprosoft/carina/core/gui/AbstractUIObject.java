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
package com.qaprosoft.carina.core.gui;

import java.util.Locale;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.DriverHelper;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedFieldDecorator;
import com.qaprosoft.carina.core.foundation.webdriver.locator.ExtendedElementLocatorFactory;

public abstract class AbstractUIObject extends DriverHelper
{
	protected Locale locale;

	public AbstractUIObject(WebDriver driver)
	{
		this(driver, null);
	}

	public AbstractUIObject(WebDriver driver, Locale locale)
	{
		super(driver);
		this.locale = locale != null ? locale : Configuration.getLocale();
		ExtendedElementLocatorFactory factory = new ExtendedElementLocatorFactory(driver, this.locale);
		PageFactory.initElements(new ExtendedFieldDecorator(factory), this);
		summary.setPrefix(this.getClass().getSimpleName());
	}
}
