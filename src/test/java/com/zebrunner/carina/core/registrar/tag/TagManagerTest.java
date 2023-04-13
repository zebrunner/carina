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
package com.zebrunner.carina.core.registrar.tag;

import com.zebrunner.agent.core.registrar.domain.LabelDTO;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.util.List;

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
        String priority = new PriorityManager().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod()).get(0).getValue();
        Assert.assertEquals(priority, "P2");
    }

    @Test
    @TestPriority(Priority.P0)
    public void testPriorityCompliance() {
        ITestResult result = Reporter.getCurrentTestResult();
        String priority = new PriorityManager().resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod()).get(0).getValue();
        Assert.assertEquals(priority, "P0");
    }

    @Test
    @TestPriority(value = Priority.P1)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    public void testTags() {
        ITestResult result = Reporter.getCurrentTestResult();
        List<LabelDTO> tags = new TagManager()
                .resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod());

        LabelDTO label = tags.stream()
                             .filter(labelDTO -> labelDTO.getKey().equals(TAG_NAME))
                             .findFirst()
                             .orElse(null);
        Assert.assertNotNull(label);
        Assert.assertEquals(TAG_VALUE, label.getValue());
    }

    @Test
    @TestPriority(Priority.P2)
    @TestTag(name = TAG_NAME, value = TAG_VALUE)
    @TestTag(name = TAG_NAME2, value = TAG_VALUE2)
    public void testRepeatableTags() {
        ITestResult result = Reporter.getCurrentTestResult();
        List<LabelDTO> tags = new TagManager()
                .resolve(this.getClass(), result.getMethod().getConstructorOrMethod().getMethod());

        LabelDTO label1 = tags.stream()
                              .filter(labelDTO -> labelDTO.getKey().equals(TAG_NAME))
                              .findFirst()
                              .orElse(null);
        Assert.assertNotNull(label1);
        Assert.assertEquals(TAG_VALUE, label1.getValue());

        LabelDTO label2 = tags.stream()
                              .filter(labelDTO -> labelDTO.getKey().equals(TAG_NAME))
                              .findFirst()
                              .orElse(null);
        Assert.assertNotNull(label2);
        Assert.assertEquals(TAG_VALUE, label2.getValue());
    }

}
