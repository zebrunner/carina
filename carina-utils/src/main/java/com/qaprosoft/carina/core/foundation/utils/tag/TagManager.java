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
package com.qaprosoft.carina.core.foundation.utils.tag;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.HashMap;

public class TagManager {
    protected static final Logger LOGGER = Logger.getLogger(TagManager.class);

    private TagManager() {
    }

    public static HashMap<String, String> getTag(ITestResult result) {

        // Get a handle to the class and method
        Class<?> testClass;
        HashMap<String, String> tag = new HashMap<>();
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
                    tag.put(methodAnnotation.name(), methodAnnotation.value());
                    LOGGER.debug(methodAnnotation.name() + " : " + methodAnnotation.value());
                }
                if (testMethod.isAnnotationPresent(TestTag.List.class)) {
                    TestTag.List methodAnnotation = testMethod.getAnnotation(TestTag.List.class);
                    for (TestTag tagLocal : methodAnnotation.value()) {
                        tag.put(tagLocal.name(), tagLocal.value());
                        LOGGER.debug(tagLocal.name() + " : " + tagLocal.value());
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            LOGGER.error(e);
        }
        return tag;
    }

}