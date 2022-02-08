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
package com.qaprosoft.carina.core.foundation.api;

import com.qaprosoft.apitools.validation.mock.method.DeleteUserMethod;
import com.qaprosoft.carina.core.foundation.api.ssl.PutDocMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AbstractApiMethodTest {

    private final static String BODY_CONTENT = "{\"key\": \"value\"}";
    private final static String EXPECTED_METHOD_PATH_URL = "https://jsonplaceholder.typicode.com/users/1";

    @Test
    public void testGetRequestBodyMethod() {
        PutDocMethod putDocMethod = new PutDocMethod();
        putDocMethod.setBodyContent(BODY_CONTENT);
        Assert.assertEquals(putDocMethod.getRequestBody(), BODY_CONTENT);
    }

    @Test
    public void testInitMethod() {
        DeleteUserMethod api = new DeleteUserMethod();
        Assert.assertEquals(api.methodPath, EXPECTED_METHOD_PATH_URL);
    }
}
