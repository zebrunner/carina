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
package com.zebrunner.carina.core.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.core.filter.IFilter;
import com.zebrunner.carina.core.registrar.tag.Priority;
import com.zebrunner.carina.core.registrar.tag.TestPriority;

public class PriorityFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        TestPriority priority = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestPriority.class);
        if (priority == null) {
            return ruleCheck(rules);
        } else {
            Priority testPriority = priority.value();
            String actualTestPriority = testPriority.toString();
            LOGGER.info("Test: [{}]. Priority: [{}]. Expected priority: [{}]", testMethod.getMethodName(), actualTestPriority,
                    rules);
            return ruleCheck(rules, actualTestPriority);
        }
    }
}
