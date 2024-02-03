package com.zebrunner.carina.core.filter.v2;

import org.testng.ITestNGMethod;

/**
 * Default tests filter
 */
public final class NoOpTestRunFilter implements ITestRunFilter {

    @Override
    public boolean perform(ITestNGMethod testMethod) {
        return true;
    }
}
