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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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
import com.qaprosoft.zafira.models.dto.TagType;

/**
 * Tests for {@link TagManager}
 */
public class TagManagerTest {
    private static final Logger LOGGER = Logger.getLogger(TagManagerTest.class);

    private static final String TAG_NAME = "tag1";
    private static final String TAG_NAME2 = "tag2";
    private static final String TAG_VALUE = "testTag1";
    private static final String TAG_VALUE2 = "testTag2";
    private static final String FORBIDDEN_KEY_PRIORITY = "priority";

    @Test
    @TestPriority(Priority.P2)
    public void testPriority() {
        ITestResult result = Reporter.getCurrentTestResult();
        String priority = PriorityManager.getPriority(result);
        Assert.assertEquals(priority, "P2");
    }

    @Test
    @TestPriority(Priority.P0)
    public void testPriorityCompliance() {
        ITestResult result = Reporter.getCurrentTestResult();
        String priority = PriorityManager.getPriority(result);
        Assert.assertEquals(priority, "P0");
    }

    @Test
    @TestPriority(value = Priority.P1)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    public void testTags() {
        ITestResult result = Reporter.getCurrentTestResult();
        Map<String, String> tag = TagManager.getTags(result);
        Assert.assertTrue(tag.containsKey(TAG_NAME));
        Assert.assertEquals(tag.get(TAG_NAME), TAG_VALUE);
    }

    @Test
    @TestPriority(Priority.P2)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    @TestTag(name = TAG_NAME2, value = TAG_VALUE2)
    public void testRepeatableTags() {
        ITestResult result = Reporter.getCurrentTestResult();
        Map<String, String> tags = TagManager.getTags(result);
        Assert.assertTrue(tags.containsKey(TAG_NAME));
        Assert.assertEquals(tags.get(TAG_NAME), TAG_VALUE);
        Assert.assertTrue(tags.containsKey(TAG_NAME2));
        Assert.assertEquals(tags.get(TAG_NAME2), TAG_VALUE2);
    }

    @Test
    @TestPriority(Priority.P2)
    @TestTag(name = TAG_NAME2, value = TAG_VALUE2)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    @TestTag(name = FORBIDDEN_KEY_PRIORITY, value = "P0")
    public void testForbiddenTags() {
        ITestResult result = Reporter.getCurrentTestResult();
        Map<String, String> tags = TagManager.getTags(result);
        Assert.assertFalse(tags.containsKey(FORBIDDEN_KEY_PRIORITY));
        Assert.assertTrue(tags.containsKey(TAG_NAME));
        Assert.assertEquals(tags.get(TAG_NAME), TAG_VALUE);
        Assert.assertTrue(tags.containsKey(TAG_NAME2));
        Assert.assertEquals(tags.get(TAG_NAME2), TAG_VALUE2);
        Assert.assertEquals(tags.size(), 2);
    }

    @Test
    @TestPriority(Priority.P1)
    @TestTag(name = FORBIDDEN_KEY_PRIORITY, value = "P5")
    public void testForbiddenPriorityTag() {
        ITestResult result = Reporter.getCurrentTestResult();
        String priority = PriorityManager.getPriority(result);
        Assert.assertEquals(priority, "P1");
        Map<String, String> tags = TagManager.getTags(result);
        Assert.assertFalse(tags.containsKey(FORBIDDEN_KEY_PRIORITY));
        Assert.assertEquals(tags.size(), 0);
    }

    @Test
    @TestPriority(Priority.P2)
    @TestTag(name = TAG_NAME2, value = TAG_VALUE2)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    @TestTag(name = FORBIDDEN_KEY_PRIORITY, value = "P0")
    public void testZafiraGetTagsMethod() {
        ITestResult result = Reporter.getCurrentTestResult();
        Map<String, String> tags = TagManager.getTags(result);
        String priority = PriorityManager.getPriority(result);
        Assert.assertEquals(priority, "P2");
        Assert.assertFalse(tags.containsKey(FORBIDDEN_KEY_PRIORITY));
        Assert.assertTrue(tags.containsKey(TAG_NAME));
        Assert.assertEquals(tags.get(TAG_NAME), TAG_VALUE);
        Assert.assertTrue(tags.containsKey(TAG_NAME2));
        Assert.assertEquals(tags.get(TAG_NAME2), TAG_VALUE2);
        Assert.assertEquals(tags.size(), 2);
        Set<TagType> tagsTypes = getTestTags(result);
        Assert.assertEquals(tagsTypes.size(), 3);
        for (TagType entry : tagsTypes) {
            if (entry.getName().equals(SpecialKeywords.TEST_PRIORITY_KEY)) {
                Assert.assertEquals(entry.getValue(), "P2");
            }
        }

        tagsTypes.stream().forEachOrdered((entry) -> {
            Object currentKey = entry.getName();
            Object currentValue = entry.getValue();
            LOGGER.info(currentKey + "=" + currentValue);
        });
    }

    @Test
    @TestTag(name = TAG_NAME2, value = TAG_VALUE2)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    @TestTag(name = FORBIDDEN_KEY_PRIORITY, value = "P0")
    public void testZafiraGetTagsMethodWoPriority() {
        ITestResult result = Reporter.getCurrentTestResult();
        String priority = PriorityManager.getPriority(result);
        Assert.assertEquals(priority, "");
        Map<String, String> tags = TagManager.getTags(result);
        Assert.assertFalse(tags.containsKey(FORBIDDEN_KEY_PRIORITY));
        Assert.assertTrue(tags.containsKey(TAG_NAME));
        Assert.assertEquals(tags.get(TAG_NAME), TAG_VALUE);
        Assert.assertTrue(tags.containsKey(TAG_NAME2));
        Assert.assertEquals(tags.get(TAG_NAME2), TAG_VALUE2);
        Assert.assertEquals(tags.size(), 2);
        Set<TagType> tagsTypes = getTestTags(result);
        Assert.assertEquals(tagsTypes.size(), 2);
        for (TagType entry : tagsTypes) {
            if (entry.getName().equals(TAG_NAME2)) {
                Assert.assertEquals(entry.getValue(), TAG_VALUE2);
            }
        }

        tagsTypes.stream().forEachOrdered((entry) -> {
            Object currentKey = entry.getName();
            Object currentValue = entry.getValue();
            LOGGER.info(currentKey + "=" + currentValue);
        });
    }

    private Set<TagType> getTestTags(ITestResult test) {
        Set<TagType> tags = new HashSet();

        String testPriority = PriorityManager.getPriority(test);
        if (testPriority != null && !testPriority.isEmpty()) {
            TagType priority = new TagType();
            priority.setName(SpecialKeywords.TEST_PRIORITY_KEY);
            priority.setValue(testPriority);
            tags.add(priority);
        }

        Map<String, String> testTags = TagManager.getTags(test);

        testTags.entrySet().stream().forEach((entry) -> {
            TagType tagEntry = new TagType();
            tagEntry.setName(entry.getKey());
            tagEntry.setValue(entry.getValue());
            tags.add(tagEntry);
        });
        return tags;
    }

}
