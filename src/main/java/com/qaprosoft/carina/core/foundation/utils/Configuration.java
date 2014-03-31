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
	 * All available configuration parameter keys along with default values.
	 */
	public enum Parameter
	{
		URL("url", "default_url"),
		
		ENV("env", "default_env"),
		
		ENV_ARG_RESOLVER("env_arg_resolver", "default_env_arg_resolver"),

		BROWSER("browser", "default_browser"),
		
		BROWSER_VERSION("browser_version", "default_browser_version"),

		SELENIUM_HOST("selenium_host", "default_selenium_host"),
		
		APP_VERSION("app_version", "default_app_version"),

		REPORT_URL("report_url", "default_report_url"),

		EMAIL_LIST("email_list", "default_email_list"),

		AUTO_SCREENSHOT("auto_screenshot", "default_auto_screenshot"),
		
		TAKE_ONLY_FAIL_SCREENSHOT("take_only_fail_screenshot", "default_take_only_fail_screenshot"),

		IMPLICIT_TIMEOUT("implicit_timeout", "default_implicit_timeout"),

		EXPLICIT_TIMEOUT("explicit_timeout", "default_explicit_timeout"),

		RETRY_TIMEOUT("retry_timeout", "default_retry_timeout"),

		PROJECT_REPORT_DIRECTORY("project_report_directory", "default_project_report_directory"),

		ROOT_REPORT_DIRECTORY("root_report_directory", "default_root_report_directory"),

		MAX_SCREENSHOOT_HISTORY("max_screen_history", "default_max_screen_history"),

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
		
		USER_AGENT("user_agent", "default_user_agent"),
		
		TEST_ID("test_id", "default_test_id"),
		
		IS_TESTEXECUTER("is_testexecuter", "default_is_testexecuter"),
		
		TESTEXECUTER_URL("testexecuter_url", "default_testexecuter_url"),
		
		LOG_ALL_JSON("log_all_json", "default_log_all_json"),

		DATE_FORMAT("date_format", "default_date_format"),
		
		IS_API("is_api", "default_is_api"),
		
		LOAD_SETTINGS("load_settings", "default_load_settings"),
		
		CRYPTO_KEY_PATH("crypto_key_path", "default_crypto_key_path"),
		
		SUITE_NAME("suite_name", "default_suite_name"),
		
		JENKINS_URL("jenkins_url", "default_jenkins_url"),
		
		JENKINS_JOB("jenkins_job", "default_jenkins_job"),
		
		MOBILE_OS("mobile_os", "default_mobile_os"),
		
		MOBILE_VERSION("mobile_version", "default_mobile_version"),
		
		MOBILE_PLATFORM("mobile_platform", "default_mobile_platform"),
		
		MOBILE_BROWSER("mobile_browser", "default_mobile_browser"), //Android capability
		
		MOBILE_APP("mobile_app", "default_mobile_app"),		
		
		MOBILE_APP_PACKAGE("mobile_app_package", "default_mobile_app_package"), //Android capability
		
		MOBILE_APP_ACTIVITY("mobile_app_activity", "default_mobile_app_activity"), //Android capability
		
		MOBILE_NEW_COMMAND_TIMEOUT("mobile_new_command_timeout", "default_mobile_new_command_timeout"), //Appium capability iOS + Android
		
		MOBILE_DEVICE("mobile_device", "default_mobile_device"),
		
		JIRA_UPDATER("jira_updater", "default_jira_updater"),
		
		JIRA_URL("jira_url", "default_jira_url"),
		
		JIRA_USER("jira_user", "default_jira_user"),
		
		JIRA_PASSWORD("jira_password", "default_jira_password"),

        JIRA_PROJECT("jira_project", "default_jira_project"),

        JIRA_PROJECT_SHORT("jira_project_short", "default_jira_project_short"),
        
        JIRA_CREATE_NEW_TICKET("jira_create_new_ticket", "default_jira_create_new_ticket");

		private final String key;

		private final String defaultKey;

		private Parameter(String key, String defaultKey)
		{
			this.key = key;
			this.defaultKey = defaultKey;
		}

		private String getKey()
		{
			return key;
		}

		private String getDefaultKey()
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
		
		if(!StringUtils.isEmpty(startupArg))
		{
			 return startupArg;
		}
		else if(!StringUtils.isEmpty(configArg))
		{
			return configArg;
		}
		else
		{
			return defaultConfigArg;
		}
	}

	public static int getInt(Parameter param)
	{
		return Integer.valueOf(get(param));
	}

	public static long getLong(Parameter param)
	{
		return Long.valueOf(get(param));
	}

	public static double getDouble(Parameter param)
	{
		return Double.valueOf(get(param));
	}

	public static boolean getBoolean(Parameter param)
	{
		return Boolean.valueOf(get(param));
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
//		if(!"NULL".equalsIgnoreCase(Configuration.get(Parameter.ENV)) && getEnvArg("base") == null)
//		{
//			throw new RuntimeException("If config arg 'evn' not null, arg 'env'.base should be set!");
//		}
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
		return get(param) == null || "NULL".equalsIgnoreCase(get(param)); 
	}
}
