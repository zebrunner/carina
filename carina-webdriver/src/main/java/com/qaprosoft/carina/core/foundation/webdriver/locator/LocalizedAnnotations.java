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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.Annotations;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.resources.L10N;

public class LocalizedAnnotations extends Annotations
{
	private static Pattern L10N_PATTERN = Pattern.compile(SpecialKeywords.L10N_PATTERN);
	
	public LocalizedAnnotations(Field field)
	{
		super(field);
	}


	@Override
	public By buildBy()
	{
		By by = super.buildBy();
		String param = by.toString();
		//replace by using localization pattern
		Matcher matcher = L10N_PATTERN.matcher(param);
		while (matcher.find())
		{
			int start = param.indexOf(SpecialKeywords.L10N + ":") + 5;
			int end = param.indexOf("}");
			String key = param.substring(start, end);

			if(!L10N.isUTF) {
				param = StringUtils.replace(param, matcher.group(), L10N.getText(key));
			} else {
				param = StringUtils.replace(param, matcher.group(), L10N.getUTFText(key));
			}

		}
		by = createBy(param);
		return by;
	}

	private By createBy(String locator)
	{
		if (locator.startsWith("id=")) {
			return By.id(StringUtils.remove(locator, "id="));
		} else if (locator.startsWith("name=")) {
			return By.name(StringUtils.remove(locator, "name="));
		} else if (locator.startsWith("xpath=")) {
			return By.xpath(StringUtils.remove(locator, "xpath="));
		} else if (locator.startsWith("linkText=")) {
			return By.linkText(StringUtils.remove(locator, "linkText="));
		} else if (locator.startsWith("partialLinkText=")) {
			return By.partialLinkText(StringUtils.remove(locator, "partialLinkText="));
		} else if (locator.startsWith("cssSelector=")) {
			return By.cssSelector(StringUtils.remove(locator, "cssSelector="));
		} else if (locator.startsWith("css=")) {
			return By.cssSelector(StringUtils.remove(locator, "css="));
		} else if (locator.startsWith("tagName=")) {
			return By.tagName(StringUtils.remove(locator, "tagName="));
		} else if (locator.startsWith("className=")) {
			return By.className(StringUtils.remove(locator, "className="));
		} else if (locator.startsWith("By.id: ")) {
			return By.id(StringUtils.remove(locator, "By.id: "));
		} else if (locator.startsWith("By.name: ")) {
			return By.name(StringUtils.remove(locator, "By.name: "));
		} else if (locator.startsWith("By.xpath: ")) {
			return By.xpath(StringUtils.remove(locator, "By.xpath: "));
		} else if (locator.startsWith("By.linkText: ")) {
			return By.linkText(StringUtils.remove(locator, "By.linkText: "));
		} else if (locator.startsWith("By.partialLinkText: ")) {
			return By.partialLinkText(StringUtils.remove(locator, "By.partialLinkText: "));
		} else if (locator.startsWith("By.css: ")) {
			return By.cssSelector(StringUtils.remove(locator, "By.css: "));
		} else if (locator.startsWith("By.cssSelector: ")) {
			return By.cssSelector(StringUtils.remove(locator, "By.cssSelector: "));
		} else if (locator.startsWith("By.className: ")) {
			return By.className(StringUtils.remove(locator, "By.className: "));
		} else if (locator.startsWith("By.tagName: ")) {
			return By.tagName(StringUtils.remove(locator, "By.tagName: "));
		}
		
		throw new RuntimeException(String.format("Unable to generate By using locator: '%s'!", locator));
	}
}
