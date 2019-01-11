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

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class PriorityManager {
    protected static final Logger LOGGER = Logger.getLogger(PriorityManager.class);

    private PriorityManager() {
    }

    public static String getPriority(ITestResult result) {

        // Get a handle to the class and method
        Class<?> testClass;
        String priority = getSuitePriority(result.getTestContext());

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

            if (testMethod != null && testMethod.isAnnotationPresent(TestPriority.class)) {
                TestPriority methodAnnotation = testMethod.getAnnotation(TestPriority.class);
                priority = methodAnnotation.value().name();
                LOGGER.debug("Method '" + testMethod + "' priority is: " + priority);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(e);
        }
        return priority;
    }

    private static String getSuitePriority(ITestContext context) {
        String priority = context.getSuite().getParameter("suitePriority");
        LOGGER.debug("suitePriority is: " + priority);
        if (priority == null) {
            priority = "";
        }
        return priority;
    }

}