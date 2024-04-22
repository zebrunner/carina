package com.zebrunner.carina.core.config;

import java.util.Optional;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.utils.config.IParameter;

public class TestConfiguration extends Configuration {

    public enum Parameter implements IParameter {

        /**
         * If enabled, turns off WebDriver quit based on initizalization phase. <b>Default: {@code false}</b>.
         * 
         * @see <a href="https://zebrunner.github.io/carina/advanced/driver/#quit">initialization phase</a>
         */
        FORCIBLY_DISABLE_DRIVER_QUIT("forcibly_disable_driver_quit"),

        /**
         * Path to the properties file with custom key-value capabilities.
         * Example: {@code zebrunner/chrome.properties}
         */
        CUSTOM_CAPABILITIES("custom_capabilities"),

        /**
         * Number of test-retryings in case of failure. <b>Default: 0 means that a test will be performed only once</b>
         */
        RETRY_COUNT("retry_count"),

        /**
         * Number of threads to use when running tests in parallel. <b>Default: -1 to use value from TestNG suite xml</b>
         */
        THREAD_COUNT("thread_count"),

        /**
         * Number of threads to use for data providers when running tests in parallel. <b>Default: -1 to use value from TestNG suite xml.</b>
         */
        DATA_PROVIDER_THREAD_COUNT("data_provider_thread_count"),

        /**
         * Executing rules logic
         */
        TEST_RUN_RULES("test_run_rules"),

        /**
         * When this option is enabled, tests are filtered by the countries for which they are intended.
         * Countries should be specified in the `groups` parameter of the `Test` annotation, for example {@code groups = "US"}.
         * Accordingly, if 'groups' does not contain the current 'locale' value of the test run, then it will be disabled.
         */
        FILTER_BY_COUNTRY("filter_by_country"),

        /**
         * The pattern by which test(s) should be selected. When using this option, the {@link #FILTER_BY_COUNTRY} parameter will be ignored.
         * <b>Examples:</b><br>
         * Run single test: TestClass#testMethodName.<br>
         * Run all tests in the class: TestClass#.<br>
         * Run several tests: TestClass#testMethodName1, TestClass#testMethodName2.
         */
        FILTER_PATTERN("filter_pattern");

        private final String key;

        Parameter(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    @Override
    public String toString() {
        Optional<String> asString = asString(Parameter.values());
        if (asString.isEmpty()) {
            return "";
        }
        return "\n============= Test configuration ==============\n" +
                asString.get();
    }
}
