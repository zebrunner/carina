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
package com.qaprosoft.carina.core.foundation.http;

import com.jayway.restassured.specification.RequestSpecification;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/*
 * HttpClient - sends HTTP request with specified parameters and returns response.
 * 
 * @author Alex Khursevich
 */
public class HttpClient
{
	private static final boolean LOG_ALL = Configuration.getBoolean(Parameter.LOG_ALL_JSON);

	public static String send(RequestSpecification request, String methodPath, HttpMethodType methodType)
	{
		String response = null;
		if (LOG_ALL)
		{
			request.log().all();
			request.expect().log().all();
		}

		switch (methodType)
		{
		case HEAD:
			response = request.head(methodPath).asString();
			break;
		case GET:
			response = request.get(methodPath).asString();
			break;
		case PUT:
			response = request.put(methodPath).asString();
			break;
		case POST:
			response = request.post(methodPath).asString();
			break;
		case DELETE:
			response = request.delete(methodPath).asString();
		default:
			throw new RuntimeException("MethodType is not specified for the API method: " + methodPath);
		}

		return response;
	}
}
