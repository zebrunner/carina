/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.R;

/**
 * Tests for {@link Configuration}
 */
public class ConfugurationTest {
    @Test
    public void testConfigOverrride() {
        R.CONFIG.put("env", "UNITTEST");
        Assert.assertEquals(Configuration.getEnvArg("override"), "override_me");
        R.CONFIG.put("UNITTEST.override", "i_am_overriden");
        Assert.assertEquals(Configuration.getEnvArg("override"), "i_am_overriden");
    }

    @Test
    public void testGetEnvArg() {
        R.CONFIG.put("env", "QA");
        Assert.assertEquals(Configuration.getEnvArg("url"), "local");
        R.CONFIG.put("env", "PROD");
        Assert.assertEquals(Configuration.getEnvArg("url"), "remote");
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void testInvalidConfigValidation() {
        R.CONFIG.put("platform", "{must_override}");
        Configuration.validateConfiguration();
    }

    @Test
    public void testConfigurationPlacehodler() {
        R.CONFIG.put("env", "STG");
        Assert.assertEquals(Configuration.getEnvArg("url"), "http://localhost:8081");
        Assert.assertEquals(Configuration.get(Parameter.URL), "http://localhost:8081");
    }
}
