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
package com.qaprosoft.carina.core.foundation.webdriver.locator.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

public class LocatingElementListHandler implements InvocationHandler
{
	private final ElementLocator locator;
	private String name;
	private By by;

	public LocatingElementListHandler(ElementLocator locator, String name, By by)
	{
		this.locator = locator;
		this.name = name;
		this.by = by;
	}

	public Object invoke(Object object, Method method, Object[] objects) throws Throwable
	{

		List<WebElement> elements = locator.findElements();
		List<ExtendedWebElement> extendedWebElements = null;
		if (elements != null)
		{
			extendedWebElements = new ArrayList<ExtendedWebElement>();
			for (WebElement element : elements)
			{
				extendedWebElements.add(new ExtendedWebElement(element, name, by));
			}
		}

		try
		{
			return method.invoke(extendedWebElements, objects);
		} 
		catch (InvocationTargetException e)
		{
			throw e.getCause();
		}
	}
}
