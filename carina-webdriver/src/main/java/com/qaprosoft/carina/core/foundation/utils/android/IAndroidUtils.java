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
package com.qaprosoft.carina.core.foundation.utils.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.android.Permissions.Permission;
import com.qaprosoft.carina.core.foundation.utils.android.Permissions.PermissionAction;
import com.qaprosoft.carina.core.foundation.utils.android.Permissions.PermissionType;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.CmdLine;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.mobile.IMobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.android.SupportsNetworkStateManagement;
import io.appium.java_client.android.connection.HasNetworkConnection;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.KeyEventFlag;
import io.appium.java_client.android.nativekey.PressesKey;

public interface IAndroidUtils extends IMobileUtils {

    // TODO: review carefully and remove duplicates and migrate completely to fluent
    // waits
    static final Logger UTILS_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final int SCROLL_MAX_SEARCH_SWIPES = 55;
    static final long SCROLL_TIMEOUT = 300;
    AdbExecutor executor = new AdbExecutor();
    String[] baseInitCmd = executor.getDefaultCmd();
    static final String LANGUAGE_CHANGE_APP_PATH = "app/ADB_Change_Language.apk";

    static final String SHELL_INIT_CONSOLE = "mobile: shell";
    static final String SHELL_INIT_DEEPLINK_CONSOLE = "mobile:deepLink";
    static final String SHELL_INIT_GET_PERMISSION_CONSOLE = "mobile:getPermissions";
    static final String SHELL_INIT_CHANGE_PERMISSION_CONSOLE = "mobile:changePermissions";

    static final String SHELL_GPS_STATUS_CMD = "settings get secure location_providers_allowed";
    static final String SHELL_CLOSE_STATUS_BAR_CMD = "cmd statusbar collapse";
    static final String SHELL_OPEN_STATUS_BAR_CMD = "cmd statusbar expand-notifications";
    static final String SHELL_INPUT_TXT_CMD = "input text ";
    static final String SHELL_OPEN_URL_CMD = "am start -a android.intent.action.VIEW";
    static final String SHELL_CLEAR_CACHE_CMD = "pm clear";
    static final String SHELL_OPEN_DEVICE_SETTINGS_CMD = "am start -a android.settings.SETTINGS";
    static final String SHELL_TAKE_SCREENSHOT_CMD = "screencap -p";
    static final String SHELL_DISABLE_GPS_CMD = "settings put secure location_providers_allowed -gps";
    static final String SHELL_ENABLE_GPS_CMD = "settings put secure location_providers_allowed +gps";
    static final String SHELL_PRESS_HOME_CMD = "input keyevent 3";
    static final String SHELL_RECENT_APPS_CMD = "input keyevent KEYCODE_APP_SWITCH";

    // seconds
    static final int DEVICE_REFRESH_TIME = 20;

    /**
     * Send a key-press event to the keyboard
     *
     * @param key keyboard key, see {@link AndroidKey}
     *
     * @see <a href="https://android-developers.googleblog.com/2008/12/touch-mode.html">This method send key without leaving the touch-mode</a>
     */
    default public void pressKeyboardKey(AndroidKey key) {
        PressesKey driver = null;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new RuntimeException("driver is not support pressKeyboardKey method", e);
        }

