package com.qaprosoft.carina.core.foundation.utils.android;

import static com.qaprosoft.carina.core.foundation.webdriver.DriverPool.getDriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.android.DeviceTimeZone.TimeFormat;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.CmdLine;
import com.qaprosoft.carina.core.foundation.utils.factory.DeviceType;
import com.qaprosoft.carina.core.foundation.utils.mobile.notifications.android.Notification;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.fakegps.FakeGpsPage;
import com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.notifications.NotificationPage;
import com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.settings.DateTimeSettingsPage;
import com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.tzchanger.TZChangerPage;

import io.appium.java_client.android.AndroidDriver;

public class AndroidService {

    private static final Logger LOGGER = Logger.getLogger(AndroidService.class);

    protected static final int INIT_TIMEOUT = 20;

    private final Pattern NOTIFICATION_PATTERN = Pattern.compile(".* NotificationRecord.*pkg=(.*) user");

    private final Pattern NOTIFICATION_TEXT_PATTERN = Pattern.compile(".*tickerText=(.*)");

    private final String TZ_CHANGE_APP_PATH = "app/TimeZone_Changer.apk";
    private final String TZ_CHANGE_APP_ACTIVITY = "com.futurek.android.tzc/com.futurek.android.tzc.MainActivity";
    private final String TZ_CHANGE_APP_PACKAGE = "com.futurek.android.tzc";

    private final String LANGUAGE_CHANGE_APP_PATH = "app/ADB_Change_Language.apk";

    private final String FAKE_GPS_APP_PATH = "app/FakeGPSLocation.apk";
    private final String FAKE_GPS_APP_ACTIVITY = "com.lexa.fakegps/com.lexa.fakegps.ui.Main";
    private final String FAKE_GPS_APP_PACKAGE = "com.lexa.fakegps";

    private String[] baseInitCmd;

    private AdbExecutor executor;

    public enum ChangeTimeZoneWorkflow {
        ADB(1), // 0b001
        SETTINGS(2), // 0b010
        APK(4), // 0b100
        ALL(7); // 0b111
        private int workflow;

        ChangeTimeZoneWorkflow(int workflow) {
            this.workflow = workflow;
        }

        public int getWorkflow() {
            return workflow;
        }

        public boolean isSupported(ChangeTimeZoneWorkflow workflow) {
            return (this.workflow & workflow.getWorkflow()) > 0;
        }
    }

    private static AndroidService instance;

    private AndroidService() {
        executor = new AdbExecutor();
        baseInitCmd = executor.getDefaultCmd();
    }

    static {
        try {
            instance = new AndroidService();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating singleton AndroidService!");
        }
    }

    public static AndroidService getInstance() {
        return instance;
    }

    // Common methods

    /**
     * executeAbdCommand
     *
     * @param command String
     * @return String command output in one line
     */
    public String executeAdbCommand(String command) {
        String deviceName = DevicePool.getDevice().getAdbName();
        if (!deviceName.isEmpty()) {
            // add remoteURL/udid reference
            command = "-s " + deviceName + " " + command;
        } else {
        	LOGGER.warn("nullDevice detected fot current thread!");
        }

        String result = "";
        LOGGER.info("Command: " + command);
        String[] listOfCommands = command.split(" ");

        String[] execCmd = CmdLine.insertCommandsAfter(baseInitCmd, listOfCommands);

        try {
            LOGGER.info("Try to execute following cmd: " + CmdLine.arrayToString(execCmd));
            List<String> execOutput = executor.execute(execCmd);
            LOGGER.info("Output after execution ADB command: " + execOutput);

            result = execOutput.toString().replaceAll("\\[|\\]", "").replaceAll(", ", " ").trim();

            LOGGER.info("Returning Output: " + result);
        } catch (Exception e) {
            LOGGER.error(e);
        }

        return result;
    }

    /**
     * press Home button to open home screen
     */

    public void gotoAndroidHome() {
        executeAdbCommand("shell input keyevent 3");
    }

    /**
     * openApp
     *
     * @param pkg String
     * @param activity String
     */
    public void openApp(String pkg, String activity) {
        openApp(pkg.trim() + "/" + activity.trim());
    }

    /**
     * openApp
     *
     * @param app String
     */
    public void openApp(String app) {
        executeAdbCommand("shell am start -n " + app);
    }

    /**
     * clear Apk Cache
     *
     * @param appPackageName for example:
     * com.bamnetworks.mobile.android.gameday.atbat
     * @return boolean
     */
    public boolean clearApkCache(String appPackageName) {
        // Later can be used:
        /*
         * String packageName = executor.getApkPackageName(String apkFile);
         * executor.clearAppData(Device device, String appPackage);
         */
        String result = executeAdbCommand("shell pm clear " + appPackageName);

        if (result.contains("Success")) {
            LOGGER.info("Cache was cleared correctly");
            return true;
        } else {
            LOGGER.error("Cache was not cleared. May be application does not exist on this device.");
            return false;
        }
    }

    /**
     * checkCurrentDeviceFocus - return actual device focused apk and compare
     * with expected.
     *
     * @param apk String
     * @return boolean
     */
    public boolean checkCurrentDeviceFocus(String apk) {
        String res = getCurrentDeviceFocus();
        if (res.contains(apk)) {
            LOGGER.info("Actual device focus is as expected and contains package or activity: '" + apk + "'.");
            return true;
        } else {
            LOGGER.error("Not expected apk '" + apk + "' is in focus. Actual result is: " + res);
            return false;
        }
    }

