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
package com.zebrunner.carina.api.apitools.builder;

import com.zebrunner.carina.api.apitools.builder.MessageBuilder;
import com.zebrunner.carina.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class MessageBuilderTest {


    @Test
    public void testBuildStringMessage() {
        String expectedStringMessage = getStringProperties(R.TESTDATA.getProperties());
        String actualStringMessage = MessageBuilder.buildStringMessage("testdata.properties");

        Assert.assertEquals(actualStringMessage, expectedStringMessage, "String message wasn't generated properly");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testBuildStringMessageWithWrongProperties() {
        MessageBuilder.buildStringMessage("nonexistent.properties");
    }

    private String getStringProperties(Properties properties) {
        StringBuilder sb = new StringBuilder();

        properties.forEach((key, value) -> sb.append(key).append("=").append(value).append(System.lineSeparator()));

        return sb.toString();
    }
}
