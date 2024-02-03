package com.zebrunner.carina.core.filter.v2;

import com.zebrunner.carina.core.config.TestConfiguration;
import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.exception.InvalidConfigurationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@API(status = API.Status.INTERNAL)
public class TestRunFilterListener implements ISuiteListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void onStart(ISuite suite) {
        String testRunFilterClass = Configuration.getRequired(TestConfiguration.Parameter.TEST_RUN_FILTER);
        Class<?> clazz = null;
        try {
            clazz = Class.forName(testRunFilterClass);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Unable to find '{}' test run filter class.", testRunFilterClass, e);
            ExceptionUtils.rethrow(e);
        }
        if (!ITestRunFilter.class.isAssignableFrom(clazz)) {
            throw new InvalidConfigurationException(String.format("Invalid '%s' value - '%s' class should implements '%s' interface.",
                    TestConfiguration.Parameter.TEST_RUN_FILTER.getKey(),
                    testRunFilterClass,
                    ITestRunFilter.class));
        }
        Constructor<?> constructor = ConstructorUtils.getAccessibleConstructor(clazz);
        if (constructor == null) {
            throw new InvalidConfigurationException(
                    String.format("Unable to create instance of '%s' test run filter class. No default constructor found.",
                            testRunFilterClass));
        }
        try {
            ITestRunFilter filter = (ITestRunFilter) constructor.newInstance();
            for (ITestNGMethod testMethod : suite.getAllMethods()) {
                if (!filter.perform(testMethod)) {
                    testMethod.setInvocationCount(0);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ExceptionUtils.rethrow(e);
        }
    }
}
