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
package com.zebrunner.carina.utils.commons;

/**
 * Special keywords used in framework.
 * 
 * @author Alex Khursevich
 */
public class SpecialKeywords {
    public static final String IGNORE = "$ignore";
    public static final String GENERATE = "\\{generate:\\d*\\}";
    public static final String GENERATE_UUID = "\\{generate_uuid\\}";
    public static final String GENERATEAN = "\\{generatean:\\d*\\}";
    public static final String GENERATEN = "\\{generaten:\\d*\\}";
    public static final String TESTDATA = "\\{testdata:.*\\}";
    public static final String ENV = "\\{env:.*\\}";

    public static final String L10N = "L10N";
    public static final String L10N_PATTERN = String.format("\\{(%s:.[^\\'\\\"]*)}", L10N);

    public static final String EXCEL = "\\{excel:.*\\}";
    @Deprecated(forRemoval = true, since = "8.0.1")
    public static final String CRYPT = "\\{crypt:[^\\{\\}]*\\}";
    @Deprecated(forRemoval = true, since = "8.0.1")
    public static final String CRYPT_WRAPPER = "{crypt:%s}";
    public static final String PLACEHOLER = "\\$\\{[^\\{\\}]*\\}";

    public static final String MUST_OVERRIDE = "{must_override}";
    public static final String EXCEL_DS_ARGS = "{excel_ds_args}";
    public static final String EXCEL_DS_UID = "{excel_ds_uid}";
    public static final String EXCEL_DS_FLAG = "{excel_ds_flag}";
    public static final String EXCEL_DS_FILE = "{excel_ds_file}";
    public static final String EXCEL_DS_SHEET = "{excel_ds_sheet}";
    @Deprecated(since = "8.0.1", forRemoval = true)
    public static final String EXCEL_DS_CUSTOM_PROVIDER = "{excel_ds_custom_provider}";
    @Deprecated(since = "8.0.1", forRemoval = true)
    public static final String DS_CUSTOM_PROVIDER = "{ds_custom_provider}";
    public static final String DS_ARGS = "{ds_args}";
    public static final String DS_UID = "{ds_uid}";
    public static final String DS_FLAG = "{ds_flag}";
    public static final String DS_FILE = "{ds_file}";
    public static final String DS_EXECUTE_COLUMN = "{ds_execute_column}";
    public static final String DS_EXECUTE_VALUE = "{ds_execute_value}";
    public static final String TUID = "TUID";

    public static final String TEST_LOG_ID = "{test_log_id}";

    public static final String TEST_FAILURE_MESSAGE = "testFailureMessage";
    public static final String UUID = "UUID";

    public static final String HTML_REPORT = "emailable-report.html";
    public static final String NULL = "NULL";

    public static final String TEST_NAME_MAP = "{map}";
    public static final String TEST_NAME = "{test_name}";
    public static final String TEST_NAME_TUID = "{tuid}";
    public static final String TEST_NAME_CLASS = "{test_class}";
    public static final String METHOD_NAME = "{method_name}";
    public static final String METHOD_DESCRIPTION = "{method_description}";
    public static final String METHOD_PRIORITY = "{method_priority}";
    public static final String METHOD_THREAD_POOL_SIZE = "{method_thread_pool_size}";
    public static final String METHOD_GROUP_NAMES = "{group_names}";

    public static final String TEST_NAME_ARGS_MAP = "testNameArgsMap";

    public static final String TESTRAIL_SUITE_ID = "com.zebrunner.app/tcm.testrail.suite-id";

    public static final String BEFORE_TEST_METHOD = "executeBeforeTestMethod";

    public static final String INV_COUNT = "InvCount";
    public static final String INVOCATION_COUNTER = " (" + INV_COUNT + "=%s)";
    
    public static final String DAPAPROVIDER_INDEX = " [L%s]";
    
    public static final String DESKTOP = "desktop";
    public static final String MOBILE = "mobile";

    public static final String CUSTOM = "custom";

    public static final String ANDROID = "Android";
    public static final String IOS = "IOS";
    public static final String MAC = "MAC";
    public static final String WINDOWS = "Windows";
    public static final String TVOS = "TVOS";
    public static final String API = "API";

    public static final String NATIVE = "native";

    public static final String TABLET = "tablet";
    public static final String PHONE = "phone";
    public static final String TV = "tv";
    public static final String ANDROID_TV = "android_tv";

    public static final String PASSED = "PASSED";
    public static final String FAILED = "FAILED";
    public static final String SKIPPED = "SKIPPED";

    public static final String COMMENT = "comment";
    
    public static final String ANDROID_START_NODE = "<android[\\w\\.]* ";
    public static final String ANDROID_END_NODE = "<\\/android[\\w\\.]*>";
    public static final String ANDROID_START_UIX_NODE = "<node ";
    public static final String ANDROID_END_UIX_NODE = "</node>";

    public static final String TEST_PRIORITY_TAG = "priority";
    public static final String TEST_FEATURE_TAG = "feature";
    