    /**
     * getCurrentDeviceFocus - get actual device apk in focus
     *
     * @return String
     */
    public String getCurrentDeviceFocus() {
        String result = executeAdbCommand("shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'");
        return result;
    }

    /**
     * install android Apk by path to apk file.
     *
     * @param apkPath String
     */
    public void installApk(final String apkPath) {
        installApk(apkPath, false);
    }

    /**
     * install android Apk by path to apk or by name in classpath.
     *
     * @param apkPath String
     * @param inClasspath boolean
     */
    public void installApk(final String apkPath, boolean inClasspath) {

        String filePath = apkPath;
        if (inClasspath) {
            URL baseResource = ClassLoader.getSystemResource(apkPath);
            if (baseResource == null) {
                throw new RuntimeException("Unable to get resource from classpath: " + apkPath);
            } else {
                LOGGER.debug("Resource was found: " + baseResource.getPath());
            }

            String fileName = FilenameUtils.getBaseName(baseResource.getPath()) + "." + FilenameUtils.getExtension(baseResource.getPath());
            // make temporary copy of resource in artifacts folder
            filePath = ReportContext.getArtifactsFolder().getAbsolutePath() + File.separator + fileName;

            File file = new File(filePath);
            if (!file.exists()) {
                InputStream link = (ClassLoader.getSystemResourceAsStream(apkPath));
                try {
                    Files.copy(link, file.getAbsoluteFile().toPath());
                } catch (IOException e) {
                    LOGGER.error("Unable to extract resource from ClassLoader!", e);
                }
            }
        }

        executeAdbCommand("install " + filePath);
    }

    /**
     * Open Development Settings on device
     */
    public void openDeveloperOptions() {
        executeAdbCommand("shell am start -n com.android.settings/.DevelopmentSettings");
    }

    // End of Common Methods

    // Notification section

    /**
     * expandStatusBar
     */
    public void expandStatusBar() {
        executeAdbCommand("shell service call statusbar 1");
    }

    /**
     * collapseStatusBar
     */
    public void collapseStatusBar() {
        executeAdbCommand("shell service call statusbar 2");
    }

    // TODO: move notifications methods into separate class if possible. Maybe
    // declare notification service instance inside AndroidService

    /**
     * getNotifications
     *
     * @return List of Notification
     */
    public List<Notification> getNotifications() {
        return getNotifications(true);
    }

    /**
     * getNotifications
     *
     * @param withLogger boolean
     * @return List of Notification
     */
    public List<Notification> getNotifications(boolean withLogger) {
        String[] getNotificationsCmd = null;
        String deviceName = DevicePool.getDevice().getAdbName();
        if (!deviceName.isEmpty()) {
            getNotificationsCmd = CmdLine.insertCommandsAfter(baseInitCmd, "-s", deviceName, "shell", "dumpsys", "notification");
        } else {
            getNotificationsCmd = CmdLine.insertCommandsAfter(baseInitCmd, "shell", "dumpsys", "notification");
        }

        LOGGER.info("getNotifications cmd was built: " + CmdLine.arrayToString(getNotificationsCmd));

        // TODO: migrate to executeAbdCommand later
        List<Notification> resultList = new ArrayList<Notification>();
        List<String> notificationsOutput = executor.execute(getNotificationsCmd);
        Notification notification = new Notification();
        for (String output : notificationsOutput) {
            boolean found = false;

            Matcher matcher = NOTIFICATION_PATTERN.matcher(output);
            while (matcher.find()) {
                notification.setNotificationPkg(matcher.group(1));
                if (withLogger)
                    LOGGER.info(matcher.group(1));
            }
            Matcher matcher2 = NOTIFICATION_TEXT_PATTERN.matcher(output);
            while (matcher2.find()) {
                notification.setNotificationText(matcher2.group(1));
                if (withLogger)
                    LOGGER.info(matcher2.group(1));
                found = true;
            }
            if (found) {
                resultList.add(notification);
                if (withLogger)
                    LOGGER.info(notification);
                notification = new Notification();
                found = false;
            }

        }
        if (withLogger)
            LOGGER.info("Found: " + resultList.size() + " notifications.");
        return resultList;
    }

    /**
     * notificationsCount
     *
     * @return notificationsCount
     */
    public int notificationsCount() {
        List<Notification> resultList = getNotifications(false);
        LOGGER.info("Found: " + resultList.size() + " notifications.");
        return resultList.size();
    }

    /**
     * isNotificationWithTextExist
     *
     * @param text String
     * @return boolean
     */
    public boolean isNotificationWithTextExist(String text) {
        List<Notification> resultList = getNotifications(false);

        for (Notification notify : resultList) {
            if (notify.getNotificationText().contains(text)) {
                LOGGER.info("Found '" + text + "' in notification '" + notify.getNotificationText() + "'.");
                return true;
            }

        }
        return false;
    }

    /**
     * waitUntilNewNotificationAppear
     *
     * @param text String
     * @param timeout long
     * @return boolean
     */
    public boolean waitUntilNewNotificationAppear(String text, long timeout) {
        // boolean found = false;
        int base = notificationsCount();
        int time = 0;
        boolean foundText = isNotificationWithTextExist(text);

        int actual = notificationsCount();
        while (actual <= base && ++time < timeout && !foundText) {
            LOGGER.info("Wait for notification. Second: " + time + ". Actual number:" + actual);
            pause(1);
            actual = notificationsCount();
            foundText = isNotificationWithTextExist(text);
        }

        return (foundText);
    }

