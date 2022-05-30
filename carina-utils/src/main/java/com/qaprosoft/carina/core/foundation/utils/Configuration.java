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
         * Base application URL <br/>
         * <tt>Example: http://qaprosoft.com</tt>
         */
        URL("url"),

        /**
         * Environment specific configuration <br/>
         * <tt>Example: STAG, PROD, DEMO</tt>
         */
        ENV("env"),

        /**
         * Browser for testing <br/>
         * <tt>Example: chrome, firefox, safari, iexplore</tt>
         */
        BROWSER("browser"),

        /**
         * Browser language <br/>
         * If it is empty - english version used by default <br/>
         * <tt>Example: es, fr</tt>
         */
        BROWSER_LANGUAGE("browser_language"),

        /**
         * Selenium/Appium server url<br/>
         * <tt>Example: http://localhost:4444/wd/hub</tt>
         */
        SELENIUM_URL("selenium_url"),

        /**
         * Comma-separated list of extra driver listeners.
         * Listeners provide extra custom actions for WebDriver and have
         * to be the instances of WebDriverEventListener<br/>
         * <tt>Example: com.some_company.core.EventListener</tt>
         */
        DRIVER_EVENT_LISTENERS("driver_event_listeners"),

        /**
         * Max number of drivers per thread.<br/>
         * <tt>Default: 3</tt>
         */
        MAX_DRIVER_COUNT("max_driver_count"),

        /**
         * If enabled turns off webdriver shutdown after test finishing by any reason.<br/>
         * <tt>Default: false</tt>
         */
        FORCIBLY_DISABLE_DRIVER_QUIT("forcibly_disable_driver_quit"),

        /**
         * Name of a properties file with custom capabilities (key-value)<br/>
         * <tt>Example: custom.properties</tt>
         */
        CUSTOM_CAPABILITIES("custom_capabilities"),

        /**
         * todo add description
         */
        CHROME_ARGS("chrome_args"),

        /**
         * todo add description
         */
        CHROME_EXPERIMENTAL_OPTS("chrome_experimental_opts"),

        /**
         * todo add description
         */
        CHROME_MOBILE_EMULATION_OPTS("chrome_mobile_emulation_opts"),

        /**
         * todo add description
         */
        CHROME_CLOSURE("chrome_closure"),

        /**
         * todo add description
         */
        FIREFOX_ARGS("firefox_args"),

        /**
         * todo add description
         */
        FIREFOX_PREFERENCES("firefox_preferences"),

        /**
         * Run tests in headless browser mode. Enabled when headless=true.<br/>
         * <tt>Default: false.</tt>
         */
        HEADLESS("headless"),

        /**
         * Application version/build number for reporting<br/>
         * <tt>Example: 1.2.5</tt>
         */
        APP_VERSION("app_version"),

        /**
         * Hostname of the server<br/>
         * <tt>Example: host.example.com</tt>
         */
        PROXY_HOST("proxy_host"),

        /**
         * Port number<br/>
         * <tt>Example: 80</tt>
         */
        PROXY_PORT("proxy_port"),

        /**
         * Comma-separated list of internet protocols used to carry the connection information
         * from the source requesting the connection to the destination for which the connection
         * was requested.<br/>
         * <tt>Example: http, https, ftp, socks</tt>
         */
        PROXY_PROTOCOLS("proxy_protocols"),

        /**
         * Excluded hostname(s) for communication via proxy. Available only when
         * proxy_host and proxy_port are declared! <br/>
         * <tt>Example: localhost.example.com</tt>
         */
        NO_PROXY("no_proxy"),

        /**
         * Boolean parameter which enables or disables the automatic BrowserMob proxy launch
         */
        BROWSERMOB_PROXY("browsermob_proxy"),

        /**
         * Port number for BrowserMob proxy<br/>
         * <tt>(if nothing or 0 specified, then any free port will be reused)</tt>
         */
        BROWSERMOB_PORT("browsermob_port"),

        /**
         * Range of ports that will be used for starting of browsermob proxy.
         * First available port from the range will be used. If all ports are used then test
         * will wait for the first freed port. <br/>
         * <tt>Example: 8001:8003</tt>
         */
        BROWSERMOB_PORTS_RANGE("browsermob_ports_range"),

        /**
         * todo add description
         */
        BROWSERMOB_MITM("browsermob_disabled_mitm"),

        /**
         * Boolean parameter which enables or disables the setup of a proxy
         */
        PROXY_SET_TO_SYSTEM("proxy_set_to_system"),

        /**
         * Direct HTTP link to Jenkins workspace report folder. <b>Automatically</b> specified by CI <br/>
         * <tt>Example: http://localhost:8888/job /my_project/1/eTAF_Report</tt>
         */
        REPORT_URL("report_url"),

        /**
         * Global switch for taking screenshots. When disabled, screenshots will be captured only after failures
         */
        AUTO_SCREENSHOT("auto_screenshot"),

        /**
         * Timeout is seconds to wait for a certain condition to occur before proceeding further in the code
         */
        EXPLICIT_TIMEOUT("explicit_timeout"),

        /**
         * The enabled parameter prevents downloading a dialog and downloading a file automatically.
         * The feature is currently available for Chrome and FireFox
         */
        AUTO_DOWNLOAD("auto_download"),

        /**
         * MIME types / Internet Media Types. The parameter is needed only to configure auto downloading for Firefox<br/>
         * <tt>Examples: <a href="https://freeformatter.com/mime-types-list.html">Mime types</a></tt>
         */
        AUTO_DOWNLOAD_APPS("auto_download_apps"),

        /**
         * Path to auto download folder for Chrome and Firefox browsers.
         * If nothing specified custom_artifacts_folder or default artifacts folder is used
         */
        AUTO_DOWNLOAD_FOLDER("auto_download_folder"),

        /**
         * todo add description
         */
        CUSTOM_ARTIFACTS_FOLDER("custom_artifacts_folder"),

        /**
         * Timeout interval between calling HTML DOM for the element<br/>
         * <b>Note</b>: in ms. For mobile automation specify a number from 500-1000 range
         */
        RETRY_INTERVAL("retry_interval"),

        /**
         * Path to a folder where the testing report will be saved<br/>
         * <tt>Example: ./reports</tt>
         */
        PROJECT_REPORT_DIRECTORY("project_report_directory"),

        /**
         * Max number of reports artifacts saving in history <br/>
         * <tt>Default: 10</tt>
         */
        MAX_SCREENSHOOT_HISTORY("max_screen_history"),

        /**
         * Screenshots will be resized according to this width if their own width is bigger<br/>
         * <tt>Default: -1 to keep existing size.</tt>
         */
        BIG_SCREEN_WIDTH("big_screen_width"),

        /**
         * Screenshots will be resized according to this height if their own height is bigger<br/>
         * <tt>Default: -1 to keep existing size.</tt>
         */
        BIG_SCREEN_HEIGHT("big_screen_height"),

        /**
         * Number of attempts to create a driver<br/>
         * <tt>The default value 0 means that there will be only 1 attempt</tt>
         */
        INIT_RETRY_COUNT("init_retry_count"),

        /**
         * Interval in seconds between the attempts to create a driver <br/>
         */
        INIT_RETRY_INTERVAL("init_retry_interval"),

        /**
         * Number of test-retryings in case of failure. <br/>
         * <tt>The default value 0 means that a test will be performed only once</tt>
         */
        RETRY_COUNT("retry_count"),

        /**
         * Locale for using L10N feature <br/>
         * <tt>Example: en_GB,de_DE,fr_FR</tt>
         */
        LOCALE("locale"),

        /**
         * Default number of threads to use when running tests in parallel.
         * Set thread-count=custom to disable any updates on carina side.
         */
        THREAD_COUNT("thread_count"),

        /**
         * Default number of threads to use for data providers when running tests in parallel.
         */
        DATA_PROVIDER_THREAD_COUNT("data_provider_thread_count"),

        /**
         * Level for Carina logging<br/>
         * <tt>Example: ALL, DEBUG, ERROR, WARN, FATAL, INFO, OFF, TRACE</tt>
         */
        CORE_LOG_LEVEL("core_log_level"),

        /**
         * API response will be logged in JSON format<br/>
         * <tt>Default: true</tt>
         */
        LOG_ALL_JSON("log_all_json"),

        /**
         * Date format for DateUtils.class<br/>
         * <tt>Example: HH:mm:ss dd/MM/yyyy, HH:mm MM/dd/yyyy</tt>
         */
        DATE_FORMAT("date_format"),

        /**
         * Date format for DateUtils.class<br/>
         * <tt>Example: HH:mm:ss.SSS, HH:mm a zzz</tt>
         */
        TIME_FORMAT("time_format"),

        /**
         * Path to a file with a crypto key<br/>
         * <tt>Example: ./src/main/resources/crypto.key</tt>
         */
        CRYPTO_KEY_PATH("crypto_key_path"),

        /**
         * Suite name for the report and TestRail. If this parameter is NULL, will
         * be taken from TestNG xml (the parameter suite name) or _email.properties (the title)
         */
        SUITE_NAME("suite_name"),

        /**
         * todo add description
         */
        TEST_NAMING_PATTERN("test_naming_pattern"),

        /**
         * Determines how carina detects appearing of web elements on page: by presence in
         * DOM model or by visibility or by any of these conditions<br/>
         * <tt>Example: BY_PRESENCE, BY_VISIBILITY, BY_PRESENCE_OR_VISIBILITY</tt>
         */
        ELEMENT_LOADING_STRATEGY("element_loading_strategy"),

        /**
         * Determines how carina detects whether expected page is opened: by expected url pattern,
         * by marker element loading state or by both these conditions <br/>
         * <tt>BY_ELEMENT, BY_URL, BY_URL_AND_ELEMENT</tt>
         */
        PAGE_OPENING_STRATEGY("page_opening_strategy"),

        /**
         * Amazon
         * todo add description
         */
        S3_BUCKET_NAME("s3_bucket_name"),

        /**
         * Access key id for Amazon S3 build uploader <br/>
         * <tt>Example: gkhcvdgvceUYF67897hbjsbdc</tt>
         * <a href="https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys">More info here</a>
         */
        ACCESS_KEY_ID("access_key_id"),

        /**
         * Secret key for Amazon S3 build uploader<br/>
         * <tt>Example: gkhcvdgvceUYF67897hbjsbdc</tt>
         * <a href="https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys">More info here</a>
         */
        SECRET_KEY("secret_key"),

        /**
         * Azure
         * todo add description
         */
        AZURE_ACCOUNT_NAME("azure_account_name"),

        /**
         * todo add description
         */
        AZURE_CONTAINER_NAME("azure_container_name"),

        /**
         * todo add description
         */
        AZURE_BLOB_URL("azure_blob_url"),

        /**
         * todo add description
         */
        AZURE_ACCESS_KEY_TOKEN("azure_access_key_token"),

        /**
         * todo add description
         */
        AZURE_LOCAL_STORAGE("azure_local_storage"),

        /**
         * Token for authentication in Hockey App <br/>
         * <tt>Example: gkhcvdgvceUYF67897hbjsbdc</tt>
         */
        APPCENTER_TOKEN("appcenter_token"),

        //
        /**
         * For localization parser
         * todo add description
         */
        LOCALIZATION_ENCODING("localization_encoding"),

        /**
         * Enables auto verification for elements that are marked with @Localized
         */
        LOCALIZATION_TESTING("localization_testing"),

        /**
         * TLS<br/>
         * Path to a directory with tls secure keys<br/>
         * <tt>Example: ./tls/keysecure</tt>
         */
        TLS_KEYSECURE_LOCATION("tls_keysecure_location"),

        /**
         *  HealthCheck<br/>
         *  Class to execute health checks<br/>
          */
        HEALTH_CHECK_CLASS("health_check_class"),

        /**
         * Comma-separated list of methods of health_check_class to execute preliminarily<br/>
         * <tt>Example: doThis, doThat</tt>
         */
        HEALTH_CHECK_METHODS("health_check_methods"),

        /**
         * todo add description
         */
        UNINSTALL_RELATED_APPS("uninstall_related_apps"),

        /**
         * For Device default timezone and language
         * todo add description
         */
        DEFAULT_DEVICE_TIMEZONE("default_device_timezone"),

        /**
         * todo add description
         */
        DEFAULT_DEVICE_TIME_FORMAT("default_device_time_format"),

        /**
         * todo add description
         */
        DEFAULT_DEVICE_LANGUAGE("default_device_language"),

        /**
         * Ignore SSL <br/>
         * API requests/responses to ignore SSL errors.<br/>
         * <tt>Default: false</tt>
         */
        IGNORE_SSL("ignore_ssl"),

        /** Test Execution Filter rules<br/>
         * Executing rules logic: test_run_rules={RULE_NAME_ENUM}=>{RULE_VALUE1}&&{RULE_VALUE2};;... <br/>
         * <tt>Example: test_run_rules=PRIORITY=>P1&&P2&&P4;;OWNER=>owner;;TAGS=>tag1=temp||!!feature=reg</tt>
         */
        TEST_RUN_RULES("test_run_rules"),

        /**
         * Test Rail
         * todo add description
         */
        TESTRAIL_ENABLED("testrail_enabled"),

        /**
         * todo add description
         */
        INCLUDE_ALL("include_all"),

        /**
         * todo add description
         */
        MILESTONE("milestone"),

        /**
         * todo add description
         */
        RUN_NAME("run_name"),

        /**
         * todo add description
         */
        ASSIGNEE("assignee"),

        /**
         * sha1
         * todo add description
         */
        GIT_HASH("git_hash");
        
        private final String key;

        Parameter(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
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
        return !(value == null || value.equalsIgnoreCase(SpecialKeywords.NULL)) ? value : StringUtils.EMPTY;
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

    public static String asString() {
        StringBuilder asString = new StringBuilder();
        asString.append("\n============= Test configuration =============\n");
        for (Parameter param : Parameter.values()) {
            //#1451 hide WARN! Value not resolved by key: azure_container_name
            if (Parameter.AZURE_BLOB_URL.equals(param) && Configuration.get(param).toLowerCase().contains(SpecialKeywords.NULL.toLowerCase())) {
                // do nothing
                continue;
            }
            if (!Parameter.CRYPTO_KEY_PATH.equals(param) && !Configuration.get(param).isEmpty()) {
                asString.append(String.format("%s=%s%n", param.getKey(), Configuration.get(param)));
            }
        }

        // write into the log extra information about selenium_url together with capabilities
        asString.append(String.format("%s=%s%n", "selenium_url", getSeleniumUrl()));
        asString.append("\n------------- Driver capabilities -----------\n");
        // read all properties from config.properties and use "capabilities.*"
        final String prefix = SpecialKeywords.CAPABILITIES + ".";
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, String> capabilitiesMap = new HashMap(R.CONFIG.getProperties());
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(prefix)) {
                asString.append(String.format("%s=%s%n", entry.getKey(), R.CONFIG.get(entry.getKey())));
            }
        }

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
