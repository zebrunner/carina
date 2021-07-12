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
package com.qaprosoft.carina.core.foundation.report.testrail;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;

public interface ITestRailManager extends ITestCases {
    static final Logger TESTRAIL_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    default Set<String> getTestRailCasesUuid(ITestResult result) {
        Set<String> testCases = new HashSet<String>();

        int projectID = this.getTestRailProjectIdFromSuite(result.getTestContext().getSuite());
        int suiteID = getTestRailSuiteIdFromSuite(result.getTestContext().getSuite());

        if (projectID == -1 || suiteID == -1) {
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
                if (testMethod.isAnnotationPresent(TestRailCases.class)) {
                    TestRailCases methodAnnotation = testMethod.getAnnotation(TestRailCases.class);
                    String platform = methodAnnotation.platform();
                    String locale = methodAnnotation.locale();
                    if (isValidPlatform(platform) && isValidLocale(locale)) {
                        String[] testCaseList = methodAnnotation.testCasesId().split(",");
                        for (String tcase : testCaseList) {
                            testCases.add(tcase);
                            TESTRAIL_LOGGER.debug("TestRail test case uuid '" + tcase + "' is registered.");
                        }

                    }
                }
                if (testMethod.isAnnotationPresent(TestRailCases.List.class)) {
                    TestRailCases.List methodAnnotation = testMethod.getAnnotation(TestRailCases.List.class);
                    for (TestRailCases tcLocal : methodAnnotation.value()) {

                        String platform = tcLocal.platform();
                        String locale = tcLocal.locale();
                        if (isValidPlatform(platform) && isValidLocale(locale)) {
                            String[] testCaseList = tcLocal.testCasesId().split(",");
                            for (String tcase : testCaseList) {
                                testCases.add(tcase);
                                TESTRAIL_LOGGER.debug("TestRail test case uuid '" + tcase + "' is registered.");
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            TESTRAIL_LOGGER.error(e.getMessage(), e);
        }

        // append cases id values from ITestCases map (custom TestNG provider)
        for (String entry: getCases()) {
            testCases.add(projectID + "-" + suiteID + "-" + entry);
        }
        clearCases();

        return testCases;
    }

    default String getTestRailProjectId(ISuite suite){
        int projectID = this.getTestRailProjectIdFromSuite(suite);
        int suiteID = getTestRailSuiteIdFromSuite(suite);

        if (projectID == -1 || suiteID == -1) {
            return "";
        } else {
            return String.valueOf(projectID);
        }
    }

    default String getTestRailSuiteId(ISuite suite){
        int projectID = this.getTestRailProjectIdFromSuite(suite);
        int suiteID = getTestRailSuiteIdFromSuite(suite);

        if (projectID == -1 || suiteID == -1) {
            return "";
        } else {
            return String.valueOf(suiteID);
        }
    }

  private int getTestRailProjectIdFromSuite(ISuite suite) {
        if (suite.getParameter(SpecialKeywords.TESTRAIL_PROJECT_ID) != null) {
            return Integer.valueOf(suite.getParameter(SpecialKeywords.TESTRAIL_PROJECT_ID).trim());
        } else if (suite.getAttribute(SpecialKeywords.TESTRAIL_PROJECT_ID) != null){
            //use-case to support unit tests
            return Integer.valueOf(suite.getAttribute(SpecialKeywords.TESTRAIL_PROJECT_ID).toString());
        } else {
            return -1;
        }

    }

  private int getTestRailSuiteIdFromSuite(ISuite suite) {
        if (suite.getParameter(SpecialKeywords.TESTRAIL_SUITE_ID) != null) {
            return Integer.valueOf(suite.getParameter(SpecialKeywords.TESTRAIL_SUITE_ID).trim());
        } else if (suite.getAttribute(SpecialKeywords.TESTRAIL_SUITE_ID) != null) {
            //use-case to support unit tests
            return Integer.valueOf(suite.getAttribute(SpecialKeywords.TESTRAIL_SUITE_ID).toString());
        } else {
            return -1;
        }
    }

}