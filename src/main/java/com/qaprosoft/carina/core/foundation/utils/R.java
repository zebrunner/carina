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
package com.qaprosoft.carina.core.foundation.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * R - loads properties from resource files.
 * 
 * @author Alexey Khursevich (hursevich@gmail.com)
 */
public enum R
{
	API("api.properties"),
	
	CONFIG("config.properties"),

	TESTDATA("testdata.properties"),

	EMAIL("email.properties"),

	REPORT("report.properties"),

	DATABASE("database.properties");

	private static final Logger LOGGER = Logger.getLogger(R.class);

	private String resourceFile;

	private static Map<String, Properties> propertiesKeeper = new HashMap<String, Properties>();

	static
	{
		for (R resource : values())
		{
			try
			{
				Properties prop = new Properties();
				prop.load(ClassLoader.getSystemResource(resource.resourceFile).openStream());

				// Ovveride properties
				try
				{
					prop.load(ClassLoader.getSystemResource("_" + resource.resourceFile).openStream());
					LOGGER.info("Properties: " + resource.resourceFile + " were overriden.");
				} catch (Exception e)
				{
				}

				propertiesKeeper.put(resource.resourceFile, prop);
			} catch (IOException e)
			{
				LOGGER.error("Properties: " + resource.resourceFile + " not found initialized!");
			}
		}
	}

	R(String resourceKey)
	{
		this.resourceFile = resourceKey;
	}

	// Will override config property if system property is specified.
	public String get(String key)
	{
		String sysProperty = System.getProperty(key);
		String cnfgProperty = propertiesKeeper.get(resourceFile).getProperty(key);
		String value = !StringUtils.isEmpty(sysProperty) ? sysProperty : cnfgProperty;
		
		if (value == null) {
			value = "";
		}
		return value;
	}

	public int getInt(String key)
	{
		return Integer.parseInt(get(key));
	}

	public long getLong(String key)
	{
		return Long.parseLong(get(key));
	}

	public double getDouble(String key)
	{
		return Double.parseDouble(get(key));
	}

	public boolean getBoolean(String key)
	{
		return Boolean.valueOf(get(key));
	}

	public static String getResourcePath(String resource)
	{
		String path = StringUtils.removeStart(ClassLoader.getSystemResource(resource).getPath(), "/");
		path = StringUtils.replaceChars(path, "/", "\\");
		path = StringUtils.replaceChars(path, "!", "");
		return path;
	}
}
