package com.qaprosoft.carina.core.foundation.utils.android;

import com.qaprosoft.carina.core.foundation.log.TestLogCollector;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.CmdLine;
import com.qaprosoft.carina.core.foundation.utils.mobile.notifications.android.Notification;
import com.qaprosoft.carina.core.foundation.webdriver.DriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.Screenshot;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;
import com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.notifications.NotificationPage;
import com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.settings.DateTimeSettingsPage;
import com.qaprosoft.carina.core.gui.mobile.devices.android.phone.pages.tzchanger.TZChangerPage;
import io.appium.java_client.android.AndroidDriver;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.qaprosoft.carina.core.foundation.webdriver.DriverPool.getDriver;

public class AndroidService {

    private static final Logger LOGGER = Logger
            .getLogger(AndroidService.class);

    protected static final int INIT_TIMEOUT = 20;

    private final Pattern NOTIFICATION_PATTERN = Pattern
            .compile(".* NotificationRecord.*pkg=(.*) user");

    private final Pattern NOTIFICATION_TEXT_PATTERN = Pattern
            .compile(".*tickerText=(.*)");

    private String[] baseInitCmd;


    private AdbExecutor executor;


    public enum TimeFormat {
        FORMAT_12("12"),
        FORMAT_24("24");
        private String format;

        TimeFormat(String format) {
            this.format = format;
        }

        public String format() {
            return format;
        }

        public String toString() {
            return format;
        }

        public static TimeFormat parse(String text) {
            if (text != null) {
                for (TimeFormat type : TimeFormat.values()) {
                    if (text.equalsIgnoreCase(type.toString())) {
                        return type;
                    }
                }
            }
            return FORMAT_12;
        }
    }


    private static AndroidService instance;

    private AndroidService() {
        executor = new AdbExecutor();
        baseInitCmd = executor.buildDefaultCmd();
    }

    static {
        try {
            instance = new AndroidService();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred in creating singleton AndroidService");
        }
    }

    public static AndroidService getInstance() {
        return instance;
    }

    /**
     * executeAbdCommand
     *
     * @param command String
     * @return String in one line
     */
    public String executeAbdCommand(String command) {
        String udid = DevicePool.getDeviceUdid();
        if (!udid.isEmpty()) {
            // add udid reference
            command = "-s " + udid + " " + command;
        }

        LOGGER.info("Command: " + command);
        String[] listOfCommands = command.split(" ");

        String[] execCmd = CmdLine.insertCommandsAfter(baseInitCmd, listOfCommands);

        LOGGER.info("Try to execute following cmd:" + CmdLine.arrayToString(execCmd));
        List<String> execOutput = executor.execute(execCmd);
        LOGGER.info("Output after execution ADB command: "
                + execOutput);

        String result = execOutput.toString().replaceAll("\\[|\\]", "").replaceAll(", ", " ").trim();

        LOGGER.info("Returning Output: " + result);

        return result;
    }

    /**
     * expandStatusBar
     */
    public void expandStatusBar() {
        String[] expandStatusBarCmd = null;
        String udid = DevicePool.getDeviceUdid();

        if (!udid.isEmpty()) {
            expandStatusBarCmd = CmdLine.insertCommandsAfter(baseInitCmd, "-s", udid, "shell", "service", "call",
                    "statusbar", "1");
        } else {
            expandStatusBarCmd = CmdLine.insertCommandsAfter(baseInitCmd, "shell", "service", "call", "statusbar", "1");
        }

        LOGGER.info("Expand status bar cmd was built: " + CmdLine.arrayToString(expandStatusBarCmd));
        LOGGER.debug("Try to expand Status bar with following cmd:" + CmdLine.arrayToString(expandStatusBarCmd));
        List<String> expandOutput = executor.execute(expandStatusBarCmd);
        LOGGER.debug("Output after attempt to expand status bar: " + expandOutput);
    }

