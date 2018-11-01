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
package com.qaprosoft.carina.core.foundation.utils.ownership;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class Ownership {
    protected static final Logger LOGGER = Logger.getLogger(Ownership.class);

    private static final String SECOND_OWNER_DEFAULT_PLATFORM = "IOS";

    public enum OwnerType {
        PRIMARY,
        SECONDARY,
        PLATFORM
    }

    private Ownership() {
    }

    public static String getMethodOwner(ITestResult result, OwnerType type) {

        @SuppressWarnings("unchecked")
        Map<Object[], String> testMethodOwnerArgsMap = (Map<Object[], String>) result.getTestContext()
                .getAttribute(SpecialKeywords.TEST_METHOD_OWNER_ARGS_MAP);
        if (testMethodOwnerArgsMap != null) {
            String testHash = String.valueOf(Arrays.hashCode(result.getParameters()));
            if (testMethodOwnerArgsMap.containsKey(testHash) && testMethodOwnerArgsMap.get(testHash) != null) {
                return testMethodOwnerArgsMap.get(testHash);
            }
        }

        // Get a handle to the class and method
        Class<?> testClass;
        String owner = "";
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
                if (testMethod.isAnnotationPresent(MethodOwner.class)) {
                    MethodOwner methodAnnotation = testMethod.getAnnotation(MethodOwner.class);
                    owner = returnCorrectOwner(methodAnnotation.owner(), methodAnnotation.secondaryOwner(), methodAnnotation.platform(), type);
                }
                if (testMethod.isAnnotationPresent(MethodOwner.List.class)) {
                    MethodOwner.List methodAnnotation = testMethod.getAnnotation(MethodOwner.List.class);
                    for (MethodOwner local : methodAnnotation.value()) {

                        String localOwner = returnCorrectOwner(local.owner(), local.secondaryOwner(), local.platform(), type);
                        if (!localOwner.isEmpty()) {
                            owner = localOwner;
                        }
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            LOGGER.error(e);
        }
        return owner;
    }

    public static String getSuiteOwner(ITestContext context) {
        String owner = context.getSuite().getParameter("suiteOwner");
        if (owner == null) {
            owner = "";
        }
        return owner;
    }


    private static String returnCorrectOwner(String primaryOwner, String secondOwner, String platform, OwnerType type) {
        String owner = "";
        if (platform.isEmpty()) {
            switch (type) {
                case PRIMARY:
                    owner = primaryOwner;
                    LOGGER.debug("Method primary owner is: " + owner);
                    return owner;

                case SECONDARY:
                    owner = secondOwner;
                    LOGGER.debug("Method secondary owner is: " + owner);
                    return owner;
                default:
                    String currentPlatform = getCurrentPlatform();
                    if (!currentPlatform.isEmpty()) {
                        if (currentPlatform.equalsIgnoreCase(SECOND_OWNER_DEFAULT_PLATFORM)) {
                            LOGGER.debug("Method owner for platform '" + SECOND_OWNER_DEFAULT_PLATFORM + "' is: " + secondOwner);
                            return secondOwner;
                        }
                    }
                    return primaryOwner;
            }
        } else {
            String currentPlatform = getCurrentPlatform();
            if (!currentPlatform.isEmpty()) {
                if (currentPlatform.equalsIgnoreCase(platform)) {
                    LOGGER.debug("Method owner for platform '" + platform + "' is: " + primaryOwner);
                    return primaryOwner;
                }
            }
        }
        return owner;
    }

    private static String getCurrentPlatform() {
        String platform = Configuration.getPlatform();
        LOGGER.debug(platform);
        return platform;
    }
}