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
package com.zebrunner.carina.utils.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
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
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.zebrunner.carina.webdriver.IDriverPool;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.android.Permissions.Permission;
import com.zebrunner.carina.utils.android.Permissions.PermissionAction;
import com.zebrunner.carina.utils.android.Permissions.PermissionType;
import com.zebrunner.carina.utils.android.recorder.utils.AdbExecutor;
import com.zebrunner.carina.utils.android.recorder.utils.CmdLine;
import com.zebrunner.carina.utils.common.CommonUtils;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.mobile.IMobileUtils;
import com.zebrunner.carina.utils.report.ReportContext;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidBatteryInfo;
import io.appium.java_client.android.AuthenticatesByFinger;
import io.appium.java_client.android.CanReplaceElementValue;
import io.appium.java_client.android.GsmCallActions;
import io.appium.java_client.android.GsmSignalStrength;
import io.appium.java_client.android.GsmVoiceState;
import io.appium.java_client.android.HasAndroidClipboard;
import io.appium.java_client.android.HasAndroidDeviceDetails;
import io.appium.java_client.android.HasAndroidSettings;
import io.appium.java_client.android.HasSupportedPerformanceDataType;
import io.appium.java_client.android.NetworkSpeed;
import io.appium.java_client.android.PowerACState;
import io.appium.java_client.android.StartsActivity;
import io.appium.java_client.android.SupportsNetworkStateManagement;
import io.appium.java_client.android.SupportsSpecialEmulatorCommands;
import io.appium.java_client.android.connection.HasNetworkConnection;
import io.appium.java_client.android.geolocation.AndroidGeoLocation;
import io.appium.java_client.android.geolocation.SupportsExtendedGeolocationCommands;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.KeyEventFlag;
import io.appium.java_client.android.nativekey.PressesKey;
import io.appium.java_client.battery.HasBattery;
import io.appium.java_client.clipboard.ClipboardContentType;

/**
 * Contains utility methods for working with android devices
 */
public interface IAndroidUtils extends IMobileUtils {
    // todo add methods from ListensToLogcatMessages
    // todo add methods from ExecuteCDPCommand
    // TODO: review carefully and remove duplicates and migrate completely to fluent waits
    static final Logger UTILS_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final int SCROLL_MAX_SEARCH_SWIPES = 55;
    static final long SCROLL_TIMEOUT = 300;
    static final AdbExecutor executor = new AdbExecutor();
    static final String[] baseInitCmd = executor.getDefaultCmd();
    static final String LANGUAGE_CHANGE_APP_PATH = "app/ADB_Change_Language.apk";

    /**
     * Send a key-press event to the keyboard
     *
     * @param key keyboard key, see {@link AndroidKey}
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     * 
     * @see <a href="https://android-developers.googleblog.com/2008/12/touch-mode.html">This method send key without leaving the touch-mode</a>
     */
    default public void pressKeyboardKey(AndroidKey key) {
        PressesKey driver = null;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pressKeyboardKey method", e);
        }

