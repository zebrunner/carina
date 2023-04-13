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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.core.filter.IFilter;
import com.zebrunner.carina.core.registrar.tag.TestTag;

public class TagFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        String tagName;
        String tagValue;

        if (testMethod != null) {
            //if test was described only by one TagFilter
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(TestTag.class)) {
                TestTag methodAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestTag.class);

                if (methodAnnotation != null) {
                    tagName = methodAnnotation.name();
                    tagValue = methodAnnotation.value();
                    String tag = tagName + "=" + tagValue;
                    LOGGER.info("Test: [{}]. Tag: [{}]. Expected tag: [{}]", testMethod.getMethodName(), tag, rules);
                    return ruleCheck(rules, tag);
                }
            }

            //if test was described by several TagFilters
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(TestTag.List.class)) {
                TestTag.List methodAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestTag.List.class);
                if (methodAnnotation != null) {
                    List<String> tags = new ArrayList<>();
                    for (TestTag tag : methodAnnotation.value()) {
                        tagName = tag.name();
                        tagValue = tag.value();
                        String fullTag = tagName + "=" + tagValue;
                        tags.add(fullTag.toLowerCase());
                    }
                    LOGGER.info("Test: [{}]. Tag: [{}]. Expected tag: [{}]", testMethod.getMethodName(), tags, rules);
                    return ruleCheck(rules, tags);
                }
            }

            //if test was not described by TagFilters
            return ruleCheck(rules);
        }
        return false;
    }
}
