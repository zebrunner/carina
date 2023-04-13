/******************************************************************************
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
 * *****************************************************************************/

package com.zebrunner.carina.core.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.core.filter.IFilter;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;

public class OwnerFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        if (testMethod != null) {
            //if test was described only by one OwnerFilter
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(MethodOwner.class)) {
                MethodOwner ownerAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.class);
                if (ownerAnnotation != null) {
                    String owner = ownerAnnotation.owner().toLowerCase();
                    LOGGER.info("Test: [{}]. Owners: {}. Expected ownerAnnotation: [{}]", testMethod.getMethodName(), owner, rules);
                    return ruleCheck(rules, owner);
                }
            }

            //if test was described by several OwnerFilters
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(MethodOwner.List.class)) {
                MethodOwner.List ownerAnnotations = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.List.class);
                if (ownerAnnotations != null) {
                    List<String> owners = new ArrayList<>();
                    for (MethodOwner methodOwner : ownerAnnotations.value()) {
                        owners.add(methodOwner.owner().toLowerCase());
                    }
                    LOGGER.info("Test: [{}]. Owners: {}. Expected owner: [{}]", testMethod.getMethodName(), owners, rules);
                    return ruleCheck(rules, owners);
                }
            }

            //if test was not described by OwnerFilter annotation
            return ruleCheck(rules);
        }
        return false;
    }
}
