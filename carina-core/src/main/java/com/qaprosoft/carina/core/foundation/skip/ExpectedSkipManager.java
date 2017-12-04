package com.qaprosoft.carina.core.foundation.skip;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.rule.IRule;

public class ExpectedSkipManager {

    private static final Logger LOGGER = Logger.getLogger(ExpectedSkipManager.class);

    private static ExpectedSkipManager instance = null;

    private ExpectedSkipManager() {
    };

    public static ExpectedSkipManager getInstance() {
        if (null == instance) {
            synchronized (ExpectedSkipManager.class) {
                if (null == instance) {
                    instance = new ExpectedSkipManager();
                }
            }
        }
        return instance;
    }

    /**
     * Return decision whether this tests should be skipped or not - based on
     * rules
     * 
     * @param testMethod test method annotated with @ExpectedSkip
     * @param context tests context which is used for rules collection from
     * initial and dependent methods
     * @return isSkip decision whether test should be skipped
     */
    public boolean isSkip(Method testMethod, ITestContext context) {
        for (Class<? extends IRule> rule : collectRules(testMethod, context)) {
            try {
                if (rule.newInstance().isPerform()) {
                    LOGGER.info("Test execution will be skipped due to following rule: ".concat(rule.getName()));
                    return true;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("Error during skip rules initialization: ".concat(rule.getName()));
                LOGGER.error("Error msg: ".concat(e.getMessage()), e);
            }
        }
        return false;
    }

    /**
     * Collect rules based on tests and its context
     * 
     * @param testMethod
     * @param context
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
     * @param annotation
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
     * @param methodName
     * @return rules list
     */
    private List<Class<? extends IRule>> getDependentMethodsRules(String methodName) {
        int indexDot = methodName.lastIndexOf(".");
        String clazz = methodName.substring(0, indexDot);
        String shortName = methodName.substring(indexDot + 1);
        List<Class<? extends IRule>> rules = new ArrayList<>();
        try {
            LOGGER.debug("Extracted class name: ".concat(clazz));
            Class<?> testClass = Class.forName(clazz);
            // Class marked with @ExpectedSkip and it applies on all tests
            // methods within
            // this class
            if (testClass.isAnnotationPresent(ExpectedSkip.class)) {
                LOGGER.debug("Class is annotated with @ExpectedSkip: ".concat(clazz));
                rules.addAll(Arrays.asList(testClass.getAnnotation(ExpectedSkip.class).rules()));
            }
            Method[] methods = testClass.getDeclaredMethods();
            // verify if dependent method is marked as expected skip
            for (Method method : methods) {
                if (shortName.equalsIgnoreCase(method.getName()) && method.isAnnotationPresent(ExpectedSkip.class)) {
                    LOGGER.debug("Method is annotated with @ExpectedSkip: ".concat(methodName));
                    rules.addAll(Arrays.asList(method.getAnnotation(ExpectedSkip.class).rules()));
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error during class initialization: ".concat(e.getMessage()));
        }
        return rules;
    }

}
