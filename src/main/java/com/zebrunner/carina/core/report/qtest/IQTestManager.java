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
package com.zebrunner.carina.core.report.qtest;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ITestResult;

import com.zebrunner.carina.core.report.testrail.ITestCases;
import com.zebrunner.carina.utils.commons.SpecialKeywords;

public interface IQTestManager extends ITestCases {
    static final Logger QTEST_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    default Set<String> getQTestCasesUuid(ITestResult result) {
        Set<String> testCases = new HashSet<>();

        int projectID = getQTestProjectIdFromSuite(result.getTestContext().getSuite());
        if (projectID == -1) {
            // no sense to return something as integration data not provided
            return testCases;
        }

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
                if (testMethod.isAnnotationPresent(QTestCases.class)) {
                    QTestCases methodAnnotation = testMethod.getAnnotation(QTestCases.class);
                    String platform = methodAnnotation.platform();
                    String locale = methodAnnotation.locale();
                    if (isValidPlatform(platform) && isValidLocale(locale)) {
                        String[] testCaseList = methodAnnotation.id().split(",");
                        for (String tcase : testCaseList) {
                            tcase = tcase.trim();
                            if (!tcase.isEmpty()) {
                                testCases.add(tcase);
                                QTEST_LOGGER.debug("qTest test case uuid '{}' is registered.", tcase);
                            } else {
                                QTEST_LOGGER.error("qTest test case uuid was not registered because of an empty value");
                            }
                        }

                    }
                }
                if (testMethod.isAnnotationPresent(QTestCases.List.class)) {
                    QTestCases.List methodAnnotation = testMethod.getAnnotation(QTestCases.List.class);
                    for (QTestCases tcLocal : methodAnnotation.value()) {

                        String platform = tcLocal.platform();
                        String locale = tcLocal.locale();
                        if (isValidPlatform(platform) && isValidLocale(locale)) {
                            String[] testCaseList = tcLocal.id().split(",");
                            for (String tcase : testCaseList) {
                                tcase = tcase.trim();
                                if (!tcase.isEmpty()) {
                                    testCases.add(tcase);
                                    QTEST_LOGGER.debug("qTest test case uuid '{}' is registered.", tcase);
                                } else {
                                    QTEST_LOGGER.error("qTest test case uuid was not registered because of an empty value");
                                }
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            QTEST_LOGGER.error("Can't find test class!", e);
        }

        // append cases id values from ITestCases map (custom TestNG provider)
        for (String entry : getCases()) {
            entry=entry.trim();
            if (!entry.isEmpty()){
                testCases.add(entry);
            }
        }
        clearCases();

        return testCases;
    }

    default String getQTestProjectId(ISuite suite) {
        int projectId = getQTestProjectIdFromSuite(suite);

        if (projectId == -1) {
            return "";
        } else {
            return String.valueOf(projectId);
        }
    }

    private int getQTestProjectIdFromSuite(ISuite suite) {
        if (suite.getParameter(SpecialKeywords.QTEST_PROJECT_ID) != null) {
            return Integer.parseInt(suite.getParameter(SpecialKeywords.QTEST_PROJECT_ID).trim());
        } else if (suite.getAttribute(SpecialKeywords.QTEST_PROJECT_ID) != null) {
            //use-case to support unit tests
            return Integer.parseInt(suite.getAttribute(SpecialKeywords.QTEST_PROJECT_ID).toString().trim());
        } else {
            return -1;
        }
    }
}
