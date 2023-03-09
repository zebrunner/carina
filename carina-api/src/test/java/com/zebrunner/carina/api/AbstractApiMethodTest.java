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
package com.zebrunner.carina.api;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.api.mock.apimethod.AutoReplaceUrlPartsMethod;
import com.zebrunner.carina.api.mock.apimethod.NoPlaceholdersInURLMethod;
import com.zebrunner.carina.api.mock.apimethod.PutDocMethod;

public class AbstractApiMethodTest {

    @Test
    public void testGetRequestBodyMethod() {
        PutDocMethod putDocMethod = new PutDocMethod();
        final String bodyContent = "{\"key\": \"value\"}";
        putDocMethod.setBodyContent(bodyContent);
        Assert.assertEquals(putDocMethod.getRequestBody(), bodyContent);
    }

    @Test
    public void testNoPlacehodlersInURL() {
        NoPlaceholdersInURLMethod api = new NoPlaceholdersInURLMethod();
        final String expectedMethodPath = "https://jsonplaceholder.typicode.com/users/1";
        Assert.assertEquals(api.methodPath, expectedMethodPath);
    }

    @Test
    public void testAutoReplacementInURL() {
        final String id = "1";
        R.CONFIG.put("some_id", id);
        AutoReplaceUrlPartsMethod method = new AutoReplaceUrlPartsMethod();
        final String expectedMethodPath = Configuration.getEnvArg("base_url") + "/mock/part/" + id;
        Assert.assertEquals(method.getMethodPath(), expectedMethodPath);
    }
}
