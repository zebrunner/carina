/******************************************************************************
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
 * *****************************************************************************/

package com.qaprosoft.carina.core.foundation.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;

public class OwnerFilter extends Filter {


    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        // Extract method level owners from test method
        List<String> owners = extractOwnersFromMethod(testMethod);

        // Log test method details for debugging purposes
        logTestMethodDetails(testMethod, owners, rules);

        // Check if any of the owners match with the expected rules
        return ruleCheck(rules, owners);
    }

    /**
     * Extracts method level owners from test method.
     * @param testMethod - TestNG method for which owners are to be extracted.
     * @return - List of owners extracted from the test method.
     */
    private List<String> extractOwnersFromMethod(ITestNGMethod testMethod) {
        List<String> owners = new ArrayList<>();
        if (testMethod != null) {
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(MethodOwner.class)) {
                MethodOwner ownerAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.class);
                if (ownerAnnotation != null) {
                    owners.add(ownerAnnotation.owner().toLowerCase());
                }
            } else if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(MethodOwner.List.class)) {
                MethodOwner.List ownerAnnotations = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.List.class);
                if (ownerAnnotations != null) {
                    for (MethodOwner methodOwner : ownerAnnotations.value()) {
                        owners.add(methodOwner.owner().toLowerCase());
                    }
                }
            }
        }
        return owners;
    }

    /**
     * Logs test method details for debugging purposes.
     * @param testMethod - TestNG method to be logged.
     * @param owners - List of extracted owners.
     * @param rules - Expected owners as per the test configuration.
     */
    private void logTestMethodDetails(ITestNGMethod testMethod, List<String> owners, List<String> rules) {
        LOGGER.info(String.format("Test: [%s]. Owners: %s. Expected ownerAnnotation: [%s]",
                testMethod.getMethodName(), owners.toString(), rules.toString()));
    }

}