    public final static String DRIVER_CONNECTION_REFUSED = "Driver connection refused";
    public final static String DRIVER_CONNECTION_REFUSED2 = "Expected to read a START_MAP but instead have: END. Last 0 characters read";
    public final static String DRIVER_TARGET_FRAME_DETACHED = "target frame detached";
    public final static String DRIVER_NO_SUCH_WINDOW = "no such window: window was already closed";

    
    // ------------- CARINA CRYPTO DEFAULT PROPERTIES -----------------
    @Deprecated(forRemoval = true, since = "8.0.1")
    public static final String CRYPTO_ALGORITHM = "AES/ECB/PKCS5Padding";
    @Deprecated(forRemoval = true, since = "8.0.1")
    public static final String CRYPTO_KEY_TYPE = "AES";
    @Deprecated(forRemoval = true, since = "8.0.1")
    public static final int CRYPTO_KEY_SIZE = 128;
    @Deprecated(forRemoval = true, since = "8.0.1")
    public static final String CRYPTO_KEY_PATH = "./src/main/resources/crypto.key";

    // ------------- PROXY PROPERTIES ---------------------------------
    public static final String PROXY_SETTER_PACKAGE = "tk.elevenk.proxysetter";
    public static final String PROXY_SETTER_RES_PATH = "app/proxy-setter-debug-0.2.apk";

    // ------------- CAPABILITIES PROPERTIES --------------------------
    public static final String CAPABILITIES = "capabilities";
    public static final String PLATFORM_NAME = "capabilities.platformName";
    public static final String PLATFORM_VERSION = "capabilities.platformVersion";
    public static final String BROWSERSTACK_PLATFORM_NAME = "capabilities.os";
    public static final String BROWSERSTACK_PLATFORM_VERSION = "capabilities.os_version";
    public static final String PROVIDER = "capabilities.provider";
    public static final String PROVIDER_OPTIONS = "capabilities.providerOptions";

    public static final String ADB_EXEC_TIMEOUT = "capabilities.adbExecTimeout";
    public static final String MOBILE_DEVICE_UDID = "capabilities.udid";
    public static final String MOBILE_DEVICE_NAME = "capabilities.deviceName";
    public static final String MOBILE_DEVICE_TYPE = "capabilities.deviceType";
    //TODO: switch into the PLATFORM_NAME later
    public static final String MOBILE_DEVICE_PLATFORM = "capabilities.platformName";
    public static final String MOBILE_DEVICE_PLATFORM_VERSION = "capabilities.platformVersion";
    public static final String MOBILE_DEVICE_REMOTE_URL = "capabilities.remoteURL";
    public static final String MOBILE_APP_ACITIVTY = "capabilities.appActivity";
    public static final String MOBILE_APP_PACKAGE = "capabilities.appPackage";
    public static final String MOBILE_PROXY_PORT = "capabilities.proxyPort";
    
    public static final String  SLOT_CAPABILITIES = "slotCapabilities";
    
    public static final String APP_PACKAGE = "appPackage";
    public static final String BUNDLE_ID = "CFBundleIdentifier";

    // ------------- STF PROPERTIES ---------------------------------
    public static final String ENABLE_ADB = "capabilities.enableAdb";
    
    // ------------- qTEST PROPERTIES -------------------------------
    public static final String QTEST_TESTCASE_UUID = "com.zebrunner.app/tcm.qtest.testcase-id";
    public static final String QTEST_PROJECT_ID = "com.zebrunner.app/tcm.qtest.project-id";


    // ------------- TEST EXECUTION FILTERS CONSTANTS ---------------
    public static final String RULE_FILTER_VALUE_SPLITTER = "=>";
    public static final String RULE_FILTER_SPLITTER = ";;";
    public static final String RULE_FILTER_AND_CONDITION = "&&";
    public static final String RULE_FILTER_OR_CONDITION = "||";
    public static final String RULE_FILTER_EXCLUDE_CONDITION = "!!";


    // ------------- Mobile screenshots cutting strategies configuration  ---------------
    public static final int DEFAULT_ADB_EXEC_TIMEOUT = 20000;
    public static final int DEFAULT_SCROLL_TIMEOUT = 100;
    public static final int DEFAULT_BLOCK = 0;
    public static final int DEFAULT_IOS_HEADER = 74;
    public static final int IPAD_HEADER = 102;
    public static final int IPHONE_X_HEADER = 95;
    public static final int IPHONE_PLUS_HEADER = 82;
    public static final int ALTERNATIVE_IOS_FOOTER = 42;
    public static final float DEFAULT_DPR= 2.0F;
    public static final float IPHONE_X_DPR= 3.0F;
    public static final int DEFAULT_WIDTH= 375;
    public static final int DEFAULT_PLUS_WIDTH= 414;
    public static final int DEFAULT_IPAD_WIDTH= 768;
    public static final int DEFAULT_SE_WIDTH= 320;
    
    // ------------- Cucumber configuration  ---------------
    public static final String CUCUMBER_REPORT_FOLDER = "cucumber-reports";
    public static final String CUCUMBER_REPORT_SUBFOLDER = "cucumber-html-reports";
    public static final String CUCUMBER_REPORT_FILE_NAME = "overview-features.html";

    public static final String NO_SUCH_ELEMENT_ERROR="no such element ";
}
