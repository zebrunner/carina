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
package com.zebrunner.carina.core.report.testrail;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ITestResult;

import com.zebrunner.carina.utils.commons.SpecialKeywords;

public interface ITestRailManager extends ITestCases {
    static final Logger TESTRAIL_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    default Set<String> getTestRailCasesUuid(ITestResult result) {
        Set<String> testCases = new HashSet<>();

        int suiteID = getTestRailSuiteIdFromSuite(result.getTestContext().getSuite());

        // Get a handle to the class and method
        Class<?> testClass;
        try {
            testClass = Class.forName(result.getMethod().getTestClass().getName());

            // We can't use getMethod() because we may have parameterized tests
            // for which we won't know the matching signature
            String methodName = result.getMethod().getMethodName();
            Method testMethod = null;
            Method[] possibleMethods = testClass.getMethods();
            for (Method possibleMethod : possibleMethods) {
                if (possibleMethod.getName().equals(methodName)) {
                    testMethod = possibleMethod;
                    break;
                }
            }

            if (testMethod != null) {
                if (testMethod.isAnnotationPresent(TestRailCases.class)) {
                    TestRailCases methodAnnotation = testMethod.getAnnotation(TestRailCases.class);
                    String platform = methodAnnotation.platform();
                    String locale = methodAnnotation.locale();
                    String expectedSuiteId = methodAnnotation.suiteId();
                    if (isValidPlatform(platform) && isValidLocale(locale) && isValidSuite(suiteID, expectedSuiteId)) {
                        String[] testCaseList = methodAnnotation.testCasesId().split(",");
                        for (String tcase : testCaseList) {
                            tcase = tcase.trim();
                            if (!tcase.isEmpty()) {
                                testCases.add(tcase);
                                TESTRAIL_LOGGER.debug("TestRail test case uuid '{}' is registered.", tcase);
                            } else {
                                TESTRAIL_LOGGER.error("TestRail test case uuid was not registered because of an empty value");
                            }
                        }

                    }
                }
                if (testMethod.isAnnotationPresent(TestRailCases.List.class)) {
                    TestRailCases.List methodAnnotation = testMethod.getAnnotation(TestRailCases.List.class);
                    for (TestRailCases tcLocal : methodAnnotation.value()) {

                        String platform = tcLocal.platform();
                        String locale = tcLocal.locale();
                        String expectedSuiteId = tcLocal.suiteId();
                        if (isValidPlatform(platform) && isValidLocale(locale) && isValidSuite(suiteID, expectedSuiteId)) {
                            String[] testCaseList = tcLocal.testCasesId().split(",");
                            for (String tcase : testCaseList) {
                                tcase = tcase.trim();
                                if (!tcase.isEmpty()) {
                                    testCases.add(tcase);
                                    TESTRAIL_LOGGER.debug("TestRail test case uuid '{}' is registered.", tcase);
                                } else {
                                    TESTRAIL_LOGGER.error("TestRail test case uuid was not registered because of an empty value");
                                }
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            TESTRAIL_LOGGER.error("Can't find test class!", e);
        }

        // append cases id values from ITestCases map (custom TestNG provider)
        for (String entry : getCases()) {
            entry = entry.trim();
            if (!entry.isEmpty()) {
                testCases.add(entry.trim());
            }
        }
        clearCases();

        return testCases;
    }

    @Deprecated
    default String getTestRailProjectId(ISuite suite) {
        return "";
    }

    default String getTestRailSuiteId(ISuite suite) {
        int suiteID = getTestRailSuiteIdFromSuite(suite);

        if (suiteID == -1) {
            return "";
        } else {
            return String.valueOf(suiteID);
        }
    }

    private int getTestRailSuiteIdFromSuite(ISuite suite) {
        if (suite.getParameter(SpecialKeywords.TESTRAIL_SUITE_ID) != null) {
            return Integer.parseInt(suite.getParameter(SpecialKeywords.TESTRAIL_SUITE_ID).trim());
        } else if (suite.getAttribute(SpecialKeywords.TESTRAIL_SUITE_ID) != null) {
            //use-case to support unit tests
            return Integer.parseInt(suite.getAttribute(SpecialKeywords.TESTRAIL_SUITE_ID).toString().trim());
        } else {
            return -1;
        }
    }
    
    private boolean isValidSuite(int actualSuiteId, String expectedSuiteId) {
        return expectedSuiteId.isEmpty() || expectedSuiteId.equalsIgnoreCase(String.valueOf(actualSuiteId));
    }

}