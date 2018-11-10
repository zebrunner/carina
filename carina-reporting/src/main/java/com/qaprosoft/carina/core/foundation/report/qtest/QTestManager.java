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
package com.qaprosoft.carina.core.foundation.report.qtest;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class QTestManager {
    protected static final Logger LOGGER = Logger.getLogger(QTestManager.class);

    private QTestManager() {
    }

    public static Set<String> getTestCases(ITestResult result) {

        // Get a handle to the class and method
        Class<?> testClass;
        Set<String> testCases = new HashSet<String>();
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
                if (testMethod.isAnnotationPresent(QTestTestCase.class)) {
                	QTestTestCase methodAnnotation = testMethod.getAnnotation(QTestTestCase.class);
                	if (isSupportedPlatform(methodAnnotation.platform())) {
	                	testCases.add(methodAnnotation.id());
	                	LOGGER.debug("qTest test case id '" + methodAnnotation.id() + "' is registered.");
                	}
                }
                if (testMethod.isAnnotationPresent(QTestTestCase.List.class)) {
                	QTestTestCase.List methodAnnotation = testMethod.getAnnotation(QTestTestCase.List.class);
                    for (QTestTestCase tcLocal : methodAnnotation.value()) {
                    	if (isSupportedPlatform(tcLocal.platform())) {
                    		testCases.add(tcLocal.id());
                    		LOGGER.debug("qTest test case id '" + tcLocal.id() + "' is registered.");
                    	}
                   }
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(e);
        }
        return testCases;
    }
    
    
    private static boolean isSupportedPlatform(String platform) {
    	// in case of platform absence (empty) we suppose to have platform independent testcase id annotation(s) 
    	return platform.equals(Configuration.getPlatform()) || platform.isEmpty();
    }

}