        driver.pressKey(new KeyEvent(key)
                .withFlag(KeyEventFlag.SOFT_KEYBOARD)
                .withFlag(KeyEventFlag.KEEP_TOUCH_MODE)
                .withFlag(KeyEventFlag.EDITOR_ACTION));
    }

    /**
     * Send a key-press event to the keyboard
     *
     * @param key keyboard key, see {@link AndroidKey}
     * @param flags event flags, see {@link KeyEventFlag}
     */
    default public void pressKeyboardKey(AndroidKey key, KeyEventFlag... flags) {
        PressesKey driver = null;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new RuntimeException("driver is not support pressKeyboardKey method", e);
        }
        KeyEvent keyEvent = new KeyEvent(key);

        if (flags.length > 0) {
            for (KeyEventFlag keyEventFlag : flags) {
                keyEvent = keyEvent.withFlag(keyEventFlag);
            }
        }

        driver.pressKey(keyEvent);
    }

    /**
     * Send a key-press events to the keyboard
     *
     * @param keys keyboard keys, see {@link AndroidKey}
     * 
     * @see <a href="https://android-developers.googleblog.com/2008/12/touch-mode.html">This method send key without leaving the touch-mode</a>
     */
    default public void pressKeyboardKeys(List<AndroidKey> keys) {
        final PressesKey driver;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new RuntimeException("driver is not support pressKeyboardKey(s) method", e);
        }

        keys.forEach(key -> driver.pressKey(new KeyEvent(key)
                .withFlag(KeyEventFlag.SOFT_KEYBOARD)
                .withFlag(KeyEventFlag.KEEP_TOUCH_MODE)
                .withFlag(KeyEventFlag.EDITOR_ACTION)));
    }
    

    /**
     * Send a key-press {@link AndroidKey#BACK} event to the keyboard
     */
    default public void pressBack() {
        WebDriver driver = getDriver();
        ((PressesKey) driver).pressKey(new KeyEvent(AndroidKey.BACK));
    }

    /**
     * Send a key-press {@link AndroidKey#SEARCH} event to the keyboard
     */
    default public void pressSearchKey() {
        WebDriver driver = getDriver();
        ((PressesKey) driver).pressKey(new KeyEvent(AndroidKey.SEARCH));
    }

    /**
     * Press next key by coordinates
     * This method does not guarantee that the next button will be clicked,
     * it clicks the bottom right button by coordinates
     */
    default public void pressNextKey() {
        // todo refactor to use pressKey
        pressBottomRightKey();
    }

    // Change Device Language section

    /**
     * change device language using ADBChangeLanguage application via ADB
     *
     * <p>
     * <b>Usage</b>: <br>
     * - install this app <br>
     * - setup adb connection to your device <br>
     * (http://developer.android.com/tools/help/adb.html) - Android OS 4.2 onwards
     * (tip: you can copy the command here and paste it to your command console): <br>
     * {@code adb shell pm grant net.sanapeli.adbchangelanguage}
     * android.permission.CHANGE_CONFIGURATION
     *
     * <p>
     * English: {@code adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language en}<br>
     * Russian: {@code adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -elanguage ru}<br>
     * Spanish: {@code adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language es}<br>
     *
     *
     * @param language to set, for example {@code es}, {@code en}, etc.
     * 
     * @see <a href="http://play.google.com/store/apps/details?id=net.sanapeli.adbchangelanguage">ADBChangeLanguage apk</a>
     * @return was the language change successful
     */
    default public boolean setDeviceLanguage(String language) {
        return setDeviceLanguage(language, DEVICE_REFRESH_TIME);
    }


    /**
     * change device language using ADBChangeLanguage application via ADB
     *
     * <p>
     * <b>Usage</b>: <br>
     * - install this app <br>
     * - setup adb connection to your device <br>
     * (http://developer.android.com/tools/help/adb.html) - Android OS 4.2 onwards
     * (tip: you can copy the command here and paste it to your command console): <br>
     * {@code adb shell pm grant net.sanapeli.adbchangelanguage android.permission.CHANGE_CONFIGURATION}

     * <p>
     * English: {@code adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language en}<br>
     * Russian: {@code adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -elanguage ru}<br>
     * Spanish: {@code adb shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language es}<br>
     *
     * @param language to set, for example {@code es}, {@code en}, etc.
     * @param waitTime int wait in seconds before device refresh
     *
     * @see <a href="http://play.google.com/store/apps/details?id=net.sanapeli.adbchangelanguage">ADBChangeLanguage apk</a>
     * @return was the language change successful
     */
    default public boolean setDeviceLanguage(String language, int waitTime) {
        boolean status = false;
        UTILS_LOGGER.info("Do not concat language for Android. Keep: {}", language);
        language = language.replace("_", "-");
        UTILS_LOGGER.info("Refactor language to : {}", language);

        String actualDeviceLanguage = getDeviceLanguage();

        if (language.contains(actualDeviceLanguage.toLowerCase()) ||
                actualDeviceLanguage.toLowerCase().contains(language)) {
            UTILS_LOGGER.info("Device already have expected language: {}", actualDeviceLanguage);
            return true;
        }

        String setLocalizationChangePermissionCmd = "shell pm grant net.sanapeli.adbchangelanguage android.permission.CHANGE_CONFIGURATION";

        String setLocalizationCmd = "shell am start -n net.sanapeli.adbchangelanguage/.AdbChangeLanguage -e language "
                + language;

        UTILS_LOGGER.info("Try set localization change permission with following cmd: {}", setLocalizationChangePermissionCmd);
        String expandOutput = executeAdbCommand(setLocalizationChangePermissionCmd);

        String pathToInstalledAppCmd = "shell pm path net.sanapeli.adbchangelanguage";
        String pathToInstalledApp = executeAdbCommand(pathToInstalledAppCmd);

        if (expandOutput.contains("Unknown package: net.sanapeli.adbchangelanguage") || pathToInstalledApp.isEmpty()) {
            UTILS_LOGGER.info("Looks like 'ADB Change Language apk' is not installed. Install it and try again.");
            installApk(LANGUAGE_CHANGE_APP_PATH, true);
            expandOutput = executeAdbCommand(setLocalizationChangePermissionCmd);
        }

        UTILS_LOGGER.info("Output after set localization change permission using 'ADB Change Language apk': {}", expandOutput);

        UTILS_LOGGER.info("Try set localization to '{}' with following cmd: {}", language, setLocalizationCmd);
        String changeLocaleOutput = executeAdbCommand(setLocalizationCmd);
        UTILS_LOGGER.info("Output after set localization to '{}' using 'ADB Change Language apk' : {}", language, changeLocaleOutput);

        if (waitTime > 0) {
            UTILS_LOGGER.info("Wait for at least '{}' seconds before device refresh.", waitTime);
            CommonUtils.pause(waitTime);
        }

        actualDeviceLanguage = getDeviceLanguage();
        UTILS_LOGGER.info("Actual Device Language: " + actualDeviceLanguage);
        if (language.contains(actualDeviceLanguage.toLowerCase())
                || actualDeviceLanguage.toLowerCase().contains(language)) {
            status = true;
        } else {
            if (getDeviceLanguage().isEmpty()) {
                UTILS_LOGGER.info("Adb return empty response without errors.");
                status = true;
            } else {
                String currentAndroidVersion = IDriverPool.getDefaultDevice().getOsVersion();
                UTILS_LOGGER.info("currentAndroidVersion=" + currentAndroidVersion);
                if (currentAndroidVersion.contains("7.")) {
                    UTILS_LOGGER.info("Adb return language command do not work on some Android 7+ devices."
                            + " Check that there are no error.");
                    status = !getDeviceLanguage().toLowerCase().contains("error");
                }
            }
        }
        return status;
    }

    /**
     * Get the current language on the device
     *
     * @return language
     */
    default public String getDeviceLanguage() {
        String locale = executeAdbCommand("shell getprop persist.sys.language");
        if (locale.isEmpty()) {
            locale = executeAdbCommand("shell getprop persist.sys.locale");
        }
        return locale;
    }

    // End Language Change section

    /**
     * Install android Apk by path to apk file
     *
     * @param apkPath todo add description
     */
    default public void installApk(final String apkPath) {
        installApk(apkPath, false);
    }

    /**
     * Install android Apk by path to apk or by name in classpath
     *
     * @param apkPath todo add description
     * @param inClasspath boolean
     */
    default public void installApk(final String apkPath, boolean inClasspath) {
        String filePath = apkPath;

        if (inClasspath) {
            URL baseResource = ClassLoader.getSystemResource(apkPath);
            if (baseResource == null) {
                throw new RuntimeException("Unable to get resource from classpath: " + apkPath);
            }
            UTILS_LOGGER.debug("Resource was found: {}", baseResource.getPath());

            String fileName = FilenameUtils.getBaseName(baseResource.getPath()) + "." + FilenameUtils.getExtension(baseResource.getPath());
            // make temporary copy of resource in artifacts folder
            filePath = ReportContext.getArtifactsFolder().getAbsolutePath() + File.separator + fileName;

            File file = new File(filePath);
            if (!file.exists()) {
                InputStream link = (ClassLoader.getSystemResourceAsStream(apkPath));
                try {
                    Files.copy(link, file.getAbsoluteFile().toPath());
                } catch (IOException e) {
                    UTILS_LOGGER.error("Unable to extract resource from ClassLoader!", e);
                }
            }
        }

        executeAdbCommand("install " + filePath);
    }

    public enum SelectorType {
        TEXT,
        TEXT_CONTAINS,
        TEXT_STARTS_WITH,
        ID,
        DESCRIPTION,
        DESCRIPTION_CONTAINS,
        CLASS_NAME
    }

    /**
     * Scrolls into view in specified container by text only and return found element
     *
     * <p>
     * example of usage: {@code ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer);}
     * 
     * @param scrollToElement text to scroll to
     * @param container the element in which the text will be searched
     * 
     * @return if element was found, return {@link ExtendedWebElement}, otherwise return {@code null}
     * 
     **/
    default public ExtendedWebElement scroll(String scrollToElement, ExtendedWebElement container) {
        return scroll(scrollToElement, container, SelectorType.ID, SelectorType.TEXT);
    }

    /**
     * Scrolls into view in a container specified by it's instance (index)
     * 
     * @param scrollToEle
     *            - has to be id, text, contentDesc or className
     * @param scrollableContainer
     *            - ExtendedWebElement type
     * @param containerSelectorType
     *            - has to be id, text, textContains, textStartsWith, Description,
     *            DescriptionContains or className
     * @param containerInstance
     *            - has to an instance number of desired container
     * @param eleSelectorType
     *            - has to be id, text, textContains, textStartsWith, Description,
     *            DescriptionContains or className
     * @return ExtendedWebElement
     *         <p>
     *         example of usage: ExtendedWebElement res =
     *         AndroidUtils.scroll("News", newsListContainer,
     *         AndroidUtils.SelectorType.CLASS_NAME, 1,
     *         AndroidUtils.SelectorType.TEXT);
     **/
    default public ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer,
            SelectorType containerSelectorType, int containerInstance, SelectorType eleSelectorType) {
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        // TODO: support multi threaded WebDriver's removing DriverPool usage
        WebDriver drv = getDriver();

        // workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","), ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
                By scrollBy = AppiumBy.androidUIAutomator("new UiScrollable("
                        + getScrollContainerSelector(scrollableContainer, containerSelectorType) + ".instance("
                        + containerInstance + "))" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")"
                        + ".scrollIntoView(" + getScrollToElementSelector(scrollToEle, eleSelectorType) + ")");

                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    UTILS_LOGGER.info("Element found!!!");
                    // initializing with driver context because scrollBy consists from container and element selectors
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                UTILS_LOGGER.error(String.format("%s %s:%s", SpecialKeywords.NO_SUCH_ELEMENT_ERROR, eleSelectorType, scrollToEle),
                        noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                AppiumBy.androidUIAutomator(
                        "new UiScrollable(" + getScrollContainerSelector(scrollableContainer, containerSelectorType)
                                + ".instance(" + containerInstance + ")).scrollForward()");
                UTILS_LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /**
     * Scrolls into view in specified container
     * 
     * @param scrollToEle
     *            - has to be id, text, contentDesc or className
     * @param scrollableContainer
     *            - ExtendedWebElement type
     * @param containerSelectorType
     *            - has to be id, text, textContains, textStartsWith, Description,
     *            DescriptionContains or className
     * @param containerInstance
     *            - has to an instance number of desired container
     * @param eleSelectorType
     *            - has to be id, text, textContains, textStartsWith, Description,
     *            DescriptionContains or className
     * @param eleSelectorInstance
     *            - has to an instance number of desired container
     * @return ExtendedWebElement
     *         <p>
     *         example of usage: ExtendedWebElement res =
     *         AndroidUtils.scroll("News", newsListContainer,
     *         AndroidUtils.SelectorType.CLASS_NAME, 1,
     *         AndroidUtils.SelectorType.TEXT, 2);
     **/
    default public ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer,
            SelectorType containerSelectorType, int containerInstance, SelectorType eleSelectorType,
            int eleSelectorInstance) {
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        // TODO: support multi threaded WebDriver's removing DriverPool usage
        WebDriver drv = getDriver();

        // workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","), ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
                By scrollBy = AppiumBy.androidUIAutomator("new UiScrollable("
                        + getScrollContainerSelector(scrollableContainer, containerSelectorType) + ".instance("
                        + containerInstance + "))" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")"
                        + ".scrollIntoView(" + getScrollToElementSelector(scrollToEle, eleSelectorType) + ".instance("
                        + eleSelectorInstance + "))");

                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    UTILS_LOGGER.info("Element found!!!");
                    // initializing with driver context because scrollBy consists from container and element selectors
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                UTILS_LOGGER.error(String.format("%s%s:%s", SpecialKeywords.NO_SUCH_ELEMENT_ERROR, eleSelectorType, scrollToEle),
                        noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                AppiumBy.androidUIAutomator(
                        "new UiScrollable(" + getScrollContainerSelector(scrollableContainer, containerSelectorType)
                                + ".instance(" + containerInstance + ")).scrollForward()");
                UTILS_LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /**
     * Scrolls into view in specified container
     * 
     * @param scrollToEle has to be id, text, contentDesc or className
     * @param scrollableContainer ExtendedWebElement type
     * @param containerSelectorType container Selector type: has to be id, text, textContains, textStartsWith, Description, DescriptionContains or
     *            className
     * @param eleSelectorType scrollToEle Selector type: has to be id, text, textContains, textStartsWith, Description, DescriptionContains or
     *            className
     * 
     * @return ExtendedWebElement
     *         <p>
     *         example of usage: {@code ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer, AndroidUtils.SelectorType.CLASS_NAME,
     *         AndroidUtils.SelectorType.TEXT);}
     **/
    default public ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer, SelectorType containerSelectorType,
            SelectorType eleSelectorType) {
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        // TODO: support multi threaded WebDriver's removing DriverPool usage
        WebDriver drv = getDriver();

        // workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","), ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
                By scrollBy = AppiumBy.androidUIAutomator(
                        "new UiScrollable(" + getScrollContainerSelector(scrollableContainer, containerSelectorType)
                                + ")" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")" + ".scrollIntoView("
                                + getScrollToElementSelector(scrollToEle, eleSelectorType) + ")");

                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    UTILS_LOGGER.info("Element found!!!");
                    // initializing with driver context because scrollBy consists from container and element selectors
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                UTILS_LOGGER.error(String.format("%s%s:%s", SpecialKeywords.NO_SUCH_ELEMENT_ERROR, eleSelectorType, scrollToEle),
                        noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                AppiumBy.androidUIAutomator("new UiScrollable("
                        + getScrollContainerSelector(scrollableContainer, containerSelectorType) + ").scrollForward()");
                UTILS_LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /**
     * Scrolls into view in specified container
     * 
     * @param scrollableContainer
     *            - ExtendedWebElement type
     * @param containerSelectorType
     *            - Selector type: has to be id, text, contentDesc or className
     * @return scrollViewContainerFinder String
     *
     **/
    default String getScrollContainerSelector(ExtendedWebElement scrollableContainer, SelectorType containerSelectorType) {
        UTILS_LOGGER.debug(scrollableContainer.getBy().toString());
        String scrollableContainerBy;
        String scrollViewContainerFinder = "";

        switch (containerSelectorType) {
        case TEXT:
            scrollableContainerBy = scrollableContainer.getBy().toString().replace("By.text:", "").trim();
            scrollViewContainerFinder = "new UiSelector().text(\"" + scrollableContainerBy + "\")";
            break;
        case TEXT_CONTAINS:
            scrollableContainerBy = scrollableContainer.getBy().toString().replace("By.textContains:", "").trim();
            scrollViewContainerFinder = "new UiSelector().textContains(\"" + scrollableContainerBy + "\")";
            break;
        case TEXT_STARTS_WITH:
            scrollableContainerBy = scrollableContainer.getBy().toString().replace("By.textStartsWith:", "").trim();
            scrollViewContainerFinder = "new UiSelector().textStartsWith(\"" + scrollableContainerBy + "\")";
            break;
        case ID:
            scrollableContainerBy = scrollableContainer.getBy().toString().replace("By.id:", "").trim();
            scrollViewContainerFinder = "new UiSelector().resourceId(\"" + scrollableContainerBy + "\")";
            break;
        case DESCRIPTION:
            scrollableContainerBy = scrollableContainer.getBy().toString().replace("By.description:", "").trim();
            scrollViewContainerFinder = "new UiSelector().description(\"" + scrollableContainerBy + "\")";
            break;
        case DESCRIPTION_CONTAINS:
            scrollableContainerBy = scrollableContainer.getBy().toString().replace("By.descriptionContains:", "")
                    .trim();
            scrollViewContainerFinder = "new UiSelector().descriptionContains(\"" + scrollableContainerBy + "\")";
            break;
        case CLASS_NAME:
            scrollableContainerBy = scrollableContainer.getBy().toString().replace("By.className:", "").trim();
            scrollViewContainerFinder = "new UiSelector().className(\"" + scrollableContainerBy + "\")";
            break;
        default:
            UTILS_LOGGER.info("Please provide valid selectorType for element to be found...");
            break;
        }

        return scrollViewContainerFinder;

    }

    /**
     * Scrolls into view in specified container
     * 
     * @param scrollToEle
     *            - String type
     * @param eleSelectorType
     *            - Selector type: has to be id, text, contentDesc or className
     * @return String
     **/
    default String getScrollToElementSelector(String scrollToEle, SelectorType eleSelectorType) {
        String neededElementFinder = "";
        String scrollToEleTrimmed;

        switch (eleSelectorType) {
        case TEXT:
            neededElementFinder = "new UiSelector().text(\"" + scrollToEle + "\")";
            break;
        case TEXT_CONTAINS:
            neededElementFinder = "new UiSelector().textContains(\"" + scrollToEle + "\")";
            break;
        case TEXT_STARTS_WITH:
            neededElementFinder = "new UiSelector().textStartsWith(\"" + scrollToEle + "\")";
            break;
        case ID:
            scrollToEleTrimmed = scrollToEle.replace("By.id:", "").trim();
            neededElementFinder = "new UiSelector().resourceId(\"" + scrollToEleTrimmed + "\")";
            break;
        case DESCRIPTION:
            neededElementFinder = "new UiSelector().description(\"" + scrollToEle + "\")";
            break;
        case DESCRIPTION_CONTAINS:
            neededElementFinder = "new UiSelector().descriptionContains(\"" + scrollToEle + "\")";
            break;
        case CLASS_NAME:
            scrollToEleTrimmed = scrollToEle.replace("By.className:", "").trim();
            neededElementFinder = "new UiSelector().className(\"" + scrollToEleTrimmed + "\")";
            break;
        default:
            UTILS_LOGGER.info("Please provide valid selectorType for element to be found...");
            break;
        }

        return neededElementFinder;
    }

    /**
     * Scroll Timeout check
     * 
     * @param startTime Long initial time for timeout count down
     **/
    default public void checkTimeout(long startTime) {
        long elapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;

        if (elapsed > SCROLL_TIMEOUT) {
            throw new NoSuchElementException("Scroll timeout has been reached..");
        }
    }

    /**
     * getCurrentDeviceFocus - get actual device apk in focus.
     *
     * @return String
     */
    default public String getCurrentDeviceFocus() {
        String result = executeAdbCommand("shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'");
        return result;
    }

    /**
     * executeAbdCommand
     *
     * @param command
     *            String
     * @return String command output in one line
     */
    default public String executeAdbCommand(String command) {
        String deviceName = getDevice().getAdbName();
        if (!deviceName.isEmpty()) {
            // add remoteURL/udid reference
            command = "-s " + deviceName + " " + command;
        } else {
            UTILS_LOGGER.warn("nullDevice detected fot current thread!");
        }

        String result = "";
        UTILS_LOGGER.info("Command: " + command);
        String[] listOfCommands = command.split(" ");

        String[] execCmd = CmdLine.insertCommandsAfter(baseInitCmd, listOfCommands);

        try {
            UTILS_LOGGER.info("Try to execute following cmd: " + CmdLine.arrayToString(execCmd));
            List<String> execOutput = executor.execute(execCmd);
            UTILS_LOGGER.info("Output after execution ADB command: " + execOutput);

            result = execOutput.toString().replaceAll("\\[|\\]", "").replaceAll(", ", " ").trim();

            UTILS_LOGGER.info("Returning Output: " + result);
        } catch (Exception e) {
            UTILS_LOGGER.error("Error while executing adb command: " + command, e);
        }

        return result;
    }

    /**
     * 
     * @param command
     * 
     *            - ADB shell command represented as single String where 1st literal
     *            is a command itself. Everything that follow is treated as
     *            arguments.
     *
     *            NOTE: "adb -s {UDID} shell" - should be omitted.
     *            Example: "adb -s {UDID} shell list packages" - list packages
     * 
     *            NOTE: shell arguments with space symbols are unsupported!
     * 
     * @return String - response (might be empty)
     */
    default public String executeShell(String command) {
        UTILS_LOGGER.info("ADB command to be executed: adb shell ".concat(command.trim()));
        List<String> literals = Arrays.asList(command.split(" "));
        return executeShell(literals);
    }

    /**
     * 
     * @param commands list of string commands
     * 
     *            - ADB shell command represented as single String where 1st literal
     *            is a command itself. Everything that follow is treated as
     *            arguments.
     *
     *            NOTE: "adb -s {UDID} shell" - should be omitted.
     *            Example: "adb -s {UDID} shell list packages" - list packages
     * 
     * @return String - response (might be empty)
     */
    default public String executeShell(List<String> commands) {
        JavascriptExecutor driver = null;
        try {
            driver = (JavascriptExecutor) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver does not support executeShell method", e);
        }

        String commandKeyWord = commands.get(0);
        List<String> args = commands.subList(1, commands.size());
        Map<String, Object> preparedCommand = ImmutableMap.of("command", commandKeyWord, "args", args);

        String output = driver.executeScript(SHELL_INIT_CONSOLE, preparedCommand)
                .toString();

        if (!StringUtils.isEmpty(output)) {
            UTILS_LOGGER.debug("ADB command output: {}}", output);
        }
        return output;
    }

    /**
     * This method performs an action corresponding to press Android device's native
     * button to show all recent applications.
     * 
     * NOTE: method could be used to get a list of running in background
     * applications with respect to particular device.
     */
    default public void displayRecentApps() {
        executeShell(SHELL_RECENT_APPS_CMD);
    }

    /**
     * Emulate tap at native 'Home' button.<br>
     * All applications will be closed to background.
     */
    default public void pressHome() {
        executeShell(SHELL_PRESS_HOME_CMD);
    }

    /**
     * Get GPS service status.
     * 
     * @return true if GPS enabled
     */
    default public boolean isGPSEnabled() {
        String response = executeShell(SHELL_GPS_STATUS_CMD);

        // Response reflects which services are used for obtaining location:
        // - "gps" - GPS only (device only);
        // - "gps,network" - GPS + Wi-Fi + Bluetooth or cellular networks (High accuracy mode);
        // - "network" - Using Wi-Fi, Bluetooth or cellular networks (Battery saving mode);
        return response.contains("gps");
    }

    /**
     * Enable GPS
     */
    default public void enableGPS() {
        executeShell(SHELL_ENABLE_GPS_CMD);
    }

    /**
     * Disable GPS<br>
     * Works if ONLY DEVICE (GPS sensor) is user for obtaining location
     */
    default public void disableGPS() {
        executeShell(SHELL_DISABLE_GPS_CMD);
    }

    /**
     * Save screenshot to specified folder on device's OS using provided path
     * 
     * @param filepath path to save screenshot to device's OS, for example {@code /storage/emulated/0/Download/scr.png}.
     */
    default public void takeScreenShot(String filepath) {
        UTILS_LOGGER.info("Screenshot will be saved to: {}", filepath);
        String command = String.format(SHELL_TAKE_SCREENSHOT_CMD.concat(" %s"), filepath);
        executeShell(command);
    }

    /**
     * Get app's version for the app that is already installed to devices, based on its package name.<br>
     *
     * @param packageName name of the package
     * @return appVersion version of app (versionCode from system dump), for example {@code 11200050}
     */
    default public String getAppVersion(String packageName) {
        String command = "dumpsys package ".concat(packageName);
        String output = executeShell(command);
        // we search for "versionCode" parameter in system dump.
        String versionCode = StringUtils.substringBetween(output, "versionCode=", " ");
        UTILS_LOGGER.info("Version code for '{}' package name is {}", packageName, versionCode);
        return versionCode;
    }

    /**
     * Get app's version name for the app that is already installed to devices, based on its package name<br>
     * 
     * @param packageName name of the package
     * @return version of app(versionName from system dump), for example {@code 11.2.0}
     */
    default public String getAppVersionName(String packageName) {
        String command = "dumpsys package ".concat(packageName);
        String output = this.executeShell(command);
        // we search for "versionName" parameter in system dump.
        String versionName = StringUtils.substringBetween(output, "versionName=", "\n");
        UTILS_LOGGER.info(String.format("Version name for '%s' package name is %s", packageName, versionName));
        return versionName;
    }

    /**
     * Open Android device native settings
     */
    default public void openDeviceSettings() {
        executeShell(SHELL_OPEN_DEVICE_SETTINGS_CMD);
    }

    /**
     * Reset test specific application by package name<br>
     * 
     * App's settings will be reset. User will be logged out. Application will be closed to background.
     * 
     * @param packageName name of the package
     */
    default public void clearAppCache(String packageName) {
        UTILS_LOGGER.info("Will clear data for the following app: {}", packageName);
        String command = String.format(SHELL_CLEAR_CACHE_CMD.concat(" %s"), packageName);
        String response = executeShell(command);
        UTILS_LOGGER.info("Output after resetting custom application by package ({}): {}", packageName, response);
        if (!response.contains("Success")) {
            UTILS_LOGGER.warn(String.format("App data was not cleared for %s app", packageName));
        }
    }

    /**
     * Trigger a deeplink (link to specific place within the application) or event open URL in mobile browser<br>
     * <br>
     * 
     * NOTE, that to open URL in browser, URL should start with "https://www.{place your link here}"<br>
     * NOTE that not all deeplinks require package name
     * 
     * @param link URL to trigger
     */
    default public void openURL(String link) {
        // TODO: #1380 make openURL call from this mobile interface in DriverHelper
        UTILS_LOGGER.info("Following link will be triggered via ADB: {}", link);
        String command = String.format(SHELL_OPEN_URL_CMD.concat(" %s"), link);
        executeShell(command);
    }

    /**
     * todo add more understandable description
     * With this method user is able to trigger a deeplink (link to specific place within the application)
     * 
     * @param link String
     * @param packageName String
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void triggerDeeplink(String link, String packageName) {
        Map<String, Object> preparedCommand = ImmutableMap.of("url", link, "package", packageName);

        JavascriptExecutor driver = null;
        try {
            driver = (JavascriptExecutor) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver does not support triggerDeeplink method", e);
        }

        try {
            driver.executeScript(SHELL_INIT_DEEPLINK_CONSOLE, preparedCommand);
        } catch (WebDriverException wde) {
            // TODO: need to pay attention
            UTILS_LOGGER.warn("org.openqa.selenium.WebDriverException is caught and ignored.", wde);
        }
    }

    /**
     * To get list of granted/denied/requested permission for specified application
     *
     * if response is not correct, return null
     *
     * @param packageName String
     * @param type PermissionType
     * @return ArrayList String
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    @SuppressWarnings("unchecked")
    default public List<String> getAppPermissions(String packageName, PermissionType type) {
        Map<String, Object> preparedCommand = ImmutableMap.of("type", type.getType(), "package", packageName);

        JavascriptExecutor driver = null;
        try {
            driver = (JavascriptExecutor) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver does not support getAppPermissions method", e);
        }

        Object responseAsObject = driver.executeScript(SHELL_INIT_GET_PERMISSION_CONSOLE, preparedCommand);
        List<String> responseValue = null;
        try {
            responseValue = (List<String>) responseAsObject;
        } catch (ClassCastException e) {
            UTILS_LOGGER.error("Cannot cast result of getAppPermissions method to the list. Possible reason that the response is not correct: {}",
                    responseAsObject);
            return null;
        }

        return responseValue;
    }

    /**
     * To change (grant or revoke) application permissions.
     * 
     * @param packageName String
     * @param action PermissionAction
     * @param permissions Permission
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void changePermissions(String packageName, PermissionAction action, Permission... permissions) {
        List<String> permissionsStr = Arrays.stream(permissions)
                .map(Permission::getPermission)
                .collect(Collectors.toList());

        JavascriptExecutor driver = null;
        try {
            driver = (JavascriptExecutor) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver does not support changePermissions method", e);
        }

        Map<String, Object> preparedCommand = ImmutableMap.of(
                "action", action.getAction(),
                "appPackage", packageName,
                "permissions", permissionsStr);

        driver.executeScript(SHELL_INIT_CHANGE_PERMISSION_CONSOLE, preparedCommand);
    }

    /**
     * Method to enter text to ACTIVATED input field.
     * 
     * NOTE: that it might be necessary to escape some special characters. Space-symbol is already escaped.
     * NOTE2: input field should be cleared previously.
     * 
     * @param text text to enter in the field
     */
    default public void typeWithADB(String text) {
        UTILS_LOGGER.info("Will enter '{}' to an active input field via ADB.", text);
        // In this method characters are entered one by one because sometimes some characters might be omitted if to enter whole text at a time.
        char[] array = text.toCharArray();
        for (char sym : array) {
            // todo refactor
            String ch = (sym == ' ') ? "%s" : String.valueOf(sym);
            String command = SHELL_INPUT_TXT_CMD + ch;
            executeShell(command);
        }
    }

    /**
     * Is airplane mode enabled or not
     * 
     * @return true if airplane mode is enabled
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean isAirplaneModeEnabled() {
        HasNetworkConnection driver = null;
        try {
            driver = (HasNetworkConnection) getDriver();
        } catch (ClassCastException e) {
            // todo add opportunity check is Airplane mode enabled via adb if we use RemoteWebDriver instead of AndroidDriver if possible
            throw new UnsupportedOperationException("Driver does not support isAirplaneModeEnabled method", e);
        }
        boolean enabled = driver.getConnection()
                .isAirplaneModeEnabled();
        UTILS_LOGGER.info("AirplaneMode enabled: {}", enabled);
        return enabled;
    }

    /**
     * Is Wi-Fi connection enabled or not
     *
     * @return true if Wi-Fi connection is enabled
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public boolean isWifiEnabled() {
        HasNetworkConnection driver = null;
        try {
            driver = (HasNetworkConnection) getDriver();
        } catch (ClassCastException e) {
            // todo add opportunity check is WiFi enabled via adb if we use RemoteWebDriver instead of AndroidDriver if possible
            throw new UnsupportedOperationException("Driver does not support isWifiEnabled method", e);
        }
        boolean enabled = driver.getConnection().isWiFiEnabled();
        UTILS_LOGGER.info("Wi-Fi enabled: {}", enabled);
        return enabled;
    }

    /**
     * Turns on Wi-Fi, if it's off
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void enableWifi() {
        boolean enabled = isWifiEnabled();
        if (enabled) {
            UTILS_LOGGER.info("Wifi is already enabled. No actions needed");
            return;
        }

        SupportsNetworkStateManagement driver = null;
        try {
            driver = (SupportsNetworkStateManagement) getDriver();
        } catch (ClassCastException e) {
            // todo add opportunity to turn on wifi via adb if we use RemoteWebDriver instead of AndroidDriver if possible
            throw new UnsupportedOperationException("Driver does not support enableWifi method", e);
        }

        driver.toggleWifi();
    }

    /**
     * Turns off Wi-Fi, if it's on
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void disableWifi() {
        boolean enabled = isWifiEnabled();
        if (!enabled) {
            UTILS_LOGGER.info("Wifi is already disabled. No actions needed");
            return;
        }

        SupportsNetworkStateManagement driver = null;
        try {
            driver = (SupportsNetworkStateManagement) getDriver();
        } catch (ClassCastException e) {
            // todo add opportunity to turn on wifi via adb if we use RemoteWebDriver instead of AndroidDriver if possible
            throw new UnsupportedOperationException("Driver does not support disableWifi method", e);
        }

        driver.toggleWifi();
    }

    /**
     * Method enters an App's menu within device System Settings
     * 
     * @param appName - Name of the app as it appears in the device's Apps list (Language specific)
     */
    default void openAppMenuFromDeviceSettings(String appName) {
        AndroidService androidService = AndroidService.getInstance();
        androidService.executeAdbCommand("shell am start -a android.settings.APPLICATION_SETTINGS");

        // initializing appItem with ExtendedWebElement constructor that initialize search context
        ExtendedWebElement appItem = new ExtendedWebElement(By.xpath(String.format("//*[contains(@text, '%s')]", appName)), "notifications",
                getDriver(), getDriver());
        swipe(appItem);

        appItem.click();
    }

    /**
     * Toggles a specified app's ability to recieve Push Notifications on the system level
     * 
     * @param appName - The app name as it appears within device System Settings
     * @param setValue - The value you wish to set the toggle to
     */
    default void toggleAppNotificationsFromDeviceSettings(String appName, boolean setValue) {
        openAppMenuFromDeviceSettings(appName);

        WebDriver driver = getDriver();
        // initializing with driver context
        ExtendedWebElement element = new ExtendedWebElement(By.xpath("//*[contains(@text, 'Notifications') or contains(@text, 'notifications')]"),
                "notifications", driver, driver);
        element.click();

        // initializing with driver context
        element = new ExtendedWebElement(By.xpath("//*[@resource-id='com.android.settings:id/switch_text']/following-sibling::android.widget.Switch"),
                "toggle", driver, driver);
        if (Boolean.valueOf(element.getAttribute("checked")) != setValue) {
            element.click();
        }
    }

    /**
     * @return - Returns if the device in use has a running LTE connection
     */
    default boolean isCarrierConnectionAvailable() {
        AndroidService androidService = AndroidService.getInstance();
        WebDriver driver = getDriver();

        boolean status = ((HasNetworkConnection) driver).getConnection().isDataEnabled();
        boolean linkProperties = false;

        String linkProp = androidService.executeAdbCommand("shell dumpsys telephony.registry | grep mPreciseDataConnectionState");
        UTILS_LOGGER.info("PROP:  " + linkProp);
        if (!linkProp.isEmpty()) {
            linkProperties = !StringUtils.substringBetween(linkProp, "APN: ", " ").equals("null");
        }
        UTILS_LOGGER.info("STATUS ENABLED: " + status);
        UTILS_LOGGER.info("CARRIER AVAILABLE: " + linkProperties);
        return ((HasNetworkConnection) driver).getConnection().isDataEnabled() && linkProperties;
    }

    /**
     * @return - Returns the value of the device model in use as a String
     */
    default String getDeviceModel() {
        AndroidService androidService = AndroidService.getInstance();
        return StringUtils.substringAfter(androidService.executeAdbCommand("shell getprop | grep 'ro.product.model'"), "ro.product.model: ");
    }

    default public void openStatusBar() {
        executeShell(SHELL_OPEN_STATUS_BAR_CMD);
    }

    default public void closeStatusBar() {
        executeShell(SHELL_CLOSE_STATUS_BAR_CMD);
    }

}
