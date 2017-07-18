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
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Predicate;

import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * The default element locator, which will lazily locate an element or an
 * element list on a page. This class is designed for use with the
 * {@link org.openqa.selenium.support.PageFactory} and understands the
 * annotations {@link org.openqa.selenium.support.FindBy} and
 * {@link org.openqa.selenium.support.CacheLookup}.
 */
public class ExtendedElementLocator implements ElementLocator
{
	private final SearchContext searchContext;
	private final boolean shouldCache;
	private final By by;
	private WebElement cachedElement;
	private List<WebElement> cachedElementList;
	
	private Boolean isPredicate;

	/**
	 * Creates a new element locator.
	 * 
	 * @param searchContext
	 *            The context to use when finding the element
	 * @param field
	 *            The field on the Page Object that will hold the located value
	 */
	public ExtendedElementLocator(SearchContext searchContext, Field field)
	{
		this.searchContext = searchContext;
		LocalizedAnnotations annotations = new LocalizedAnnotations(field);
		shouldCache = annotations.isLookupCached();
		by = annotations.buildBy();
		
		isPredicate = false;
        if (field.isAnnotationPresent(Predicate.class)) {
        	isPredicate = field.getAnnotation(Predicate.class).enabled(); 
        }
	}

	/**
	 * Find the element.
	 */
	public WebElement findElement()
	{
		if (cachedElement != null && shouldCache)
		{
			return cachedElement;
		}

		WebElement element = null;
		if (!isPredicate) { 
			element = searchContext.findElement(by);
		} else {
			WebDriver drv = DriverPool.getDriver();
			if (drv instanceof IOSDriver) {
				element = drv.findElement(MobileBy.iOSNsPredicateString(getLocator(by)));
			} else if (drv instanceof AndroidDriver) {
				element = ((AndroidDriver) drv).findElementByAndroidUIAutomator(getLocator(by));
			} else {
				throw new RuntimeException("Unable to to detect valid driver for searching " + by.toString());
			}
		}
		
		if (shouldCache)
		{
			cachedElement = element;
		}		
		return element;
	}

	/**
	 * Find the element list.
	 */
	public List<WebElement> findElements()
	{
		if (cachedElementList != null && shouldCache)
		{
			return cachedElementList;
		}

		List<WebElement> elements = null;
		if (!isPredicate) {
			elements = searchContext.findElements(by);
		}  else {
			WebDriver drv = DriverPool.getDriver();
			if (drv instanceof IOSDriver) {
				elements = drv.findElements(MobileBy.iOSNsPredicateString(getLocator(by)));	
			} else if (drv instanceof AndroidDriver) {
				elements = ((AndroidDriver) drv).findElementsByAndroidUIAutomator(getLocator(by));
			} else {
				throw new RuntimeException("Unable to to detect valid driver for searching " + by.toString());
			}
		}
		
		if (shouldCache)
		{
			cachedElementList = elements;
		}

		return elements;
	}
	
	
	private String getLocator(By by)
	{
		String locator = by.toString();
		
		if (locator.startsWith("id=")) {
			return StringUtils.remove(locator, "id=");
		} else if (locator.startsWith("name=")) {
			return StringUtils.remove(locator, "name=");
		} else if (locator.startsWith("xpath=")) {
			return StringUtils.remove(locator, "xpath=");
		} else if (locator.startsWith("linkText=")) {
			return StringUtils.remove(locator, "linkText=");
		} else if (locator.startsWith("partialLinkText=")) {
			return StringUtils.remove(locator, "partialLinkText=");
		} else if (locator.startsWith("css=")) {
			return StringUtils.remove(locator, "css=");
		} else if (locator.startsWith("tagName=")) {
			return StringUtils.remove(locator, "tagName=");
		} else if (locator.startsWith("By.id: ")) {
			return StringUtils.remove(locator, "By.id: ");
		} else if (locator.startsWith("By.name: ")) {
			return StringUtils.remove(locator, "By.name: ");
		} else if (locator.startsWith("By.xpath: ")) {
			return StringUtils.remove(locator, "By.xpath: ");
		} else if (locator.startsWith("By.linkText: ")) {
			return StringUtils.remove(locator, "By.linkText: ");
		} else if (locator.startsWith("By.partialLinkText: ")) {
			return StringUtils.remove(locator, "By.partialLinkText: ");
		} else if (locator.startsWith("By.css: ")) {
			return StringUtils.remove(locator, "By.css: ");
		} else if (locator.startsWith("By.cssSelector: ")) {
			return StringUtils.remove(locator, "By.cssSelector: ");
		} else if (locator.startsWith("By.tagName: ")) {
			return StringUtils.remove(locator, "By.tagName: ");
		}
		
		throw new RuntimeException(String.format("Unable to generate By using locator: '%s'!", locator));
	}
}
