/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Tests for {@link R}
 */
public class RTest {
    @Test
    public void testDefaultValue() {
        Assert.assertEquals(R.CONFIG.get("browser"), "chrome");
    }

    @Test(dependsOnMethods = "testDefaultValue")
    public void testOverrideBySystemParam() {
        final String BROWSER = "firefox";
        R.CONFIG.put("browser", BROWSER);
        Assert.assertEquals(R.CONFIG.get("browser"), BROWSER);
    }

    @Test
    public void testOverrideInProperties() {
        Assert.assertEquals(R.CONFIG.get("port"), "8081");
    }

    @Test
    public void testGetInt() {
        Assert.assertEquals(R.CONFIG.getInt("port"), 8081);
    }
    
    @Test
    public void testGetLong() {
        Assert.assertEquals(R.CONFIG.getLong("port"), 8081L);
    }
    
    @Test
    public void testGetDouble() {
        Double doubleValue = 3.14;
        Assert.assertEquals(R.CONFIG.getDouble("double"), doubleValue);
    }
    
    @Test
    public void testGetBoolean() {
        Assert.assertEquals(R.CONFIG.getBoolean("boolean"), true);
    }
    
    @Test
    public void testGetProperties() {
        Assert.assertEquals(R.CONFIG.getProperties().getProperty("port"), "8081");
    }  
    
    @Test
    public void testPlaceholders() {
        Assert.assertEquals(R.CONFIG.get("url"), "http://localhost:8081");
    }
    
    @Test
    public void testGetEmptyTestProperties() {
        R.CONFIG.clearTestProperties();
        Assert.assertTrue(R.CONFIG.getTestProperties().isEmpty(), "Default temp test properties object should be empty!");
    }
    
    @Test
    public void testOverrideTempProperty() {
        R.CONFIG.clearTestProperties();
        Assert.assertEquals(R.CONFIG.get("key1"), "");
        R.CONFIG.put("key1", "value1", true);
        Assert.assertEquals(R.CONFIG.get("key1"), "value1");
        R.CONFIG.clearTestProperties();
        Assert.assertEquals(R.CONFIG.get("key1"), "");
    }

    @Test
    public void testContainsKey() {
        Assert.assertEquals(R.CONFIG.containsKey("boolean"), true);
    }
    
    @Test
    public void testContainsTempKey() {
        R.CONFIG.put("key1", "value1", true);
        Assert.assertEquals(R.CONFIG.containsKey("key1"), true);
    }
    
    /*
     * @Test
     * public void testEncryption()
     * {
     * Assert.assertEquals(R.CONFIG.get("password"), "EncryptMe");
     * Assert.assertEquals(R.CONFIG.getSecured("password"), "{crypt:8O9iA4+f3nMzz85szmvKmQ==}");
     * }
     * 
     * @Test
     * public void testPlaceholdersWithEncryption()
     * {
     * Assert.assertEquals(R.CONFIG.get("credentials"), "test@gmail.com/EncryptMe");
     * }
     */

    @Test
    public void testPlaceholdersInEmail() {
        Assert.assertEquals(R.EMAIL.get("title"), "${test}");
    }

    @Test
    public void testOverride() {
        R.CONFIG.clearTestProperties();
        Assert.assertEquals(R.TESTDATA.get("key1"), "3");
        Assert.assertEquals(R.TESTDATA.get("key2"), "2");
        Assert.assertEquals(R.TESTDATA.get("key3"), "1");
        Assert.assertEquals(R.TESTDATA.get("key4"), "3");
        Assert.assertEquals(R.TESTDATA.get("key5"), "2");
    }
}
