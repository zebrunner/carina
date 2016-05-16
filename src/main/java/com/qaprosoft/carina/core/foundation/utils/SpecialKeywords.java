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
	public static final String L10N_PATTERN = String.format("\\{%s:.*\\}", L10N);
	
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
	
	public static final String SPIRA_RELEASE_ID = "SPIRA_RELEASE_ID";
	public static final String SPIRA_TESTSET_ID = "SPIRA_TESTSET_ID";
	public static final String SPIRA_TESTCASE_ID = "SPIRA_TESTCASE_ID";
	public static final String SPIRA_TESTSTEP_ID = "SPIRA_TESTSTEP_ID";
	
	public static final String HTML_REPORT = "emailable-report.html";
	public static final String NULL = "NULL";

	public static final String TEST_NAME_ARGS_MAP = "testNameArgsMap";
	public static final String TEST_METHOD_NAME_ARGS_MAP = "testMethodNameArgsMap";
	public static final String TEST_METHOD_OWNER_ARGS_MAP = "testMethodOwnerArgsMap";	
	public static final String JIRA_ARGS_MAP = "jiraArgsMap";
	public static final String TESTRAIL_ARGS_MAP = "testRailCasesArgsMap";
	public static final String TESTRAIL_CASES_ID = "testRailCases";
	public static final String TESTRAIL_SUITE_ID = "TestRailSuiteId";
	public static final String TESTRAIL_PROJECT_ID = "TestRailProjectId";
	
	
	public static final String INVOCATION_COUNTER = " (InvCount=%s)";
	public static final String DESKTOP = "desktop";
	public static final String MOBILE = "mobile";
	public static final String MOBILE_POOL = "mobile_pool";
	public static final String MOBILE_GRID = "mobile_grid";

	public static final String CUSTOM = "custom";

	public static final String ANDROID = "Android";
	public static final String IOS = "IOS";

	public static final String NATIVE = "native";

	public static final String TABLET = "tablet";
	public static final String PHONE = "phone";
	
	public static final String CORE = "core";
	
	public static final String PASSED = "PASSED";
	public static final String FAILED = "FAILED";
	public static final String SKIPPED = "SKIPPED";
	
	public static final String ALREADY_PASSED = "ALREADY_PASSED";
	
	
	
}
