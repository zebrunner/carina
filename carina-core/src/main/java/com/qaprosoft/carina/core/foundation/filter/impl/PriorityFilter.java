package com.qaprosoft.carina.core.foundation.filter.impl;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.utils.tag.Priority;
import com.qaprosoft.carina.core.foundation.utils.tag.TestPriority;
import org.apache.log4j.Logger;
import org.testng.ITestNGMethod;

import java.util.List;

public class PriorityFilter implements IFilter {

    protected static final Logger LOGGER = Logger.getLogger(PriorityFilter.class);

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> expectedData) {
        TestPriority priority = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestPriority.class);
        if (priority != null) {
            Priority testPriority = priority.value();
            String actualTestPriority = testPriority.toString();
            LOGGER.info(String.format("Test: [%s]. Priority: [%s]. Expected priority: [%s]", testMethod.getMethodName(), actualTestPriority,
                    expectedData.toString()));
            return expectedData.parallelStream().anyMatch(d -> d.equals(actualTestPriority));
        }
        return false;
    }

}
