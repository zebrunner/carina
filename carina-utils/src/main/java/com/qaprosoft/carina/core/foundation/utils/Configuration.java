/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.utils;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.zebrunner.agent.core.registrar.CurrentTestRun;

/**
 * Configuration utility.
 * 
 * @author Aliaksei_Khursevich
 *         hursevich@gmail.com
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static IEnvArgResolver envArgResolver = new DefaultEnvArgResolver();

    /**
     * All available configuration parameter keys along with default values.
     */
    public enum Parameter {
        /**
         * Base application URL
         */
        URL("url", false),

        /**
         * Environment specific configuration
         */
        ENV("env", true),

        /**
         * Browser for testing
         */
        BROWSER("browser", true),

        /**
         * Browser language
         * If it is empty - english version used by default
         */
        BROWSER_LANGUAGE("browser_language", false),

        /**
         * Selenium/Appium server url
         */
        SELENIUM_URL("selenium_url", true),

        /**
         * Comma-separated list of extra driver listeners.
         * Listeners provide extra custom actions for WebDriver and have
         * to be the instances of WebDriverEventListener
         */
        DRIVER_EVENT_LISTENERS("driver_event_listeners", false),

        /**
         * Max number of drivers per thread.
         */
        MAX_DRIVER_COUNT("max_driver_count", false),

        /**
         * If enabled turns off webdriver shutdown after test finishing by any reason.
         */
        FORCIBLY_DISABLE_DRIVER_QUIT("forcibly_disable_driver_quit", false),

        /**
         * Name of a properties file with custom capabilities (key-value)
         */
        CUSTOM_CAPABILITIES("custom_capabilities", false),

        /**
         * todo add description
         */
        CHROME_ARGS("chrome_args", false),

        /**
         * todo add description
         */
        CHROME_EXPERIMENTAL_OPTS("chrome_experimental_opts", false),

        /**
         * todo add description
         */
        CHROME_MOBILE_EMULATION_OPTS("chrome_mobile_emulation_opts", false),

        /**
         * todo add description
         */
        CHROME_CLOSURE("chrome_closure", false),

        /**
         * todo add description
         */
        FIREFOX_ARGS("firefox_args", false),

        /**
         * todo add description
         */
        FIREFOX_PREFERENCES("firefox_preferences", false),

        /**
         * Run tests in headless browser mode. Enabled when headless=true.
         */
        HEADLESS("headless", false),

        /**
         * Application version/build number for reporting
         */
        APP_VERSION("app_version", true),

        /**
         * Hostname of the server
         */
        PROXY_HOST("proxy_host", false),

        /**
         * Port number
         */
        PROXY_PORT("proxy_port", false),

        /**
         * Comma-separated list of internet protocols used to carry the connection information
         * from the source requesting the connection to the destination for which the connection
         * was requested.
         */
        PROXY_PROTOCOLS("proxy_protocols", false),

        /**
         * Excluded hostname(s) for communication via proxy. Available only when
         * proxy_host and proxy_port are declared!
         */
        NO_PROXY("no_proxy", false),

        /**
         * Boolean parameter which enables or disables the automatic BrowserMob proxy launch
         */
        BROWSERMOB_PROXY("browsermob_proxy", false),

        /**
         * Port number for BrowserMob proxy
         * (if nothing or 0 specified, then any free port will be reused)
         */
        BROWSERMOB_PORT("browsermob_port", false),

        /**
         * Range of ports that will be used for starting of browsermob proxy.
         * First available port from the range will be used. If all ports are used then test
         * will wait for the first freed port.
         */
        BROWSERMOB_PORTS_RANGE("browsermob_ports_range", false),

        /**
         * todo add description
         */
        BROWSERMOB_MITM("browsermob_disabled_mitm", false),

        /**
         * Boolean parameter which enables or disables the setup of a proxy
         */
        PROXY_SET_TO_SYSTEM("proxy_set_to_system", false),

        /**
         * Direct HTTP link to Jenkins workspace report folder. Automatically specified by CI
         */
        REPORT_URL("report_url", true),

        /**
         * Global switch for taking screenshots. When disabled, screenshots will be captured only after failures
         */
        AUTO_SCREENSHOT("auto_screenshot", false),

        /**
         * Timeout is seconds to wait for a certain condition to occur before proceeding further in the code
         */
        EXPLICIT_TIMEOUT("explicit_timeout", false),

        /**
         * The enabled parameter prevents downloading a dialog and downloading a file automatically.
         * The feature is currently available for Chrome and FireFox
         */
        AUTO_DOWNLOAD("auto_download", false),

        /**
         * MIME types / Internet Media Types. The parameter is needed only to configure auto downloading for Firefox
         */
        AUTO_DOWNLOAD_APPS("auto_download_apps", false),

        /**
         * Path to auto download folder for Chrome and Firefox browsers.
         * If nothing specified custom_artifacts_folder or default artifacts folder is used
         */
        AUTO_DOWNLOAD_FOLDER("auto_download_folder", false),

        /**
         * todo add description
         */
        CUSTOM_ARTIFACTS_FOLDER("custom_artifacts_folder", false),

        /**
         * Timeout interval between calling HTML DOM for the element
         * Note: in ms. For mobile automation specify a number from 500-1000 range
         */
        RETRY_INTERVAL("retry_interval", false),

        /**
         * Path to a folder where the testing report will be saved
         */
        PROJECT_REPORT_DIRECTORY("project_report_directory", true),

        /**
         * Max number of reports artifacts saving in history
         */
        MAX_SCREENSHOOT_HISTORY("max_screen_history", false),

        /**
         * Screenshots will be resized according to this width if their own width is bigger
         */
        BIG_SCREEN_WIDTH("big_screen_width", false),

        /**
         * Screenshots will be resized according to this height if their own height is bigger
         */
        BIG_SCREEN_HEIGHT("big_screen_height", false),

        /**
         * Number of attempts to create a driver
         * The default value 0 means that there will be only 1 attempt
         */
        INIT_RETRY_COUNT("init_retry_count", false),

        /**
         * Interval in seconds between the attempts to create a driver
         */
        INIT_RETRY_INTERVAL("init_retry_interval", false),

        /**
         * Number of test-retryings in case of failure.
         * The default value 0 means that a test will be performed only once
         */
        RETRY_COUNT("retry_count", false),

        /**
         * Locale for using L10N feature
         */
        LOCALE("locale", true),

        /**
         * Default number of threads to use when running tests in parallel.
         * Set thread-count=custom to disable any updates on carina side.
         */
        THREAD_COUNT("thread_count", true),

        /**
         * Default number of threads to use for data providers when running tests in parallel.
         */
        DATA_PROVIDER_THREAD_COUNT("data_provider_thread_count", true),

        /**
         * Level for Carina logging
         */
        CORE_LOG_LEVEL("core_log_level", true),

        /**
         * API response will be logged in JSON format
         */
        LOG_ALL_JSON("log_all_json", false),

        /**
         * Date format for DateUtils.class
         */
        DATE_FORMAT("date_format", false),

        /**
         * Date format for DateUtils.class
         */
        TIME_FORMAT("time_format", false),

        /**
         * Path to a file with a crypto key
         */
        // Do not log by security
        CRYPTO_KEY_PATH("crypto_key_path", false),

        /**
         * Suite name for the report and TestRail. If this parameter is NULL, will
         * be taken from TestNG xml (the parameter suite name) or _email.properties (the title)
         */
        SUITE_NAME("suite_name", false),

        /**
         * todo add description
         */
        TEST_NAMING_PATTERN("test_naming_pattern", false),

        /**
         * Determines how carina detects appearing of web elements on page: by presence in
         * DOM model or by visibility or by any of these conditions
         */
        ELEMENT_LOADING_STRATEGY("element_loading_strategy", true),

        /**
         * Determines how carina detects whether expected page is opened: by expected url pattern,
         * by marker element loading state or by both these conditions
         * BY_ELEMENT, BY_URL, BY_URL_AND_ELEMENT
         */
        PAGE_OPENING_STRATEGY("page_opening_strategy", true),

        /**
         * Amazon
         * todo add description
         */
        S3_BUCKET_NAME("s3_bucket_name", false),

        /**
         * Access key id for Amazon S3 build uploader
         */
        ACCESS_KEY_ID("access_key_id", false),

        /**
         * Secret key for Amazon S3 build uploader
         */
        SECRET_KEY("secret_key", false),

        /**
         * Azure
         * todo add description
         */
        AZURE_ACCOUNT_NAME("azure_account_name", false),

        /**
         * todo add description
         */
        AZURE_CONTAINER_NAME("azure_container_name", false),

        /**
         * todo add description
         */
        AZURE_BLOB_URL("azure_blob_url", false),

        /**
         * todo add description
         */
        AZURE_ACCESS_KEY_TOKEN("azure_access_key_token", false),

        /**
         * todo add description
         */
        AZURE_LOCAL_STORAGE("azure_local_storage", false),

        /**
         * Token for authentication in Hockey App
         */
        APPCENTER_TOKEN("appcenter_token", false),

        //
        /**
         * For localization parser
         * todo add description
         */
        LOCALIZATION_ENCODING("localization_encoding", false),

        /**
         * Enables auto verification for elements that are marked with @Localized
         */
        LOCALIZATION_TESTING("localization_testing", false),

        /**
         * TLS
         * Path to a directory with tls secure keys
         */
        TLS_KEYSECURE_LOCATION("tls_keysecure_location", false),

        /**
         * HealthCheck
         * Class to execute health checks
         */
        HEALTH_CHECK_CLASS("health_check_class", false),

        /**
         * Comma-separated list of methods of health_check_class to execute preliminarily
         */
        HEALTH_CHECK_METHODS("health_check_methods", false),

        /**
         * todo add description
         */
        UNINSTALL_RELATED_APPS("uninstall_related_apps", false),

        /**
         * For Device default timezone and language
         * todo add description
         */
        DEFAULT_DEVICE_TIMEZONE("default_device_timezone", false),

        /**
         * todo add description
         */
        DEFAULT_DEVICE_TIME_FORMAT("default_device_time_format", false),

        /**
         * todo add description
         */
        DEFAULT_DEVICE_LANGUAGE("default_device_language", false),

        /**
         * Ignore SSL
         * API requests/responses to ignore SSL errors.
         */
        IGNORE_SSL("ignore_ssl", false),

        /**
         * Test Execution Filter rules
         */
        TEST_RUN_RULES("test_run_rules", false),

        /**
         * Test Rail
         * todo add description
         */
        TESTRAIL_ENABLED("testrail_enabled", false),

        /**
         * todo add description
         */
        INCLUDE_ALL("include_all", false),

        /**
         * todo add description
         */
        MILESTONE("milestone", false),

        /**
         * todo add description
         */
        RUN_NAME("run_name", false),

        /**
         * todo add description
         */
        ASSIGNEE("assignee", false),

        /**
         * sha1
         * todo add description
         */
        GIT_HASH("git_hash", false);
        
        private final String key;
        private final boolean isLogable;

        Parameter(String key, boolean isLogable) {
            this.key = key;
            this.isLogable = isLogable;
        }

        public String getKey() {
            return key;
        }

        public boolean isLogable() {
            return isLogable;
        }
    }

    /**
     * Returns configuration value from startup properties or from configuration
     * file if not found in startup args.
     * 
     * @param param - parameter key.
     * @return parameter value if it is found by key or default value if not.
     */
    public static String get(Parameter param) {
        String value = R.CONFIG.get(param.getKey());
        return (value == null || value.equalsIgnoreCase(SpecialKeywords.NULL)) ? StringUtils.EMPTY : value;
    }

    public static int getInt(Parameter param) {
        return Integer.valueOf(get(param).trim());
    }

    public static long getLong(Parameter param) {
        return Long.valueOf(get(param).trim());
    }

    public static double getDouble(Parameter param) {
        return Double.valueOf(get(param).trim());
    }

    public static boolean getBoolean(Parameter param) {
        String value = get(param).trim();
        if (value == null || value.equalsIgnoreCase(SpecialKeywords.NULL)) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    private static StringBuilder testConfigurationAsString() {
        StringBuilder asString = new StringBuilder();
        asString.append("\n============= Test configuration =============\n");

        for (Parameter param : Parameter.values()) {
            // todo why contains?
            // #1451 hide WARN! Value not resolved by key: azure_container_name
            if (Parameter.AZURE_BLOB_URL.equals(param) && Configuration.get(param).toLowerCase().contains(SpecialKeywords.NULL.toLowerCase())) {
                // do nothing
                continue;
            }

            String parameterValue = Configuration.get(param);
            if (param.isLogable() && !parameterValue.isEmpty()) {
                asString.append(String.format("%s=%s%n", param.getKey(), parameterValue));
            }
        }
        return asString;
    }

    private static StringBuilder driverCapabilitiesAsString() {
        StringBuilder asString = new StringBuilder();
        asString.append("\n============= Driver capabilities =============\n");
        // read all properties from config.properties and use "capabilities.*"
        final String prefix = SpecialKeywords.CAPABILITIES + ".";
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, String> capabilitiesMap = new HashMap(R.CONFIG.getProperties());
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(prefix)) {
                asString.append(String.format("%s=%s%n", entry.getKey(), R.CONFIG.get(entry.getKey())));
            }
        }
        return asString;
    }

    public static String asString() {
        StringBuilder asString = new StringBuilder();
        asString.append(testConfigurationAsString());
        asString.append(driverCapabilitiesAsString());
        asString.append("================================================\n");
        return asString.toString();
    }

    public static void validateConfiguration() {
        for (Parameter param : Parameter.values()) {
            if (StringUtils.isEmpty(Configuration.get(param)) || SpecialKeywords.MUST_OVERRIDE.equals(Configuration.get(param))) {
                throw new RuntimeException("Configuration failure: parameter '" + param.getKey() + "' not specified!");
            }
        }
    }

    public static String getEnvArg(String key) {
        return envArgResolver.get(get(Parameter.ENV), key);
    }

    public static IEnvArgResolver getEnvArgResolver() {
        return envArgResolver;
    }

    public static boolean isNull(Parameter param) {
        // null is never returned from configuration now so compare with empty string
        return get(param).isEmpty();
    }

    /**
     * Get capabilities.adbExecTimeout from configuration properties.
     * if it is missing return the default value
     * @return int capabilities.adbExecTimeout
     */
    public static int getAdbExecTimeout() {
        // default "capabilities.adbExecTimeout=value" should be used to determine current platform
        int adbExecTimeout = SpecialKeywords.DEFAULT_ADB_EXEC_TIMEOUT;

        // redefine adb exec timeout if capabilities.AdbExecTimeout is available
        if (!R.CONFIG.get(SpecialKeywords.ADB_EXEC_TIMEOUT).isEmpty()) {
            adbExecTimeout = R.CONFIG.getInt(SpecialKeywords.ADB_EXEC_TIMEOUT);
        }

        return adbExecTimeout;
    }

    /**
     * Get platform name from configuration properties.
     * @return String platform name
     */
    public static String getPlatform() {
        return getPlatform(new DesiredCapabilities());
    }

    /**
     * Get platform name from configuration properties or DesiredCapabilities.
     * @param caps
     *            DesiredCapabilities
     * @return String platform name
     */
    public static String getPlatform(DesiredCapabilities caps) {
        // any platform by default
        String platform = "*";
        
        LOGGER.debug("platform1: " + platform);
        // redefine platform if os caps is available
        if (!R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_NAME).isEmpty()) {
            platform = R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_NAME);
        }
        LOGGER.debug("platform2: " + platform);

        // redefine platform if platformName caps is available
        if (!R.CONFIG.get(SpecialKeywords.PLATFORM_NAME).isEmpty()) {
            platform = R.CONFIG.get(SpecialKeywords.PLATFORM_NAME);
        }
        LOGGER.debug("platform3: " + platform);

        // do not read "os" or "platformName" from caps as Saucelabs return LINUX instead of ANDROID
