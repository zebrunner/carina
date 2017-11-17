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
package com.qaprosoft.carina.proxy;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

public class SystemProxy {
	
	protected static final Logger LOGGER = Logger.getLogger(SystemProxy.class);

	public static void setupProxy()
	{
		String proxyHost = Configuration.get(Parameter.PROXY_HOST);
		String proxyPort = Configuration.get(Parameter.PROXY_PORT);
		
		List<String> protocols = Arrays.asList(Configuration.get(Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));
		
		if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()
				&& Configuration.getBoolean(Parameter.PROXY_SET_TO_SYSTEM))
		{
			if (protocols.contains("http")) {
				LOGGER.info(String.format("HTTP client will use http: %s:%s", proxyHost, proxyPort));

				System.setProperty("http.proxyHost", proxyHost);
				System.setProperty("http.proxyPort", proxyPort);
			}

			if (protocols.contains("https")) {
				LOGGER.info(String.format("HTTP client will use https proxies: %s:%s", proxyHost, proxyPort));

				System.setProperty("https.proxyHost", proxyHost);
				System.setProperty("https.proxyPort", proxyPort);
			}

			if (protocols.contains("ftp")) {
				LOGGER.info(String.format("HTTP client will use ftp proxies: %s:%s", proxyHost, proxyPort));

				System.setProperty("ftp.proxyHost", proxyHost);
				System.setProperty("ftp.proxyPort", proxyPort);
			}

			if (protocols.contains("socks")) {
				LOGGER.info(String.format("HTTP client will use socks proxies: %s:%s", proxyHost, proxyPort));
				System.setProperty("socksProxyHost", proxyHost);
				System.setProperty("socksProxyPort", proxyPort);
			}
		}
	}
	
}
