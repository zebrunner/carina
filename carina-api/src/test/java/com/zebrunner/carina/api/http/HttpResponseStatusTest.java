/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.api.http;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpResponseStatusTest {

    @Test
    public void testGetCode() {
        int expectedCode = 200;
        HttpResponseStatus status = new HttpResponseStatus(expectedCode, null);
        Assert.assertEquals(status.getCode(), expectedCode);
    }

    @Test
    public void testGetMessage() {
        String expectedMessage = "expected message";
        HttpResponseStatus status = new HttpResponseStatus(201, expectedMessage);
        Assert.assertEquals(status.getMessage(), expectedMessage);
    }

    @Test
    public void testWithMessageOverride() {
        int expectedCode = 200;
        String initialMessage = "initial message";
        String messageOverride = "overridden message";
        HttpResponseStatus status = new HttpResponseStatus(expectedCode, initialMessage).withMessageOverride(messageOverride);
        Assert.assertEquals(status.getCode(), expectedCode);
        Assert.assertEquals(status.getMessage(), messageOverride);
    }

    @Test
    public void testWithNullMessageOverride() {
        int expectedCode = 201;
        String initialMessage = "initial message";
        HttpResponseStatus status = new HttpResponseStatus(expectedCode, initialMessage);
        HttpResponseStatus overriddenMessageStatus = status.withMessageOverride(null);
        Assert.assertSame(status, overriddenMessageStatus);
    }

    @Test
    public void testWithEmptyMessageOverride() {
        int expectedCode = 201;
        String initialMessage = "initial message";
        HttpResponseStatus status = new HttpResponseStatus(expectedCode, initialMessage);
        HttpResponseStatus overriddenMessageStatus = status.withMessageOverride("");
        Assert.assertSame(status, overriddenMessageStatus);
    }

    @Test
    public void testWithSameMessageOverride() {
        int expectedCode = 201;
        String initialMessage = "initial message";
        HttpResponseStatus status = new HttpResponseStatus(expectedCode, initialMessage);
        HttpResponseStatus overriddenMessageStatus = status.withMessageOverride(initialMessage);
        Assert.assertSame(status, overriddenMessageStatus);
    }
}