        driver.pressKey(new KeyEvent(key)
                .withFlag(KeyEventFlag.SOFT_KEYBOARD)
                .withFlag(KeyEventFlag.KEEP_TOUCH_MODE)
                .withFlag(KeyEventFlag.EDITOR_ACTION));
    }

    /**
     * Send a key-press event to the keyboard<br>
     *
     * @param key keyboard key, see {@link AndroidKey}
     * @param flags event flags, see {@link KeyEventFlag}
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void pressKeyboardKey(AndroidKey key, KeyEventFlag... flags) {
        PressesKey driver = null;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pressKeyboardKey method", e);
        }
        KeyEvent keyEvent = new KeyEvent(key);

        if (flags != null && flags.length > 0) {
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
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void pressKeyboardKeys(List<AndroidKey> keys) {
        PressesKey driver;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pressKeyboardKey(s) method", e);
        }

        keys.forEach(key -> driver.pressKey(new KeyEvent(key)));
    }

    /**
     * Send a long press key event to the device
     *
     * @param keyEvent The generated native key event
     */
    default public void longPressKey(KeyEvent keyEvent) {
        PressesKey driver;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support longPressKey method", e);
        }

        driver.longPressKey(keyEvent);
    }

    /**
     * Send a key-press {@link AndroidKey#BACK} event to the keyboard
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void pressBack() {
        PressesKey driver;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pressBack method", e);
        }
        driver.pressKey(new KeyEvent(AndroidKey.BACK));
    }

    /**
     * Send a key-press {@link AndroidKey#SEARCH} event to the keyboard
     * 
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void pressSearchKey() {
        PressesKey driver;
        try {
            driver = (PressesKey) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support pressSearchKey method", e);
        }
        driver.pressKey(new KeyEvent(AndroidKey.SEARCH));
    }

    /**
     * Press next key
     */
    default public void pressNextKey() {
        // todo investigate to use keyEvent with pressKey instead
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
        int deviceRefreshTimeSec = 20;
        return setDeviceLanguage(language, deviceRefreshTimeSec);
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
        UTILS_LOGGER.info("Actual Device Language: {}", actualDeviceLanguage);
        if (language.contains(actualDeviceLanguage.toLowerCase())
                || actualDeviceLanguage.toLowerCase().contains(language)) {
            status = true;
        } else {
            if (getDeviceLanguage().isEmpty()) {
                UTILS_LOGGER.info("Adb return empty response without errors.");
                status = true;
            } else {
                String currentAndroidVersion = IDriverPool.getDefaultDevice()
                        .getOsVersion();
                UTILS_LOGGER.info("currentAndroidVersion={}", currentAndroidVersion);
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
     * and
     * 
     * @return language, for example {@code fr}, or {@code fr-CA}
     * @deprecated this method calls adb bypassing the driver, so use {@link #getSystemDeviceLanguage()} instead
     */
    @Deprecated(forRemoval = true, since = "8.x")
    default public String getDeviceLanguage() {
        // get language only, for example 'fr'
        String locale = executeAdbCommand("shell getprop persist.sys.language");
        if (locale.isEmpty()) {
            // get locale, for example 'fr-CA'
            locale = executeAdbCommand("shell getprop persist.sys.locale");
        }
        return locale;
    }

    /**
     * Get the current language on the device
     *
     * @return language, for example {@code fr}, or {@code fr-CA}
     */
    default public String getSystemDeviceLanguage() {
        // get language only, for example 'fr'
        String locale = executeShell("getprop persist.sys.language").trim();
        if (locale.isEmpty()) {
            // get locale, for example 'fr-CA'
            locale = executeShell("getprop persist.sys.locale");
        }
        // executeShell return value like as 'fr-CA/n', so need to trim
        return locale.trim();
    }

    // End Language Change section

    /**
     * Install android Apk by path to apk file
     * 
     * @param apkPath path to apk
     */
    default public void installApk(final String apkPath) {
        installApk(apkPath, false);
    }

    /**
     * Install android Apk by path to apk or by name in classpath
     *
     * @param apkPath path to apk
     * @param inClasspath whether to search for apk in classpath
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
     * @param scrollToElement text to scroll to. Defaults to text Selector Type
     * @param container the element in which the text will be searched. Defaults to id Selector Type
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
     * @param scrollToEle has to be id, text, contentDesc or className
     * @param scrollableContainer ExtendedWebElement type
     * @param containerSelectorType has to be id, text, textContains, textStartsWith, Description, DescriptionContains or className
     * @param containerInstance has to an instance number of desired container
     * @param eleSelectorType has to be id, text, textContains, textStartsWith, Description, DescriptionContains or className
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
     * @param scrollToEle has to be id, text, contentDesc or className
     * @param scrollableContainer ExtendedWebElement type
     * @param containerSelectorType has to be id, text, textContains, textStartsWith, Description, DescriptionContains or className
     * @param containerInstance has to an instance number of desired container
     * @param eleSelectorType has to be id, text, textContains, textStartsWith, Description, DescriptionContains or className
     * @param eleSelectorInstance has to an instance number of desired container
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
     * @param scrollableContainer ExtendedWebElement type
     * @param containerSelectorType Selector type: has to be id, text, contentDesc or className
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
     * @param scrollToEle String type
     * @param eleSelectorType Selector type: has to be id, text, contentDesc or className
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
     * Get current pack in focus
     * 
     * @deprecated this method calls adb bypassing the driver, so use {@link #getCurrentPackage()} instead
     * @return String
     */
    @Deprecated(since = "8.x", forRemoval = true)
    default public String getCurrentDeviceFocus() {
        String result = executeAdbCommand("shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'");
        return result;
    }

    /**
     * Get current device package
     * 
     * @return current package name, for example {@code com.android.settings}
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public String getCurrentPackage() {
        StartsActivity startsActivity;
        try {
            startsActivity = (StartsActivity) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getCurrentPackage method", e);
        }
        return startsActivity.getCurrentPackage();
    }

    /**
     * execute ADB command bypassing the driver
     *
     * @param command adb command
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
        UTILS_LOGGER.info("Command: {}", command);
        String[] listOfCommands = command.split(" ");

        String[] execCmd = CmdLine.insertCommandsAfter(baseInitCmd, listOfCommands);

        try {
            UTILS_LOGGER.info("Try to execute following cmd: {}", CmdLine.arrayToString(execCmd));
            List<String> execOutput = executor.execute(execCmd);
            UTILS_LOGGER.info("Output after execution ADB command: {}", execOutput);

            result = execOutput.toString().replaceAll("\\[|\\]", "").replaceAll(", ", " ").trim();

            UTILS_LOGGER.info("Returning Output: {}", result);
        } catch (Exception e) {
            UTILS_LOGGER.error("Error while executing adb command: " + command, e);
        }

        return result;
    }

    /**
     * Execute android-specific commands throw driver using adb
     * 
     * @param command adb-shell command represented as single String where 1st literal is a command itself.
     *            Everything that follow is treated as arguments.<br>
     *
     *            <b>IMPORTANT</b>: "adb -s {UDID} shell" - <b>should be omitted</b> in {@code command} param.<br>
     *            Example: "adb -s {UDID} shell list packages" - list packages <br>
     * 
     *            <b>IMPORTANT</b>: shell arguments with space symbols are unsupported! Use {@link #executeShell(List)} instead
     * 
     * @return response (might be empty)
     * @throws UnsupportedOperationException if driver does not support this feature
     * 
     * @see <a href="https://github.com/appium/appium-uiautomator2-driver#platform-specific-extensions">Platform-specific extensions</a>
     */
    default public String executeShell(String command) {
        UTILS_LOGGER.info("ADB command to be executed: adb shell {}", command);
        List<String> literals = Arrays.asList(command.split(" "));
        return executeShell(literals);
    }

    /**
     * Execute android-specific commands throw driver using adb
     *
     * @param commands list of commands and arguments<br>
     *            adb-shell command represented as single String where 1st literal is a command itself.
     *            Everything that follow is treated as arguments.<br>
     * 
     *            <b>IMPORTANT</b>: "adb -s {UDID} shell" - <b>should be omitted</b> in {@code command} param.<br>
     *            Example: "adb -s {UDID} shell list packages" - list packages <br>
     * 
     * @return response (might be empty)
     * @throws UnsupportedOperationException if driver does not support this feature
     * 
     * @see <a href="https://github.com/appium/appium-uiautomator2-driver#platform-specific-extensions">Platform-specific extensions</a>
     */
    default public String executeShell(List<String> commands) {
        JavascriptExecutor driver = null;
        try {
            driver = (JavascriptExecutor) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver does not support executeShell method", e);
        }

        String command = commands.get(0);
        List<String> args = commands.subList(1, commands.size());
        Map<String, Object> preparedCommand = Map.of("command", command, "args", args);

        String output = driver.executeScript("mobile: shell", preparedCommand)
                .toString();

        if (!StringUtils.isEmpty(output)) {
            UTILS_LOGGER.debug("ADB command output: {}}", output);
        }
        return output;
    }

    /**
     * <b>For internal use only</b>
     * Start URI that may take users directly to the specific content in the app
     *
     * @param url the URL to start, for example {@code theapp://login/}
     * @param packageName the name of the package to start the URI with, for example {@code com.mycompany}
     * @return Response
     * @see <a href="https://appiumpro.com/editions/84-reliably-opening-deep-links-across-platforms-and-devices">
     *      Reliably Opening Deep Links Across Platforms and Devices</a>
     */
    default Response executeDeepLink(String url, String packageName) {
        WebDriver driver = getDriver();

        Map<String, Object> preparedCommand = Map.of("url", url, "package", packageName);
        return executeMobileScript(driver, "mobile: deepLink", preparedCommand);
    }

    /**
     * <b>For internal use only</b>
     *
     * Execute scripts
     * 
     * @param driver WebDriver instance
     * @param scriptType name of script type, for example {@code mobile:deepLink}
     */
    default Response executeMobileScript(WebDriver driver, String scriptType, Map<String, Object> arguments) {
        Map<String, ?> command = ImmutableMap.of(
                "script", scriptType, "args", arguments);

        ExecutesMethod executesMethod = null;

        try {
            executesMethod = (ExecutesMethod) driver;
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver does not support executeMobileScript method", e);
        }

        return executesMethod.execute(DriverCommand.EXECUTE_SCRIPT, command);
    }

    /**
     * Bring up the application switcher dialog
     */
    default public void displayRecentApps() {
        // todo investigate replace by pressKeyboardKey(AndroidKey.APP_SWITCH, null);
        executeShell("input keyevent KEYCODE_APP_SWITCH");
    }

    /**
     * Emulate tap at native 'Home' button.<br>
     * All applications will be closed to background.
     */
    default public void pressHome() {
        executeShell("input keyevent 3");
    }

    /**
     * Get GPS service status.
     * 
     * @return true if GPS enabled
     */
    default public boolean isGPSEnabled() {
        String response = executeShell("settings get secure location_providers_allowed");
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
        executeShell("settings put secure location_providers_allowed +gps");
    }

    /**
     * Disable GPS<br>
     * Works if ONLY DEVICE (GPS sensor) is user for obtaining location
     */
    default public void disableGPS() {
        executeShell("settings put secure location_providers_allowed -gps");
    }

    /**
     * Save screenshot to specified folder on device's OS using provided path
     * 
     * @param filepath path to save screenshot to device's OS, for example {@code /storage/emulated/0/Download/scr.png}.
     */
    default public void takeScreenShot(String filepath) {
        UTILS_LOGGER.info("Screenshot will be saved to: {}", filepath);
        executeShell(String.format("screencap -p %s", filepath));
    }

    /**
     * Get app's version for the app that is already installed to devices, based on its package name.<br>
     *
     * @param packageName name of the package
     * @return appVersion version of app (versionCode from system dump), for example {@code 11200050}
     */
    default public String getAppVersion(String packageName) {
        String output = executeShell("dumpsys package ".concat(packageName));
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
        UTILS_LOGGER.info("Version name for '{}' package name is {}", packageName, versionName);
        return versionName;
    }

    /**
     * Open android device native settings
     */
    default public void openDeviceSettings() {
        executeShell("am start -a android.settings.SETTINGS");
    }

    /**
     * Open development settings
     */
    default public void openDeveloperOptions() {
        executeShell("am start -n com.android.settings/.DevelopmentSettings");
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
        String response = executeShell(String.format("pm clear %s", packageName));
        UTILS_LOGGER.info("Output after resetting custom application by package ({}): {}", packageName, response);
        if (!response.contains("Success")) {
            UTILS_LOGGER.warn("App data was not cleared for {} app", packageName);
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
        UTILS_LOGGER.info("Following link will be triggered via ADB: {}", link);
        executeShell(String.format("am start -a android.intent.action.VIEW %s", link));
    }

    /**
     * Start URI that may take users directly to the specific content in the app
     *
     * @param url the URL to start, for example {@code theapp://login/}
     * @param packageName the name of the package to start the URI with, for example {@code com.mycompany}
     * @see <a href="https://appiumpro.com/editions/84-reliably-opening-deep-links-across-platforms-and-devices">
     *      Reliably Opening Deep Links Across Platforms and Devices</a>
     */
    default public void triggerDeeplink(String url, String packageName) {
        WebDriver driver = getDriver();
        try {
            executeDeepLink(url, packageName);
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
     * @param appPackage the application package to get permissions from, for example {@code }
     * @param type permission type. See {@link PermissionType}
     * @return ArrayList String
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    @SuppressWarnings("unchecked")
    default public List<String> getAppPermissions(String appPackage, PermissionType type) {
        Map<String, Object> preparedCommand = ImmutableMap.of("type", type.getType(), "package", appPackage);
        return (List<String>) executeMobileScript(getDriver(), "mobile: getPermissions", preparedCommand);
    }

    /**
     * Change package permissions in runtime
     * 
     * @param packageName String
     * @param action permission action, see {@link PermissionAction}
     * @param permissions list of permissions {@link Permission}
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void changePermissions(String packageName, PermissionAction action, Permission... permissions) {
        List<String> permissionsStr = Arrays.stream(permissions)
                .map(Permission::getPermission)
                .collect(Collectors.toList());

        Map<String, Object> preparedCommand = ImmutableMap.of(
                "action", action.getAction(),
                "appPackage", packageName,
                "permissions", permissionsStr);
        executeMobileScript(getDriver(), "mobile: changePermissions", preparedCommand);
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
            executeShell(String.format("input text %s", ch));
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
            throw new UnsupportedOperationException("Driver is not support isAirplaneModeEnabled method", e);
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
            throw new UnsupportedOperationException("Driver is not support isWifiEnabled method", e);
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
            throw new UnsupportedOperationException("Driver is not support enableWifi method", e);
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
            throw new UnsupportedOperationException("Driver is not support disableWifi method", e);
        }

        driver.toggleWifi();
    }

    /**
     * Method enters an App's menu within device System Settings
     * 
     * @param appName - Name of the app as it appears in the device's Apps list (Language specific)
     */
    default void openAppMenuFromDeviceSettings(String appName) {
        executeAdbCommand("shell am start -a android.settings.APPLICATION_SETTINGS");

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
        if (Boolean.parseBoolean(element.getAttribute("checked")) != setValue) {
            element.click();
        }
    }

    /**
     * @return - Returns if the device in use has a running LTE connection
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default boolean isCarrierConnectionAvailable() {
        HasNetworkConnection hasNetworkConnection = null;

        try {
            hasNetworkConnection = (HasNetworkConnection) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver does not support isCarrierConnectionAvailable method", e);
        }

        boolean status = hasNetworkConnection.getConnection()
                .isDataEnabled();
        boolean linkProperties = false;

        String linkProp = executeAdbCommand("shell dumpsys telephony.registry | grep mPreciseDataConnectionState");
        UTILS_LOGGER.info("PROP:  {}", linkProp);
        if (!linkProp.isEmpty()) {
            linkProperties = !StringUtils.substringBetween(linkProp, "APN: ", " ").equals("null");
        }
        UTILS_LOGGER.info("STATUS ENABLED: {}", status);
        UTILS_LOGGER.info("CARRIER AVAILABLE: {}", linkProperties);
        return hasNetworkConnection.getConnection().isDataEnabled() && linkProperties;
    }

    /**
     * Get device model<br>
     *
     * <b>Important: </b> before carina 8.x this method calls adb bypassing the driver,
     * but now it gets device model using driver
     * 
     * @return device model, for example {@code G3112}
     */
    default public String getDeviceModel() {
        // executeShell returns model with \n, for example G3112\n, so need to trim
        return executeShell("getprop ro.product.model")
                .trim();
    }

    default public void openStatusBar() {
        executeShell("cmd statusbar expand-notifications");
    }

    default public void closeStatusBar() {
        executeShell("cmd statusbar collapse");
    }

    /**
     * Get device timezone
     * 
     * @return device timezone, for example {@code Europe/Moscow}
     */
    default public String getDeviceTimezone() {
        // executeShell returns timezone with \n, for example Europe/Moscow\n, so need to trim
        return executeShell("getprop persist.sys.timezone")
                .trim();
    }

    /**
     * Set android device default timezone and language based on config or to GMT and En
     * without restoring actual focused apk
     */
    default public void setDeviceDefaultTimeZoneLanguage() {
        setDeviceDefaultTimeZoneLanguage(false);
    }

    /**
     * Set default timezone and language based on config or to GMT and En
     *
     * @param returnAppFocus - if true store actual Focused apk and activity, than restore after setting Timezone and Language.
     */
    default public void setDeviceDefaultTimeZoneLanguage(boolean returnAppFocus) {
        try {
            Activity activity = null;
            String os = IDriverPool.getDefaultDevice().getOs();
            if (os.equalsIgnoreCase(SpecialKeywords.ANDROID)) {

                AndroidService androidService = AndroidService.getInstance();

                if (returnAppFocus) {
                    activity = new Activity(getCurrentPackage(), getCurrentActivity());
                }

                String deviceTimezone = Configuration.get(Configuration.Parameter.DEFAULT_DEVICE_TIMEZONE);
                String deviceTimeFormat = Configuration.get(Configuration.Parameter.DEFAULT_DEVICE_TIME_FORMAT);
                String deviceLanguage = Configuration.get(Configuration.Parameter.DEFAULT_DEVICE_LANGUAGE);

                DeviceTimeZone.TimeFormat timeFormat = DeviceTimeZone.TimeFormat.parse(deviceTimeFormat);
                DeviceTimeZone.TimeZoneFormat timeZone = DeviceTimeZone.TimeZoneFormat.parse(deviceTimezone);

                UTILS_LOGGER.info("Set device timezone to {}", timeZone);
                UTILS_LOGGER.info("Set device time format to {}", timeFormat);
                UTILS_LOGGER.info("Set device language to {}", deviceLanguage);

                boolean timeZoneChanged = androidService.setDeviceTimeZone(timeZone.getTimeZone(), timeZone.getSettingsTZ(), timeFormat);
                boolean languageChanged = setDeviceLanguage(deviceLanguage);

                UTILS_LOGGER.info("Device TimeZone was changed to timeZone '{}' : {}. Device Language was changed to language '{}': {}",
                        deviceTimezone,
                        timeZoneChanged, deviceLanguage, languageChanged);

                if (returnAppFocus) {
                    androidService.startActivity(activity);
                }

            } else {
                UTILS_LOGGER.info("Current OS is {}. But we can set default TimeZone and Language only for Android.", os);
            }
        } catch (Exception e) {
            UTILS_LOGGER.error("Error while setting to device default timezone and language!", e);
        }
    }

    /**
     * Retrieves battery info from the device under test
     *
     * @return BatteryInfo instance, containing the battery information
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public AndroidBatteryInfo getBatteryInfo() {
        HasBattery<AndroidBatteryInfo> driver = null;
        try {
            driver = (HasBattery<AndroidBatteryInfo>) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getBatteryInfo method", e);
        }
        return driver.getBatteryInfo();
    }

    /**
     * This method should start arbitrary activity during a test. If the activity belongs to
     * another application, that application is started and the activity is opened.
     * <p>
     * Usage:
     * </p>
     * 
     * <pre>
     * {
     *     &#64;code
     *     Activity activity = new Activity("app package goes here", "app activity goes here");
     *     activity.setWaitAppPackage("app wait package goes here");
     *     activity.setWaitAppActivity("app wait activity goes here");
     *     driver.startActivity(activity);
     * }
     * </pre>
     *
     * @param activity The {@link Activity} object
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void startActivity(Activity activity) {
        StartsActivity driver = null;
        try {
            driver = (StartsActivity) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support startActivity method", e);
        }
        driver.startActivity(activity);
    }

    /**
     * Get the current activity being run on the mobile device
     *
     * @return a current activity being run on the mobile device
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public String getCurrentActivity() {
        StartsActivity driver = null;
        try {
            driver = (StartsActivity) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getCurrentActivity method", e);
        }
        return driver.currentActivity();
    }

    /**
     * Get the current activity being run on the mobile device with package name (apkPackage/apkActivity)
     *
     * @return {@code apkPackage/apkActivity} string
     */
    default public String getCurrentPackageActivity() {
        StartsActivity driver = null;
        try {
            driver = (StartsActivity) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getCurrentPackageActivity method", e);
        }
        return driver.getCurrentPackage() + "/" + driver.currentActivity();
    }

    /**
     * Retrieve the display density of the Android device
     * 
     * @return The density value in dpi
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public Long getDisplayDensity() {
        HasAndroidDeviceDetails driver = null;
        try {
            driver = (HasAndroidDeviceDetails) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getDisplayDensity method", e);
        }
        return driver.getDisplayDensity();
    }

    /**
     * Retrieve visibility and bounds information of the status and navigation bars
     * 
     * @return The map where keys are bar types and values are mappings of bar properties
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public Map<String, Map<String, Object>> getSystemBars() {
        HasAndroidDeviceDetails driver = null;
        try {
            driver = (HasAndroidDeviceDetails) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getSystemBars method", e);
        }
        return driver.getSystemBars();
    }

    /**
     * returns the information type of the system state which is supported to read
     * as like cpu, memory, network traffic, and battery
     * 
     * @return output - array like below
     *         [cpuinfo, batteryinfo, networkinfo, memoryinfo]
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public List<String> getSupportedPerformanceDataTypes() {
        HasSupportedPerformanceDataType driver = null;
        try {
            driver = (HasSupportedPerformanceDataType) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getSupportedPerformanceDataTypes method", e);
        }
        return driver.getSupportedPerformanceDataTypes();
    }

    /**
     * returns the resource usage information of the application. the resource is one of the system state
     * which means cpu, memory, network traffic, and battery
     *
     * @param packageName the package name of the application
     * @param dataType the type of system state which wants to read.
     *            It should be one of the supported performance data types,
     *            the return value of the function "getSupportedPerformanceDataTypes"
     * @param dataReadTimeout the number of attempts to read
     * @return table of the performance data, The first line of the table represents the type of data.
     *         The remaining lines represent the values of the data.
     *         in case of battery info : [[power], [23]]
     *         in case of memory info :
     *         [[totalPrivateDirty, nativePrivateDirty, dalvikPrivateDirty, eglPrivateDirty, glPrivateDirty,
     *         totalPss, nativePss, dalvikPss, eglPss, glPss, nativeHeapAllocatedSize, nativeHeapSize],
     *         [18360, 8296, 6132, null, null, 42588, 8406, 7024, null, null, 26519, 10344]]
     *         in case of network info :
     *         [[bucketStart, activeTime, rxBytes, rxPackets, txBytes, txPackets, operations, bucketDuration,],
     *         [1478091600000, null, 1099075, 610947, 928, 114362, 769, 0, 3600000],
     *         [1478095200000, null, 1306300, 405997, 509, 46359, 370, 0, 3600000]]
     *         in case of network info :
     *         [[st, activeTime, rb, rp, tb, tp, op, bucketDuration],
     *         [1478088000, null, null, 32115296, 34291, 2956805, 25705, 0, 3600],
     *         [1478091600, null, null, 2714683, 11821, 1420564, 12650, 0, 3600],
     *         [1478095200, null, null, 10079213, 19962, 2487705, 20015, 0, 3600],
     *         [1478098800, null, null, 4444433, 10227, 1430356, 10493, 0, 3600]]
     *         in case of cpu info : [[user, kernel], [0.9, 1.3]]
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default List<List<Object>> getPerformanceData(String packageName, String dataType, int dataReadTimeout) {
        HasSupportedPerformanceDataType driver = null;
        try {
            driver = (HasSupportedPerformanceDataType) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support getPerformanceData method", e);
        }
        return driver.getPerformanceData(packageName, dataType, dataReadTimeout);
    }

    /**
     * Authenticate users by using their finger print scans on supported emulators
     *
     * @param fingerPrintId finger prints stored in Android Keystore system (from 1 to 10)
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void authByFingerPrint(int fingerPrintId) {
        AuthenticatesByFinger driver = null;
        try {
            driver = (AuthenticatesByFinger) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support authByFingerPrint method", e);
        }
        driver.fingerPrint(fingerPrintId);
    }

    /**
     * Emulate send SMS event on the connected emulator
     *
     * @param phoneNumber The phone number of message sender
     * @param message The message content
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void sendSMS(String phoneNumber, String message) {
        SupportsSpecialEmulatorCommands driver = null;
        try {
            driver = (SupportsSpecialEmulatorCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support sendSMS method", e);
        }
        driver.sendSMS(phoneNumber, message);
    }

    /**
     * Emulate GSM call event on the connected emulator
     *
     * @param phoneNumber The phone number of the caller
     * @param gsmCallActions One of available {@link GsmCallActions} values
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void makeGsmCall(String phoneNumber, GsmCallActions gsmCallActions) {
        SupportsSpecialEmulatorCommands driver = null;
        try {
            driver = (SupportsSpecialEmulatorCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support makeGsmCall method", e);
        }
        driver.makeGsmCall(phoneNumber, gsmCallActions);
    }

    /**
     * Emulate GSM signal strength change event on the connected emulator
     *
     * @param gsmSignalStrength One of available {@link GsmSignalStrength} values
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setGsmSignalStrength(GsmSignalStrength gsmSignalStrength) {
        SupportsSpecialEmulatorCommands driver = null;
        try {
            driver = (SupportsSpecialEmulatorCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setGsmSignalStrength method", e);
        }
        driver.setGsmSignalStrength(gsmSignalStrength);
    }

    /**
     * Emulate GSM voice event on the connected emulator
     *
     * @param gsmVoiceState One of available {@link GsmVoiceState} values
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setGsmVoice(GsmVoiceState gsmVoiceState) {
        SupportsSpecialEmulatorCommands driver = null;
        try {
            driver = (SupportsSpecialEmulatorCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setGsmVoice method", e);
        }
        driver.setGsmVoice(gsmVoiceState);
    }

    /**
     * Emulate network speed change event on the connected emulator
     *
     * @param networkSpeed One of available {@link NetworkSpeed} values
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setNetworkSpeed(NetworkSpeed networkSpeed) {
        SupportsSpecialEmulatorCommands driver = null;
        try {
            driver = (SupportsSpecialEmulatorCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setNetworkSpeed method", e);
        }
        driver.setNetworkSpeed(networkSpeed);
    }

    /**
     * Emulate power capacity change on the connected emulator
     *
     * @param percent Percentage value in range [0, 100]
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setPowerCapacity(int percent) {
        SupportsSpecialEmulatorCommands driver = null;
        try {
            driver = (SupportsSpecialEmulatorCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setPowerCapacity method", e);
        }
        driver.setPowerCapacity(percent);
    }

    /**
     * Emulate power state change on the connected emulator
     *
     * @param powerACState One of available {@link PowerACState} values
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setPowerAC(PowerACState powerACState) {
        SupportsSpecialEmulatorCommands driver = null;
        try {
            driver = (SupportsSpecialEmulatorCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setPowerAC method", e);
        }
        driver.setPowerAC(powerACState);
    }

    /**
     * Set the content of device's clipboard
     *
     * @param label clipboard data label
     * @param contentType one of supported content types
     * @param base64Content base64-encoded content to be set
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setClipboard(String label, ClipboardContentType contentType, byte[] base64Content) {
        HasAndroidClipboard driver = null;
        try {
            driver = (HasAndroidClipboard) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setClipboard method", e);
        }
        driver.setClipboard(label, contentType, base64Content);
    }

    /**
     * Set the clipboard text
     *
     * @param label clipboard data label
     * @param text The actual text to be set
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void setClipboardText(String label, String text) {
        setClipboard(label, ClipboardContentType.PLAINTEXT, Base64
                .getEncoder()
                .encode(text.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Replaces element value with the given one
     *
     * @param element The destination element
     * @param value The value to set
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default public void replaceElementValue(RemoteWebElement element, String value) {
        CanReplaceElementValue driver = null;
        try {
            driver = (CanReplaceElementValue) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support replaceElementValue method", e);
        }
        driver.replaceElementValue(element, value);
    }

    /**
     * Allows to set geo location with extended parameters available for Android platform
     *
     * @param location the location object to set, see {@link AndroidGeoLocation}
     * @throws UnsupportedOperationException if driver does not support this feature
     */
    default void setLocation(AndroidGeoLocation location) {
        SupportsExtendedGeolocationCommands driver = null;
        try {
            driver = (SupportsExtendedGeolocationCommands) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setLocation method", e);
        }
        driver.setLocation(location);
    }

    /**
     * Switch to focused app to interact with it (be it a native app or a browser)<br>
     * Application will not be restarted<br>
     */
    default public void switchToApp() {
        switchToApp(getCurrentPackage(), getCurrentActivity(), false);
    }

    /**
     * Switch to focused app to interact with it (be it a native app or a browser)<br>
     *
     * @param isRerun set true if you want to reopen app
     */
    default public void switchToApp(boolean isRerun) {
        switchToApp(getCurrentPackage(), getCurrentActivity(), isRerun);
    }

    /**
     * Switch to another app to interact with it (be it a native app or a browser)<br>
     * If you need more control over the activity settings of the launched application, use {@link #startActivity(Activity)}
     *
     * @param packageName name of the package, for example {@code com.solvd.carinademoapplication}
     * @param activityName name of the activity in app, for example {@code .ActivityTestScreens}
     * @param isRerun set true if you want to reopen app
     */
    default public void switchToApp(String packageName, String activityName, boolean isRerun) {
        Activity activity = new Activity(packageName, activityName);
        activity.setAppWaitPackage(packageName);
        activity.setAppWaitActivity(activityName);
        activity.setStopApp(isRerun);
        startActivity(activity);
    }

    /**
     * Set the `ignoreUnimportantViews` setting. *Android-only method*.
     * Sets whether Android devices should use `setCompressedLayoutHeirarchy()`
     * which ignores all views which are marked IMPORTANT_FOR_ACCESSIBILITY_NO
     * or IMPORTANT_FOR_ACCESSIBILITY_AUTO (and have been deemed not important
     * by the system), in an attempt to make things less confusing or faster.
     *
     * @param compress ignores unimportant views if true, doesn't ignore otherwise.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings ignoreUnimportantViews(Boolean compress) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support ignoreUnimportantViews method", e);
        }
        return driver.ignoreUnimportantViews(compress);
    }

    /**
     * Invoke {@code setWaitForIdleTimeout} in {@code com.android.uiautomator.core.Configurator}.
     *
     * @param timeout a negative value would reset to its default value. Minimum time unit
     *            resolution is one millisecond.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings configuratorSetWaitForIdleTimeout(Duration timeout) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support configuratorSetWaitForIdleTimeout method", e);
        }
        return driver.configuratorSetWaitForIdleTimeout(timeout);
    }

    /**
     * Invoke {@code setWaitForSelectorTimeout} in {@code com.android.uiautomator.core.Configurator}.
     *
     * @param timeout a negative value would reset to its default value. Minimum time unit
     *            resolution is one millisecond.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings configuratorSetWaitForSelectorTimeout(Duration timeout) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support configuratorSetWaitForSelectorTimeout method", e);
        }
        return driver.configuratorSetWaitForSelectorTimeout(timeout);
    }

    /**
     * Invoke {@code setScrollAcknowledgmentTimeout} in {@code com.android.uiautomator.core.Configurator}.
     *
     * @param timeout a negative value would reset to its default value. Minimum time unit
     *            resolution is one millisecond
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings configuratorSetScrollAcknowledgmentTimeout(Duration timeout) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support configuratorSetScrollAcknowledgmentTimeout method", e);
        }
        return driver.configuratorSetScrollAcknowledgmentTimeout(timeout);
    }

    /**
     * Invoke {@code configuratorSetKeyInjectionDelay} in {@code com.android.uiautomator.core.Configurator}.
     *
     * @param delay a negative value would reset to its default value. Minimum time unit
     *            resolution is one millisecond.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings configuratorSetKeyInjectionDelay(Duration delay) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support configuratorSetKeyInjectionDelay method", e);
        }
        return driver.configuratorSetKeyInjectionDelay(delay);
    }

    /**
     * Invoke {@code setActionAcknowledgmentTimeout} in {@code com.android.uiautomator.core.Configurator}.
     *
     * @param timeout a negative value would reset to its default value. Minimum time unit
     *            resolution is one millisecond
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings configuratorSetActionAcknowledgmentTimeout(Duration timeout) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support configuratorSetActionAcknowledgmentTimeout method", e);
        }
        return driver.configuratorSetActionAcknowledgmentTimeout(timeout);
    }

    /**
     * Setting this value to true will enforce source tree dumper
     * to transliterate all class names used as XML tags to the limited
     * set of ASCII characters supported by Apache Harmony
     * lib and used by default in Android to avoid possible
     * XML parsing exceptions caused by XPath lookup.
     * The Unicode to ASCII transliteration is based on
     * JUnidecode library (https://github.com/gcardone/junidecode).
     * Works for UIAutomator2 only.
     *
     * @param enabled either true or false. The default value if false.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings normalizeTagNames(boolean enabled) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support normalizeTagNames method", e);
        }
        return driver.normalizeTagNames(enabled);
    }

    /**
     * Whether to return compact (standards-compliant) and faster responses in find element/s
     * (the default setting). If set to false then the response may also contain other
     * available element attributes.
     *
     * @param enabled Either true or false. The default value if true.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings setShouldUseCompactResponses(boolean enabled) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setShouldUseCompactResponses method", e);
        }
        return driver.setShouldUseCompactResponses(enabled);
    }

    /**
     * Which attributes should be returned if compact responses are disabled.
     * It works only if shouldUseCompactResponses is false. Defaults to "" (empty string).
     *
     * @param attrNames the comma-separated list of fields to return with each element.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings setElementResponseAttributes(String attrNames) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setElementResponseAttributes method", e);
        }
        return driver.setElementResponseAttributes(attrNames);
    }

    /**
     * Set whether the source output/xpath search should consider all elements, visible and invisible.
     * Disabling this setting speeds up source and xml search. Works for UIAutomator2 only.
     *
     * @param enabled Either true or false. The default value if false.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings allowInvisibleElements(boolean enabled) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support allowInvisibleElements method", e);
        }
        return driver.allowInvisibleElements(enabled);
    }

    /**
     * Whether to enable or disable the notification listener.
     * No toast notifications are going to be added into page source output if
     * this setting is disabled.
     * Works for UIAutomator2 only.
     *
     * @param enabled either true or false. The default value if true.
     * @return {@link HasAndroidSettings} instance for chaining
     */
    public default HasAndroidSettings enableNotificationListener(boolean enabled) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support enableNotificationListener method", e);
        }
        return driver.enableNotificationListener(enabled);
    }

    /**
     * Whether to enable or disable shutdown the server through
     * the broadcast receiver on ACTION_POWER_DISCONNECTED.
     *
     * @param enabled either true or false. The default value if true.
     * @return {@link HasAndroidSettings} instance for chaining
     */
    public default HasAndroidSettings shutdownOnPowerDisconnect(boolean enabled) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support shutdownOnPowerDisconnect method", e);
        }
        return driver.shutdownOnPowerDisconnect(enabled);
    }

    /**
     * Turn on or off the tracking of scroll events as they happen.
     * If {@code true}, a field {@code lastScrollData} is added to the results of
     * {@code getSession}, which can then be used to check on scroll progress.
     * Turning this feature off significantly increases touch action performance.
     *
     * @param enabled either true or false. The default value if true.
     * @return {@link HasAndroidSettings} instance for chaining.
     */
    public default HasAndroidSettings setTrackScrollEvents(boolean enabled) {
        HasAndroidSettings driver = null;
        try {
            driver = (HasAndroidSettings) getDriver();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Driver is not support setTrackScrollEvents method", e);
        }
        return driver.setTrackScrollEvents(enabled);
    }

}
