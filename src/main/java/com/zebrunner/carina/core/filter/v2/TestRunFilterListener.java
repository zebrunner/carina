package com.zebrunner.carina.core.filter.v2;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.core.config.TestConfiguration;
import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.exception.InvalidConfigurationException;

@API(status = API.Status.INTERNAL)
public class TestRunFilterListener implements ISuiteListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void onStart(ISuite suite) {
        Configuration.get(TestConfiguration.Parameter.TEST_RUN_FILTERS).ifPresent(testRunFilterClass -> {
            List<ITestRunFilter> filters = new ArrayList<>();

            Arrays.stream(StringUtils.split(testRunFilterClass, ",")).forEach(name -> {
                try {
                    Class<?> clazz = Class.forName(name);
                    if (!ITestRunFilter.class.isAssignableFrom(clazz)) {
                        throw new InvalidConfigurationException(String.format("Invalid '%s' value - '%s' class should implements '%s' interface.",
                                TestConfiguration.Parameter.TEST_RUN_FILTERS.getKey(),
                                testRunFilterClass,
                                ITestRunFilter.class));
                    }
                    Constructor<?> constructor = ConstructorUtils.getAccessibleConstructor(clazz);
                    if (constructor == null) {
                        throw new InvalidConfigurationException(
                                String.format("Unable to create instance of '%s' test run filter class. No default constructor found.",
                                        testRunFilterClass));
                    }
                    filters.add((ITestRunFilter) constructor.newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    ExceptionUtils.rethrow(e);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Unable to find '{}' test run filter class.", testRunFilterClass, e);
                    ExceptionUtils.rethrow(e);
                }
            });

            for (ITestNGMethod testMethod : suite.getAllMethods()) {
                for (ITestRunFilter filter : filters) {
                    if (!filter.perform(testMethod)) {
                        testMethod.setInvocationCount(0);
                    }
                }
            }
        });
    }
}
