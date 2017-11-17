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

import static com.jayway.restassured.RestAssured.given;

import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.xml.HasXPath;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.config.SSLConfig;
import com.jayway.restassured.filter.log.RequestLoggingFilter;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.qaprosoft.carina.core.foundation.api.http.HttpClient;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.qaprosoft.carina.core.foundation.api.log.LoggingOutputStream;
import com.qaprosoft.carina.core.foundation.api.ssl.NullHostnameVerifier;
import com.qaprosoft.carina.core.foundation.api.ssl.NullX509TrustManager;
import com.qaprosoft.carina.core.foundation.api.ssl.SSLContextBuilder;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;

@SuppressWarnings("deprecation")
public abstract class AbstractApiMethod extends HttpClient
{
	protected static final Logger LOGGER = Logger.getLogger(AbstractApiMethod.class);
	private StringBuilder bodyContent = null;
	protected String methodPath = null;
	protected HttpMethodType methodType = null;
	protected Object response;
	public RequestSpecification request;
	private boolean logRequest = Configuration.getBoolean(Parameter.LOG_ALL_JSON);
	private boolean logResponse = Configuration.getBoolean(Parameter.LOG_ALL_JSON);

	public AbstractApiMethod()
	{
		init(getClass());
		bodyContent = new StringBuilder();
		request = given();
		request.contentType(ContentType.TEXT);
	}
	
	public AbstractApiMethod(String contentType)
	{
		init(getClass());
		bodyContent = new StringBuilder();
		request = given();
		request.contentType(contentType);
	}

	@SuppressWarnings("rawtypes")
	private void init(Class clazz)
	{
		String typePath = R.API.get(clazz.getSimpleName());
		if (typePath == null)
		{
			throw new RuntimeException("Method type and path are not specified for: " + clazz.getSimpleName());
		}
		if(typePath.contains(":"))
		{
			methodType = HttpMethodType.valueOf(typePath.split(":")[0]);
			methodPath = typePath.split(":")[1];
		}
		else
		{
			methodType = HttpMethodType.valueOf(typePath);
		}
		
	}

	public void setHeaders(String... headerKeyValues)
	{
		for (String headerKeyValue : headerKeyValues)
		{
			String key = headerKeyValue.split("=")[0];
			String value = headerKeyValue.split("=")[1];
			request.header(key, value);
		}
	}

	public void addUrlParameter(String key, String value)
	{
		if (value != null)
		{
			request.queryParam(key, value);
		}
	}

	public void addParameter(String key, String value)
	{
		request.param(key, value.replace(" ", "%20"));
	}

	public void addParameterIfNotNull(String key, String value)
	{
		if (value != null)
		{
			this.addParameter(key, value);
		}
	}
	
	public void addBodyParameter(String key, Object value)
	{
		if (bodyContent.length() != 0)
		{
			bodyContent.append("&");
		}
		bodyContent.append(key + "=" + value);
	}

	protected void addBodyParameterIfNotNull(String key, Object value)
	{
		if (value != null)
		{
			addBodyParameter(key, value);
		}
	}
	
	public void addCookie(String key, String value)
	{
		request.given().cookie(key, value);
	}
	
	public void addCookies(Map<String, String> cookies)
	{
		request.given().cookies(cookies);
	}

	public void replaceUrlPlaceholder(String placeholder, String value)
	{
		if (value != null)
		{
			methodPath = methodPath.replace("${" + placeholder + "}", value);
		}
		else
		{
			methodPath = methodPath.replace("${" + placeholder + "}", "");
			methodPath = StringUtils.removeEnd(methodPath, "/");
		}
	}

	public void expectResponseStatus(HttpResponseStatusType status)
	{
		request.expect().statusCode(status.getCode());
		request.expect().statusLine(Matchers.containsString(status.getMessage()));
	}

	public <T> void expectResponseContains(Matcher<T> key, Matcher<T> value)
	{
		request.expect().body(key, value);
	}

	public void expectValueByXpath(String xPath, String value)
	{
		request.expect().body(Matchers.hasXPath(xPath), Matchers.containsString(value));
	}

	public void expectValueByXpath(String xPath, String value1, String value2)
	{
		request.expect().body(Matchers.hasXPath(xPath), Matchers.anyOf(Matchers.containsString(value1), Matchers.containsString(value2)));
	}

	public <T> void expectResponseContains(Matcher<T> value)
	{
		request.expect().body(value);
	}

	public <T> void expectResponseContains(String key, Matcher<T> value)
	{
		request.expect().body(key, value);
	}

	public <T> void expectResponseContainsXpath(String xPath)
	{
		request.expect().body(HasXPath.hasXPath(xPath));
	}
	
	public Response callAPI()
	{
		if (bodyContent.length() != 0)
			request.body(bodyContent.toString());

		Response rs = null;

		PrintStream ps = null;
		if (logRequest || logResponse)
		{
			ps = new PrintStream(new LoggingOutputStream(LOGGER, Level.INFO));
		}

		if (logRequest)
			request.filter(new RequestLoggingFilter(ps));

		if (logResponse)
			request.filter(new ResponseLoggingFilter(ps));
		try
		{
			rs = HttpClient.send(request, methodPath, methodType);
		} finally
		{
			if (ps != null)
				ps.close();
		}
		return rs;
	}
	
	/**
	 * @deprecated use {@link #callAPI()} instead.  
	 * 
	 * @return String
	 */
	@Deprecated
	public String call()
	{
		Response response = callAPI();
		return response != null ? response.asString() : null;
	}
	
	public void expectInResponse(Matcher<?> matcher)
	{
		request.expect().body(matcher);
	}
	
	public void expectInResponse(String locator, Matcher<?> value)
	{
		request.expect().body(locator, value);
	}

	public String getMethodPath()
	{
		return methodPath;
	}

	public void setMethodPath(String methodPath)
	{
		RestAssured.reset();
		this.methodPath = methodPath;
	}

	public void setBodyContent(String content)
	{
		this.bodyContent = new StringBuilder(content);
	}
	
	public RequestSpecification getRequest()
	{
		return request;
	}

	public void setLogRequest(boolean logRequest)
	{
		this.logRequest = logRequest;
	}

	public void setLogResponse(boolean logResponse)
	{
		this.logResponse = logResponse;
	}

	public void ignoreSSLCerts()
	{
		SSLContext sslContext = null;
		try
		{
			sslContext = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		TrustManager[] trustManagerArray = { new NullX509TrustManager() };
		try
		{
			sslContext.init(null, trustManagerArray, null);
		} catch (KeyManagementException e)
		{
			throw new RuntimeException(e);
		}

		SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, new NullHostnameVerifier());
		SSLConfig sslConfig = new SSLConfig();
		sslConfig = sslConfig.sslSocketFactory(socketFactory);
		sslConfig = sslConfig.x509HostnameVerifier(new NullHostnameVerifier());

		RestAssuredConfig cfg = new RestAssuredConfig();
		cfg = cfg.sslConfig(sslConfig);
		request = request.config(cfg);
	}

	public void setSSLContext(SSLContext sslContext)
	{
		SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext);
		SSLConfig sslConfig = new SSLConfig();
		sslConfig = sslConfig.sslSocketFactory(socketFactory);

		RestAssuredConfig cfg = new RestAssuredConfig();
		cfg = cfg.sslConfig(sslConfig);
		request = request.config(cfg);
	}

	public void setDefaultTLSSupport()
	{
		setSSLContext(new SSLContextBuilder(true).createSSLContext());
	}

}
