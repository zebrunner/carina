/*
 * Copyright 2013 QAPROSOFT (http://qaprosoft.com/).
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

import java.lang.reflect.Constructor;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * Configuration utility.
 * 
 * @author Aliaksei_Khursevich
 * @email hursevich@gmail.com
 */
public class Configuration
{
	private static final String MUST_OVERRIDE = "{must_override}";
	
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

		private DriverMode(String key)
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
		URL("url", "default_url"),
		
		ENV("env", "default_env"),
		
		ENV_ARG_RESOLVER("env_arg_resolver", "default_env_arg_resolver"),
		
		PLATFORM("platform", "default_platform"),

		BROWSER("browser", "default_browser"),
		
		BROWSER_VERSION("browser_version", "default_browser_version"),

		SELENIUM_HOST("selenium_host", "default_selenium_host"),
		
		DRIVER_MODE("driver_mode", "default_driver_mode"),
		
		APP_VERSION("app_version", "default_app_version"),

		REPORT_URL("report_url", "default_report_url"),

		EMAIL_LIST("email_list", "default_email_list"),

		AUTO_SCREENSHOT("auto_screenshot", "default_auto_screenshot"),
		
		TAKE_ONLY_FAIL_SCREENSHOT("take_only_fail_screenshot", "default_take_only_fail_screenshot"),

		IMPLICIT_TIMEOUT("implicit_timeout", "default_implicit_timeout"),

		EXPLICIT_TIMEOUT("explicit_timeout", "default_explicit_timeout"),
		
		AUTO_DOWNLOAD("auto_download", "default_auto_download"),
		
		AUTO_DOWNLOAD_APPS("auto_download_apps", "default_auto_download_apps"),

		RETRY_TIMEOUT("retry_timeout", "default_retry_timeout"),

		PROJECT_REPORT_DIRECTORY("project_report_directory", "default_project_report_directory"),

		MAX_SCREENSHOOT_HISTORY("max_screen_history", "default_max_screen_history"),
		
		RESULT_SORTING("result_sorting", "default_result_sorting"),

		KEEP_ALL_SCREENSHOTS("keep_all_screenshots", "default_keep_all_screenshots"),

		BIG_SCREEN_WIDTH("big_screen_width", "default_big_screen_width"),

		BIG_SCREEN_HEIGHT("big_screen_height", "default_big_screen_height"),

		SMALL_SCREEN_WIDTH("small_screen_width", "default_small_screen_width"),

		SMALL_SCREEN_HEIGHT("small_screen_height", "default_small_screen_height"),

		SENDER_EMAIL("sender_email", "default_sender_email"),

		SENDER_PASSWORD("sender_pswd", "default_sender_pswd"),
		
		RETRY_COUNT("retry_count", "default_retry_count"),

		LOCALE("locale", "default_locale"),

		THREAD_COUNT("thread_count", "default_thread_count"),
		
		TEST_ID("test_id", "default_test_id"),
		
		IS_TESTEXECUTER("is_testexecuter", "default_is_testexecuter"),
		
		TESTEXECUTER_URL("testexecuter_url", "default_testexecuter_url"),
		
		LOG_ALL_JSON("log_all_json", "default_log_all_json"),

		DATE_FORMAT("date_format", "default_date_format"),
		
		IS_API("is_api", "default_is_api"),
		
		LOAD_SETTINGS("load_settings", "default_load_settings"),
		
		CRYPTO_KEY_PATH("crypto_key_path", "default_crypto_key_path"),
		
		SUITE_NAME("suite_name", "default_suite_name"),

		CI_URL("ci_url", "default_ci_url"),
		
		CI_BUILD("ci_build", "default_ci_build"),		
		
		CI_BUILD_CAUSE("ci_build_cause", "default_ci_build_cause"),
		
		CI_PARENT_URL("ci_parent_url", "default_ci_parent_url"),
		
		CI_PARENT_BUILD("ci_parent_build", "default_ci_parent_build"),
		
		CI_USER_ID("ci_user_id", "default_ci_user_id"),
		
