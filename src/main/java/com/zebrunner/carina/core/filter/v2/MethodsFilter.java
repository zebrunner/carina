package com.zebrunner.carina.core.filter.v2;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.zebrunner.carina.core.config.TestConfiguration;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.utils.config.Configuration;

public class MethodsFilter implements ITestFilter {

    private static final LazyInitializer<Boolean> IS_PERFORM = new LazyInitializer<>() {
        @Override
        protected Boolean initialize() throws ConcurrentException {
            return Configuration.get(TestConfiguration.Parameter.FILTER_METHOD_PATTERN)
                    .isPresent();
        }
    };

    private static final LazyInitializer<List<Pattern>> PATTERN = new LazyInitializer<>() {
        @Override
        protected List<Pattern> initialize() throws ConcurrentException {
            return Arrays.stream(Configuration.getRequired(TestConfiguration.Parameter.FILTER_METHOD_PATTERN).split(","))
                    .map(Pattern::compile)
                    .collect(Collectors.toList());
        }
    };

    @Override
    public String key() {
        return "methods";
    }

    @Override
    public boolean isPerform(ITestNGMethod testMethod) {
        try {
            if (!IS_PERFORM.get()) {
                return true;
            }
            for (Pattern pattern : PATTERN.get()) {
                if (testMethod != null && pattern.matcher(testMethod.getRealClass().getSimpleName() + "#" + testMethod.getMethodName())
                        .matches()) {
                    return true;
                }
            }
            return false;
        } catch (ConcurrentException e) {
            return ExceptionUtils.rethrow(e);
        }
    }
}