    /**
     * collapseStatusBar
     */
    public void collapseStatusBar() {
        String[] collapseStatusBarCmd = null;
        String udid = DevicePool.getDeviceUdid();
        if (!udid.isEmpty()) {
            collapseStatusBarCmd = CmdLine.insertCommandsAfter(baseInitCmd, "-s", udid, "shell", "service", "call",
                    "statusbar", "2");
        } else {
            collapseStatusBarCmd = CmdLine.insertCommandsAfter(baseInitCmd, "shell", "service", "call", "statusbar",
                    "2");
        }

        LOGGER.info("Collapse status bar cmd was built: " + CmdLine.arrayToString(collapseStatusBarCmd));
        List<String> collapseOutput = executor.execute(collapseStatusBarCmd);
        LOGGER.debug("Output after attempt to collapse status bar: " + collapseOutput);
    }

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
        String udid = DevicePool.getDeviceUdid();
        if (!udid.isEmpty()) {
            getNotificationsCmd = CmdLine.insertCommandsAfter(baseInitCmd, "-s", udid, "shell", "dumpsys",
                    "notification");
        } else {
            getNotificationsCmd = CmdLine.insertCommandsAfter(baseInitCmd, "shell", "dumpsys", "notification");
        }

        LOGGER.info("getNotifications cmd was built: " + CmdLine.arrayToString(getNotificationsCmd));

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
     * @param text    String
     * @param timeout long
     * @return boolean
     */
    public boolean waitUntilNewNotificationAppear(String text, long timeout) {
        //boolean found = false;
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
                LOGGER.info("Found '" + text + "' in notification packages '" + notify.getNotificationPkg() + "' with text '" + notify.getNotificationText() + "'.");
                return true;
            }

        }
        return false;
    }

    /**
     * waitUntilNewNotificationPackageAppear
     *
     * @param pkg     String
     * @param timeout long
     * @return boolean
     */
    public boolean waitUntilNewNotificationPackageAppear(String pkg, long timeout) {
        //boolean found = false;
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
     * @param expectedText  String
     * @return boolean
     */
    public boolean findExpectedNotification(String expectedTitle, String expectedText) {
        return findExpectedNotification(expectedTitle, expectedText, true);
    }

    /**
     * find Expected Notification
     *
     * @param expectedTitle String
     * @param expectedText  String
     * @param partially     boolean
     * @return boolean
     */
    public boolean findExpectedNotification(String expectedTitle, String expectedText, boolean partially) {
        //open notification
        try {
            ((AndroidDriver) getDriver()).openNotifications();
            pause(2); //wait while notifications are playing animation to appear to avoid missed taps
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
                        LOGGER.info("Found that expected title '" + expectedTitle + "' contains '" + title + "' in notification #" + notificationItemNum);
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
                        LOGGER.info("Found that expected text '" + expectedText + "' contains '" + text + "' in notification #" + notificationItemNum);
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
                LOGGER.info(String.format("Status bar isn't opened after %d seconds. One more attempt.",
                        (int) INIT_TIMEOUT));
                expandStatusBar();
            }
            LOGGER.debug("Page source [expand status bar]: ".concat(
                    getDriver().getPageSource()));
            TestLogCollector.addScreenshotComment(
                    Screenshot.capture(getDriver(), true),
                    "Clear notification - screenshot. Status bar should be opened. Attempt: " + i);
            try {
                notificationPage.clearNotifications();
            } catch (Exception e) {
                LOGGER.info("Exception during notification extraction.");
            }
        }
        collapseStatusBar();
    }


    /**
     * Open developer settings on device
     */
    public void openDeveloperOptions() {
        try {
            executeAbdCommand("shell am start -n com.android.settings/.DevelopmentSettings");
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * openDateTimeSettingsSetupWizard in settings
     *
     * @param turnOffAuto - turn off AutoTimeZone and AutoTime
     * @param timeFormat  - can be 12 or 24. Or empty.
     */
    public void openDateTimeSettingsSetupWizard(boolean turnOffAuto, TimeFormat timeFormat) {
        try {
            if (turnOffAuto) {
                switchDeviceAutoTimeAndTimeZone(false);
            }

            switch (timeFormat) {
                case FORMAT_12: {
                    LOGGER.info("Set 12 hours format");
                    executeAbdCommand("shell settings put system time_12_24 12");
                    break;
                }
                case FORMAT_24: {
                    LOGGER.info("Set 24 hours format");
                    executeAbdCommand("shell settings put system time_12_24 24");
                    break;
                }
            }

            //executeAbdCommand("shell am start -n com.android.settings/.DateTimeSettingsSetupWizard");
            openApp("com.android.settings/.DateTimeSettingsSetupWizard");
        } catch (Exception e) {
            LOGGER.error("Exception: " + e);
        }
    }

    /**
     * openDateTimeSettingsSetupWizard in settings
     *
     * @param turnOffAuto - turn off AutoTimeZone and AutoTime
     * @param timeFormat  - can be 12 or 24. Or empty.
     */
    public void openTZChangingApk(boolean turnOffAuto, TimeFormat timeFormat) {
        try {
            if (turnOffAuto) {
                switchDeviceAutoTimeAndTimeZone(false);
            }

            switch (timeFormat) {
                case FORMAT_12: {
                    LOGGER.info("Set 12 hours format");
                    executeAbdCommand("shell settings put system time_12_24 12");
                    break;
                }
                case FORMAT_24: {
                    LOGGER.info("Set 24 hours format");
                    executeAbdCommand("shell settings put system time_12_24 24");
                    break;
                }
            }

            openApp("com.futurek.android.tzc/com.futurek.android.tzc.MainActivity");
            pause(2);
        } catch (Exception e) {
            LOGGER.error("Exception. ", e);
        }
    }

    /**
     * switchDeviceAutoTimeAndTimeZone
     *
     * @param autoSwitch boolean. If true - auto Time and TimeZone will be set as On.
     */
    public void switchDeviceAutoTimeAndTimeZone(boolean autoSwitch) {
        try {
            if (autoSwitch) {
                executeAbdCommand("shell settings put global auto_time 1");
                executeAbdCommand("shell settings put global auto_time_zone 1");
            } else {
                executeAbdCommand("shell settings put global auto_time 0");
                executeAbdCommand("shell settings put global auto_time_zone 0");
            }

        } catch (Exception e) {
            LOGGER.error("Exception. ", e);
        }
    }

    /**
     * @param turnOffAuto boolean
     * @param timeFormat  TimeFormat
     * @param attemps     int
     * @return boolean
     */
    private boolean forceTZChangingApkOpen(boolean turnOffAuto, TimeFormat timeFormat, int attemps) {
        boolean res = false;

        String tzPackageName = "com.futurek.android.tzc";

        boolean isTzOpened = checkCurrentDeviceFocus(tzPackageName);
        while (!isTzOpened && attemps > 0) {
            LOGGER.info("TimeZoneChanger apk was not open. Attempt to open...");
            openTZChangingApk(turnOffAuto, timeFormat);
            isTzOpened = checkCurrentDeviceFocus(tzPackageName);
            attemps--;
        }

        if (!isTzOpened) {
            LOGGER.info("Probably TimeZone Changer APK was not installed correctly. Try to reinstall.");
            try {
                installTZChangerApk(false);
            } catch (Exception e) {
                LOGGER.error("Error during TZ Changer installation: ", e);
            }

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
     * openApp
     *
     * @param pkg      String
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
        executeAbdCommand("shell am start -n " + app);
    }

    /**
     * clear Apk Cache
     *
     * @param appPackageName for example: com.bamnetworks.mobile.android.gameday.atbat
     * @return boolean
     */
    public boolean clearApkCache(String appPackageName) {
        //Later can be used:
        /*
        String packageName = executor.getApkPackageName(String apkFile);
        executor.clearAppData(Device device, String appPackage);
        */
        String result = "";
        try {
            result = executeAbdCommand("shell pm clear " + appPackageName);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        if (result.contains("Success")) {
            LOGGER.info("Cache was cleared correctly");
            return true;
        } else {
            LOGGER.error("Cache was not cleared. May be application does not exist on this device.");
            return false;
        }
    }


    /**
     * getCurrentDeviceFocus - get actual device apk in focus
     *
     * @return String
     */
    public String getCurrentDeviceFocus() {
        String result = "";
        try {
            result = executeAbdCommand("shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'");
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return result;
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


    /**
     * setDeviceTimeZoneByADB
     *
     * @param timeZone      String
     * @param timeFormat    TimeFormat
     * @param deviceSetDate String in format yyyyMMdd.HHmmss. Can be empty.
     * @return String
     */
    public String setDeviceTimeZoneByADB(String timeZone, TimeFormat timeFormat, String deviceSetDate) {
        boolean changeDateTime = true;
        //TODO: tzGMT
        String tzGMT = "";
        if (deviceSetDate.isEmpty()) {
            changeDateTime = false;
        }
        DeviceTimeZone dt = new DeviceTimeZone(false, false, timeFormat, timeZone, tzGMT, deviceSetDate, changeDateTime, true);
        return setDeviceTimeZoneByADB(dt);
    }

    /**
     * setDeviceTimeZoneByADB
     * Automatic date and time = OFF (settings - date and time)
     * adb shell settings put global auto_time 0
     * Automatic time zone = OFF (settings - date and time)
     * adb shell settings put global auto_time_zone 0
     * <p>
     * Set Time Zone on device
     * adb shell setprop persist.sys.timezone "America/Chicago"
     * <p>
     * Check timezones:
     * <a href="https://en.wikipedia.org/wiki/List_of_tz_database_time_zones">List_of_tz_database_time_zones</a>
     * <p>
     * Check time on device
     * adb shell date -s %mynow%
     * <p>
     * Restart application
     *
     * @param dt DeviceTimeZone
     * @return String actual Device Date and Time
     */
    public String setDeviceTimeZoneByADB(DeviceTimeZone dt) {

        if (dt == null) {
            LOGGER.error("DeviceTimeZone is not initialised.");
            dt = new DeviceTimeZone();
        }
        LOGGER.info(dt.toString());

        try {
            if (dt.isAutoTime()) {
                executeAbdCommand("shell settings put global auto_time 1");
            } else {
                executeAbdCommand("shell settings put global auto_time 0");
            }

            if (dt.isAutoTimezone()) {
                executeAbdCommand("shell settings put global auto_time_zone 1");
            } else {
                executeAbdCommand("shell settings put global auto_time_zone 0");
            }

            switch (dt.getTimeFormat()) {
                case FORMAT_12: {
                    LOGGER.info("Set 12 hours format");
                    executeAbdCommand("shell settings put system time_12_24 12");
                    break;
                }
                case FORMAT_24: {
                    LOGGER.info("Set 24 hours format");
                    executeAbdCommand("shell settings put system time_12_24 24");
                    break;
                }
            }

            if (!dt.getTimezone().isEmpty()) {
                executeAbdCommand("shell setprop persist.sys.timezone \"" + dt.getTimezone() + "\"");
            }

            if (dt.isRefreshDeviceTime()) {
                executeAbdCommand("shell am broadcast -a android.intent.action.TIME_SET");
            }

            if (dt.isChangeDateTime() && !dt.getSetDeviceDateTime().isEmpty()) {
                //Try to set date for device but it will not work on not rooted devices
                executeAbdCommand("shell date " + dt.getSetDeviceDateTime());
            }

        } catch (Exception e) {
            LOGGER.error(e);
        }

        String actualDT = executeAbdCommand("shell date -s %mynow%");
        LOGGER.info(actualDT);
        return actualDT;
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

        getDriver(); //start driver in before class to assign it for particular thread
        DeviceTimeZone dt = new DeviceTimeZone();

        try {
            String value = executeAbdCommand("shell settings get global auto_time");
            if (value.contains("0")) {
                dt.setAutoTime(false);
            } else {
                dt.setAutoTime(true);
            }

            value = executeAbdCommand("shell settings get global auto_time_zone");
            if (value.contains("0")) {
                dt.setAutoTimezone(false);
            } else {
                dt.setAutoTimezone(true);
            }

            value = executeAbdCommand("shell settings get system time_12_24");
            if (value.contains("12")) {
                dt.setTimeFormat(TimeFormat.FORMAT_12);
            } else {
                dt.setTimeFormat(TimeFormat.FORMAT_24);
            }

            if (defaultTZ.isEmpty()) {
                value = executeAbdCommand("shell getprop persist.sys.timezone");
                if (!value.isEmpty()) {
                    dt.setTimezone(value);
                }
            } else {
                dt.setTimezone(defaultTZ);
            }

            value = executeAbdCommand("shell date -s %mynow%");
            LOGGER.info(value);
            if (!value.isEmpty()) {
                value = convertDateInCorrectString(parseOutputDate(value));
                dt.setSetDeviceDateTime(value);
                LOGGER.info(value);
            }

            dt.setChangeDateTime(false);
            dt.setRefreshDeviceTime(true);

        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info(dt.toString());

        return dt;
    }

    /**
     * get Device Actual TimeZone
     *
     * @return String
     */
    public String getDeviceActualTimeZone() {
        String res = "";
        try {

            String value = executeAbdCommand("shell getprop persist.sys.timezone");
            if (!value.isEmpty()) {
                res = value;
                LOGGER.info(value);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return res;
    }


    //Start of TimeZone Setting section

    /**
     * set Device TimeZone. By ADB and Settings
     *
     * @param timeZone   String required timeZone
     * @param timeFormat String 12 or 24
     * @param settingsTZ TimeFormat
     * @return boolean
     */
    public boolean setDeviceTimeZone(String timeZone, String settingsTZ, TimeFormat timeFormat) {

        boolean changed = false;

        getDriver(); //start driver in before class to assign it for particular thread
        String actualTZ = getDeviceActualTimeZone();

        String variant = "all";
        String currentAndroidVersion = Configuration.get(Configuration.Parameter.MOBILE_PLATFORM_VERSION);
        LOGGER.info("currentAndroidVersion=" + currentAndroidVersion);
        if (currentAndroidVersion.contains("7.")) {
            LOGGER.info("TimeZone changing for Android 7+ works only by TimeZone changer apk.");
            variant = "3";
        }

        if (comparingExpectedAndActualTZ(actualTZ, timeZone)) {
            LOGGER.info("Required timeZone is already set.");
            return true;
        }

        String deviceSetDate = "";

        //Solution for ADB timezone changing.
        if (variant.equals("all") || variant.contains("1")) {
            LOGGER.info("Solution for ADB timezone changing.");
            String response = setDeviceTimeZoneByADB(timeZone, timeFormat, deviceSetDate);

            LOGGER.info(response);

            actualTZ = getDeviceActualTimeZone();
            if (comparingExpectedAndActualTZ(actualTZ, timeZone)) {
                LOGGER.info("Required timeZone '" + timeZone + "' was set by ADB.");
                try {
                    LOGGER.info("Restart driver when correct TimeZone set.");
                    DriverPool.restartDriver();
                } catch (AssertionError err) {
                    LOGGER.error("Assertion error. ", err);
                } catch (Exception e) {
                    LOGGER.error("Error restarting driver: ", e);
                }
                changed = true;
            } else {
                LOGGER.error("TimeZone was not changed by ADB. Actual TZ is: " + actualTZ);
                changed = false;
            }
        }

        //Solution for timezone changing by device Settings. (Tested on S7, Note 3, S6, S5).
        if (!changed) {
            if (variant.equals("all") || variant.contains("2")) {
                LOGGER.info("Try to change TimeZone by Device Settings");
                changed = setDeviceTimeZoneBySetting(timeZone, settingsTZ, timeFormat);
                if (changed) {
                    LOGGER.info("Required timeZone '" + timeZone + "' was set by device Settings.");
                    try {
                        LOGGER.info("Restart driver at the end of Settings change.");
                        DriverPool.restartDriver();
                    } catch (AssertionError err) {
                        LOGGER.error("Assertion error. ", err);
                    } catch (Exception e) {
                        LOGGER.error("Error restarting driver: ", e);
                    }
                }
            }
        }

        //Solution for using TimeZone Changer apk.
        if (!changed) {
            if (variant.equals("all") || variant.contains("3")) {
                LOGGER.info("Use TimeZone Changer apk.");
                changed = setDeviceTimeZoneByChangerApk(timeZone, timeFormat);
                try {
                    LOGGER.info("Restart driver at the end of usage TimeZone change apk.");
                    DriverPool.restartDriver();
                } catch (AssertionError err) {
                    LOGGER.error("Assertion error. ", err);
                } catch (Exception e) {
                    LOGGER.error("Error restarting driver: ", e);
                }
            }
        }
        return changed;
    }

    /**
     * setDeviceTimeZoneBySetting
     *
     * @param timeZone   String
     * @param settingsTZ String
     * @param timeFormat TimeFormat
     * @return boolean
     */
    public boolean setDeviceTimeZoneBySetting(String timeZone, String settingsTZ, TimeFormat timeFormat) {

        String actualTZ = getDeviceActualTimeZone();

        String tz = DeviceTimeZone.getTimezoneOffset(timeZone);

        if (comparingExpectedAndActualTZ(actualTZ, timeZone)) {
            LOGGER.info("Required timeZone is already set.");
            return true;
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
        actualTZ = getDeviceActualTimeZone();
        if (comparingExpectedAndActualTZ(actualTZ, timeZone)) {
            LOGGER.info("Required timeZone '" + timeZone + "' was set by Settings.");
            return true;
        } else {
            LOGGER.error("TimeZone was not changed by Settings. Actual TZ is: " + actualTZ);
            return false;
        }
    }


    /**
     * setDeviceTimeZoneByChangerApk
     *
     * @param timeZone   String
     * @param timeFormat TimeFormat
     * @return boolean
     */
    public boolean setDeviceTimeZoneByChangerApk(String timeZone, TimeFormat timeFormat) {

        String actualTZ = getDeviceActualTimeZone();

        String tz = DeviceTimeZone.getTimezoneOffset(timeZone);
        LOGGER.info("Required TimeZone offset: " + tz);

        if (comparingExpectedAndActualTZ(actualTZ, timeZone)) {
            LOGGER.info("Required timeZone is already set.");
            return true;
        }

        try {
            installTZChangerApk(false);
        } catch (Exception e) {
            LOGGER.error("Error during TZ Changer installation: ", e);
        }

        try {

            forceTZChangingApkOpen(true, timeFormat, 3);

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


        actualTZ = getDeviceActualTimeZone();
        if (comparingExpectedAndActualTZ(actualTZ, timeZone)) {
            LOGGER.info("Required timeZone '" + timeZone + "' was set by TimeZoneChanger apk.");
            return true;
        } else {
            LOGGER.error("TimeZone was not changed by TimeZoneChanger apk. Actual TZ is: '" + actualTZ + "' but expected: '" + timeZone + "'.");
            return false;
        }
    }


    /**
     * checkCurrentDeviceFocus - return actual device focused apk and compare with expected.
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
     * install android Apk. Should be put in src/main/resources/app
     * @param apkPath String
     * @param standartUsage boolean
     * @return boolean
     */
    public boolean installApk(String apkPath, boolean standartUsage) {
        String path = "";
        boolean ret = true;
        try {
            path = getResourceFilePath(apkPath);
        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
        LOGGER.info("Path: " + path);

        if (standartUsage) {
            Device device = DevicePool.getDevice();
            String defautlPropUninstall = R.CONFIG.get("mobile_app_uninstall");
            String defautlPropInstall = R.CONFIG.get("mobile_app_install");
            LOGGER.info("defautlPropUninstall = " + defautlPropUninstall);
            LOGGER.info("defautlPropInstall = " + defautlPropInstall);
            R.CONFIG.put("mobile_app_uninstall", "true");
            R.CONFIG.put("mobile_app_install", "true");

            executor.reinstallApp(device, path);

            R.CONFIG.put("mobile_app_uninstall", defautlPropUninstall);
            R.CONFIG.put("mobile_app_install", defautlPropInstall);
        } else {
            String res = executeAbdCommand("install " + path);
            LOGGER.info(res);
        }
        return ret;
    }

    /**
     * installTZChangerApk with standard usage
     */
    private void installTZChangerApk() {
        installTZChangerApk(true);
    }

    /**
     * installTZChangerApk
     *
     * @param standartUsage boolean
     */
    private void installTZChangerApk(boolean standartUsage) {
        String TZ_CHANGE_APP_PATH = "app/TimeZone_Changer.apk";
        boolean res = installApk(TZ_CHANGE_APP_PATH,standartUsage);
        if(res) {
            LOGGER.info("Application '"+TZ_CHANGE_APP_PATH+"' was successfully installed.");
        } else {
            LOGGER.info("Application '"+TZ_CHANGE_APP_PATH+"' was NOT installed.");
        }
     }

    /**
     * getResourceFilePath
     *
     * @param resourceFileName String
     * @return String
     * @throws Exception error
     */
    private String getResourceFilePath(String resourceFileName) throws Exception {
        String filePath = "";
        try {
            filePath = URLDecoder.decode(getClass().getClassLoader().getResource(resourceFileName).getFile(), "utf-8");
            LOGGER.info("Path1: " + filePath);
            String filePath2 = URLDecoder.decode(getClass().getClassLoader().getSystemResource(resourceFileName).getFile(), "utf-8");
            LOGGER.info("Path2: " + filePath2);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error in path decoding for '" + resourceFileName + "' : " + e);
        } catch (Exception e) {
            LOGGER.error("Error for '" + resourceFileName + "' : " + e);
            try {
                String filePath2 = URLDecoder.decode(getClass().getClassLoader().getSystemResource(resourceFileName).getFile(), "utf-8");
                LOGGER.info("Path2: " + filePath2);
                filePath = filePath2;
            } catch (Exception e2) {
                LOGGER.error("Error for Path 2 '" + resourceFileName + "' : " + e);
                throw new Exception("Error in getting resources for " + resourceFileName);
            }
        }
        if (filePath.contains(":")) {
            LOGGER.info("Windows execution. Return with substring.");
            filePath = filePath.substring(1);
        } else {
            if (filePath.startsWith("/")) {
                LOGGER.info("Path is correct: " + filePath);
            } else filePath = "/" + filePath;
        }
        return filePath;
    }

    /**
     * comparingExpectedAndActualTZ
     *
     * @param actualTZ String
     * @param tz       String
     * @return boolean
     */
    private boolean comparingExpectedAndActualTZ(String actualTZ, String tz) {
        boolean res = actualTZ.equals(tz);
        if (!res) {
            String[] actTZ = actualTZ.split("/");
            String lastActTZ = actTZ[actTZ.length - 1];
            String[] timeZoneTZ = tz.split("/");
            String lastTimeZoneTZ = timeZoneTZ[timeZoneTZ.length - 1];
            LOGGER.debug("Comparing '" + lastActTZ + "' with '" + lastTimeZoneTZ + "'.");
            res = lastActTZ.equals(lastTimeZoneTZ);
        }
        return res;
    }

//End of TimeZone Setting section

    //Private section

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


    /**
     * Pause
     *
     * @param timeout long
     */
    public void pause(long timeout) {
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}