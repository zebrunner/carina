package com.zebrunner.carina.core.filter.v2;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.testng.ITestNGMethod;

public class MethodsFilter implements ITestFilter {

    private final List<Pattern> patterns;

    public MethodsFilter(String pattern) {
        patterns = Arrays.stream(pattern.split(","))
                .map(StringUtils::trim)
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPerform(ITestNGMethod testMethod) {
        for (Pattern pattern : patterns) {
            if (testMethod != null && pattern.matcher(testMethod.getRealClass().getSimpleName() + "#" + testMethod.getMethodName())
                    .find()) {
                return true;
            }
        }
        return false;
    }
}
