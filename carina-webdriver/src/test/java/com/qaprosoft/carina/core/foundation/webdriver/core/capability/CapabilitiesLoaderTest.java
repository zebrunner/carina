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
package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.utils.R;

public class CapabilitiesLoaderTest {

    private final static String customCapabilities = "custom_capabilities.properties";
    // customCapabilities is the resource files with such props declared inside:
    // capabilities.stringParam=stringValue
    // capabilities.booleanParamTrue=true
    // capabilities.booleanParamFalse=false
    // coreParam=coreValue

    private final static String stringParam = "stringParam";
    private final static String stringValue = "stringValue";

    private final static String booleanParamTrue = "booleanParamTrue";
    private final static String booleanParamFalse = "booleanParamFalse";

    private final static String coreParam = "coreParam";
    private final static String coreValue = "coreValue";

    /*
     * Test that loadCapabilities() raise exception if no properties file detected on classpath
     */
    @Test(expectedExceptions = {
            AssertionError.class }, expectedExceptionsMessageRegExp = "Unable to find custom capabilities file 'unexisting_file'!")
    public void loadCapabilitiesFromNonExistingFileTest() {
        new CapabilitiesLoader().loadCapabilities("unexisting_file");
    }

    /*
     * Test that getCapabilities() return valid DesiredCapabilities values only for "capabilities.name=value" properties
     */
    @Test()
    public void getCapabilitiesTest() {
        DesiredCapabilities caps = new CapabilitiesLoader().getCapabilities(customCapabilities);
        String value = (String) caps.getCapability(stringParam);

        Assert.assertNotNull(value, "Unable to find '" + stringParam + "' capability!");
        Assert.assertEquals(value, stringValue, "Returned capability value is not valid!");

        Assert.assertTrue((Boolean) caps.getCapability(booleanParamTrue), "Returned capability value is not valid!");

        Assert.assertFalse((Boolean) caps.getCapability(booleanParamFalse), "Returned capability value is not valid!");

        // verify that param without "capabilities." prefix is not loaded here
        Assert.assertNull(caps.getCapability(coreParam), coreParam + " is present among capabilities mistakenly!");
    }
    
    /*
     * Test that loadCapabilities(file, true) load into the R.CONFIG all properties for current thread only!
     */
    @Test(dependsOnMethods = { "getCapabilitiesTest" })
    public void loadTempCapabilitiesTest() {
        new CapabilitiesLoader().loadCapabilities(customCapabilities, true);

        Assert.assertEquals(R.CONFIG.get("capabilities." + stringParam), stringValue, "Returned capability value is not valid!");
        Assert.assertTrue(R.CONFIG.getBoolean("capabilities." + booleanParamTrue), "Returned capability value is not valid!");
        Assert.assertFalse(R.CONFIG.getBoolean("capabilities." + booleanParamFalse), "Returned capability value is not valid!");

        Assert.assertEquals(R.CONFIG.get(coreParam), coreValue, "Returned property value is not valid!");
    }
    
    /*
     * Test that loadCapabilities() load into the R.CONFIG all properties globally
     */
    @Test(dependsOnMethods = { "loadTempCapabilitiesTest" })
    public void loadGlobalCapabilitiesTest() {
        new CapabilitiesLoader().loadCapabilities(customCapabilities);

        Assert.assertEquals(R.CONFIG.get("capabilities." + stringParam), stringValue, "Returned capability value is not valid!");
        Assert.assertTrue(R.CONFIG.getBoolean("capabilities." + booleanParamTrue), "Returned capability value is not valid!");
        Assert.assertFalse(R.CONFIG.getBoolean("capabilities." + booleanParamFalse), "Returned capability value is not valid!");

        Assert.assertEquals(R.CONFIG.get(coreParam), coreValue, "Returned property value is not valid!");
    }
    

}
