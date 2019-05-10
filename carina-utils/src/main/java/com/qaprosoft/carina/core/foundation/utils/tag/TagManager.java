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
package com.qaprosoft.carina.core.foundation.utils.tag;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

public class TagManager {
    protected static final Logger LOGGER = Logger.getLogger(TagManager.class);

    private static final ThreadLocal<HashMap<String, String>> testTags = ThreadLocal.withInitial(HashMap::new);
    private static final String FORBIDDEN_TAG_NAMES = "priority";

    private TagManager() {
    }

    /**
     * get all test tags from annotations and defined via code to be register in Zafira.
     *
     * @param result - ITestResult
     * @return Map of tags
     */
    public static Map<String, String> getTags(ITestResult result) {

        // Get a handle to the class and method
        Class<?> testClass;
        //get custom tags which are set via code
        HashMap<String, String> tag = testTags.get();
        
        // append tags from annotations if any
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
                if (testMethod.isAnnotationPresent(TestTag.class)) {
                    TestTag methodAnnotation = testMethod.getAnnotation(TestTag.class);
                    if (isValid(methodAnnotation.name())) {
                        tag.put(methodAnnotation.name(), methodAnnotation.value());
                        LOGGER.debug("Method '" + testMethod + "' tag pair: " + methodAnnotation.name() + " : " + methodAnnotation.value());
                    }
                }
                if (testMethod.isAnnotationPresent(TestTag.List.class)) {
                    TestTag.List methodAnnotation = testMethod.getAnnotation(TestTag.List.class);
                    for (TestTag tagLocal : methodAnnotation.value()) {
                        if (isValid(tagLocal.name())) {
                            tag.put(tagLocal.name(), tagLocal.value());
                            LOGGER.debug("Method '" + testMethod + "' tag pair: " + tagLocal.name() + " : " + tagLocal.value());
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(e);
        } finally {
            // remove all tags from current thread as information put to zafira by current method
            testTags.remove();
        }
        return tag;
    }

    /**
     * Add tags via code to current Method/Thread.
     *
     * @param tags - Map of tags/values
     */
    public static void add(HashMap<String, String> tags) {
        HashMap<String, String> curTags = testTags.get();
        curTags.putAll(tags);
        testTags.set(curTags);
    }
    
    /**
     * Add tag via code to current Method/Thread.
     *
     * @param name - String
     * @param value - String
     */
    public static void add(String name, String value) {
        HashMap<String, String> tags = testTags.get();
        // append to existing map
        tags.put(name,  value);
        testTags.set(tags);
    }    

    private static boolean isValid(String content) {
        Pattern pattern = Pattern.compile(FORBIDDEN_TAG_NAMES);
        if (content != null) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                LOGGER.error("TestTag name contains one of the forbidden tag names: " + content);
                return false;
            }
        }
        return true;
    }

}