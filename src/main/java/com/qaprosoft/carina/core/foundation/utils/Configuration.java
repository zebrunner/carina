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
		if(!Configuration.isNull(Parameter.ENV_ARG_RESOLVER))
		{
			try
			{
				Class<?> cl = Class.forName(Configuration.get(Parameter.ENV_ARG_RESOLVER));
				Constructor<?> ct = cl.getConstructor();
				Configuration.setEnvArgResolver((IEnvArgResolver)ct.newInstance());
			}
			catch(Exception e)
			{
				throw new RuntimeException("Configuration failure: can not initiate EnvArgResolver - + " + Configuration.get(Parameter.ENV_ARG_RESOLVER));
			}
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
		
		DEVELOP("develop"), 
		
		PLATFORM("platform"),

		BROWSER("browser"),
		
		BROWSER_VERSION("browser_version"),

		SELENIUM_HOST("selenium_host"),
		
		DRIVER_TYPE("driver_type"),
		
		DRIVER_MODE("driver_mode"),
		
		CUSTOM_CAPABILITIES("custom_capabilities"),
		
		EXTRA_CAPABILITIES("extra_capabilities"),
		
		APP_VERSION("app_version"),
		
		PROXY_HOST("proxy_host"),
		
		PROXY_PORT("proxy_port"),
		
		PROXY_PROTOCOLS("proxy_protocols"),
		
		REPORT_URL("report_url"),

		EMAIL_LIST("email_list"),
		
		TEMP_EMAIL_LIST("temp_email_list"),
		
		FAILURE_EMAIL_LIST("failure_email_list"),
		
		IGNORE_KNOWN_ISSUES("ignore_known_issues"),

		AUTO_SCREENSHOT("auto_screenshot"),
		
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
		
		LOCALE("locale"),
		
		ENABLE_I18N("enable_i18n"),
		
		LANGUAGE("language"),
		
		THREAD_COUNT("thread_count"),
		
		DATA_PROVIDER_THREAD_COUNT("data_provider_thread_count"),
		
		CORE_LOG_LEVEL("core_log_level"),
		
		LOG_ALL_JSON("log_all_json"),

		DATE_FORMAT("date_format"),
		
		TIME_FORMAT("time_format"),
		
		IS_API("is_api"),
		
		LOAD_SETTINGS("load_settings"),
		
		CRYPTO_KEY_PATH("crypto_key_path"),
		
		SUITE_NAME("suite_name"),

		CI_RUN_ID("ci_run_id"),
		
		CI_URL("ci_url"),
		
		CI_BUILD("ci_build"),		
		
		CI_BUILD_CAUSE("ci_build_cause"),
		
		CI_PARENT_URL("ci_parent_url"),
		
		CI_PARENT_BUILD("ci_parent_build"),
		
		CI_USER_ID("ci_user_id"),
		
		CI_USER_FIRST_NAME("ci_user_first_name"),
		
		CI_USER_LAST_NAME("ci_user_last_name"),
		
		CI_USER_EMAIL("ci_user_email"),
		
		JIRA_UPDATER("jira_updater"),
		
		JIRA_URL("jira_url"),
		
		JIRA_USER("jira_user"),
		
		JIRA_PASSWORD("jira_password"),
		
		JIRA_SUITE_ID("jira_suite_id"),

        JIRA_PROJECT("jira_project"),

        JIRA_PROJECT_SHORT("jira_project_short"),
        
        JIRA_CREATE_NEW_TICKET("jira_create_new_ticket"),
        
        // Appium 1.1.x mobile capabilities: iOS and Android
        MOBILE_DEVICE_NAME("mobile_device_name"), 
        
        MOBILE_PLATFORM_NAME("mobile_platform_name"),
        
        MOBILE_PLATFORM_VERSION("mobile_platform_version"),
        
        MOBILE_AUTOMATION_NAME("mobile_automation_name"), // Sendroid 
        
        MOBILE_APP("mobile_app"),
        
        MOBILE_APP_ACTIVITY("mobile_app_activity"),
        
		MOBILE_APP_PACKAGE("mobile_app_package"), 
		
		MOBILE_NEW_COMMAND_TIMEOUT("mobile_new_command_timeout"),
		
		MOBILE_DEVICES("mobile_devices"),
		
		// video recording and uploading to Dropbox
		MOBILE_DEVICE_UDID("mobile_device_udid"),
		
		MOBILE_DEVICE_TYPE("mobile_device_type"),
		
		MOBILE_SCREEN_SWITCHER("mobile_screen_switcher"),
		
		//DROPBOX_ACCESS_TOKEN("dropbox_access_token"), 
		
		VIDEO_RECORDING("video_recording"),
		
		ADB_HOST("adb_host"),
		
		ADB_PORT("adb_port"),

		//TestRail

		TESTRAIL_URL("testrail_url"),

		TESTRAIL_USER("testrail_user"),

		TESTRAIL_PASSWORD("testrail_password"),

		TESTRAIL_UPDATER("testrail_updater"),

		TESTRAIL_MILESTONE("testrail_milestone"),

		TESTRAIL_ASSIGNEE_USER("testrail_assignee"),

		// spira
		SPIRA_URL("spira_url"),
		
		SPIRA_USER("spira_user"),
		
		SPIRA_PASSWORD("spira_password"),
		
		SPIRA_UPDATER("spira_updater"),
		
		SPIRA_RELEASE_ID("spira_release_id"),
		
		SPIRA_TESTSET_ID("spira_testset_id"),
		
		//zafira
		ZAFIRA_GRID_ENABLED("zafira_grid_enabled"),
		
		ZAFIRA_GRID_PKEY("zafira_grid_pkey"),
		
		ZAFIRA_GRID_SKEY("zafira_grid_skey"),
		
		ZAFIRA_GRID_CHANNEL("zafira_grid_channel"),
		
		ZAFIRA_SERVICE_URL("zafira_service_url"),
		
		ZAFIRA_USERNAME("zafira_username"),
		
		ZAFIRA_PASSWORD("zafira_password"),
		
		ZAFIRA_PROJECT("zafira_project"),
		
		RERUN_FAILURES("rerun_failures"),
		
		GIT_BRANCH("git_branch"),
		
		GIT_COMMIT("git_commit"),
		
		GIT_URL("git_url"),
		
		UNIQUE_TESTRUN_FIELDS("unique_testrun_fields"),
		
		//Amazon
		S3_BUCKET_NAME("s3_bucket_name"),
		
		ACCESS_KEY_ID("access_key_id"),
		
		SECRET_KEY("secret_key"),
		
		//Amazon-Screenshot
		S3_SCREENSHOT_BUCKET_NAME("s3_screenshot_bucket_name"),
		
		S3_SAVE_SCREENSHOTS("s3_save_screenshots"),
		
		//For localization parser
		ADD_NEW_LOCALIZATION("add_new_localization"), 
		
		ADD_NEW_LOCALIZATION_PATH("add_new_localization_path"),
		
		ADD_NEW_LOCALIZATION_PROPERTY_NAME("add_new_localization_property_name"),

		//For cucumber tests
		CUCUMBER_TESTS("cucumber_tests"),

		CUCUMBER_TESTS_APP_VERSION("cucumber_tests_app_version"),

		CUCUMBER_TESTS_NAME("cucumber_tests_name"),

		CUCUMBER_TESTS_RESULTS_IMAGE_RESIZE("cucumber_tests_results_image_resize"),

		CUCUMBER_USE_JS_IN_REPORT("cucumber_user_js_in_report"),

		CUCUMBER_REPORT_SUBFOLDER("cucumber_report_subfolder"),
		
		SSH_USERNAME("ssh_username"),
		
		ADB_PATH("adb_path"),

		// TLS
		TLS_KEYSECURE_LOCATION("tls_keysecure_location")
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
		return get(param) == null || SpecialKeywords.NULL.equalsIgnoreCase(get(param)); 
	}
	
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void loadCoreProperties(String fileName) {

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

    }
}
