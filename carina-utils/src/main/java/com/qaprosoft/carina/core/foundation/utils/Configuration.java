/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;

/**
 * Configuration utility.
 * 
 * @author Aliaksei_Khursevich
 *         hursevich@gmail.com
 */
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Configuration.class);
    private static IEnvArgResolver envArgResolver;

    static {
        String envArgResolverClass = Configuration.get(Parameter.ENV_ARG_RESOLVER);
        if (envArgResolverClass.isEmpty()) {
            // redefine using default class
            envArgResolverClass = "com.qaprosoft.carina.core.foundation.utils.DefaultEnvArgResolver";
        }

        try {
            Class<?> cl = Class.forName(envArgResolverClass);
            Constructor<?> ct = cl.getConstructor();
            Configuration.setEnvArgResolver((IEnvArgResolver) ct.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Configuration failure: can not initiate EnvArgResolver - '" + envArgResolverClass + "'", e);
        }

    }

    /**
     * All available configuration parameter keys along with default values.
     */
    public enum Parameter {
        URL("url"),

        ENV("env"),

        ENV_ARG_RESOLVER("env_arg_resolver"),

        PLATFORM("platform"),

        BROWSER("browser"),

        BROWSER_VERSION("browser_version"),
        
        BROWSER_LANGUAGE("browser_language"),

        SELENIUM_HOST("selenium_host"),

        DRIVER_EVENT_LISTENERS("driver_event_listeners"),

        MAX_DRIVER_COUNT("max_driver_count"),

        CUSTOM_CAPABILITIES("custom_capabilities"),

        APP_VERSION("app_version"),

        PROXY_HOST("proxy_host"),

        PROXY_PORT("proxy_port"),

        PROXY_PROTOCOLS("proxy_protocols"),

        BROWSERMOB_PROXY("browsermob_proxy"),
        
        BROWSERMOB_HOST("browsermob_host"),

        BROWSERMOB_PORT("browsermob_port"),
        
        BROWSERMOB_PORTS_RANGE("browsermob_ports_range"),
        
        BROWSERMOB_MITM("browsermob_disabled_mitm"),

        PROXY_SET_TO_SYSTEM("proxy_set_to_system"),

        REPORT_URL("report_url"),

        EMAIL_LIST("email_list"),

        FAILURE_EMAIL_LIST("failure_email_list"),

        TRACK_KNOWN_ISSUES("track_known_issues"),

        AUTO_SCREENSHOT("auto_screenshot"),

        SMART_SCREENSHOT("smart_screenshot"),

        //TODO: temporary restore to keep compilation. remove later
        IMPLICIT_TIMEOUT("implicit_timeout"),

        EXPLICIT_TIMEOUT("explicit_timeout"),

        AUTO_DOWNLOAD("auto_download"),

        AUTO_DOWNLOAD_APPS("auto_download_apps"),
        
    	CUSTOM_ARTIFACTS_FOLDER("custom_artifacts_folder"),

        RETRY_INTERVAL("retry_interval"),

        PROJECT_REPORT_DIRECTORY("project_report_directory"),

        MAX_SCREENSHOOT_HISTORY("max_screen_history"),

        MAX_LOG_FILE_SIZE("max_log_file_size"),

        RESULT_SORTING("result_sorting"),

        BIG_SCREEN_WIDTH("big_screen_width"),

        BIG_SCREEN_HEIGHT("big_screen_height"),

        SMALL_SCREEN_WIDTH("small_screen_width"),

        SMALL_SCREEN_HEIGHT("small_screen_height"),

        INIT_RETRY_COUNT("init_retry_count"),

        INIT_RETRY_INTERVAL("init_retry_interval"),

        RETRY_COUNT("retry_count"),

        ENABLE_L10N("enable_l10n"),

        L10N_ENCODING("l10n_encoding"),

        LOCALE("locale"),

        ENABLE_I18N("enable_i18n"),

        LANGUAGE("language"),

        THREAD_COUNT("thread_count"),

        DATA_PROVIDER_THREAD_COUNT("data_provider_thread_count"),

        CORE_LOG_LEVEL("core_log_level"),
        
        CORE_LOG_PACKAGES("core_log_packages"),
        
        ARTIFACTS_EXPIRATION_SECONDS("artifacts_expiration_seconds"),

        LOG_ALL_JSON("log_all_json"),

        DATE_FORMAT("date_format"),

        TIME_FORMAT("time_format"),

        CRYPTO_KEY_PATH("crypto_key_path"),

        SUITE_NAME("suite_name"),

        JIRA_UPDATER("jira_updater"),

        JIRA_URL("jira_url"),

        JIRA_USER("jira_user"),

        JIRA_PASSWORD("jira_password"),

        JIRA_SUITE_ID("jira_suite_id"),

        JIRA_PROJECT("jira_project"),

        JIRA_PROJECT_SHORT("jira_project_short"),

        JIRA_CREATE_NEW_TICKET("jira_create_new_ticket"),

        TEST_NAMING_PATTERN("test_naming_pattern"),

        // Enable video recording capabilities only for the final retry attempt
        OPTIMIZE_VIDEO_RECORDING("optimize_video_recording"),

        // TestRail
        TESTRAIL_RUN_NAME("testrail_run_name"),
        
        TESTRAIL_MILESTONE("testrail_milestone"),

        TESTRAIL_ASSIGNEE_USER("testrail_assignee"),
        
        // qTest
        QTEST_CYCLE_NAME("qtest_cycle_name"),
        
        QTEST_SUITE_NAME("qtest_suite_name"),
        
        // Amazon
        S3_BUCKET_NAME("s3_bucket_name"),

        ACCESS_KEY_ID("access_key_id"),

        SECRET_KEY("secret_key"),

        S3_LOCAL_STORAGE("s3_local_storage"),

        // HockeyApp token
        HOCKEYAPP_TOKEN("hockeyapp_token"),

        HOCKEYAPP_LOCAL_STORAGE("hockeyapp_local_storage"),

        // For localization parser
        ADD_NEW_LOCALIZATION("add_new_localization"),

        ADD_NEW_LOCALIZATION_ENCODING("add_new_localization_encoding"),

        ADD_NEW_LOCALIZATION_PATH("add_new_localization_path"),

        ADD_NEW_LOCALIZATION_PROPERTY_NAME("add_new_localization_property_name"),

        // TLS
        TLS_KEYSECURE_LOCATION("tls_keysecure_location"),

        // HealthCheck
        HEALTH_CHECK_CLASS("health_check_class"),

        HEALTH_CHECK_METHODS("health_check_methods"),

        UNINSTALL_RELATED_APPS("uninstall_related_apps"),

        // For Device default timezone and language
        DEFAULT_DEVICE_TIMEZONE("default_device_timezone"),

        DEFAULT_DEVICE_TIME_FORMAT("default_device_time_format"),

        DEFAULT_DEVICE_LANGUAGE("default_device_language"),
        
        //For screen recording
        ANDROID_SCREEN_RECORDING_SIZE("android_screen_record_size"),
        
        ANDROID_SCREEN_RECORDING_BITRATE("android_screen_record_bitrate"),
        
        ANDROID_ENABLE_BUG_REPORT("android_enable_bug_report"),
        
        IOS_SCREEN_RECORDING_QUALITY("ios_screen_record_quality"),
        
        IOS_SCREEN_RECORDING_CODEC("ios_screen_record_codec"),
        
        IOS_SCREEN_RECORDING_FPS("ios_screen_record_fps"),
        
        SCREEN_RECORD_DURATION("screen_record_duration"),
        
        VIDEO_SCALE("video_scale"),

        // Ignore SSL
        IGNORE_SSL("ignore_ssl"),

        // Test Execution Filter rules
        TEST_RUN_RULES("test_run_rules"),
        
        HUB_MODE("hub_mode"),
        
        EXTRACT_SYS_LOG("extract_sys_log");

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
     * @param param
     *            - parameter key.
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
        return Boolean.valueOf(get(param).trim());
    }

    @Deprecated
    public static Locale getLocale() {
        Locale locale = null;
        if (!StringUtils.isEmpty(get(Parameter.LOCALE))) {
            if (Configuration.get(Parameter.LOCALE).contains("_")) {
                locale = new Locale(get(Parameter.LOCALE).split("_")[0], get(Parameter.LOCALE).split("_")[1]);
            } else {
                locale = new Locale("", get(Parameter.LOCALE));
            }
        }
        return locale;
    }

    public static String asString() {
        StringBuilder asString = new StringBuilder();
        asString.append("\n============= Test configuration =============\n");
        for (Parameter param : Parameter.values()) {
            if (!Parameter.CRYPTO_KEY_PATH.equals(param)) {
                asString.append(String.format("%s=%s%n", param.getKey(), Configuration.get(param)));
            }
        }

        //write into the log extra information about selenium_host together with capabilities
        asString.append(String.format("%s=%s%n", "selenium_host", R.CONFIG.get("selenium_host")));
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

    public static void setEnvArgResolver(IEnvArgResolver envArgResolver) {
        Configuration.envArgResolver = envArgResolver;
    }

    public static boolean isNull(Parameter param) {
        // null is never returned from configuration now so compare with empty string
        return get(param).isEmpty();
    }

    public static String getPlatform() {
        // default "platform=value" should be used to determine current platform
        String platform = Configuration.get(Parameter.PLATFORM);

        // redefine platform if capabilities.platform is available
        if (!R.CONFIG.get("capabilities.platform").isEmpty()) {
            platform = R.CONFIG.get("capabilities.platform");
        }

        // redefine platform if mobile.platformName is available
        if (!R.CONFIG.get("capabilities.platformName").isEmpty()) {
            platform = R.CONFIG.get("capabilities.platformName");
        }
        return platform;
    }
    
    public static String getBrowser() {
        String browser = "";
        if (!Configuration.get(Parameter.BROWSER).isEmpty()) {
            // default "browser=value" should be used to determine current browser
            browser = Configuration.get(Parameter.BROWSER);
        }

        // redefine browser if capabilities.browserName is available
        if (!R.CONFIG.get("capabilities.browserName").isEmpty()) {
            browser = R.CONFIG.get("capabilities.browserName");
        }
        return browser;
    }

    public static String getDriverType() {

        String platform = getPlatform();
        if (platform.equalsIgnoreCase(SpecialKeywords.ANDROID) || platform.equalsIgnoreCase(SpecialKeywords.IOS)) {
        	LOGGER.debug("Detected MOBILE driver_type by platform: " + platform);
            return SpecialKeywords.MOBILE;
        }

        LOGGER.debug("Return default DESKTOP driver_type");
        return SpecialKeywords.DESKTOP;
    }

    public static String getDriverType(DesiredCapabilities capabilities) {
    	if (capabilities == null) {
    		//calculate driver type based on config.properties arguments
    		return getDriverType();
    	}
    	
    	LOGGER.debug("Detecting driver_type by capabilities: " + capabilities);
    	String platform = "";
    	if (capabilities.getCapability("platform") != null) {
    		platform = capabilities.getCapability("platform").toString();
    	}
    	
    	if (capabilities.getCapability("platformName") != null) {
    		platform = capabilities.getCapability("platformName").toString();
    	}
    	
        if (SpecialKeywords.ANDROID.equalsIgnoreCase(platform) || SpecialKeywords.IOS.equalsIgnoreCase(platform)) {
        	LOGGER.debug("Detected MOBILE driver_type by platform: " + platform);
            return SpecialKeywords.MOBILE;
        }
        
        // handle use-case when we provide only uuid object among desired capabilities
    	if (capabilities.getCapability("udid") != null) {
    		LOGGER.debug("Detected MOBILE driver_type by uuid inside capabilities");
    		return SpecialKeywords.MOBILE;
    	}

    	LOGGER.debug("Return default DESKTOP driver_type");
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
}
