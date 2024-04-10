package com.zebrunner.carina.core.filter.v2;

import org.testng.ITestNGMethod;

import com.zebrunner.carina.utils.exception.InvalidConfigurationException;

public interface ITestFilter {

    boolean isPerform(ITestNGMethod testMethod);

    default String key() {
        throw new InvalidConfigurationException("Filter should have key.");
    }

}
