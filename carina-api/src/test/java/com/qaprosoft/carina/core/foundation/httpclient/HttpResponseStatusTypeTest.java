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
package com.qaprosoft.carina.core.foundation.httpclient;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

public class HttpResponseStatusTypeTest {
	
	@Test
	public void testOK_200Method() {
		HttpResponseStatusType type = HttpResponseStatusType.OK_200;
		Assert.assertEquals(type.getCode(), 200);
		Assert.assertEquals(type.getMessage(), "OK");
	}

	@Test
	public void testCREATED_201Method() {
		HttpResponseStatusType type = HttpResponseStatusType.CREATED_201;
		Assert.assertEquals(type.getCode(), 201);
		Assert.assertEquals(type.getMessage(), "Created");
	}

	@Test
	public void testACCEPTED_202Method() {
		HttpResponseStatusType type = HttpResponseStatusType.ACCEPTED_202;
		Assert.assertEquals(type.getCode(), 202);
		Assert.assertEquals(type.getMessage(), "Accepted");
	}

	@Test
	public void testNO_CONTENT_204Method() {
		HttpResponseStatusType type = HttpResponseStatusType.NO_CONTENT_204;
		Assert.assertEquals(type.getCode(), 204);
		Assert.assertEquals(type.getMessage(), "No Content");
	}

	@Test
	public void testBAD_REQUEST_400Method() {
		HttpResponseStatusType type = HttpResponseStatusType.BAD_REQUEST_400;
		Assert.assertEquals(type.getCode(), 400);
		Assert.assertEquals(type.getMessage(), "Bad Request");
	}

	@Test
	public void testUNAUTHORIZED_401Method() {
		HttpResponseStatusType type = HttpResponseStatusType.UNAUTHORIZED_401;
		Assert.assertEquals(type.getCode(), 401);
		Assert.assertEquals(type.getMessage(), "Unauthorized");
	}

	@Test
	public void testFORBIDDEN_403Method() {
		HttpResponseStatusType type = HttpResponseStatusType.FORBIDDEN_403;
		Assert.assertEquals(type.getCode(), 403);
		Assert.assertEquals(type.getMessage(), "Forbidden");
	}

	@Test
	public void testNOT_FOUND_404Method() {
		HttpResponseStatusType type = HttpResponseStatusType.NOT_FOUND_404;
		Assert.assertEquals(type.getCode(), 404);
		Assert.assertEquals(type.getMessage(), "Not Found");
	}

	@Test
	public void testCONFLICT_409Method() {
		HttpResponseStatusType type = HttpResponseStatusType.CONFLICT_409;
		Assert.assertEquals(type.getCode(), 409);
		Assert.assertEquals(type.getMessage(), "Conflict");
	}

	@Test
	public void testUNSUPPORTED_MEDIA_TYPE_415Method() {
		HttpResponseStatusType type = HttpResponseStatusType.UNSUPPORTED_MEDIA_TYPE_415;
		Assert.assertEquals(type.getCode(), 415);
		Assert.assertEquals(type.getMessage(), "Unsupported Media Type");
	}

	@Test
	public void testEXPECTATION_FAILED_417Method() {
		HttpResponseStatusType type = HttpResponseStatusType.EXPECTATION_FAILED_417;
		Assert.assertEquals(type.getCode(), 417);
		Assert.assertEquals(type.getMessage(), "Expectation Failed");
	}

	@Test
	public void testUNPROCESSABLE_ENTITY_422Method() {
		HttpResponseStatusType type = HttpResponseStatusType.UNPROCESSABLE_ENTITY_422;
		Assert.assertEquals(type.getCode(), 422);
		Assert.assertEquals(type.getMessage(), "Unprocessable Entity");
	}

}