		CI_USER_FIRST_NAME("ci_user_first_name", "default_ci_user_first_name"),
		
		CI_USER_LAST_NAME("ci_user_last_name", "default_ci_user_last_name"),
		
		CI_USER_EMAIL("ci_user_email", "default_ci_user_email"),
		
		JIRA_UPDATER("jira_updater", "default_jira_updater"),
		
		JIRA_URL("jira_url", "default_jira_url"),
		
		JIRA_USER("jira_user", "default_jira_user"),
		
		JIRA_PASSWORD("jira_password", "default_jira_password"),
		
		JIRA_SUITE_ID("jira_suite_id", "default_jira_suite_id"),

        JIRA_PROJECT("jira_project", "default_jira_project"),

        JIRA_PROJECT_SHORT("jira_project_short", "default_jira_project_short"),
        
        JIRA_CREATE_NEW_TICKET("jira_create_new_ticket", "default_jira_create_new_ticket"),
        
        // Appium 1.1.x mobile capabilities: iOS and Android
        MOBILE_DEVICE_NAME("mobile_device_name", "default_mobile_device_name"), 
        
        MOBILE_PLATFORM_NAME("mobile_platform_name", "default_mobile_platform_name"),
        
        MOBILE_PLATFORM_VERSION("mobile_platform_version", "default_mobile_platform_version"),
        
        MOBILE_BROWSER_NAME("mobile_browser_name", "default_mobile_browser_name"),
        
        MOBILE_AUTOMATION_NAME("mobile_automation_name", "default_mobile_automation_name"), // Sendroid 
        
        MOBILE_APP("mobile_app", "default_mobile_app"),
        
        MOBILE_APP_ACTIVITY("mobile_app_activity", "default_mobile_app_activity"),
        
		MOBILE_APP_PACKAGE("mobile_app_package", "default_mobile_app_package"), 
		
		MOBILE_NEW_COMMAND_TIMEOUT("mobile_new_command_timeout", "default_mobile_new_command_timeout"),
		
		// video recording and uploading to Dropbox
		MOBILE_DEVICE_UDID("mobile_device_udid", "default_mobile_device_udid"),
		
		DROPBOX_ACCESS_TOKEN("dropbox_access_token", "default_dropbox_access_token"), 
		
		VIDEO_RECORDING("video_recording", "default_video_recording"),
		
		ADB_HOST("adb_host", "default_adb_host"),
		
		ADB_PORT("adb_port", "default_adb_port"),
		
		// spira
		SPIRA_RELEASE_ID("spira_release_id", "default_spira_release_id"),
		
		SPIRA_TESTSET_ID("spira_testset_id", "default_spira_testset_id"),
		
		//zafira
		ZAFIRA_SERVICE_URL("zafira_service_url", "default_zafira_service_url"),
		
		GIT_BRANCH("git_branch", "default_git_branch"),
		
		GIT_COMMIT("git_commit", "default_git_commit"),
		
		GIT_URL("git_url", "default_git_url");
		
		private final String key;

		private final String defaultKey;

		private Parameter(String key, String defaultKey)
		{
			this.key = key;
			this.defaultKey = defaultKey;
		}

		public String getKey()
		{
			return key;
		}

		public String getDefaultKey()
		{
			return defaultKey;
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
		String startupArg = System.getProperty(param.getKey());
		String defaultConfigArg = R.CONFIG.get(param.getDefaultKey());
		String configArg = R.CONFIG.get(param.getKey());
		String value = defaultConfigArg;
		
		if(!StringUtils.isEmpty(configArg)) {
			value = configArg;
		}
		else if(!StringUtils.isEmpty(startupArg)) {
			 value = startupArg;
		}
		
		if (value == null || value.equalsIgnoreCase(SpecialKeywords.NULL)) {
			value = "";
		}
		
	
		return value;
		
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

	public static DriverMode getDriverMode(Parameter param)
	{
		return DriverMode.valueOf(get(param).trim().toUpperCase());
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
			if (StringUtils.isEmpty(Configuration.get(param)) || MUST_OVERRIDE.equals(Configuration.get(param)))
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
}
