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

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.PriorityManager;
import com.qaprosoft.carina.core.foundation.utils.tag.TagManager;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestTag;

/**
 * Tests for {@link TagManager}
 */
public class TagManagerTest {
    
    private static final String TAG_NAME = "tag1";
    private static final String TAG_NAME2 = "tag2";
    private static final String TAG_VALUE = "testTag1";
    private static final String TAG_VALUE2 = "testTag2";

    @Test
    @TestPriority(Priority.P2)
    public void testPriority() {
        ITestResult result = Reporter.getCurrentTestResult();
        String priority = new PriorityManager().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod()).get(SpecialKeywords.TEST_PRIORITY_TAG).get(0);
        Assert.assertEquals(priority, "P2");
    }

    @Test
    @TestPriority(Priority.P0)
    public void testPriorityCompliance() {
        ITestResult result = Reporter.getCurrentTestResult();
        String priority = new PriorityManager().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod()).get(SpecialKeywords.TEST_PRIORITY_TAG).get(0);
        Assert.assertEquals(priority, "P0");
    }

    @Test
    @TestPriority(value = Priority.P1)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    public void testTags() {
        ITestResult result = Reporter.getCurrentTestResult();
        Map<String, List<String>> tags = new TagManager().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod());
        Assert.assertTrue(tags.containsKey(TAG_NAME));
        Assert.assertTrue(tags.get(TAG_NAME).contains(TAG_VALUE));
    }

    @Test
    @TestPriority(Priority.P2)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    @TestTag(name = TAG_NAME2, value = TAG_VALUE2)
    public void testRepeatableTags() {
        ITestResult result = Reporter.getCurrentTestResult();
        Map<String, List<String>> tags = new TagManager().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod());
        Assert.assertTrue(tags.containsKey(TAG_NAME));
        Assert.assertTrue(tags.get(TAG_NAME).contains(TAG_VALUE));
        Assert.assertTrue(tags.containsKey(TAG_NAME2));
        Assert.assertTrue(tags.get(TAG_NAME2).contains(TAG_VALUE2));
    }

}
