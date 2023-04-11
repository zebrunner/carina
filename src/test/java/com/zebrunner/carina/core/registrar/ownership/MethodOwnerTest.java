/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.registrar.ownership;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.R;

/**
 * Tests for {@link Ownership}
 */
public class MethodOwnerTest {

    private static final String ANDROID_OWNER = "androidTestOwner";
    private static final String IOS_OWNER = "iosTestOwner";
    private static final String DEFAULT_OWNER = "defaultOwner";

    @Test
    @MethodOwner(owner = DEFAULT_OWNER)
    @MethodOwner(owner = ANDROID_OWNER, platform = SpecialKeywords.ANDROID)
    @MethodOwner(owner = IOS_OWNER, platform = SpecialKeywords.IOS)
    public void testDefaultMethodOwner() {
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = new Ownership().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod());
        Assert.assertEquals(ownerName, DEFAULT_OWNER);
    }
    
    @Test(dependsOnMethods="testDefaultMethodOwner")
    @MethodOwner(owner = ANDROID_OWNER, platform = SpecialKeywords.ANDROID)
    @MethodOwner(owner = DEFAULT_OWNER)
    @MethodOwner(owner = IOS_OWNER, platform = SpecialKeywords.IOS)
    public void testAndroidMethodOwner() {
    	R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, "android", true);
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = new Ownership().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod());
        Assert.assertEquals(ownerName, ANDROID_OWNER);
    }
    
    @Test(dependsOnMethods="testAndroidMethodOwner")
    @MethodOwner(owner = ANDROID_OWNER, platform = SpecialKeywords.ANDROID)
    @MethodOwner(owner = IOS_OWNER, platform = SpecialKeywords.IOS)
    @MethodOwner(owner = DEFAULT_OWNER)
    public void testIOSMethodOwner() {
    	R.CONFIG.put(SpecialKeywords.PLATFORM_NAME, "ios", true);
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = new Ownership().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod());
        Assert.assertEquals(ownerName, IOS_OWNER);
    }
}
