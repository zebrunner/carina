/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpClient.Builder;
import org.openqa.selenium.remote.internal.OkHttpClient;

import com.google.common.base.Strings;
import com.qaprosoft.carina.core.foundation.utils.R;

import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;

public class HttpClientFactoryCustom implements HttpClient.Factory {

	private final ConnectionPool pool = new ConnectionPool();

	@Override
	public Builder builder() {
		return new Builder() {
			@Override
			public HttpClient createClient(URL url) {
				okhttp3.OkHttpClient.Builder client = new okhttp3.OkHttpClient.Builder().connectionPool(pool)
						.followRedirects(true).followSslRedirects(true).proxy(proxy)
						.readTimeout(readTimeout.toMillis(), MILLISECONDS)
						.connectTimeout(connectionTimeout.toMillis(), MILLISECONDS);

				String info = url.getUserInfo();
				if (!Strings.isNullOrEmpty(info)) {
					String[] parts = info.split(":", 2);
					String user = parts[0];
					String pass = parts.length > 1 ? parts[1] : null;

					String credentials = Credentials.basic(user, pass);

					client.authenticator((route, response) -> {
						if (response.request().header("Authorization") != null) {
							return null; // Give up, we've already attempted to authenticate.
						}

						return response.request().newBuilder().header("Authorization", credentials).build();
					});
				}

				client.addNetworkInterceptor(chain -> {
					Request request = chain.request();
					Response response = chain.proceed(request);
					return response.code() == 408
							? response.newBuilder().code(500).message("Server-Side Timeout").build()
							: response;
				});
				
				return new OkHttpClient(client.build(), url);
			}
		}.readTimeout(Duration.ofMinutes(R.CONFIG.getInt("appium_http_client_read_timeout_min")));
	}

	@Override
	public void cleanupIdleClients() {
		pool.evictAll();
	}
}