//        if (caps != null && caps.getCapability("os") != null) {
//            platform = caps.getCapability("os").toString();
//        }   
        
//        if (caps != null && caps.getCapability("platformName") != null) {
//            platform = caps.getCapability("platformName").toString();
//        }
        LOGGER.debug("platform4: " + platform);
        
        return platform;
    }
    
    public static String getPlatformVersion() {
        return getPlatformVersion(new DesiredCapabilities());
    }
    
    public static String getPlatformVersion(DesiredCapabilities caps) {
        // default "os_version=value" should be used to determine current platform
        String platformVersion = "";

        // redefine platform if os_version caps is available
        if (!R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_VERSION).isEmpty()) {
            platformVersion = R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_VERSION);
        }
        
        // redefine platform if platformVersion caps is available
        if (!R.CONFIG.get(SpecialKeywords.PLATFORM_VERSION).isEmpty()) {
            platformVersion = R.CONFIG.get(SpecialKeywords.PLATFORM_VERSION);
        }
        
        if (caps != null && caps.getCapability("os_version") != null) {
            platformVersion = caps.getCapability("os_version").toString();
        }           
        
        if (caps != null && caps.getCapability("platformVersion") != null) {
            platformVersion = caps.getCapability("platformVersion").toString();
        }        
        
        return platformVersion;
    }

    public static String getBrowser() {
        String browser = "";
        if (!Configuration.get(Parameter.BROWSER).isEmpty()) {
            // default "browser=value" should be used to determine current browser
            browser = Configuration.get(Parameter.BROWSER);
        }
        // redefine browser if capabilities.browserName is available
        if (!R.CONFIG.get("capabilities.browserName").isEmpty() && !"null".equalsIgnoreCase(R.CONFIG.get("capabilities.browserName"))) {
            browser = R.CONFIG.get("capabilities.browserName");
        }
        return browser;
    }
    
    public static String getBrowserVersion() {
        String browserVersion = "";

        // redefine browserVersion if capabilities.browserVersion is available
        if (!R.CONFIG.get("capabilities.browserVersion").isEmpty()  && !"null".equalsIgnoreCase(R.CONFIG.get("capabilities.browserVersion"))) {
            browserVersion = R.CONFIG.get("capabilities.browserVersion");
        }
        
        return browserVersion;
    }

    public static String getDriverType() {

        String platform = getPlatform();
        if (platform.equalsIgnoreCase(SpecialKeywords.ANDROID) || platform.equalsIgnoreCase(SpecialKeywords.IOS) || platform.equalsIgnoreCase(SpecialKeywords.TVOS)) {
            return SpecialKeywords.MOBILE;
        }
        
        if (SpecialKeywords.WINDOWS.equalsIgnoreCase(platform)) {
            return SpecialKeywords.WINDOWS;
        }

        return SpecialKeywords.DESKTOP;
    }

    public static String getDriverType(DesiredCapabilities capabilities) {
        if (capabilities == null) {
            // calculate driver type based on config.properties arguments
            return getDriverType();
        }

        String platform = "";
        if (capabilities.getCapability("platformName") != null) {
            platform = capabilities.getCapability("platformName").toString();
        }

        if (SpecialKeywords.ANDROID.equalsIgnoreCase(platform) || SpecialKeywords.IOS.equalsIgnoreCase(platform) || SpecialKeywords.TVOS.equalsIgnoreCase(platform)) {
            return SpecialKeywords.MOBILE;
        }
        
        if (SpecialKeywords.WINDOWS.equalsIgnoreCase(platform)) {
            return SpecialKeywords.WINDOWS;
        }

        // handle use-case when we provide only uuid object among desired capabilities
        if (capabilities.getCapability("udid") != null) {
            LOGGER.debug("Detected MOBILE driver_type by uuid inside capabilities");
            return SpecialKeywords.MOBILE;
        }

        return SpecialKeywords.DESKTOP;
    }

    public static String getMobileApp() {
        // redefine platform if capabilities.app is available
        String mobileApp = "";
        String prefix = SpecialKeywords.CAPABILITIES + ".";
        if (!R.CONFIG.get(prefix + "app").isEmpty()) {
            mobileApp = R.CONFIG.get(prefix + "app");
        }
        return mobileApp;
    }

    public static void setMobileApp(String mobileApp) {
        R.CONFIG.put(SpecialKeywords.CAPABILITIES + ".app", mobileApp);
        LOGGER.info("Updated mobile app: " + mobileApp);
    }

    public static Object getCapability(String name) {
        return R.CONFIG.get("capabilities." + name);
    }
    
    /**
     * Register APP_VERSION number in CONFIG space and as Zebrunner Reporting build number if not empty.
     *
     * @param build String
     */
    public static void setBuild(String build) {
        R.CONFIG.put(Parameter.APP_VERSION.getKey(), build);
        if (!build.isEmpty()) {
            LOGGER.debug("build: " + build);
            CurrentTestRun.setBuild(build);
        }
    }

    public static String getSeleniumUrl() {
        return Configuration.get(Parameter.SELENIUM_URL);
    }

    public static int getThreadCount() {
        return Configuration.getInt(Parameter.THREAD_COUNT);
    }

    public static int getDataProviderThreadCount() {
        return Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT);
    }

}
