package com.zebrunner.carina.core.filter.v2;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.testng.ITestNGMethod;

import com.zebrunner.carina.webdriver.config.WebDriverConfiguration;

public class CountryFilter implements ITestFilter {

    private static final LazyInitializer<String> COUNTRY = new LazyInitializer<>() {
        @Override
        protected String initialize() throws ConcurrentException {
            return WebDriverConfiguration.getLocale()
                    .getCountry();
        }
    };

    @Override
    public boolean isPerform(ITestNGMethod testMethod) {
        try {
            String country = COUNTRY.get();
            return testMethod != null && testMethod.getGroups().length > 0 &&
                    Arrays.stream(testMethod.getGroups())
                            .anyMatch(group -> StringUtils.equals(country, group));
        } catch (ConcurrentException e) {
            return ExceptionUtils.rethrow(e);
        }
    }
}
