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
package com.zebrunner.carina.api.apitools.util;

import com.zebrunner.carina.api.apitools.util.PropertiesUtil;
import com.zebrunner.carina.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class PropertiesUtilTest {

    @Test
    public void testReadProperties() {
        Properties expectedProperties = R.TESTDATA.getProperties();
        Properties actualProperties = PropertiesUtil.readProperties("testdata.properties");

        Assert.assertEquals(actualProperties, expectedProperties, "Properties wasn't read properly");
    }

    @Test
    public void testReadPropertiesWithWrongProperties() {
        try {
            PropertiesUtil.readProperties("nonexistent.properties");
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Can't read properties from file", "Exception not thrown in readProperties()");
        }
    }
}
