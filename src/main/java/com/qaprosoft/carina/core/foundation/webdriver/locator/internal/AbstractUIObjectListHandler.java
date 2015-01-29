/*
 * Copyright 2015 QAPROSOFT (http://qaprosoft.com/).
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
package com.qaprosoft.carina.core.foundation.webdriver.locator.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedFieldDecorator;
import com.qaprosoft.carina.core.gui.AbstractUIObject;

public class AbstractUIObjectListHandler<T extends AbstractUIObject> implements InvocationHandler
{
	private Class<?> clazz;
	private WebDriver webDriver;
	private final ElementLocator locator;
	private String name;

	private Logger LOGGER = Logger.getLogger(ExtendedFieldDecorator.class);

	public AbstractUIObjectListHandler(Class<?> clazz, WebDriver webDriver, ElementLocator locator, String name)
	{
		this.clazz = clazz;
		this.webDriver = webDriver;
		this.locator = locator;
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	public Object invoke(Object object, Method method, Object[] objects) throws Throwable
	{

		List<WebElement> elements = locator.findElements();
		List<T> uIObjects = new ArrayList<T>();
		int index = 0;
		if (elements != null)
		{
			for (WebElement element : elements)
			{
				T uiObject;
				try
				{
					uiObject = (T) clazz.getConstructor(WebDriver.class, SearchContext.class, Locale.class)
							.newInstance(
						webDriver, element, Configuration.getLocale());
				} catch (NoSuchMethodException e)
				{
					LOGGER.error("Implement appropriate AbstractUIObject constructor for auto-initialization: "
							+ e.getMessage());
					throw new RuntimeException(
							"Implement appropriate AbstractUIObject constructor for auto-initialization: "
									+ e.getMessage(), e);
				}
				uiObject.setName(String.format("%s - %d", name, index++));
				uiObject.setRootElement(element);
				uIObjects.add(uiObject);
			}
		}

		try
		{
			return method.invoke(uIObjects, objects);
		} catch (InvocationTargetException e)
		{
			throw e.getCause();
		}
	}
}