    /**
     * isNotificationPkgExist
     *
     * @param text package text
     * @return boolean
     */
    public boolean isNotificationPkgExist(String text) {
        List<Notification> resultList = getNotifications(false);

        for (Notification notify : resultList) {
            if (notify.getNotificationPkg().contains(text)) {
                LOGGER.info("Found '" + text + "' in notification packages '" + notify.getNotificationPkg() + "' with text '"
                        + notify.getNotificationText() + "'.");
                return true;
            }

        }
        return false;
    }

    /**
     * waitUntilNewNotificationPackageAppear
     *
     * @param pkg String
     * @param timeout long
     * @return boolean
     */
    public boolean waitUntilNewNotificationPackageAppear(String pkg, long timeout) {
        // boolean found = false;
        int base = notificationsCount();
        int time = 0;
        boolean foundText = isNotificationPkgExist(pkg);

        int actual = notificationsCount();
        while (actual <= base && ++time < timeout && !foundText) {
            LOGGER.info("Wait for notification. Second: " + time + ". Actual number:" + actual);
            pause(1);
            actual = notificationsCount();
            foundText = isNotificationPkgExist(pkg);
        }

        return (foundText);
    }

    /**
     * find Expected Notification with partial text
     *
     * @param expectedTitle String
     * @param expectedText String
     * @return boolean
     */
    public boolean findExpectedNotification(String expectedTitle, String expectedText) {
        return findExpectedNotification(expectedTitle, expectedText, true);
    }

