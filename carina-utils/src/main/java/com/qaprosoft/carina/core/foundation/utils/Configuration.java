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
package com.qaprosoft.carina.core.foundation.utils;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.zebrunner.agent.core.registrar.CurrentTestRun;

/**
 * Configuration utility.
 * 
 * @author Aliaksei_Khursevich
 *         hursevich@gmail.com
 */
public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pattern S3_BUCKET_PATTERN = Pattern.compile("s3:\\/\\/([a-zA-Z-0-9][^\\/]*)\\/(.*)");
    private static final Pattern AZURE_CONTAINER_PATTERN = Pattern
            .compile("\\/\\/([a-z0-9]{3,24})\\.blob.core.windows.net\\/(?:(\\$root|(?:[a-z0-9](?!.*--)[a-z0-9-]{1,61}[a-z0-9]))\\/)?(.{1,1024})");
    // appcenter://appName/platformName/buildType/version
    private static final Pattern APPCENTER_PATTERN = Pattern.compile(
            "appcenter:\\/\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)\\/([a-zA-Z-0-9][^\\/]*)");

    private static IEnvArgResolver envArgResolver = new DefaultEnvArgResolver();


    /**
     * All available configuration parameter keys along with default values.
     */
    public enum Parameter {
        URL("url"),

        ENV("env"),

        BROWSER("browser"),

        BROWSER_LANGUAGE("browser_language"),

        SELENIUM_URL("selenium_url"),

        DRIVER_EVENT_LISTENERS("driver_event_listeners"),

        MAX_DRIVER_COUNT("max_driver_count"),
        
        FORCIBLY_DISABLE_DRIVER_QUIT("forcibly_disable_driver_quit"),

        CUSTOM_CAPABILITIES("custom_capabilities"),
        
        CHROME_ARGS("chrome_args"),
        
        CHROME_EXPERIMENTAL_OPTS("chrome_experimental_opts"),
        
        CHROME_MOBILE_EMULATION_OPTS("chrome_mobile_emulation_opts"),
        
        CHROME_CLOSURE("chrome_closure"),
        
        FIREFOX_ARGS("firefox_args"),
        
        FIREFOX_PREFERENCES("firefox_preferences"),

        HEADLESS("headless"),

        APP_VERSION("app_version"),

        PROXY_HOST("proxy_host"),

        PROXY_PORT("proxy_port"),

        PROXY_PROTOCOLS("proxy_protocols"),
        
        NO_PROXY("no_proxy"),

        BROWSERMOB_PROXY("browsermob_proxy"),

        BROWSERMOB_PORT("browsermob_port"),

        BROWSERMOB_PORTS_RANGE("browsermob_ports_range"),

        BROWSERMOB_MITM("browsermob_disabled_mitm"),

        PROXY_SET_TO_SYSTEM("proxy_set_to_system"),

        REPORT_URL("report_url"),

        AUTO_SCREENSHOT("auto_screenshot"),

        ERROR_SCREENSHOT("error_screenshot"),

        ALLOW_FULLSIZE_SCREENSHOT("allow_fullsize_screenshot"),

        EXPLICIT_TIMEOUT("explicit_timeout"),

        AUTO_DOWNLOAD("auto_download"),

        AUTO_DOWNLOAD_APPS("auto_download_apps"),

        CUSTOM_ARTIFACTS_FOLDER("custom_artifacts_folder"),

        RETRY_INTERVAL("retry_interval"),

        PROJECT_REPORT_DIRECTORY("project_report_directory"),

        MAX_SCREENSHOOT_HISTORY("max_screen_history"),

        BIG_SCREEN_WIDTH("big_screen_width"),

        BIG_SCREEN_HEIGHT("big_screen_height"),

        INIT_RETRY_COUNT("init_retry_count"),

        INIT_RETRY_INTERVAL("init_retry_interval"),

        RETRY_COUNT("retry_count"),

        LOCALE("locale"),

        THREAD_COUNT("thread_count"),

        DATA_PROVIDER_THREAD_COUNT("data_provider_thread_count"),

        CORE_LOG_LEVEL("core_log_level"),

        LOG_ALL_JSON("log_all_json"),

        DATE_FORMAT("date_format"),

        TIME_FORMAT("time_format"),

        CRYPTO_KEY_PATH("crypto_key_path"),

        SUITE_NAME("suite_name"),

        TEST_NAMING_PATTERN("test_naming_pattern"),
        
        ELEMENT_LOADING_STRATEGY("element_loading_strategy"),
        
        PAGE_OPENING_STRATEGY("page_opening_strategy"),
        
        // Amazon
        S3_BUCKET_NAME("s3_bucket_name"),
        
        S3_REGION("s3_region"),

        ACCESS_KEY_ID("access_key_id"),

        SECRET_KEY("secret_key"),

        // Azure
        AZURE_ACCOUNT_NAME("azure_account_name"),

        AZURE_CONTAINER_NAME("azure_container_name"),

        AZURE_BLOB_URL("azure_blob_url"),

        AZURE_ACCESS_KEY_TOKEN("azure_access_key_token"),

        AZURE_LOCAL_STORAGE("azure_local_storage"),

        // AppCenter token
        APPCENTER_TOKEN("appcenter_token"),

        // For localization parser
        LOCALIZATION_ENCODING("localization_encoding"),

        LOCALIZATION_TESTING("localization_testing"),

        // TLS
        TLS_KEYSECURE_LOCATION("tls_keysecure_location"),

        UNINSTALL_RELATED_APPS("uninstall_related_apps"),

        // For Device default timezone and language
        DEFAULT_DEVICE_TIMEZONE("default_device_timezone"),

        DEFAULT_DEVICE_TIME_FORMAT("default_device_time_format"),

        DEFAULT_DEVICE_LANGUAGE("default_device_language"),

        // Ignore SSL
        IGNORE_SSL("ignore_ssl"),

        // Test Execution Filter rules
        TEST_RUN_RULES("test_run_rules"),
        
        // Test Rail
        TESTRAIL_ENABLED("testrail_enabled"),
        
        INCLUDE_ALL("include_all"),
        
        MILESTONE("milestone"),
        
        RUN_NAME("run_name"),
        
        ASSIGNEE("assignee"),
        
        // sha1
        GIT_HASH("git_hash");
        
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
        String value = get(param).trim();
        if (value == null || value.equalsIgnoreCase(SpecialKeywords.NULL)) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    public static String asString() {
        StringBuilder asString = new StringBuilder();
        asString.append("\n============= Test configuration =============\n");
        for (Parameter param : Parameter.values()) {
            //#1451 hide WARN! Value not resolved by key: azure_container_name
            if (Parameter.AZURE_BLOB_URL.equals(param) && Configuration.get(param).toLowerCase().contains(SpecialKeywords.NULL.toLowerCase())) {
                // do nothing
                continue;
            }
            if (!Parameter.CRYPTO_KEY_PATH.equals(param) && !Configuration.get(param).isEmpty()) {
                asString.append(String.format("%s=%s%n", param.getKey(), Configuration.get(param)));
            }
        }

        // write into the log extra information about selenium_url together with capabilities
        asString.append(String.format("%s=%s%n", "selenium_url", getSeleniumUrl()));
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

    public static boolean isNull(Parameter param) {
        // null is never returned from configuration now so compare with empty string
        return get(param).isEmpty();
    }

    /**
     * Get capabilities.adbExecTimeout from configuration properties.
     * if it is missing return the default value
     * @return int capabilities.adbExecTimeout
     */
    public static int getAdbExecTimeout() {
        // default "capabilities.adbExecTimeout=value" should be used to determine current platform
        int adbExecTimeout = SpecialKeywords.DEFAULT_ADB_EXEC_TIMEOUT;

        // redefine adb exec timeout if capabilities.AdbExecTimeout is available
        if (!R.CONFIG.get(SpecialKeywords.ADB_EXEC_TIMEOUT).isEmpty()) {
            adbExecTimeout = R.CONFIG.getInt(SpecialKeywords.ADB_EXEC_TIMEOUT);
        }

        return adbExecTimeout;
    }

    /**
     * Get platform name from configuration properties.
     * @return String platform name
     */
    public static String getPlatform() {
        return getPlatform(new DesiredCapabilities());
    }

    /**
     * Get platform name from configuration properties or DesiredCapabilities.
     * @param caps
     *            DesiredCapabilities
     * @return String platform name
     */
    public static String getPlatform(DesiredCapabilities caps) {
        // any platform by default
        String platform = "*";
        
        LOGGER.debug("platform1: " + platform);
        // redefine platform if os caps is available
        if (!R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_NAME).isEmpty()) {
            platform = R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_NAME);
        }
        LOGGER.debug("platform2: " + platform);

        // redefine platform if platformName caps is available
        if (!R.CONFIG.get(SpecialKeywords.PLATFORM_NAME).isEmpty()) {
            platform = R.CONFIG.get(SpecialKeywords.PLATFORM_NAME);
        }
        LOGGER.debug("platform3: " + platform);

        // do not read "os" or "platformName" from caps as Saucelabs return LINUX instead of ANDROID
//        if (caps != null && caps.getCapability("os") != null) {
//            platform = caps.getCapability("os").toString();
//        }   
        
//        if (caps != null && caps.getCapability("platformName") != null) {
//            platform = caps.getCapability("platformName").toString();
//        }
        LOGGER.debug("platform4: " + platform);
        
        return platform;
    }
    
    public static String getPlatformVersion() {
        return getPlatformVersion(new DesiredCapabilities());
    }
    
    public static String getPlatformVersion(DesiredCapabilities caps) {
        // default "os_version=value" should be used to determine current platform
        String platformVersion = "";

        // redefine platform if os_version caps is available
        if (!R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_VERSION).isEmpty()) {
            platformVersion = R.CONFIG.get(SpecialKeywords.BROWSERSTACK_PLATFORM_VERSION);
        }
        
        // redefine platform if platformVersion caps is available
        if (!R.CONFIG.get(SpecialKeywords.PLATFORM_VERSION).isEmpty()) {
            platformVersion = R.CONFIG.get(SpecialKeywords.PLATFORM_VERSION);
        }
        
        if (caps != null && caps.getCapability("os_version") != null) {
            platformVersion = caps.getCapability("os_version").toString();
        }           
        
        if (caps != null && caps.getCapability("platformVersion") != null) {
            platformVersion = caps.getCapability("platformVersion").toString();
        }        
        
        return platformVersion;
    }

    public static String getBrowser() {
        String browser = "";
        if (!Configuration.get(Parameter.BROWSER).isEmpty()) {
            // default "browser=value" should be used to determine current browser
            browser = Configuration.get(Parameter.BROWSER);
        }

        // redefine browser if capabilities.browserName is available
        if (!R.CONFIG.get("capabilities.browserName").isEmpty() && !"null".equalsIgnoreCase(R.CONFIG.get("capabilities.browserName"))) {
            browser = R.CONFIG.get("capabilities.browserName");
        }
        return browser;
    }
    
    public static String getBrowserVersion() {
        String browserVersion = "";

        // redefine browserVersion if capabilities.browserVersion is available
        if (!R.CONFIG.get("capabilities.browserVersion").isEmpty()  && !"null".equalsIgnoreCase(R.CONFIG.get("capabilities.browserVersion"))) {
            browserVersion = R.CONFIG.get("capabilities.browserVersion");
        }
        
        return browserVersion;
    }

    public static String getDriverType() {

        String platform = getPlatform();
        if (platform.equalsIgnoreCase(SpecialKeywords.ANDROID) || platform.equalsIgnoreCase(SpecialKeywords.IOS) || platform.equalsIgnoreCase(SpecialKeywords.TVOS)) {
            return SpecialKeywords.MOBILE;
        }
        
        if (SpecialKeywords.WINDOWS.equalsIgnoreCase(platform)) {
            return SpecialKeywords.WINDOWS;
        }

        return SpecialKeywords.DESKTOP;
    }

    public static String getDriverType(DesiredCapabilities capabilities) {
        if (capabilities == null) {
            // calculate driver type based on config.properties arguments
            return getDriverType();
        }

        String platform = "";
        if (capabilities.getCapability("platformName") != null) {
            platform = capabilities.getCapability("platformName").toString();
        }

        if (SpecialKeywords.ANDROID.equalsIgnoreCase(platform) || SpecialKeywords.IOS.equalsIgnoreCase(platform) || SpecialKeywords.TVOS.equalsIgnoreCase(platform)) {
            return SpecialKeywords.MOBILE;
        }
        
        if (SpecialKeywords.WINDOWS.equalsIgnoreCase(platform)) {
            return SpecialKeywords.WINDOWS;
        }

        // handle use-case when we provide only uuid object among desired capabilities
        if (capabilities.getCapability("udid") != null) {
            LOGGER.debug("Detected MOBILE driver_type by uuid inside capabilities");
            return SpecialKeywords.MOBILE;
        }

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
        String updatedAppPath = updateAppPath(mobileApp);
        R.CONFIG.put(SpecialKeywords.CAPABILITIES + ".app", updatedAppPath);
        LOGGER.info("Updated mobile app: {}", updatedAppPath);
    }

    public static Object getCapability(String name) {
        return R.CONFIG.get("capabilities." + name);
    }
    
    /**
     * Register APP_VERSION number in CONFIG space and as Zebrunner Reporting build number if not empty.
     *
     * @param build String
     */
    public static void setBuild(String build) {
        R.CONFIG.put(Parameter.APP_VERSION.getKey(), build);
        if (!build.isEmpty()) {
            LOGGER.debug("build: " + build);
            CurrentTestRun.setBuild(build);
        }
    }

    public static String getSeleniumUrl() {
        return Configuration.get(Parameter.SELENIUM_URL);
    }

    public static int getThreadCount() {
        return Configuration.getInt(Parameter.THREAD_COUNT);
    }

    public static int getDataProviderThreadCount() {
        return Configuration.getInt(Parameter.DATA_PROVIDER_THREAD_COUNT);
    }

    private static String updateAppPath(String mobileAppPath) {

        try {
            Matcher matcher = S3_BUCKET_PATTERN.matcher(mobileAppPath);
            LOGGER.debug("Analyzing if mobile app is located on S3...");
            if (matcher.find()) {
                mobileAppPath = updateS3AppPath(mobileAppPath);
            }

            matcher = AZURE_CONTAINER_PATTERN.matcher(mobileAppPath);
            LOGGER.debug("Analyzing if mobile app is located on Azure...");
            if (matcher.find()) {
                mobileAppPath = updateAzureAppPath(mobileAppPath);
            }

            matcher = APPCENTER_PATTERN.matcher(mobileAppPath);
            LOGGER.debug("Analyzing if mobile_app is located on AppCenter...");
            if (matcher.find()) {
                mobileAppPath = updateAppCenterAppPath(mobileAppPath);
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find class using reflection: " + e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot find method using reflection: " + e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access using reflection: " + e);
        }

        return mobileAppPath;
    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in Hockey App.
     */
    private static String updateAppCenterAppPath(String mobileAppPath)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Matcher matcher = APPCENTER_PATTERN.matcher(mobileAppPath);
        if (matcher.find()) {
            LOGGER.info("app artifact is located on AppCenter...");
            String appName = matcher.group(1);
            String platformName = matcher.group(2);
            String buildType = matcher.group(3);
            String version = matcher.group(4);

            // TODO: test if generated appcenter download url is valid
            Object instance = Class.forName("com.qaprosoft.appcenter.AppCenterManager")
                    .getDeclaredMethod("getInstance")
                    .invoke(null);
            mobileAppPath = (String) instance.getClass()
                    .getMethod("getDownloadUrl", String.class, String.class, String.class, String.class)
                    .invoke(instance, appName, platformName, buildType, version);

        } else {
            LOGGER.error("Unable to parse '{}' path using AppCenter pattern", mobileAppPath);
        }
        return mobileAppPath;
    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in s3 bucket.
     */
    private static String updateS3AppPath(String mobileAppPath)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // get app path to be sure that we need(do not need) to download app
        // from s3 bucket
        Matcher matcher = S3_BUCKET_PATTERN.matcher(mobileAppPath);
        if (matcher.find()) {
            LOGGER.info("app artifact is located on s3...");
            String bucketName = matcher.group(1);
            String key = matcher.group(2);
            Pattern pattern = Pattern.compile(key);

            Object amazonS3Manager = Class.forName("com.qaprosoft.amazon.AmazonS3Manager")
                    .getDeclaredMethod("getInstance")
                    .invoke(null);

            // analyze if we have any pattern inside mobile_app to make extra
            // search in AWS
            int position = key.indexOf(".*");
            if (position > 0) {
                // /android/develop/dfgdfg.*/Mapmyrun.apk
                int slashPosition = key.substring(0, position).lastIndexOf("/");
                if (slashPosition > 0) {
                    key = key.substring(0, slashPosition);

                    Object lastBuild = amazonS3Manager.getClass()
                            .getDeclaredMethod("getLatestBuildArtifact", String.class, String.class, Pattern.class)
                            .invoke(amazonS3Manager, bucketName, key, pattern);

                    key = (String) lastBuild.getClass().getDeclaredMethod("getKey").invoke(lastBuild);
                }

            } else {
                Object s3Object = amazonS3Manager.getClass()
                        .getDeclaredMethod("get", String.class, String.class)
                        .invoke(amazonS3Manager, bucketName, key);

                key = (String) s3Object.getClass().getDeclaredMethod("getKey").invoke(s3Object);
            }
            LOGGER.info("next s3 app key will be used: " + key);

            // generate presign url explicitly to register link as run artifact
            long hours = 72L * 1000 * 60 * 60; // generate presigned url for nearest 3 days
            mobileAppPath = amazonS3Manager.getClass()
                    .getDeclaredMethod("generatePreSignUrl", String.class, String.class, long.class)
                    .invoke(amazonS3Manager, bucketName, key, hours)
                    .toString();
        } else {
            LOGGER.error("Unable to parse '{}' path using S3 pattern", mobileAppPath);
        }

        return mobileAppPath;
    }

    /**
     * Method to update MOBILE_APP path in case if apk is located in Azure storage.
     */
    private static String updateAzureAppPath(String mobileAppPath)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Matcher matcher = AZURE_CONTAINER_PATTERN.matcher(mobileAppPath);
        if (matcher.find()) {
            LOGGER.info("app artifact is located on Azure...");
            String accountName = matcher.group(1);
            String containerName = matcher.group(2) == null ? "$root" : matcher.group(2);
            String remoteFilePath = matcher.group(3);

            LOGGER.info(
                    "Account: " + accountName + "\n" +
                            "Container: " + containerName + "\n" +
                            "RemotePath: " + remoteFilePath + "\n");

            R.CONFIG.put(Parameter.AZURE_ACCOUNT_NAME.getKey(), accountName);
            Object azureManager = Class.forName("com.qaprosoft.azure.AzureManager").getDeclaredMethod("getInstance")
                    .invoke(null);
            Object blobProperties = azureManager.getClass()
                    .getDeclaredMethod("get", String.class, String.class)
                    .invoke(azureManager, containerName, remoteFilePath);

            String azureLocalStorage = Configuration.get(Parameter.AZURE_LOCAL_STORAGE);
            String localFilePath = azureLocalStorage + File.separator + StringUtils.substringAfterLast(remoteFilePath, "/");

            File file = new File(localFilePath);

            try {
                byte[] contentMd5 = (byte[]) blobProperties.getClass()
                        .getDeclaredMethod("getContentMd5")
                        .invoke(blobProperties);
                // verify requested artifact by checking the checksum
                if (file.exists() && FileManager.getFileChecksum(FileManager.Checksum.MD5, file)
                        .equals(Base64.encodeBase64String(contentMd5))) {
                    LOGGER.info("build artifact with the same checksum already downloaded: " + file.getAbsolutePath());
                } else {
                    LOGGER.info("Following data was extracted: container: {}, remotePath: {}, local file: {}",
                            containerName, remoteFilePath, file.getAbsolutePath());

                    azureManager.getClass().getDeclaredMethod("download", String.class, String.class, File.class)
                            .invoke(azureManager, containerName, remoteFilePath, file);
                }

            } catch (Exception exception) {
                LOGGER.error("Azure app path update exception detected!", exception);
            }

            mobileAppPath = file.getAbsolutePath();

            // try to redefine app_version if it's value is latest or empty
            String appVersion = Configuration.get(Parameter.APP_VERSION);
            if (appVersion.equals("latest") || appVersion.isEmpty()) {
                Configuration.setBuild(file.getName());
            }
        } else {
            LOGGER.error("Unable to parse '{}' path using Azure pattern", mobileAppPath);
        }

        return mobileAppPath;
    }
}
