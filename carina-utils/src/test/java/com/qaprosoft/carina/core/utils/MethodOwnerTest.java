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
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import com.qaprosoft.carina.core.foundation.utils.ownership.Ownership;

/**
 * Tests for {@link Ownership}
 */
public class MethodOwnerTest {

    private static final String ANDROID_OWNER = "androidTestOwner";
    private static final String IOS_OWNER = "iosTestOwner";

    @Test
    @MethodOwner(owner = ANDROID_OWNER, platform = SpecialKeywords.ANDROID)
    @MethodOwner(owner = IOS_OWNER, platform = SpecialKeywords.IOS)
    public void testMethodOwner() {
        ITestResult result = Reporter.getCurrentTestResult();
        String ownerName = Ownership.getMethodOwner(result, SpecialKeywords.ANDROID);
        Assert.assertEquals(ownerName, ANDROID_OWNER);
        String secondOwnerName = Ownership.getMethodOwner(result, SpecialKeywords.IOS);
        Assert.assertEquals(secondOwnerName, IOS_OWNER);
    }
}
