/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
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

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.zafira.models.dto.TagType;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.*;

//TODO: howto init testrail testcase uuid values from dataProvider?
//Works now with annotation and csv/xls files.
public class TestRailManager {
    protected static final Logger LOGGER = Logger.getLogger(TestRailManager.class);

    private TestRailManager() {
    }

    public static Set<String> getTestCasesUuid(ITestResult result) {
        Set<String> testCases = new HashSet<String>();

        List<String> dataProviderIds = getCasesIdFromDataProvider(result);

        testCases.addAll(dataProviderIds);

        LOGGER.debug(dataProviderIds);

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
                    if (isSupportedPlatform(methodAnnotation.platform())) {
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
                        if (isSupportedPlatform(tcLocal.platform())) {
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
            LOGGER.error(e);
        }
        return testCases;
    }

    public static int getProjectId(ITestContext context) {
        String id = context.getSuite().getParameter(SpecialKeywords.TESTRAIL_PROJECT_ID);
        if (id != null) {
            return Integer.valueOf(id.trim());
        } else {
            return -1;
        }
    }

    public static int getSuiteId(ITestContext context) {
        String id = context.getSuite().getParameter(SpecialKeywords.TESTRAIL_SUITE_ID);
        if (id != null) {
            return Integer.valueOf(id.trim());
        } else {
            return -1;
        }
    }

    //TODO: Think where to add it in ZafiraListener and how to show it.
    //From other side it may be possible to reuse getArtifacts.
    public static Set<TagType> addTestRailTagsAfterTest(ITestResult test, boolean debug) {
        Set<TagType> tags = new HashSet<TagType>();

        Set<String> testCases = new HashSet<String>();

        int projectID = TestRailManager.getProjectId(test.getTestContext());
        int suiteID = TestRailManager.getSuiteId(test.getTestContext());

        testCases = getTestCasesUuid(test);

        //Collect testrail ids from new annotations
        LOGGER.debug(testCases.toString());

        //Collect all testrail ids from Dataprovider, setCases, XLS, etc
        List<String> testRailIds = TestRail.getCases(test);

        LOGGER.debug(testRailIds.toString());

        //Remove duplicates.
        testRailIds.removeAll(testCases);

        LOGGER.debug("Missed testrail ids: " + testRailIds.toString());

        if (projectID != -1 && suiteID != -1 || debug) {
            testRailIds.forEach((entry) -> {
                TagType tagEntry = new TagType();
                tagEntry.setName(SpecialKeywords.TESTRAIL_TESTCASE_UUID);
                tagEntry.setValue(projectID + "-" + suiteID + "-" + entry);
                tags.add(tagEntry);
            });
        }
        LOGGER.info("Found " + tags.size() + " new TestRailIds");
        return tags;
    }


    private static List<String> getCasesIdFromDataProvider(ITestResult result) {
        List<String> cases = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        Map<Object[], String> testNameTestRailMap = (Map<Object[], String>) result.getTestContext().getAttribute(SpecialKeywords.TESTRAIL_ARGS_MAP);
        if (testNameTestRailMap != null) {
            String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
            if (testNameTestRailMap.containsKey(testHash) && testNameTestRailMap.get(testHash) != null) {
                cases = new ArrayList<String>(Arrays.asList(testNameTestRailMap.get(testHash).split(",")));
            }
        }
        return cases;
    }


    private static boolean isSupportedPlatform(String platform) {
        // in case of platform absence (empty) we suppose to have platform independent testcase id annotation(s)
        return platform.equalsIgnoreCase(Configuration.getPlatform()) || platform.isEmpty();
    }

}