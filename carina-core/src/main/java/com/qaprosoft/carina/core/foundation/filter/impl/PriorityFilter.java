package com.qaprosoft.carina.core.foundation.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;

public class PriorityFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        TestPriority priority = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestPriority.class);
        if (priority != null) {
            Priority testPriority = priority.value();
            String actualTestPriority = testPriority.toString();
            LOGGER.info(String.format("Test: [%s]. Priority: [%s]. Expected priority: [%s]", testMethod.getMethodName(), actualTestPriority,
                    rules.toString()));
            return ruleCheck(rules, actualTestPriority);
        }
        return false;
    }

}
