/*
 * Copyright 2013-2015 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.core.foundation.utils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;

/**
 * Configuration utility.
 * 
 * @author Aliaksei_Khursevich
 * hursevich@gmail.com
 */
public class Configuration
{
    private static final Logger LOGGER = Logger.getLogger(Configuration.class);
	private static IEnvArgResolver envArgResolver;
	
	static
	{
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
	 * All available configuration for diver initialization phase.
	 */

	public enum DriverMode
	{
		SUITE_MODE("suite_mode"),
		
		CLASS_MODE("class_mode"),
		
		METHOD_MODE("method_mode");
		
		private final String key;

		DriverMode(String key)
		{
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}
	
	/**
	 * All available configuration parameter keys along with default values.
	 */
	public enum Parameter
	{
		URL("url"),
		
		ENV("env"),
		
		ENV_ARG_RESOLVER("env_arg_resolver"),
		
		PLATFORM("platform"),

		BROWSER("browser"),
		
		BROWSER_VERSION("browser_version"),

		SELENIUM_HOST("selenium_host"),
		
		DRIVER_MODE("driver_mode"),
		
		MAX_DRIVER_COUNT("max_driver_count"),
		
		CUSTOM_CAPABILITIES("custom_capabilities"),
		
		EXTRA_CAPABILITIES("extra_capabilities"),
		
		APP_VERSION("app_version"),
		
		PROXY_HOST("proxy_host"),
		
		PROXY_PORT("proxy_port"),
		
		PROXY_PROTOCOLS("proxy_protocols"),
		
		BROWSERMOB_PROXY("browsermob_proxy"),
		
		BROWSERMOB_PORT("browsermob_port"),
		
		PROXY_SET_TO_SYSTEM("proxy_set_to_system"),

		REPORT_URL("report_url"),

		EMAIL_LIST("email_list"),
		
		FAILURE_EMAIL_LIST("failure_email_list"),
		
		TRACK_KNOWN_ISSUES("track_known_issues"),

		AUTO_SCREENSHOT("auto_screenshot"),
		
		SMART_SCREENSHOT("smart_screenshot"),
		
		IMPLICIT_TIMEOUT("implicit_timeout"),

		EXPLICIT_TIMEOUT("explicit_timeout"),
		
		AUTO_DOWNLOAD("auto_download"),
		
		AUTO_DOWNLOAD_APPS("auto_download_apps"),

		RETRY_INTERVAL("retry_interval"),

		PROJECT_REPORT_DIRECTORY("project_report_directory"),

		MAX_SCREENSHOOT_HISTORY("max_screen_history"),
		
		RESULT_SORTING("result_sorting"),

		KEEP_ALL_SCREENSHOTS("keep_all_screenshots"),

		BIG_SCREEN_WIDTH("big_screen_width"),

		BIG_SCREEN_HEIGHT("big_screen_height"),

		SMALL_SCREEN_WIDTH("small_screen_width"),

		SMALL_SCREEN_HEIGHT("small_screen_height"),

		SENDER_EMAIL("sender_email"),

		SENDER_PASSWORD("sender_pswd"),
		
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
		
		LOG_ALL_JSON("log_all_json"),

		DATE_FORMAT("date_format"),
		
		TIME_FORMAT("time_format"),
		
		CRYPTO_KEY_PATH("crypto_key_path"),
		
		SUITE_NAME("suite_name"),

		CI_URL("ci_url"),
		
		CI_BUILD("ci_build"),		
		
		JIRA_UPDATER("jira_updater"),
		
		JIRA_URL("jira_url"),
		
		JIRA_USER("jira_user"),
		
		JIRA_PASSWORD("jira_password"),
		
		JIRA_SUITE_ID("jira_suite_id"),

        JIRA_PROJECT("jira_project"),

        JIRA_PROJECT_SHORT("jira_project_short"),
        
        JIRA_CREATE_NEW_TICKET("jira_create_new_ticket"),
        
        // Appium 1.1.x mobile capabilities: iOS and Android
		MOBILE_SCREEN_SWITCHER("mobile_screen_switcher"),
		
		//DROPBOX_ACCESS_TOKEN("dropbox_access_token"), 
		
		VIDEO_RECORDING("video_recording"),
		
		//TestRail

		TESTRAIL_URL("testrail_url"),

		TESTRAIL_USER("testrail_user"),

		TESTRAIL_PASSWORD("testrail_password"),

		TESTRAIL_UPDATER("testrail_updater"),

		TESTRAIL_MILESTONE("testrail_milestone"),

		TESTRAIL_ASSIGNEE_USER("testrail_assignee"),

		//Amazon
		S3_BUCKET_NAME("s3_bucket_name"),
		
		ACCESS_KEY_ID("access_key_id"),
		
		SECRET_KEY("secret_key"),
		
		S3_LOCAL_STORAGE("s3_local_storage"),
				
		//Amazon-Screenshot
		S3_SCREENSHOT_BUCKET_NAME("s3_screenshot_bucket_name"),
		
		S3_SAVE_SCREENSHOTS("s3_save_screenshots"),
		
		//HockeyApp token
		HOCKEYAPP_TOKEN("hockeyapp_token"),
		
		HOCKEYAPP_LOCAL_STORAGE("hockeyapp_local_storage"),
		
		//For localization parser
		ADD_NEW_LOCALIZATION("add_new_localization"),

		ADD_NEW_LOCALIZATION_ENCODING("add_new_localization_encoding"),

		ADD_NEW_LOCALIZATION_PATH("add_new_localization_path"),
		
		ADD_NEW_LOCALIZATION_PROPERTY_NAME("add_new_localization_property_name"),

		//For cucumber tests
		CUCUMBER_TESTS("cucumber_tests"),

		CUCUMBER_TESTS_APP_VERSION("cucumber_tests_app_version"),

		CUCUMBER_TESTS_NAME("cucumber_tests_name"),

		CUCUMBER_TESTS_RESULTS_IMAGE_RESIZE("cucumber_tests_results_image_resize"),

		CUCUMBER_USE_JS_IN_REPORT("cucumber_user_js_in_report"),

		CUCUMBER_REPORT_SUBFOLDER("cucumber_report_subfolder"),
		
		// TLS
		TLS_KEYSECURE_LOCATION("tls_keysecure_location"),
		
		//HealthCheck
		HEALTH_CHECK_CLASS("health_check_class"),
		
		HEALTH_CHECK_METHODS("health_check_methods"),
		
		UNINSTALL_RELATED_APPS("uninstall_related_apps")
		;

		private final String key;

		Parameter(String key)
		{
			this.key = key;
		}

		public String getKey()
		{
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
	public static String get(Parameter param)
	{
		String value = R.CONFIG.get(param.getKey());
		return !(value == null || value.equalsIgnoreCase(SpecialKeywords.NULL)) ? value : StringUtils.EMPTY;
	}

	public static int getInt(Parameter param)
	{
		return Integer.valueOf(get(param).trim());
	}

	public static long getLong(Parameter param)
	{
		return Long.valueOf(get(param).trim());
	}

	public static double getDouble(Parameter param)
	{
		return Double.valueOf(get(param).trim());
	}

	public static boolean getBoolean(Parameter param)
	{
		return Boolean.valueOf(get(param).trim());
	}

	public static DriverMode getDriverMode()
	{
		return DriverMode.valueOf(get(Parameter.DRIVER_MODE).trim().toUpperCase());
	}
	
	@Deprecated
	public static Locale getLocale()
	{
		Locale locale = null;
		if (!StringUtils.isEmpty(get(Parameter.LOCALE)))
		{
			if (Configuration.get(Parameter.LOCALE).contains("_"))
			{
				locale = new Locale(get(Parameter.LOCALE).split("_")[0], get(Parameter.LOCALE).split("_")[1]);
			} else
			{
				locale = new Locale("", get(Parameter.LOCALE));
			}
		}
		return locale;
	}

	public static String asString()
	{
		StringBuilder asString = new StringBuilder();
		asString.append("\n============= Test configuration =============\n");
		for (Parameter param : Parameter.values())
		{
			if(!Parameter.CRYPTO_KEY_PATH.equals(param))
			{
				asString.append(String.format("%s=%s\n", param.getKey(), Configuration.get(param)));
			}
		}
		
		asString.append("\n------------- Driver capabilities -----------\n");
		// read all properties from config.properties and use "capabilities.*"
		final String prefix = SpecialKeywords.CAPABILITIES + ".";
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, String> capabilitiesMap = new HashMap(R.CONFIG.getProperties());
		for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
			if (entry.getKey().toLowerCase().startsWith(prefix)) {
				asString.append(String.format("%s=%s\n", entry.getKey(), R.CONFIG.get(entry.getKey())));
			}
		}
		
		asString.append("================================================\n");
		return asString.toString();
	}

	public static void validateConfiguration()
	{
		for (Parameter param : Parameter.values())
		{
			if (StringUtils.isEmpty(Configuration.get(param)) || SpecialKeywords.MUST_OVERRIDE.equals(Configuration.get(param)))
			{
				throw new RuntimeException("Configuration failure: parameter '" + param.getKey() + "' not specified!");
			}
		}
	}
	
	public static String getEnvArg(String key)
	{
		return envArgResolver.get(get(Parameter.ENV), key);
	}
	
	public static IEnvArgResolver getEnvArgResolver()
	{
		return envArgResolver;
	}

	public static void setEnvArgResolver(IEnvArgResolver envArgResolver)
	{
		Configuration.envArgResolver = envArgResolver;
	}
	
	public static boolean isNull(Parameter param)
	{
		// null is never returned from configuration now so compare with empty string
		return get(param).isEmpty(); 
	}
	
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, String> loadCoreProperties(String fileName) {

        LOGGER.info("Loading capabilities:");
        Properties props = new Properties();
        URL baseResource = ClassLoader.getSystemResource(fileName);
		try {
			if(baseResource != null)
			{
				props.load(baseResource.openStream());
				LOGGER.info("Custom capabilities properties loaded: " + fileName);
			} else {
				throw new RuntimeException("Unable to find custom capabilities file '" + fileName + "'!");	
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load custom capabilities from '" + baseResource.getPath() + "'!", e);
		}

        Map<String, String> propertiesMap = new HashMap(props);
        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            if (entry.getKey().startsWith(SpecialKeywords.CORE)) {
            	
                String valueFromEnv = null;
                if (!entry.getKey().equalsIgnoreCase("os")) {
                	valueFromEnv = System.getenv(entry.getKey());
                } else {
                	LOGGER.warn("'os' property can't be loaded from environment as it is default system variable!");
                }
                String value = (valueFromEnv != null) ? valueFromEnv : entry.getValue();
                
            	String key = entry.getKey().replaceAll(SpecialKeywords.CORE + ".", "");
            	LOGGER.info("Set custom core property: " + key + "; value: " + value);
            	R.CONFIG.put(key, value);
            }
        }
        
        return propertiesMap;
    }
    
    public static String getPlatform() {
    	// default "platform=value" should be used to determine current platform 
    	String platform = Configuration.get(Parameter.PLATFORM);
    	
    	//redefine platform if capabilities.platform is available
    	String prefix = SpecialKeywords.CAPABILITIES + ".";
    	if (!R.CONFIG.get(prefix + "platform").isEmpty()) {
    		platform = R.CONFIG.get(prefix + "platform");
    	}

    	//redefine platform if mobile.platformName is available
    	if (!R.CONFIG.get(prefix + "platformName").isEmpty()) {
    		platform = R.CONFIG.get(prefix + "platformName");
    	}
    	return platform;
    }
    
    
	public static String getDriverType() {
		String platform = getPlatform();
		String mobileType = SpecialKeywords.DESKTOP;
		if (platform.equalsIgnoreCase(SpecialKeywords.ANDROID) || platform.equalsIgnoreCase(SpecialKeywords.IOS)) {
			mobileType = SpecialKeywords.MOBILE;
		}
		return mobileType;
	}
    
    
    public static String getMobileApp() {
    	//redefine platform if capabilities.app is available
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
   
}
