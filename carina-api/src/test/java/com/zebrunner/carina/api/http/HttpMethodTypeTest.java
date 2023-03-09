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

public class HttpMethodTypeTest {
    @Test
    public void testGetHttpMethod() {
        HttpMethodType type = HttpMethodType.HEAD;
        Assert.assertEquals(type.get("POST"), HttpMethodType.POST);
    }

    @Test
    public void testHeadMethod() {
        HttpMethodType type = HttpMethodType.HEAD;
        Assert.assertEquals(type.getCode(), 1);
        Assert.assertEquals(type.getName(), "HEAD");
    }

    @Test
    public void testGetMethod() {
        HttpMethodType type = HttpMethodType.GET;
        Assert.assertEquals(type.getCode(), 2);
        Assert.assertEquals(type.getName(), "GET");
    }

    @Test
    public void testPutMethod() {
        HttpMethodType type = HttpMethodType.PUT;
        Assert.assertEquals(type.getCode(), 3);
        Assert.assertEquals(type.getName(), "PUT");
    }

    @Test
    public void testPostMethod() {
        HttpMethodType type = HttpMethodType.POST;
        Assert.assertEquals(type.getCode(), 4);
        Assert.assertEquals(type.getName(), "POST");
    }

    @Test
    public void testDeleteMethod() {
        HttpMethodType type = HttpMethodType.DELETE;
        Assert.assertEquals(type.getCode(), 5);
        Assert.assertEquals(type.getName(), "DELETE");
    }

    @Test
    public void testPatchMethod() {
        HttpMethodType type = HttpMethodType.PATCH;
        Assert.assertEquals(type.getCode(), 6);
        Assert.assertEquals(type.getName(), "PATCH");
    }
    
    @Test
    public void testOptionsMethod() {
        HttpMethodType type = HttpMethodType.OPTIONS;
        Assert.assertEquals(type.getCode(), 7);
        Assert.assertEquals(type.getName(), "OPTIONS");
    }
}
