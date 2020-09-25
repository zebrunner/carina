package com.qaprosoft.carina.core.foundation.utils;

import org.testng.ITestResult;

import com.qaprosoft.carina.core.foundation.listeners.TestNamingService;
import com.zebrunner.agent.testng.core.testname.TestNameResolver;

public class ZebrunnerNameResolver implements TestNameResolver {

    @Override
    public String resolve(ITestResult result) {
        return TestNamingService.getTestName(result);
    }

}
