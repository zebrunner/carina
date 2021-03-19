package com.qaprosoft.carina.core.foundation.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.utils.tag.TestTag;

public class TagFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> expectedData) {
        String tagName;
        String tagValue;

        if (testMethod != null) {
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(TestTag.class)) {
                TestTag methodAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestTag.class);

                if (methodAnnotation != null) {
                    tagName = methodAnnotation.name();
                    tagValue = methodAnnotation.value();
                    String tag = tagName + "=" + tagValue;
                    LOGGER.info(String.format("Test: [%s]. Tag: [%s]. Expected tag: [%s]", testMethod.getMethodName(), tag, expectedData.toString()));
                    return expectedData.parallelStream().anyMatch(d -> d.equalsIgnoreCase(tag));
                }
            }

            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(TestTag.List.class)) {
                TestTag.List methodAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestTag.List.class);
                for (TestTag tagLocal : methodAnnotation.value()) {
                    if (tagLocal != null) {
                        tagName = tagLocal.name();
                        tagValue = tagLocal.value();
                        String tag = tagName + "=" + tagValue;
                        LOGGER.info(
                                String.format("Test: [%s]. Tag: [%s]. Expected tag: [%s]", testMethod.getMethodName(), tag, expectedData.toString()));
                        return expectedData.parallelStream().anyMatch(d -> d.equalsIgnoreCase(tag));
                    }
                }
            }
        }
        return false;
    }

}
