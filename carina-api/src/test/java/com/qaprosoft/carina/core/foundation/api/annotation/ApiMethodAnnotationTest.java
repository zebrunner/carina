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
package com.qaprosoft.carina.core.foundation.api.annotation;

import com.qaprosoft.carina.core.foundation.api.http.ContentTypeEnum;
import io.restassured.internal.RequestSpecificationImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ApiMethodAnnotationTest {
    private ApiMethodWAnnotation m;

    @BeforeClass
    public void init() {
        m = new ApiMethodWAnnotation();
    }

    @Test
    public void testEndpoint() {
        Assert.assertEquals(m.getMethodPath(), "http://test.api.com", "Method path from annotation not as expected");
    }

    @Test
    public void testContentType() {
        Assert.assertEquals(((RequestSpecificationImpl) m.getRequest()).getContentType(), ContentTypeEnum.XML
                .getStringValues()[0], "Content type from annotation not as expected");
    }
}
