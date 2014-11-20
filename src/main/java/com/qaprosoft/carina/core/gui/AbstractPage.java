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

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/**
 * All page POJO objects should extend this abstract page to get extra logic.
 * 
 * @author Alex Khursevich
 */
public abstract class AbstractPage extends AbstractUIObject
{
	protected String pageURL = Configuration.get(Parameter.URL);

	public AbstractPage(WebDriver driver)
	{
		super(driver);
	}

	public AbstractPage(WebDriver driver, Locale locale)
	{
		super(driver, locale);
	}

	/**
	 * Opens page according to specified in constructor URL.
	 */
	public void open()
	{
		openURL(pageURL);
	}

	protected void setPageURL(String relURL)
	{
		String baseURL;
		//if(!"NULL".equalsIgnoreCase(Configuration.get(Parameter.ENV)))
		if (!Configuration.get(Parameter.ENV).isEmpty())
		{
			baseURL = Configuration.getEnvArg("base");
		}
		else
		{
			baseURL = Configuration.get(Parameter.URL);
		}
		pageURL = baseURL + relURL;
	}
	
	protected void setPageAbsoluteURL(String url)
	{
		this.pageURL = url;
	}

	public String getPageURL()
	{
		return pageURL;
	}
}
