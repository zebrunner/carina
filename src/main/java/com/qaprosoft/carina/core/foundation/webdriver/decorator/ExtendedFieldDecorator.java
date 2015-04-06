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
package com.qaprosoft.carina.core.foundation.webdriver.decorator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.locator.LocalizedAnnotations;
import com.qaprosoft.carina.core.foundation.webdriver.locator.internal.AbstractUIObjectListHandler;
import com.qaprosoft.carina.core.foundation.webdriver.locator.internal.LocatingElementListHandler;
import com.qaprosoft.carina.core.gui.AbstractUIObject;

public class ExtendedFieldDecorator implements FieldDecorator
{
	private Logger LOGGER = Logger.getLogger(ExtendedFieldDecorator.class);

	protected ElementLocatorFactory factory;

	private WebDriver webDriver;

	public ExtendedFieldDecorator(ElementLocatorFactory factory, WebDriver webDriver)
	{
		this.factory = factory;
		this.webDriver = webDriver;
	}

	public Object decorate(ClassLoader loader, Field field)
	{
		if (!(ExtendedWebElement.class.isAssignableFrom(field.getType())
				|| AbstractUIObject.class.isAssignableFrom(field.getType()) || isDecoratableList(field)))
		{
			return null;
		}

		ElementLocator locator;
		try
		{
			locator = factory.createLocator(field);
		} catch (Exception e)
		{
			return null;
		}
		if (locator == null)
		{
			return null;
		}

		if (ExtendedWebElement.class.isAssignableFrom(field.getType()))
		{
			return proxyForLocator(loader, field, locator);
		}
		if (AbstractUIObject.class.isAssignableFrom(field.getType()))
		{
			return proxyForAbstractUIObject(loader, field, locator);
		}
		else if (List.class.isAssignableFrom(field.getType()))
		{
			Type listType = getListType(field);
			if (ExtendedWebElement.class.isAssignableFrom((Class<?>) listType))
			{
				return proxyForListLocator(loader, field, locator);
			}
			else if (AbstractUIObject.class.isAssignableFrom((Class<?>) listType))
			{
				return proxyForListUIObjects(loader, field, locator);
			}
			else
			{
				return null;
			}
		} else
		{
			return null;
		}
	}

	private boolean isDecoratableList(Field field)
	{
		if (!List.class.isAssignableFrom(field.getType()))
		{
			return false;
		}

		Type listType = getListType(field);
		if (listType == null)
		{
			return false;
		}

		try
		{
			if (!(ExtendedWebElement.class.equals(listType) || AbstractUIObject.class.isAssignableFrom((Class<?>) listType)))
			{
				return false;
			}
		} catch (ClassCastException e)
		{
			return false;
		}

		return true;
	}

	protected ExtendedWebElement proxyForLocator(ClassLoader loader, Field field, ElementLocator locator)
	{
		InvocationHandler handler = new LocatingElementHandler(locator);
		WebElement proxy = (WebElement) Proxy.newProxyInstance(loader, new Class[]
		{ WebElement.class, WrapsElement.class, Locatable.class }, handler);
		return new ExtendedWebElement(proxy, field.getName(), new LocalizedAnnotations(field, Configuration.getLocale()).buildBy());
	}

	@SuppressWarnings("unchecked")
	protected <T extends AbstractUIObject> T proxyForAbstractUIObject(ClassLoader loader, Field field,
			ElementLocator locator)
	{
		InvocationHandler handler = new LocatingElementHandler(locator);
		WebElement proxy = (WebElement) Proxy.newProxyInstance(loader, new Class[]
		{ WebElement.class, WrapsElement.class, Locatable.class }, handler);
		Class<? extends AbstractUIObject> clazz = (Class<? extends AbstractUIObject>) field.getType();
		T uiObject;
		try
		{
			uiObject = (T) clazz.getConstructor(WebDriver.class, SearchContext.class, Locale.class).newInstance(
					webDriver, proxy, Configuration.getLocale());
		} catch (NoSuchMethodException e)
		{
			LOGGER.error("Implement appropriate AbstractUIObject constructor for auto-initialization: "
					+ e.getMessage());
			throw new RuntimeException(
					"Implement appropriate AbstractUIObject constructor for auto-initialization: "
							+ e.getMessage(), e);
		} catch (Exception e)
		{
			LOGGER.error("Error creating UIObject: " + e.getMessage());
			throw new RuntimeException("Error creating UIObject: " + e.getMessage(), e);
		}
		uiObject.setName(field.getName());
		uiObject.setRootElement(proxy);
		return uiObject;
	}

	@SuppressWarnings("unchecked")
	protected List<ExtendedWebElement> proxyForListLocator(ClassLoader loader, Field field, ElementLocator locator)
	{
		InvocationHandler handler = new LocatingElementListHandler(locator, field.getName(), new LocalizedAnnotations(field, Configuration.getLocale()).buildBy());
		List<ExtendedWebElement> proxies = (List<ExtendedWebElement>) Proxy.newProxyInstance(loader, new Class[]
		{ List.class }, handler);

		return proxies;
	}

	@SuppressWarnings("unchecked")
	protected <T extends AbstractUIObject> List<T> proxyForListUIObjects(ClassLoader loader, Field field,
			ElementLocator locator)
	{
		InvocationHandler handler = new AbstractUIObjectListHandler<T>((Class<?>) getListType(field), webDriver,
				locator, field.getName());
		List<T> proxies = (List<T>) Proxy.newProxyInstance(loader, new Class[]
		{ List.class }, handler);
		return proxies;
	}

	private Type getListType(Field field)
	{
		// Type erasure in Java isn't complete. Attempt to discover the generic
		// type of the list.
		Type genericType = field.getGenericType();
		if (!(genericType instanceof ParameterizedType))
		{
			return null;
		}

		return ((ParameterizedType) genericType).getActualTypeArguments()[0];
	}
}