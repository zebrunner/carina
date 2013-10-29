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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementHandler;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.locator.LocalizedAnnotations;
import com.qaprosoft.carina.core.foundation.webdriver.locator.internal.LocatingElementListHandler;

public class ExtendedFieldDecorator implements FieldDecorator
{
	protected ElementLocatorFactory factory;

	public ExtendedFieldDecorator(ElementLocatorFactory factory)
	{
		this.factory = factory;
	}

	public Object decorate(ClassLoader loader, Field field)
	{
		if (!(ExtendedWebElement.class.isAssignableFrom(field.getType()) || isDecoratableList(field)))
		{
			return null;
		}

		ElementLocator locator = factory.createLocator(field);
		if (locator == null)
		{
			return null;
		}

		if (ExtendedWebElement.class.isAssignableFrom(field.getType()))
		{
			return proxyForLocator(loader, field, locator);
		} else if (List.class.isAssignableFrom(field.getType()))
		{
			return proxyForListLocator(loader, field, locator);
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

		// Type erasure in Java isn't complete. Attempt to discover the generic
		// type of the list.
		Type genericType = field.getGenericType();
		if (!(genericType instanceof ParameterizedType))
		{
			return false;
		}

		Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];

		if (!ExtendedWebElement.class.equals(listType))
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
	protected List<ExtendedWebElement> proxyForListLocator(ClassLoader loader, Field field, ElementLocator locator)
	{
		InvocationHandler handler = new LocatingElementListHandler(locator, field.getName(), new LocalizedAnnotations(field, Configuration.getLocale()).buildBy());
		List<ExtendedWebElement> proxies = (List<ExtendedWebElement>) Proxy.newProxyInstance(loader, new Class[]
		{ List.class }, handler);

		return proxies;
	}
}
