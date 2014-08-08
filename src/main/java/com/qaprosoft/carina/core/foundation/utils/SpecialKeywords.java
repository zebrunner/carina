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
	public static final String TESTDATA = "\\{testdata:.*\\}";
	public static final String ENV = "\\{env:.*\\}";
	public static final String L18N = "\\{l18n:.*\\}";
	public static final String EXCEL = "\\{excel:.*\\}";
	public static final String CRYPT = "\\{crypt:[^\\{\\}]*\\}";
	
	public static final String EXCEL_DS_ARGS = "{excel_ds_args}";
	public static final String EXCEL_DS_UID = "{excel_ds_uid}";
	public static final String EXCEL_DS_JIRA = "{excel_ds_jira}";
	public static final String EXCEL_DS_FLAG = "{excel_ds_flag}";
	public static final String EXCEL_DS_FILE = "{excel_ds_file}";
	public static final String EXCEL_DS_SHEET = "{excel_ds_sheet}";
	public static final String EXCEL_DS_CUSTOM_PROVIDER = "{excel_ds_custom_provider}";
	public static final String DS_CUSTOM_PROVIDER = "{ds_custom_provider}";
	public static final String TUID = "TUID";
	
	public static final String TEST_LOG_ID = "{test_log_id}";
	
	public static final String JIRA_TICKET = "JIRA#";
	public static final String SESSION_ID = "sessionId";
	public static final String INITIALIZATION_FAILURE = "initializeFailure";
	public static final String UUID = "UUID";
	
}
