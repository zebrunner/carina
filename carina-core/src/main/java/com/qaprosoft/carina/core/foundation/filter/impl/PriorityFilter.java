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
package com.qaprosoft.carina.core.foundation.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.zebrunner.carina.core.registrar.tag.Priority;
import com.zebrunner.carina.core.registrar.tag.TestPriority;

public class PriorityFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
// Extracted a new method 'ruleCheck' to simplify the code and increase its readability.
// The new method checks if the expected priority is included in the list of rules,
// which represents the priorities that are allowed to run.
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        // Extract the TestPriority annotation from the method, if present
        TestPriority priority = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestPriority.class);

        // If the method has no TestPriority annotation, simply check if the actual priority matches any of the rules
        if (priority == null) {
            return ruleCheck(rules);
        }
        // Otherwise, get the actual priority from the TestPriority annotation and log information about the test and its priority
        else {
            Priority testPriority = priority.value();
            String actualTestPriority = testPriority.toString();
            LOGGER.info(String.format("Test: [%s]. Priority: [%s]. Expected priority: [%s]", testMethod.getMethodName(), actualTestPriority,
                    rules.toString()));
            // Check if the actual priority matches any of the rules
            return ruleCheck(rules, actualTestPriority);
        }
    }

    // Helper method that checks if the actual priority matches any of the allowed rules.
    private boolean ruleCheck(List<String> rules, String... actualPriority) {
        // Loop through the list of rules and check if the actual priority matches any of them
        for (String rule : rules) {
            // If the actual priority is provided as an argument, check if it matches the current rule
            if (actualPriority.length > 0) {
                if (actualPriority[0].equals(rule)) {
                    return true;
                }
            }
            // If no actual priority is provided, check if the rule is "NO_PRIORITY"
            else {
                if ("NO_PRIORITY".equals(rule)) {
                    return true;
                }
            }
        }
        // If no rule matches the actual priority, return false
        return false;
    }
}
