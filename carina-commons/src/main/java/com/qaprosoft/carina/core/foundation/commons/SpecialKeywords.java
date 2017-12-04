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
package com.qaprosoft.carina.core.foundation.commons;

/**
 * Special keywords used in framework.
 * 
 * @author Alex Khursevich
 */
public class SpecialKeywords
{
	public static final String IGNORE = "$ignore";
	public static final String GENERATE = "\\{generate:\\d*\\}";
	public static final String GENERATE_UUID = "\\{generate_uuid\\}";
	public static final String GENERATEAN = "\\{generatean:\\d*\\}";
	public static final String GENERATEN = "\\{generaten:\\d*\\}";
	public static final String TESTDATA = "\\{testdata:.*\\}";
	public static final String ENV = "\\{env:.*\\}";
	
	public static final String I18N = "I18N";
	public static final String L10N = "L10N";
	public static final String I18N_PATTERN = String.format("\\{%s:.*\\}", I18N);
	public static final String L10N_PATTERN = String.format("\\{(%s:.[^\\'\\\"]*)}", L10N);
	
	public static final String EXCEL = "\\{excel:.*\\}";
	public static final String CRYPT = "\\{crypt:[^\\{\\}]*\\}";
	public static final String CRYPT_WRAPPER = "{crypt:%s}";
	public static final String PLACEHOLER = "\\$\\{[^\\{\\}]*\\}";
	
	public static final String MUST_OVERRIDE = "{must_override}";
	@Deprecated
	public static final String EXCEL_DS_ARGS = "{excel_ds_args}";
	@Deprecated
	public static final String EXCEL_DS_UID = "{excel_ds_uid}";
	@Deprecated
	public static final String EXCEL_DS_JIRA = "{excel_ds_jira}";
	@Deprecated
	public static final String EXCEL_DS_FLAG = "{excel_ds_flag}";
	public static final String EXCEL_DS_FILE = "{excel_ds_file}";
	@Deprecated
	public static final String EXCEL_DS_SHEET = "{excel_ds_sheet}";
	@Deprecated
	public static final String EXCEL_DS_CUSTOM_PROVIDER = "{excel_ds_custom_provider}";
	public static final String DS_CUSTOM_PROVIDER = "{ds_custom_provider}";
	public static final String DS_ARGS = "{ds_args}";
	public static final String DS_UID = "{ds_uid}";
	public static final String DS_JIRA = "{ds_jira}";
	public static final String DS_FLAG = "{ds_flag}";
	public static final String DS_FILE = "{ds_file}";
	public static final String DS_EXECUTE_COLUMN = "{ds_execute_column}";
	public static final String DS_EXECUTE_VALUE = "{ds_execute_value}";
	public static final String TUID = "TUID";
	
	public static final String TEST_LOG_ID = "{test_log_id}";
	
	public static final String JIRA_TICKET = "JIRA#";
	public static final String TEST_FAILURE_MESSAGE = "testFailureMessage";
	public static final String UUID = "UUID";
	public static final String VIDEO_FILE_NAME = "/sdcard/test_record.mp4";
	
	public static final String HTML_REPORT = "emailable-report.html";
	public static final String NULL = "NULL";

	public static final String TEST_NAME_ARGS_MAP = "testNameArgsMap";
	public static final String CANONICAL_TEST_NAME_ARGS_MAP = "canonicalTestNameArgsMap";
	public static final String TEST_METHOD_NAME_ARGS_MAP = "testMethodNameArgsMap";
	public static final String TEST_METHOD_OWNER_ARGS_MAP = "testMethodOwnerArgsMap";	
	public static final String JIRA_ARGS_MAP = "jiraArgsMap";
	public static final String TESTRAIL_ARGS_MAP = "testRailCasesArgsMap";
	public static final String TESTRAIL_CASES_ID = "testRailCases";
	public static final String TESTRAIL_SUITE_ID = "TestRailSuiteId";
	public static final String TESTRAIL_PROJECT_ID = "TestRailProjectId";
	public static final String BUG_ARGS_MAP = "bugArgsMap";
	public static final String DO_NOT_RUN_TESTS = "doNotRunTests";
	
	public static final String BEFORE_TEST_METHOD = "executeBeforeTestMethod";
	
	public static final String INV_COUNT = "InvCount";
	public static final String INVOCATION_COUNTER = " (" + INV_COUNT + "=%s)";
	public static final String DESKTOP = "desktop";
	public static final String MOBILE = "mobile";

	public static final String CUSTOM = "custom";

	public static final String ANDROID = "Android";
	public static final String IOS = "IOS";

	public static final String NATIVE = "native";

	public static final String TABLET = "tablet";
	public static final String PHONE = "phone";
	public static final String TV = "tv";
	
	public static final String CORE = "core";
	
	public static final String PASSED = "PASSED";
	public static final String FAILED = "FAILED";
	public static final String SKIPPED = "SKIPPED";
	
	public static final String ALREADY_PASSED = "ALREADY_PASSED";
	public static final String SKIP_EXECUTION = "SKIP_EXECUTION";
	
	public static final String ZAFIRA_PROJECT = "zafira_project";
	
	public static final String COMMENT = "comment";
	
	
	
	// ------------- CARINA CRYPTO DEFAULT PROPERTIES -----------------
	public static final String CRYPTO_ALGORITHM = "AES/ECB/PKCS5Padding";
	public static final String CRYPTO_KEY_TYPE = "AES";
	public static final int CRYPTO_KEY_SIZE = 128;
	public static final String CRYPTO_KEY_PATH = "./src/main/resources/crypto.key";
	
	
	// ------------- PROXY PROPERTIES ---------------------------------
	public static final String PROXY_SETTER_PACKAGE = "tk.elevenk.proxysetter";
	public static final String PROXY_SETTER_RES_PATH = "app/proxy-setter-debug-0.2.apk";
    
	// ------------- CAPABILITIES PROPERTIES --------------------------
	public static final String CAPABILITIES = "capabilities";
	public static final String MOBILE_DEVICE_UDID = "capabilities.udid";
	public static final String MOBILE_DEVICE_NAME = "capabilities.deviceName";
	public static final String MOBILE_DEVICE_TYPE = "capabilities.deviceType";
	public static final String MOBILE_DEVICE_PLATFORM = "capabilities.platformName";
	public static final String MOBILE_DEVICE_PLATFORM_VERSION = "capabilities.platformVersion";
	public static final String MOBILE_DEVICE_REMOTE_URL = "capabilities.remoteURL";
	public static final String MOBILE_APP_ACITIVTY = "capabilities.appActivity";
	public static final String MOBILE_APP_PACKAGE = "capabilities.appPackage";
	
	
	// ------------- STF PROPERTIES ---------------------------------
	public static final String STF_ENABLED = "STF_ENABLED";
	public static final String STF_URL = "STF_URL";
	public static final String STF_TOKEN = "STF_TOKEN";
}
