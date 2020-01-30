/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver.httpclient;

import java.io.IOException;

import org.apache.log4j.Logger;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

public class HttpClientLoggingInterceptor implements Interceptor {

	private final static Logger LOGGER = Logger.getLogger(HttpClientLoggingInterceptor.class);

	@Override
	public Response intercept(Interceptor.Chain chain) throws IOException {
		Request request = chain.request();

		long t1 = System.nanoTime();
		LOGGER.info(
				String.format("Sending REQUEST %s %s on %s%n%s%n%s", request.method(), request.url(), chain.connection(), request.headers(), bodyToString(request)));

		Response response = chain.proceed(request);

		long t2 = System.nanoTime();
		LOGGER.info(String.format("Received RESPONSE for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d,
				response.headers()));

		return response;
	}
	
	private static String bodyToString(final Request request) {

		try {
			final Request copy = request.newBuilder().build();
			final Buffer buffer = new Buffer();
			if(copy.body()!=null) {
				copy.body().writeTo(buffer);
				return buffer.readUtf8();
			}
			else
			{
				buffer.close();
				return "NO BODY";
			}
		} catch (final IOException e) {
			return "did not work";
		}
	}

}