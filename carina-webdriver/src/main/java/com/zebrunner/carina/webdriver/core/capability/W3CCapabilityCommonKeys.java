package com.zebrunner.carina.webdriver.core.capability;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.YouiEngineCapabilityType;
import io.appium.java_client.remote.options.W3CCapabilityKeys;

public class W3CCapabilityCommonKeys extends W3CCapabilityKeys {
    public static final W3CCapabilityCommonKeys INSTANCE = new W3CCapabilityCommonKeys();

    private W3CCapabilityCommonKeys() {
    }

    private static final Predicate<String> COMMON_CAPABILITIES_PATTERNS = Stream.of(
            /**
             * see {{{@link W3CCapabilityKeys}}}
             */
            "^[\\w-]+:.*$",
            "^acceptInsecureCerts$",
            "^browserName$",
            "^browserVersion$",
            "^platformName$",
            "^pageLoadStrategy$",
            "^proxy$",
            "^setWindowRect$",
            "^strictFileInteractability$",
            "^timeouts$",
            "^unhandledPromptBehavior$",
            "^webSocketUrl$",
            // MobileCapabilityType capabilities
            "^" + MobileCapabilityType.AUTOMATION_NAME + "$",
            "^" + MobileCapabilityType.PLATFORM_VERSION + "$",
            "^" + MobileCapabilityType.DEVICE_NAME + "$",
            "^" + MobileCapabilityType.NEW_COMMAND_TIMEOUT + "$",
            "^" + MobileCapabilityType.APP + "$",
            "^" + MobileCapabilityType.UDID + "$",
            "^" + MobileCapabilityType.LANGUAGE + "$",
            "^" + MobileCapabilityType.LOCALE + "$",
            "^" + MobileCapabilityType.ORIENTATION + "$",
            "^" + MobileCapabilityType.AUTO_WEBVIEW + "$",
            "^" + MobileCapabilityType.NO_RESET + "$",
            "^" + MobileCapabilityType.FULL_RESET + "$",
            "^" + MobileCapabilityType.CLEAR_SYSTEM_FILES + "$",
            "^" + MobileCapabilityType.EVENT_TIMINGS + "$",
            "^" + MobileCapabilityType.ENABLE_PERFORMANCE_LOGGING + "$",
            "^" + MobileCapabilityType.OTHER_APPS + "$",
            "^" + MobileCapabilityType.PRINT_PAGE_SOURCE_ON_FIND_FAILURE + "$",
            // YouiEngineCapabilityType
            "^" + YouiEngineCapabilityType.APP_ADDRESS + "$",
            // IOSMobileCapabilityType
            "^" + IOSMobileCapabilityType.CALENDAR_FORMAT + "$",
            "^" + IOSMobileCapabilityType.BUNDLE_ID + "$",
            "^" + IOSMobileCapabilityType.LOCATION_SERVICES_ENABLED + "$",
            "^" + IOSMobileCapabilityType.LOCATION_SERVICES_AUTHORIZED + "$",
            "^" + IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS + "$",
            "^" + IOSMobileCapabilityType.AUTO_DISMISS_ALERTS + "$",
            "^" + IOSMobileCapabilityType.NATIVE_INSTRUMENTS_LIB + "$",
            "^" + IOSMobileCapabilityType.NATIVE_WEB_TAP + "$",
            "^" + IOSMobileCapabilityType.SAFARI_INITIAL_URL + "$",
            "^" + IOSMobileCapabilityType.SAFARI_ALLOW_POPUPS + "$",
            "^" + IOSMobileCapabilityType.SAFARI_IGNORE_FRAUD_WARNING + "$",
            "^" + IOSMobileCapabilityType.SAFARI_OPEN_LINKS_IN_BACKGROUND + "$",
            "^" + IOSMobileCapabilityType.KEEP_KEY_CHAINS + "$",
            "^" + IOSMobileCapabilityType.LOCALIZABLE_STRINGS_DIR + "$",
            "^" + IOSMobileCapabilityType.PROCESS_ARGUMENTS + "$",
            "^" + IOSMobileCapabilityType.INTER_KEY_DELAY + "$",
            "^" + IOSMobileCapabilityType.SHOW_IOS_LOG + "$",
            "^" + IOSMobileCapabilityType.SEND_KEY_STRATEGY + "$",
            "^" + IOSMobileCapabilityType.SCREENSHOT_WAIT_TIMEOUT + "$",
            "^" + IOSMobileCapabilityType.WAIT_FOR_APP_SCRIPT + "$",
            "^" + IOSMobileCapabilityType.WEBVIEW_CONNECT_RETRIES + "$",
            "^" + IOSMobileCapabilityType.APP_NAME + "$",
            "^" + IOSMobileCapabilityType.CUSTOM_SSL_CERT + "$",
            "^" + IOSMobileCapabilityType.TAP_WITH_SHORT_PRESS_DURATION + "$",
            "^" + IOSMobileCapabilityType.SCALE_FACTOR + "$",
            "^" + IOSMobileCapabilityType.WDA_LOCAL_PORT + "$",
            "^" + IOSMobileCapabilityType.SHOW_XCODE_LOG + "$",
            "^" + IOSMobileCapabilityType.IOS_INSTALL_PAUSE + "$",
            "^" + IOSMobileCapabilityType.XCODE_CONFIG_FILE + "$",
            "^" + IOSMobileCapabilityType.KEYCHAIN_PASSWORD + "$",
            "^" + IOSMobileCapabilityType.USE_PREBUILT_WDA + "$",
            "^" + IOSMobileCapabilityType.PREVENT_WDAATTACHMENTS + "$",
            "^" + IOSMobileCapabilityType.WEB_DRIVER_AGENT_URL + "$",
            "^" + IOSMobileCapabilityType.KEYCHAIN_PATH + "$",
            "^" + IOSMobileCapabilityType.USE_NEW_WDA + "$",
            "^" + IOSMobileCapabilityType.WDA_LAUNCH_TIMEOUT + "$",
            "^" + IOSMobileCapabilityType.WDA_CONNECTION_TIMEOUT + "$",
            "^" + IOSMobileCapabilityType.XCODE_ORG_ID + "$",
            "^" + IOSMobileCapabilityType.XCODE_SIGNING_ID + "$",
            "^" + IOSMobileCapabilityType.UPDATE_WDA_BUNDLEID + "$",
            "^" + IOSMobileCapabilityType.RESET_ON_SESSION_START_ONLY + "$",
            "^" + IOSMobileCapabilityType.COMMAND_TIMEOUTS + "$",
            "^" + IOSMobileCapabilityType.WDA_STARTUP_RETRIES + "$",
            "^" + IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL + "$",
            "^" + IOSMobileCapabilityType.CONNECT_HARDWARE_KEYBOARD + "$",
            "^" + IOSMobileCapabilityType.MAX_TYPING_FREQUENCY + "$",
            "^" + IOSMobileCapabilityType.SIMPLE_ISVISIBLE_CHECK + "$",
            "^" + IOSMobileCapabilityType.USE_CARTHAGE_SSL + "$",
            "^" + IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER + "$",
            "^" + IOSMobileCapabilityType.START_IWDP + "$",
            "^" + IOSMobileCapabilityType.ALLOW_TOUCHID_ENROLL + "$",
            // AndroidMobileCapabilityType
            "^" + AndroidMobileCapabilityType.APP_ACTIVITY + "$",
            "^" + AndroidMobileCapabilityType.APP_PACKAGE + "$",
            "^" + AndroidMobileCapabilityType.APP_WAIT_ACTIVITY + "$",
            "^" + AndroidMobileCapabilityType.APP_WAIT_PACKAGE + "$",
            "^" + AndroidMobileCapabilityType.APP_WAIT_DURATION + "$",
            "^" + AndroidMobileCapabilityType.DEVICE_READY_TIMEOUT + "$",
            "^" + AndroidMobileCapabilityType.ALLOW_TEST_PACKAGES + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_COVERAGE + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_COVERAGE_END_INTENT + "$",
            "^" + AndroidMobileCapabilityType.ENABLE_PERFORMANCE_LOGGING + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_DEVICE_READY_TIMEOUT + "$",
            "^" + AndroidMobileCapabilityType.ADB_PORT + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_DEVICE_SOCKET + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_INSTALL_TIMEOUT + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_INSTALL_PATH + "$",
            "^" + AndroidMobileCapabilityType.AVD + "$",
            "^" + AndroidMobileCapabilityType.AVD_LAUNCH_TIMEOUT + "$",
            "^" + AndroidMobileCapabilityType.AVD_READY_TIMEOUT + "$",
            "^" + AndroidMobileCapabilityType.AVD_ARGS + "$",
            "^" + AndroidMobileCapabilityType.USE_KEYSTORE + "$",
            "^" + AndroidMobileCapabilityType.KEYSTORE_PATH + "$",
            "^" + AndroidMobileCapabilityType.KEYSTORE_PASSWORD + "$",
            "^" + AndroidMobileCapabilityType.KEY_ALIAS + "$",
            "^" + AndroidMobileCapabilityType.KEY_PASSWORD + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_ARGS + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE_DIR + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_CHROME_MAPPING_FILE + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_USE_SYSTEM_EXECUTABLE + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_PORT + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_PORTS + "$",
            "^" + AndroidMobileCapabilityType.CHROMEDRIVER_DISABLE_BUILD_CHECK + "$",
            "^" + AndroidMobileCapabilityType.AUTO_WEBVIEW_TIMEOUT + "$",
            "^" + AndroidMobileCapabilityType.INTENT_ACTION + "$",
            "^" + AndroidMobileCapabilityType.INTENT_CATEGORY + "$",
            "^" + AndroidMobileCapabilityType.INTENT_FLAGS + "$",
            "^" + AndroidMobileCapabilityType.OPTIONAL_INTENT_ARGUMENTS + "$",
            "^" + AndroidMobileCapabilityType.DONT_STOP_APP_ON_RESET + "$",
            "^" + AndroidMobileCapabilityType.UNICODE_KEYBOARD + "$",
            "^" + AndroidMobileCapabilityType.RESET_KEYBOARD + "$",
            "^" + AndroidMobileCapabilityType.NO_SIGN + "$",
            "^" + AndroidMobileCapabilityType.IGNORE_UNIMPORTANT_VIEWS + "$",
            "^" + AndroidMobileCapabilityType.DISABLE_ANDROID_WATCHERS + "$",
            "^" + AndroidMobileCapabilityType.CHROME_OPTIONS + "$",
            "^" + AndroidMobileCapabilityType.RECREATE_CHROME_DRIVER_SESSIONS + "$",
            "^" + AndroidMobileCapabilityType.NATIVE_WEB_SCREENSHOT + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_SCREENSHOT_PATH + "$",
            "^" + AndroidMobileCapabilityType.NETWORK_SPEED + "$",
            "^" + AndroidMobileCapabilityType.GPS_ENABLED + "$",
            "^" + AndroidMobileCapabilityType.IS_HEADLESS + "$",
            "^" + AndroidMobileCapabilityType.ADB_EXEC_TIMEOUT + "$",
            "^" + AndroidMobileCapabilityType.LOCALE_SCRIPT + "$",
            "^" + AndroidMobileCapabilityType.SKIP_DEVICE_INITIALIZATION + "$",
            "^" + AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS + "$",
            "^" + AndroidMobileCapabilityType.ANDROID_NATURAL_ORIENTATION + "$",
            "^" + AndroidMobileCapabilityType.SYSTEM_PORT + "$",
            "^" + AndroidMobileCapabilityType.REMOTE_ADB_HOST + "$",
            "^" + AndroidMobileCapabilityType.SKIP_UNLOCK + "$",
            "^" + AndroidMobileCapabilityType.UNLOCK_TYPE + "$",
            "^" + AndroidMobileCapabilityType.UNLOCK_KEY + "$",
            "^" + AndroidMobileCapabilityType.AUTO_LAUNCH + "$",
            "^" + AndroidMobileCapabilityType.SKIP_LOGCAT_CAPTURE + "$",
            "^" + AndroidMobileCapabilityType.UNINSTALL_OTHER_PACKAGES + "$",
            "^" + AndroidMobileCapabilityType.DISABLE_WINDOW_ANIMATION + "$",
            "^" + AndroidMobileCapabilityType.BUILD_TOOLS_VERSION + "$",
            "^" + AndroidMobileCapabilityType.ENFORCE_APP_INSTALL + "$",
            "^" + AndroidMobileCapabilityType.ENSURE_WEBVIEWS_HAVE_PAGES + "$",
            "^" + AndroidMobileCapabilityType.WEBVIEW_DEVTOOLS_PORT + "$",
            "^" + AndroidMobileCapabilityType.REMOTE_APPS_CACHE_LIMIT + "$")

            .map(Pattern::compile)
            .map(Pattern::asPredicate)
            .reduce(identity -> false, Predicate::or);

    /**
     * Checks capability for compliance with w3c (whether it is contained in standard capabilities or meets certain rules)
     * 
     * @param capabilityName name of capability (ps. without any prefix, as appium:)
     * @return is capability w3c-compliance
     */
    @Override
    public boolean test(String capabilityName) {
        return COMMON_CAPABILITIES_PATTERNS.test(capabilityName);

    }
}
