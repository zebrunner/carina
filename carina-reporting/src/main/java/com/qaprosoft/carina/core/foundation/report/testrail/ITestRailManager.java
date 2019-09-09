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
package com.qaprosoft.carina.core.foundation.report.testrail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;

public interface ITestRailManager extends ITestCases {
    static final Logger LOGGER = Logger.getLogger(ITestRailManager.class);

    default Set<String> getTestRailCasesUuid(ITestResult result) {
        Set<String> testCases = new HashSet<String>();

        //add cases form xls/cvs dataprovider
        int projectID = getTestRailProjectId(result.getTestContext());
        int suiteID = getTestRailSuiteId(result.getTestContext());

        //do not add test rail id if no integration tags/parameters detected
        if (projectID != -1 && suiteID != -1) {

            List<String> dataProviderIds = new ArrayList<String>();
            @SuppressWarnings("unchecked")
            Map<Object[], String> testNameTestRailMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TESTRAIL_ARGS_MAP);
            if (testNameTestRailMap != null) {
                String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
                if (testNameTestRailMap.containsKey(testHash) && testNameTestRailMap.get(testHash) != null) {
                    dataProviderIds = new ArrayList<String>(Arrays.asList(testNameTestRailMap.get(testHash).split(",")));
                }
            }

            testCases.addAll(dataProviderIds);

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
                    String language = methodAnnotation.language();
                    String locale = methodAnnotation.locale();
                    if (isValidPlatform(platform) && isValidLanguage(language) && isValidLocale(locale)) {
                        String[] testCaseList = methodAnnotation.testCasesId().split(",");
                        for (String tcase : testCaseList) {
                            String uuid = tcase;
                            testCases.add(uuid);
                            LOGGER.debug("TestRail test case uuid '" + uuid + "' is registered.");
                        }

                    }
                }
                if (testMethod.isAnnotationPresent(TestRailCases.List.class)) {
                    TestRailCases.List methodAnnotation = testMethod.getAnnotation(TestRailCases.List.class);
                    for (TestRailCases tcLocal : methodAnnotation.value()) {

                        String platform = tcLocal.platform();
                        String language = tcLocal.language();
                        String locale = tcLocal.locale();
                        if (isValidPlatform(platform) && isValidLanguage(language) && isValidLocale(locale)) {
                            String[] testCaseList = tcLocal.testCasesId().split(",");
                            for (String tcase : testCaseList) {
                                String uuid = tcase;
                                testCases.add(uuid);
                                LOGGER.debug("TestRail test case uuid '" + uuid + "' is registered.");
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // append cases id values from ITestCases map (custom TestNG provider)
        List<String> customCases = getCases();
        testCases.addAll(customCases);

        clearCases();

        return testCases;
    }

    default int getTestRailProjectId(ITestContext context) {
        String id = context.getSuite().getParameter(SpecialKeywords.TESTRAIL_PROJECT_ID);
        if (id != null) {
            return Integer.valueOf(id.trim());
        } else {
            return -1;
        }
    }

    default int getTestRailSuiteId(ITestContext context) {
        String id = context.getSuite().getParameter(SpecialKeywords.TESTRAIL_SUITE_ID);
        if (id != null) {
            return Integer.valueOf(id.trim());
        } else {
            return -1;
        }
    }
    
}