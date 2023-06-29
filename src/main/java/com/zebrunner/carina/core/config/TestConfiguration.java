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
        TEST_RUN_RULES("test_run_rules");

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
