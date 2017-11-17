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
 * HTTP response types.
 * 
 * @author Alex Khursevich
 */
public enum HttpResponseStatusType
{
	OK_200(200, "OK"),
	CREATED_201(201, "Created"),
	ACCEPTED_202(202, "Accepted"),
	NO_CONTENT_204(204, "No Content"),
	NOT_MODIFIED_304(304, "Not Modified"),
	BAD_REQUEST_400(400, "Bad Request"),
	UNAUTHORIZED_401(401, "Unauthorized"),
	FORBIDDEN_403(403, "Forbidden"),
	NOT_FOUND_404(404, "Not Found"),
	CONFLICT_409(409, "Conflict"),
	UNSUPPORTED_MEDIA_TYPE_415(415, "Unsupported Media Type"),
	EXPECTATION_FAILED_417(417, "Expectation Failed"),
	UNPROCESSABLE_ENTITY_422(422, "Unprocessable Entity");

	private int code;
	private String message;

	HttpResponseStatusType(int code, String message)
	{
		this.code = code;
		this.message = message;
	}

	public int getCode()
	{
		return code;
	}

	public String getMessage()
	{
		return message;
	}
}
