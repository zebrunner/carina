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
package com.zebrunner.carina.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.carina.utils.common.CommonUtils;

public class CommonUtilsTest {

    @Test
    public void testPause() {
        long pause = 2L;
        long startTime = System.currentTimeMillis();
        CommonUtils.pause(pause);
        long endTime = System.currentTimeMillis();
        long actualPause = (endTime - startTime) / 1000;
        Assert.assertEquals(actualPause, pause);
    }
}
