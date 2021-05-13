package com.qaprosoft.carina.core.foundation.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.utils.tag.TestTag;

public class TagFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        String tagName;
        String tagValue;

        if (testMethod != null) {
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(TestTag.class)) {
                TestTag methodAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestTag.class);

                if (methodAnnotation != null) {
                    tagName = methodAnnotation.name();
                    tagValue = methodAnnotation.value();
                    String tag = tagName + "=" + tagValue;
//                    LOGGER.info(String.format("Test: [%s]. Tag: [%s]. Expected tag: [%s]", testMethod.getMethodName(), tag, expectedData.toString()));
//                    return expectedData.parallelStream().anyMatch(d -> d.equalsIgnoreCase(tag));
                    return ruleCheck(rules, tag);
                }
            }

            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(TestTag.List.class)) {
                TestTag.List methodAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(TestTag.List.class);
                if (methodAnnotation!=null) {
                    List<String> tags = new ArrayList<String>();
                    for (TestTag tag : methodAnnotation.value()) {
                        tagName = tag.name();
                        tagValue = tag.value();
                        String fullTag = tagName + "=" + tagValue;
                        tags.add(fullTag.toLowerCase());
                    }
                    return ruleCheck(rules, tags);
//                    LOGGER.info(
//                            String.format("Test: [%s]. Tag: [%s]. Expected tag: [%s]", testMethod.getMethodName(), tags, expectedData.toString()));
//                    return expectedData.parallelStream().anyMatch(d -> tags.stream().anyMatch( tag -> tag.equalsIgnoreCase(d)));
                    }
                }
            }
        return false;
    }

}
