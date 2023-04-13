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
package com.zebrunner.carina.core.skip;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.core.rule.IRule;

public class ExpectedSkipManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static ExpectedSkipManager instance = null;

    private ExpectedSkipManager() {
        // do nothing
    }

    public static synchronized ExpectedSkipManager getInstance() {
        if (null == instance) {
            instance = new ExpectedSkipManager();
        }
        return instance;
    }

    /**
     * Return decision whether this tests should be skipped or not - based on
     * rules
     * 
     * @param testMethod test method annotated with @ExpectedSkip
     * @param context tests context which is used for rules collection from
     *            initial and dependent methods
     * @return isSkip decision whether test should be skipped
     */
    public boolean isSkip(Method testMethod, ITestContext context) {
        for (Class<? extends IRule> rule : collectRules(testMethod, context)) {
            try {
                if (rule.newInstance().isPerform()) {
                    LOGGER.info("Test execution will be skipped due to following rule: {}", rule.getName());
                    return true;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("Error during skip rules initialization: ".concat(rule.getName()));
                LOGGER.error("Error msg: ", e);
            }
        }
        return false;
    }

    /**
     * Collect rules based on tests and its context
     * 
     * @param testMethod Method
     * @param context ITestContext
     * @return rules list
     */
    private List<Class<? extends IRule>> collectRules(Method testMethod, ITestContext context) {
        List<Class<? extends IRule>> rules = new ArrayList<>();
        // collect rules from current class and method
        ExpectedSkip classSkipAnnotation = testMethod.getDeclaringClass().getAnnotation(ExpectedSkip.class);
        ExpectedSkip methodSkipAnnotation = testMethod.getAnnotation(ExpectedSkip.class);
        rules.addAll(getRulesFromAnnotation(classSkipAnnotation));
        rules.addAll(getRulesFromAnnotation(methodSkipAnnotation));

        // analyze all dependent methods and collect rules
        ITestNGMethod[] methods = context.getAllTestMethods();
        for (ITestNGMethod iTestNGMethod : methods) {
            if (iTestNGMethod.getMethodName().equalsIgnoreCase(testMethod.getName())) {
                String[] methodsDep = iTestNGMethod.getMethodsDependedUpon();
                for (String method : methodsDep) {
                    rules.addAll(getDependentMethodsRules(method));
                }
            }
        }

        return rules;
    }

    /**
     * Get rules from annotation
     * 
     * @param annotation ExpectedSkip
     * @return rules list
     */
    private List<Class<? extends IRule>> getRulesFromAnnotation(ExpectedSkip annotation) {
        List<Class<? extends IRule>> rules = new ArrayList<>();
        if (annotation != null) {
            rules.addAll(Arrays.asList(annotation.rules()));
        }
        return rules;
    }

    /**
     * Get rules from dependent methods and their classes
     * 
     * @param methodName String
     * @return rules list
     */
    private List<Class<? extends IRule>> getDependentMethodsRules(String methodName) {
        int indexDot = methodName.lastIndexOf(".");
        String clazz = methodName.substring(0, indexDot);
        String shortName = methodName.substring(indexDot + 1);
        List<Class<? extends IRule>> rules = new ArrayList<>();
        try {
            LOGGER.debug("Extracted class name: {}", clazz);
            Class<?> testClass = Class.forName(clazz);
            // Class marked with @ExpectedSkip and it applies on all tests
            // methods within
            // this class
            if (testClass.isAnnotationPresent(ExpectedSkip.class)) {
                LOGGER.debug("Class is annotated with @ExpectedSkip: {}", clazz);
                rules.addAll(Arrays.asList(testClass.getAnnotation(ExpectedSkip.class).rules()));
            }
            Method[] methods = testClass.getDeclaredMethods();
            // verify if dependent method is marked as expected skip
            for (Method method : methods) {
                if (shortName.equalsIgnoreCase(method.getName()) && method.isAnnotationPresent(ExpectedSkip.class)) {
                    LOGGER.debug("Method is annotated with @ExpectedSkip: {}", methodName);
                    rules.addAll(Arrays.asList(method.getAnnotation(ExpectedSkip.class).rules()));
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error during class initialization: ".concat(e.getMessage()));
        }
        return rules;
    }

}
