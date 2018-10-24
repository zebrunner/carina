/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.utils.ownership.Ownership;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Tests for {@link Ownership}
 */
public class MethodOwnerTest {

    private static final String OWNER = "testowner";
    private static final String SECONDARY_OWNER = "testSecondOwner";
    private static final String DESKTOP_OWNER = "testDesktopOwner";
    private static final String IOS_OWNER = "testIOSOwner";

    @Test
    @MethodOwner(owner = OWNER)
    @MethodOwner(owner = SECONDARY_OWNER, platform = "ios")
    public void testMethodOwner() {
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PRIMARY);
        Assert.assertEquals(ownerName, OWNER);
    }

    @Test
    @MethodOwner(owner = OWNER)
    @MethodOwner(owner = SECONDARY_OWNER, platform = "ios")
    public void testMethodOwnerEmptyPlatform() {
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, OWNER);
    }

    @Test
    @MethodOwner(owner = OWNER, secondaryOwner = SECONDARY_OWNER)
    public void testMethodSecondaryOwner() {
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PRIMARY);
        Assert.assertEquals(ownerName, OWNER);
        String secondOwnerName = Ownership.getMethodOwner(result, Ownership.OwnerType.SECONDARY);
        Assert.assertEquals(secondOwnerName, SECONDARY_OWNER);
    }

    @Test
    @MethodOwner(owner = OWNER, platform = "android")
    @MethodOwner(owner = SECONDARY_OWNER, platform = "ios")
    @MethodOwner(owner = DESKTOP_OWNER, platform = "desktop")
    public void testMethodOwnerByPlatform() {
        R.CONFIG.put(Configuration.Parameter.PLATFORM.getKey(), "DESKTOP");
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, DESKTOP_OWNER);
        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "IOS");
        ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, SECONDARY_OWNER);
        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "ANDROID");
        ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, OWNER);
        R.CONFIG.put(Configuration.Parameter.PLATFORM.getKey(), "");
        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "");
    }

    @Test
    @MethodOwner(owner = OWNER, platform = "android")
    @MethodOwner(owner = SECONDARY_OWNER, platform = "ios")
    @MethodOwner(owner = DESKTOP_OWNER, platform = "desktop")
    public void testMethodOwnerByEmptyPlatform() {
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, "");
    }

    @Test
    @MethodOwner(owner = OWNER, secondaryOwner = SECONDARY_OWNER)
    public void testMethodSecondaryOwnerByPlatform() {
        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "IOS");
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, SECONDARY_OWNER);
        String secondOwnerName = Ownership.getMethodOwner(result, Ownership.OwnerType.SECONDARY);
        Assert.assertEquals(secondOwnerName, SECONDARY_OWNER);
        R.CONFIG.put(Configuration.Parameter.PLATFORM.getKey(), "");
        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "");
    }

    @Test
    @MethodOwner(owner = OWNER, secondaryOwner = SECONDARY_OWNER)
    @MethodOwner(owner = IOS_OWNER, platform = "ios")
    public void testMethodSecondaryOwnerWoPlatform() {
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, OWNER);
        String secondOwnerName = Ownership.getMethodOwner(result, Ownership.OwnerType.SECONDARY);
        Assert.assertEquals(secondOwnerName, SECONDARY_OWNER);
        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "IOS");
        ownerName = Ownership.getMethodOwner(result, Ownership.OwnerType.PLATFORM);
        Assert.assertEquals(ownerName, IOS_OWNER);
        R.CONFIG.put(Configuration.Parameter.PLATFORM.getKey(), "");
        R.CONFIG.put(SpecialKeywords.MOBILE_DEVICE_PLATFORM, "");
    }

}
