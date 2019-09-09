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
package com.qaprosoft.carina.core.foundation.report.qtest;

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
import com.qaprosoft.carina.core.foundation.report.testrail.ITestCases;

public interface IQTestManager extends ITestCases {
    static final Logger LOGGER = Logger.getLogger(IQTestManager.class);

    default Set<String> getQTestCasesUuid(ITestResult result) {
        Set<String> testCases = new HashSet<String>();

        //add cases form xls/cvs dataprovider
        int projectID = getQTestProjectId(result.getTestContext());
        if (projectID != -1) {

            List<String> dataProviderIds = new ArrayList<String>();
            @SuppressWarnings("unchecked")
            Map<Object[], String> testNameQTestMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TESTRAIL_ARGS_MAP);
            if (testNameQTestMap != null) {
                String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
                if (testNameQTestMap.containsKey(testHash) && testNameQTestMap.get(testHash) != null) {
                    dataProviderIds = new ArrayList<String>(Arrays.asList(testNameQTestMap.get(testHash).split(",")));
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
                if (testMethod.isAnnotationPresent(QTestCases.class)) {
                    QTestCases methodAnnotation = testMethod.getAnnotation(QTestCases.class);
                    String platform = methodAnnotation.platform();
                    String language = methodAnnotation.language();
                    String locale = methodAnnotation.locale();
                    if (isValidPlatform(platform) && isValidLanguage(language) && isValidLocale(locale)) {
                        String[] testCaseList = methodAnnotation.id().split(",");
                        for (String tcase : testCaseList) {
                            String uuid = tcase;
                            testCases.add(uuid);
                            LOGGER.debug("qTest test case uuid '" + uuid + "' is registered.");
                        }

                    }
                }
                if (testMethod.isAnnotationPresent(QTestCases.List.class)) {
                    QTestCases.List methodAnnotation = testMethod.getAnnotation(QTestCases.List.class);
                    for (QTestCases tcLocal : methodAnnotation.value()) {

                        String platform = tcLocal.platform();
                        String language = tcLocal.language();
                        String locale = tcLocal.locale();
                        if (isValidPlatform(platform) && isValidLanguage(language) && isValidLocale(locale)) {
                            String[] testCaseList = tcLocal.id().split(",");
                            for (String tcase : testCaseList) {
                                String uuid = tcase;
                                testCases.add(uuid);
                                LOGGER.debug("qTest test case uuid '" + uuid + "' is registered.");
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


    default int getQTestProjectId(ITestContext context) {
        String id = context.getSuite().getParameter(SpecialKeywords.QTEST_PROJECT_ID);
        if (id != null) {
            return Integer.valueOf(id.trim());
        } else {
            return -1;
        }
    }
    
}