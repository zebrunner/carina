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
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.Annotations;

import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;

public class LocalizedAnnotations extends Annotations
{
	private Locale locale;

	private String xlsPath;

	private String locatorKey;

	public LocalizedAnnotations(Field field, Locale locale)
	{
		super(field);
		this.locale = locale;
		this.xlsPath = getXLSPath(field);
		this.locatorKey = field.getName();
	}

	private String getXLSPath(Field field)
	{
		String path = null;
		try
		{
			ResourceBundle rb = ResourceBundle.getBundle("GUI.mapping");
			path = "GUI/" + rb.getString(field.getDeclaringClass().getCanonicalName());
		} catch (Exception e)
		{
			path = "GUI/" + StringUtils.replace(field.getDeclaringClass().getCanonicalName(), ".", "/") + ".xls";
		}
		return path;
	}

	@Override
	protected By buildByFromDefault()
	{
		String value = XLSParser.parseValue(locatorKey, xlsPath, locale);
		if (!StringUtils.isEmpty(value))
		{
			return createBy(value);
		} else
		{
			return super.buildByFromDefault();
		}
	}

	private By createBy(String locator)
	{
		if (locator.startsWith("id="))
		{
			return By.id(StringUtils.remove(locator, "id="));
		}
		if (locator.startsWith("name="))
		{
			return By.name(StringUtils.remove(locator, "name="));
		}
		if (locator.startsWith("xpath="))
		{
			return By.xpath(StringUtils.remove(locator, "xpath="));
		}
		if (locator.startsWith("linkText="))
		{
			return By.linkText(StringUtils.remove(locator, "linkText="));
		}
		if (locator.startsWith("css="))
		{
			return By.cssSelector(StringUtils.remove(locator, "css="));
		}
		if (locator.startsWith("tagName="))
		{
			return By.tagName(StringUtils.remove(locator, "tagName="));
		}
		// Fall through
		return null;
	}
}