    /**
     * find Expected Notification
     *
     * @param expectedTitle String
     * @param expectedText String
     * @param partially boolean
     * @return boolean
     */
    @SuppressWarnings("rawtypes")
    public boolean findExpectedNotification(String expectedTitle, String expectedText, boolean partially) {
        // open notification
        try {
            ((AndroidDriver) getDriver()).openNotifications();
            pause(2); // wait while notifications are playing animation to
                      // appear to avoid missed taps
        } catch (Exception e) {
            LOGGER.error(e);
            LOGGER.info("Using adb to expand Status bar. ");
            expandStatusBar();

        }

        NotificationPage nativeNotificationPage = new NotificationPage(getDriver());

        LOGGER.info("Native notification page is loaded: " + nativeNotificationPage.isNativeNotificationPage());

        int itemsListSize = nativeNotificationPage.getLastItemsContentSize();

        String title, text;
        int notificationItemNum = 0;
        for (int i = 0; i <= itemsListSize; i++) {
            title = nativeNotificationPage.getItemTitle(i);
            text = nativeNotificationPage.getItemText(i);
            LOGGER.info("Notification title is: " + title);
            LOGGER.info("Notification text is: " + text);
            if (!expectedTitle.isEmpty()) {
                if (title.equals(expectedTitle)) {
                    notificationItemNum = i;
                    LOGGER.info("Found expected title '" + expectedTitle + "' in notification #" + notificationItemNum);
                    return true;
                } else if (partially) {
                    if (expectedTitle.contains(title)) {
                        notificationItemNum = i;
                        LOGGER.info(
                                "Found that expected title '" + expectedTitle + "' contains '" + title + "' in notification #" + notificationItemNum);
                        return true;
                    }
                }
            }
            if (!expectedText.isEmpty()) {
                if (text.equals(expectedText)) {
                    notificationItemNum = i;
                    LOGGER.info("Found expected text '" + expectedText + "' in notification #" + notificationItemNum);
                    return true;
                } else if (partially) {
                    if (expectedText.contains(text)) {
                        notificationItemNum = i;
                        LOGGER.info(
                                "Found that expected text '" + expectedText + "' contains '" + text + "' in notification #" + notificationItemNum);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * clearNotifications
     */
    public void clearNotifications() {
        LOGGER.info("Clear notifications");
        NotificationPage notificationPage = new NotificationPage(getDriver());
        int attempts = 3;
        boolean isStatusBarOpened;
        // three attempts will be executed to clear notifications
        for (int i = 0; i < attempts; i++) {
            collapseStatusBar();
            expandStatusBar();
            // wait until status bar will be opened
            isStatusBarOpened = notificationPage.isOpened(INIT_TIMEOUT);
            if (!isStatusBarOpened) {
                LOGGER.info(String.format("Status bar isn't opened after %d seconds. One more attempt.", (int) INIT_TIMEOUT));
                expandStatusBar();
            }
            LOGGER.debug("Page source [expand status bar]: ".concat(getDriver().getPageSource()));
            Screenshot.capture(getDriver(), "Clear notification - screenshot. Status bar should be opened. Attempt: " + i);
            try {
                notificationPage.clearNotifications();
            } catch (Exception e) {
                LOGGER.info("Exception during notification extraction.");
            }
        }
        collapseStatusBar();
    }

    /**
     * isStatusBarExpanded
     *
     * @return boolean
     */
    public boolean isStatusBarExpanded() {
        NotificationPage notificationPage = new NotificationPage(getDriver());
        return notificationPage.isStatusBarExpanded();
    }

    // End of Notification section

    // Change Device Language section

    /**
     * change Android Device Language with default parameters
     *
     * @param language String
     * @return boolean
     */
    public boolean setDeviceLanguage(String language) {
        return setDeviceLanguage(language, true, 20);
    }

    /**
     * change Android Device Language
     * <p>
     * Url: <a href=
     * "http://play.google.com/store/apps/details?id=net.sanapeli.adbchangelanguage&hl=ru&rdid=net.sanapeli.adbchangelanguage">
     * ADBChangeLanguage apk </a> Change locale (language) of your device via
     * ADB (on Android OS version 6.0, 5.0, 4.4, 4.3, 4.2 and older). No need to
     * root your device! With ADB (Android Debug Bridge) on your computer, you
     * can fast switch the device locale to see how your application UI looks on
     * different languages. Usage: - install this app - setup adb connection to
     * your device (http://developer.android.com/tools/help/adb.html) - Android
     * OS 4.2 onwards (tip: you can copy the command here and paste it to your
     * command console): adb shell pm grant net.sanapeli.adbchangelanguage
     * android.permission.CHANGE_CONFIGURATION
     * <p>
     * English: adb shell am start -n
     * net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language en Russian:
     * adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage
     * -e language ru Spanish: adb shell am start -n
     * net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language es
     *
     * @param language to set. Can be es, en, etc.
     * @param changeConfig boolean if true - update config locale and language
     * params
     * @param waitTime int wait in seconds before device refresh.
     * @return boolean
     */
    public boolean setDeviceLanguage(String language, boolean changeConfig, int waitTime) {
        boolean status = false;

        String initLanguage = language;

        String currentAndroidVersion = DevicePool.getDevice().getOsVersion();

        LOGGER.info("Do not concat language for Android. Keep: " + language);
        language=language.replace("_","-");
        LOGGER.info("Refactor language to : " + language);

        String actualDeviceLanguage = getDeviceLanguage();

        if (language.contains(actualDeviceLanguage.toLowerCase()) || actualDeviceLanguage.toLowerCase().contains(language)) {
            LOGGER.info("Device already have expected language: " + actualDeviceLanguage);
            return true;
        }

        String setLocalizationChangePermissionCmd = "shell pm grant net.sanapeli.adbchangelanguage android.permission.CHANGE_CONFIGURATION";

        String setLocalizationCmd = "shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language " + language;

        LOGGER.info("Try set localization change permission with following cmd:" + setLocalizationChangePermissionCmd);
        String expandOutput = executeAdbCommand(setLocalizationChangePermissionCmd);

        if (expandOutput.contains("Unknown package: net.sanapeli.adbchangelanguage")) {
            LOGGER.info("Looks like 'ADB Change Language apk' is not installed. Install it and try again.");
            installApk(LANGUAGE_CHANGE_APP_PATH, true);
            expandOutput = executeAdbCommand(setLocalizationChangePermissionCmd);
        }

        LOGGER.info("Output after set localization change permission using 'ADB Change Language apk': " + expandOutput);

        LOGGER.info("Try set localization to '" + language + "' with following cmd: " + setLocalizationCmd);
        String changeLocaleOutput = executeAdbCommand(setLocalizationCmd);
        LOGGER.info("Output after set localization to '" + language + "' using 'ADB Change Language apk' : " + changeLocaleOutput);

        if (waitTime > 0) {
            LOGGER.info("Wait for at least '" + waitTime + "' seconds before device refresh.");
            pause(waitTime);
        }

        if (changeConfig) {
            String loc;
            String lang;
            if (initLanguage.contains("_")) {
                lang = initLanguage.split("_")[0];
                loc = initLanguage.split("_")[1];
            } else {
                lang = initLanguage;
                loc = initLanguage;
            }
            LOGGER.info("Update config.properties locale to '" + loc + "' and language to '" + lang + "'.");
            R.CONFIG.put("locale", loc);
            R.CONFIG.put("language", lang);
        }

        actualDeviceLanguage = getDeviceLanguage();
        LOGGER.info("Actual Device Language: " + actualDeviceLanguage);
        if (language.contains(actualDeviceLanguage.toLowerCase()) || actualDeviceLanguage.toLowerCase().contains(language)) {
            status = true;
        } else {
            if (getDeviceLanguage().isEmpty()) {
                LOGGER.info("Adb return empty response without errors.");
                status = true;
            } else {
                currentAndroidVersion = DevicePool.getDevice().getOsVersion();
                LOGGER.info("currentAndroidVersion=" + currentAndroidVersion);
                if (currentAndroidVersion.contains("7.")) {
                    LOGGER.info("Adb return language command do not work on some Android 7+ devices." + " Check that there are no error.");
                    status = !getDeviceLanguage().toLowerCase().contains("error");
                }
            }
        }
        return status;
    }

    /**
     * getDeviceLanguage
     *
     * @return String
     */
    public String getDeviceLanguage() {
        String locale = executeAdbCommand("shell getprop persist.sys.language");
        if(locale.isEmpty()) {
            locale = executeAdbCommand("shell getprop persist.sys.locale");
        }
        return locale;
    }
    // End Language Change section

    // Fake GPS section

    /**
     * startFakeGPS to emulate GPS location
     *
     * @param location String - existing city (for ex. New York)
     * @return boolean return true if everything is ok.
     */
    public boolean setFakeGPSLocation(String location) {
        return setFakeGPSLocation(location, false);
    }

    /**
     * startFakeGPS to emulate GPS location
     *
     * @param location String - existing city (for ex. New York)
     * @param restartApk - if true DriverPool.restartDriver(true);
     * @return boolean return true if everything is ok.
     */
    public boolean setFakeGPSLocation(String location, boolean restartApk) {
        getDriver();
        boolean res = false;
        installApk(FAKE_GPS_APP_PATH, true);

        String activity = FAKE_GPS_APP_ACTIVITY;

        try {
            forceFakeGPSApkOpen();

            FakeGpsPage fakeGpsPage = new FakeGpsPage(getDriver());
            if (!fakeGpsPage.isOpened(1)) {
                LOGGER.error("Fake GPS application should be open but wasn't. Force opening.");
                openApp(activity);
                pause(2);
            }
            res = fakeGpsPage.locationSearch(location);
            if (res) {
                LOGGER.info("Set Fake GPS locale: " + location);
                AndroidUtils.hideKeyboard();
                fakeGpsPage.clickSetLocation();
            }
            res = true;
            if (restartApk)
                DriverPool.restartDriver(true);
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
        return res;
    }

    /**
     * stopFakeGPS stop using Fake GPS
     *
     * @return boolean
     */
    public boolean stopFakeGPS() {
        return stopFakeGPS(false);
    }

    /**
     * stopFakeGPS stop using Fake GPS
     *
     * @param restartApk - if true DriverPool.restartDriver(true);
     * @return boolean
     */
    public boolean stopFakeGPS(boolean restartApk) {
        getDriver();
        boolean res = false;
        String activity = FAKE_GPS_APP_ACTIVITY;

        try {
            forceFakeGPSApkOpen();

            FakeGpsPage fakeGpsPage = new FakeGpsPage(getDriver());
            if (!fakeGpsPage.isOpened(1)) {
                LOGGER.error("Fake GPS application should be open but wasn't. Force opening.");
                openApp(activity);
                pause(2);
            }
            LOGGER.info("STOP Fake GPS locale");
            res = fakeGpsPage.clickStopFakeGps();
            if (restartApk)
                DriverPool.restartDriver(true);
        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
        LOGGER.info("Stop Fake GPS button was clicked: " + res);
        return res;
    }

    /**
     * forceFakeGPSApkOpen
     *
     * @return boolean
     */
    private boolean forceFakeGPSApkOpen() {
        return forceApkOpen(FAKE_GPS_APP_ACTIVITY, FAKE_GPS_APP_PACKAGE, FAKE_GPS_APP_PATH);
    }

    /**
     * forceApkOpen
     *
     * @param activity String
     * @param packageName String
     * @param apkPath String
     * @return boolean
     */
    private boolean forceApkOpen(String activity, String packageName, String apkPath) {
        boolean res;

        int attemps = 3;

        boolean isApkOpened = checkCurrentDeviceFocus(packageName);
        while (!isApkOpened && attemps > 0) {
            LOGGER.info("Apk was not open. Attempt to open...");
            openApp(activity);
            pause(2);
            isApkOpened = checkCurrentDeviceFocus(packageName);
            attemps--;
        }

        if (!isApkOpened) {
            LOGGER.info("Probably APK was not installed correctly. Try to reinstall.");
            installApk(apkPath, true);
            openApp(activity);
            pause(2);
        }

        if (checkCurrentDeviceFocus(packageName)) {
            LOGGER.info("On '" + packageName + "' apk page");
            res = true;
        } else {
            LOGGER.error("Not on '" + packageName + "' page after all tries. Please check logs.");
            res = false;
        }
        return res;
    }

    // End of Fake GPS section

    // TimeZone change section

    /**
     * switchDeviceAutoTimeAndTimeZone
     *
     * @param autoSwitch boolean. If true - auto Time and TimeZone will be set
     * as On.
     */
    public void switchDeviceAutoTimeAndTimeZone(boolean autoSwitch) {
        String value = "0";
        if (autoSwitch) {
            value = "1";
        }

        executeAdbCommand("shell settings put global auto_time " + value);
        executeAdbCommand("shell settings put global auto_time_zone " + value);
    }

    /**
     * get Device Time Zone
     *
     * @return DeviceTimeZone
     */
    public DeviceTimeZone getDeviceTimeZone() {
        return getDeviceTimeZone("");
    }

    /**
     * get Device Time Zone. Set default TimeZone
     *
     * @param defaultTZ - default string.
     * @return DeviceTimeZone
     */
    public DeviceTimeZone getDeviceTimeZone(String defaultTZ) {

        getDriver(); // start driver in before class to assign it for particular
                     // thread
        DeviceTimeZone dt = new DeviceTimeZone();

        String value = executeAdbCommand("shell settings get global auto_time");
        if (value.contains("0")) {
            dt.setAutoTime(false);
        } else {
            dt.setAutoTime(true);
        }

        value = executeAdbCommand("shell settings get global auto_time_zone");
        if (value.contains("0")) {
            dt.setAutoTimezone(false);
        } else {
            dt.setAutoTimezone(true);
        }

        value = executeAdbCommand("shell settings get system time_12_24");
        if (value.contains("12")) {
            dt.setTimeFormat(TimeFormat.FORMAT_12);
        } else {
            dt.setTimeFormat(TimeFormat.FORMAT_24);
        }

        if (defaultTZ.isEmpty()) {
            value = executeAdbCommand("shell getprop persist.sys.timezone");
            if (!value.isEmpty()) {
                dt.setTimezone(value);
            }
        } else {
            dt.setTimezone(defaultTZ);
        }

        value = executeAdbCommand("shell date -s %mynow%");
        LOGGER.info(value);
        if (!value.isEmpty()) {
            value = convertDateInCorrectString(parseOutputDate(value));
            dt.setSetDeviceDateTime(value);
            LOGGER.info(value);
        }

        dt.setChangeDateTime(false);
        dt.setRefreshDeviceTime(true);

        LOGGER.info(dt.toString());

        return dt;
    }

    /**
     * get Device Actual TimeZone
     *
     * @return String
     */
    public String getDeviceActualTimeZone() {
        String value = executeAdbCommand("shell getprop persist.sys.timezone");
        if (!value.isEmpty()) {
            LOGGER.info(value);
        }
        return value;
    }

    // Start of TimeZone Setting section

    /**
     * set Device TimeZone by using Apk
     *
     * @param timeZone String required timeZone in Android standard format
     * (Europe/London)
     * @param timeFormat String 12 or 24
     * @return boolean
     */
    public boolean setDeviceTimeZone(String timeZone, TimeFormat timeFormat) {
        return setDeviceTimeZone(timeZone, "", timeFormat, ChangeTimeZoneWorkflow.APK);
    }

    /**
     * set Device TimeZone using all supported workflows. By ADB, Settings and
     * Apk
     *
     * @param timeZone String required timeZone
     * @param timeFormat String 12 or 24
     * @param settingsTZ TimeFormat
     * @return boolean
     */
    public boolean setDeviceTimeZone(String timeZone, String settingsTZ, TimeFormat timeFormat) {
        return setDeviceTimeZone(timeZone, settingsTZ, timeFormat, ChangeTimeZoneWorkflow.ALL);
    }

    /**
     * set Device TimeZone. By required workflow: ADB, Settings or APK
     *
     * @param timeZone String required timeZone
     * @param timeFormat String 12 or 24
     * @param settingsTZ TimeFormat
     * @param workflow ChangeTimeZoneWorkflow
     * @return boolean
     */
    public boolean setDeviceTimeZone(String timeZone, String settingsTZ, TimeFormat timeFormat, ChangeTimeZoneWorkflow workflow) {
        boolean changed = false;

        getDriver(); // start driver in before class to assign it for particular
                     // thread
        String actualTZ = getDeviceActualTimeZone();

        if (isRequiredTimeZone(actualTZ, timeZone)) {
            LOGGER.info("Required TimeZone is already set.");
            return true;
        }

        String currentAndroidVersion = DevicePool.getDevice().getOsVersion();
        LOGGER.info("currentAndroidVersion=" + currentAndroidVersion);
        if (currentAndroidVersion.contains("7.") || (DevicePool.getDevice().getDeviceType() == DeviceType.Type.ANDROID_TABLET)) {
            LOGGER.info("TimeZone changing for Android 7+ and tablets works only by TimeZone changer apk.");
            workflow = ChangeTimeZoneWorkflow.APK;
        }

        // Solution for ADB timezone changing.
        if (ChangeTimeZoneWorkflow.ADB.isSupported(workflow)) {
            LOGGER.info("Try to change TimeZone by ADB");
            LOGGER.info(setDeviceTimeZoneByADB(timeZone, timeFormat, ""));
            changed = applyTZChanges(ChangeTimeZoneWorkflow.ADB, timeZone);
        }

        // Solution for timezone changing by device Settings. (Tested on S7,
        // Note 3, S6, S5).
        if (!changed && ChangeTimeZoneWorkflow.SETTINGS.isSupported(workflow)) {
            LOGGER.info("Try to change TimeZone by Device Settings");
            setDeviceTimeZoneBySetting(timeZone, settingsTZ, timeFormat);
            changed = applyTZChanges(ChangeTimeZoneWorkflow.SETTINGS, timeZone);
        }

        // Solution for using TimeZone Changer apk.
        if (!changed && ChangeTimeZoneWorkflow.APK.isSupported(workflow)) {
            LOGGER.info("Try to change TimeZone by TimeZone Changer apk.");
            setDeviceTimeZoneByChangerApk(timeZone, timeFormat);
            changed = applyTZChanges(ChangeTimeZoneWorkflow.APK, timeZone);
        }
        return changed;
    }

    // End of TimeZone change sections
    
    /**
     * Open camera on device
     */    
    public void openCamera() {
        LOGGER.info("Camera will be opened");
        executeAdbCommand("shell am start -a android.media.action.IMAGE_CAPTURE");
    }
    
    /**
     * Android camera should be already opened
     */
    public void takePhoto() {
        LOGGER.info("Will take photo");
        executeAdbCommand("shell input keyevent KEYCODE_CAMERA");
    }

    // Private section

    // TimeZone Private methods

    /**
     * setDeviceTimeZoneByADB
     *
     * @param timeZone String
     * @param timeFormat TimeFormat
     * @param deviceSetDate String in format yyyyMMdd.HHmmss. Can be empty.
     * @return String
     */
    private String setDeviceTimeZoneByADB(String timeZone, TimeFormat timeFormat, String deviceSetDate) {
        boolean changeDateTime = true;
        String tzGMT = "";
        if (deviceSetDate.isEmpty()) {
            changeDateTime = false;
        }
        DeviceTimeZone dt = new DeviceTimeZone(false, false, timeFormat, timeZone, tzGMT, deviceSetDate, changeDateTime, true);
        return setDeviceTimeZoneByADB(dt);
    }

    /**
     * setDeviceTimeZoneByADB Automatic date and time = OFF (settings - date and
     * time) adb shell settings put global auto_time 0 Automatic time zone = OFF
     * (settings - date and time) adb shell settings put global auto_time_zone 0
     * <p>
     * Set Time Zone on device adb shell setprop persist.sys.timezone
     * "America/Chicago"
     * <p>
     * Check timezones: <a href=
     * "https://en.wikipedia.org/wiki/List_of_tz_database_time_zones">List_of_tz_database_time_zones</a>
     * <p>
     * Check time on device adb shell date -s %mynow%
     * <p>
     * Restart application
     *
     * @param dt DeviceTimeZone
     * @return String actual Device Date and Time
     */
    private String setDeviceTimeZoneByADB(DeviceTimeZone dt) {

        if (dt == null) {
            LOGGER.error("DeviceTimeZone is not initialised.");
            dt = new DeviceTimeZone();
        }
        LOGGER.info(dt.toString());

        String autoTime = "0";
        String autoTimeZone = "0";

        if (dt.isAutoTime()) {
            autoTime = "1";
        }
        executeAdbCommand("shell settings put global auto_time " + autoTime);

        if (dt.isAutoTimezone()) {
            autoTimeZone = "1";
        }
        executeAdbCommand("shell settings put global auto_time_zone " + autoTimeZone);

        setSystemTime(dt.getTimeFormat());

        if (!dt.getTimezone().isEmpty()) {
            executeAdbCommand("shell setprop persist.sys.timezone \"" + dt.getTimezone() + "\"");
        }

        if (dt.isRefreshDeviceTime()) {
            executeAdbCommand("shell am broadcast -a android.intent.action.TIME_SET");
        }

        if (dt.isChangeDateTime() && !dt.getSetDeviceDateTime().isEmpty()) {
            // Try to set date for device but it will not work on not rooted
            // devices
            executeAdbCommand("shell date " + dt.getSetDeviceDateTime());
        }

        String actualDT = executeAdbCommand("shell date -s %mynow%");
        LOGGER.info(actualDT);
        return actualDT;
    }

    /**
     * setDeviceTimeZoneBySetting
     *
     * @param timeZone String
     * @param settingsTZ String
     * @param timeFormat TimeFormat
     */
    private void setDeviceTimeZoneBySetting(String timeZone, String settingsTZ, TimeFormat timeFormat) {

        String actualTZ = getDeviceActualTimeZone();

        String tz = DeviceTimeZone.getTimezoneOffset(timeZone);

        if (isRequiredTimeZone(actualTZ, timeZone)) {
            LOGGER.info("Required timeZone is already set.");
            return;
        }

        try {
            openDateTimeSettingsSetupWizard(true, timeFormat);

            String res = getCurrentDeviceFocus();
            if (res.contains("settings.DateTimeSettingsSetupWizard")) {
                LOGGER.info("On settings.DateTimeSettingsSetupWizard page");
            } else {
                LOGGER.error("Not on settings.DateTimeSettingsSetupWizard page");
            }
            DateTimeSettingsPage dtSettingsPage = new DateTimeSettingsPage(getDriver());
            if (!dtSettingsPage.isOpened(3)) {
                openDateTimeSettingsSetupWizard(true, timeFormat);
            }
            if (dtSettingsPage.isOpened(3)) {
                LOGGER.info("Date Time Settings page was open.");
            } else {
                LOGGER.error("Date Time Settings page should be open.");
            }
            dtSettingsPage.openTimeZoneSetting();
            dtSettingsPage.selectTimeZone(tz, settingsTZ);
            dtSettingsPage.clickNextButton();

        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
    }

    /**
     * setDeviceTimeZoneByChangerApk
     *
     * @param timeZone String
     * @param timeFormat TimeFormat
     */
    private void setDeviceTimeZoneByChangerApk(String timeZone, TimeFormat timeFormat) {
        String actualTZ = getDeviceActualTimeZone();

        String tz = DeviceTimeZone.getTimezoneOffset(timeZone);
        LOGGER.info("Required TimeZone offset: " + tz);

        if (isRequiredTimeZone(actualTZ, timeZone)) {
            LOGGER.info("Required timeZone is already set.");
            return;
        }
        installApk(TZ_CHANGE_APP_PATH, true);
        try {
            forceTZChangingApkOpen(true, timeFormat);

            TZChangerPage tzChangerPage = new TZChangerPage(getDriver());

            if (tzChangerPage.isOpened(3)) {
                LOGGER.info("TimeZone changer main page was open.");
            } else {
                LOGGER.error("TimeZone changer main page should be open. Retry to open.");
                openTZChangingApk(true, timeFormat);
            }

            tzChangerPage.selectTimeZone(timeZone);

        } catch (Exception e) {
            LOGGER.error("Exception: ", e);
        }
    }

    private boolean applyTZChanges(ChangeTimeZoneWorkflow workflow, String expectedZone) {
        boolean res = false;
        String actualTZ = getDeviceActualTimeZone();
        if (isRequiredTimeZone(actualTZ, expectedZone)) {
            LOGGER.info("Required timeZone '" + expectedZone + "' was set by " + workflow.toString() + ". Restarting driver to apply changes.");
            DriverPool.restartDriver(true);
            res = true;
        } else {
            LOGGER.error("TimeZone was not changed by " + workflow.toString() + ". Actual TZ is: " + actualTZ);
        }
        return res;
    }

    /**
     * comparingExpectedAndActualTZ
     *
     * @param actualTZ String
     * @param expextedTZ String
     * @return boolean
     */
    private boolean isRequiredTimeZone(String actualTZ, String expextedTZ) {
        boolean res = actualTZ.equals(expextedTZ);
        if (!res) {
            String[] actTZ = actualTZ.split("/");
            String lastActTZ = actTZ[actTZ.length - 1];
            String[] timeZoneTZ = expextedTZ.split("/");
            String lastTimeZoneTZ = timeZoneTZ[timeZoneTZ.length - 1];
            LOGGER.debug("Comparing '" + lastActTZ + "' with '" + lastTimeZoneTZ + "'.");
            res = lastActTZ.equals(lastTimeZoneTZ);
        }
        return res;
    }

    /**
     * @param turnOffAuto boolean
     * @param timeFormat TimeFormat
     * @return boolean
     */
    private boolean forceTZChangingApkOpen(boolean turnOffAuto, TimeFormat timeFormat) {
        boolean res = false;

        String tzPackageName = TZ_CHANGE_APP_PACKAGE;
        int attemps = 3;

        boolean isTzOpened = checkCurrentDeviceFocus(tzPackageName);
        while (!isTzOpened && attemps > 0) {
            LOGGER.info("TimeZoneChanger apk was not open. Attempt to open...");
            openTZChangingApk(turnOffAuto, timeFormat);
            isTzOpened = checkCurrentDeviceFocus(tzPackageName);
            attemps--;
        }

        if (!isTzOpened) {
            LOGGER.info("Probably TimeZone Changer APK was not installed correctly. Try to reinstall.");
            installApk(TZ_CHANGE_APP_PATH, true);
            openTZChangingApk(turnOffAuto, timeFormat);
        }

        TZChangerPage tzChangerPage = new TZChangerPage(getDriver());
        if (!tzChangerPage.isOpened(10)) {
            openTZChangingApk(turnOffAuto, timeFormat);
        }
        if (tzChangerPage.isOpened(3)) {
            LOGGER.info("TimeZone changer main page was open.");
            res = true;
        } else {
            LOGGER.error("TimeZone changer main page should be open.");
            openTZChangingApk(turnOffAuto, timeFormat);
            res = false;
        }
        if (checkCurrentDeviceFocus(tzPackageName)) {
            LOGGER.info("On TZ changer apk page");
            res = true;
        } else {
            LOGGER.error("Not on com.futurek.android.tzc page after all tries. Please check logs.");
            res = false;
        }
        return res;
    }

    /**
     * openDateTimeSettingsSetupWizard in settings
     *
     * @param turnOffAuto - turn off AutoTimeZone and AutoTime
     * @param timeFormat - can be 12 or 24. Or empty.
     */
    private void openDateTimeSettingsSetupWizard(boolean turnOffAuto, TimeFormat timeFormat) {
        if (turnOffAuto) {
            switchDeviceAutoTimeAndTimeZone(false);
        }

        setSystemTime(timeFormat);

        openApp("com.android.settings/.DateTimeSettingsSetupWizard");
    }

    /**
     * openDateTimeSettingsSetupWizard in settings
     *
     * @param turnOffAuto - turn off AutoTimeZone and AutoTime
     * @param timeFormat - can be 12 or 24. Or empty.
     */
    private void openTZChangingApk(boolean turnOffAuto, TimeFormat timeFormat) {
        if (turnOffAuto) {
            switchDeviceAutoTimeAndTimeZone(false);
        }

        setSystemTime(timeFormat);

        openApp(TZ_CHANGE_APP_ACTIVITY);
        pause(2);
    }

    private void setSystemTime(TimeFormat timeFormat) {
        switch (timeFormat) {
        case FORMAT_12:
            LOGGER.info("Set 12 hours format");
            executeAdbCommand("shell settings put system time_12_24 12");
            break;
        case FORMAT_24:
            LOGGER.info("Set 24 hours format");
            executeAdbCommand("shell settings put system time_12_24 24");
            break;
        }
    }

    /**
     * Parse DateTime which came in format 'EE MMM dd hh:mm:ss zz yyyy'
     *
     * @param inputDate String
     * @return Date
     */
    private Date parseOutputDate(String inputDate) {
        Date result = new Date();
        try {
            LOGGER.info("Input date: " + inputDate);
            SimpleDateFormat inDateFormat = new SimpleDateFormat("EE MMM dd hh:mm:ss zz yyyy");

            result = inDateFormat.parse(inputDate);
            LOGGER.info("Output date: " + result);

        } catch (Exception e) {
            LOGGER.error(e);
        }
        return result;
    }

    /**
     * convertDateInCorrectString
     *
     * @param inputDate String
     * @return String
     */
    private String convertDateInCorrectString(Date inputDate) {
        String res = "";
        try {
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
            res = outputDateFormat.format(inputDate);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("Output date in expected format: " + res);
        return res;
    }

    // End of TimeZone private section

    /**
     * Pause
     *
     * @param timeout long
     */
    private void pause(long timeout) {
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}