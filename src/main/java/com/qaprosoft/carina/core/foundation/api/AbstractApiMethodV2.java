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
package com.qaprosoft.carina.core.foundation.api;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.skyscreamer.jsonassert.JSONCompareMode;

import com.jayway.restassured.response.Response;
import com.qaprosoft.apitools.builder.PropertiesProcessorMain;
import com.qaprosoft.apitools.message.TemplateMessage;
import com.qaprosoft.apitools.validation.JsonValidator;

public abstract class AbstractApiMethodV2 extends AbstractApiMethod
{
	private Properties properties;
	private String rqPath;
	private String rsPath;
	private String actualRsBody;

	public AbstractApiMethodV2(String rqPath, String rsPath, String propertiesPath)
	{
		super("application/json");
		setHeaders("Accept=*/*");

		URL baseResource = ClassLoader.getSystemResource(propertiesPath);
		if (baseResource != null)
		{
			properties = new Properties();
			try
			{
				properties.load(baseResource.openStream());
			} catch (IOException e)
			{
				throw new RuntimeException("Properties can't be loaded by path: " + propertiesPath, e);
			}
			LOGGER.info("Base properties loaded: " + propertiesPath);
		} else
		{
			throw new RuntimeException("Properties can't be found by path: " + propertiesPath);
		}
		properties = PropertiesProcessorMain.processProperties(properties);
		this.rqPath = rqPath;
		this.rsPath = rsPath;
	}

	public AbstractApiMethodV2(String rqPath, String rsPath, Properties properties)
	{
		super("application/json");
		setHeaders("Accept=*/*");
		if (properties != null)
		{
			this.properties = PropertiesProcessorMain.processProperties(properties);
		}
		this.rqPath = rqPath;
		this.rsPath = rsPath;
	}

	public AbstractApiMethodV2(String rqPath, String rsPath)
	{
		this(rqPath, rsPath, (Properties) null);
	}

	@Override
	@Deprecated
	public String call()
	{
		if (rqPath != null)
		{
			TemplateMessage tm = new TemplateMessage();
			tm.setTemplatePath(rqPath);
			tm.setPropertiesStorage(properties);
			setBodyContent(tm.getMessageText());
		}
		String rs = super.call();
		actualRsBody = rs;
		return rs;
	}

	@Override
	public Response callAPI()
	{
		if (rqPath != null)
		{
			TemplateMessage tm = new TemplateMessage();
			tm.setTemplatePath(rqPath);
			tm.setPropertiesStorage(properties);
			setBodyContent(tm.getMessageText());
		}
		Response rs = super.callAPI();
		actualRsBody = rs.asString();
		return rs;
	}
	
	public void addProperty(String key, Object value)
	{
		if (properties == null)
		{
			throw new RuntimeException("API method properties are not initialized!");
		}
		properties.put(key, value);
	}

	public void removeProperty(String key)
	{
		if (properties == null)
		{
			throw new RuntimeException("API method properties are not initialized!");
		}
		properties.remove(key);
	}

	public Properties getProperties()
	{
		return properties;
	}

	public void validateResponse(JSONCompareMode mode)
	{
		if (rsPath == null)
		{
			throw new RuntimeException("Please specify rsPath to make Response body validation");
		}
		if (properties == null)
		{
			properties = new Properties();
		}
		if (actualRsBody == null)
		{
			throw new RuntimeException("Actual response body is null. Pleae make API call before validation response");
		}
		TemplateMessage tm = new TemplateMessage();
		tm.setTemplatePath(rsPath);
		tm.setPropertiesStorage(properties);
		String expectedRs = tm.getMessageText();
		JsonValidator.validateJson(expectedRs, actualRsBody, mode);
	}

	public void validateResponse()
	{
		validateResponse(JSONCompareMode.STRICT);
	}

	public void validateResponseAgainstJSONSchema(String schemaPath)
	{
		if (actualRsBody == null)
		{
			throw new RuntimeException("Actual response body is null. Pleae make API call before validation response");
		}
		TemplateMessage tm = new TemplateMessage();
		tm.setTemplatePath(schemaPath);
		String schema = tm.getMessageText();
		JsonValidator.validateJsonAgainstSchema(schema, actualRsBody);
	}
	
	public void setAuth(String jSessionId)
	{
		addCookie("pfJSESSIONID", jSessionId);
	}
}
