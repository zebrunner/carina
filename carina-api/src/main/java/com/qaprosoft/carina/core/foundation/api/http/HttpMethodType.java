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
package com.qaprosoft.carina.core.foundation.api.http;

/*
 * HTTP method types.
 * 
 * @author Alex Khursevich
 */
public enum HttpMethodType
{
	HEAD(1, "HEAD"),
	GET(2, "GET"),
	PUT(3, "PUT"),
	POST(4, "POST"),
	DELETE(5, "DELETE"),
	PATCH(6, "PATCH");

	private int code;
	private String name;

	HttpMethodType(int code, String name)
	{
		this.code = code;
		this.name = name;
	}

	public int getCode()
	{
		return code;
	}

	public String getName()
	{
		return name;
	}

	public HttpMethodType get(String name)
	{
		return valueOf(name);
	}
}
