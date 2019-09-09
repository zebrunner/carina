/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.AdbExecutor;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.utils.CmdLine;
import com.qaprosoft.carina.core.foundation.utils.common.CommonUtils;
import com.qaprosoft.carina.core.foundation.utils.mobile.IMobileUtils;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;

import io.appium.java_client.MobileBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.KeyEventFlag;
import io.appium.java_client.touch.offset.PointOption;

public interface IAndroidUtils extends IMobileUtils {
    
    //TODO: review carefully and remove duplicates and migrate completely to fluent waits
    static final Logger LOGGER = Logger.getLogger(IAndroidUtils.class);
    static final int SCROLL_MAX_SEARCH_SWIPES = 55;
    static final long SCROLL_TIMEOUT = 300;
    AdbExecutor executor = new AdbExecutor();
    String[] baseInitCmd = executor.getDefaultCmd();
    static final String LANGUAGE_CHANGE_APP_PATH = "app/ADB_Change_Language.apk";
    
    default public void pressKeyboardKey(AndroidKey key) {
        ((AndroidDriver<?>) castDriver()).pressKey(new KeyEvent(key)
                .withFlag(KeyEventFlag.SOFT_KEYBOARD).withFlag(KeyEventFlag.KEEP_TOUCH_MODE).withFlag(KeyEventFlag.EDITOR_ACTION));
    }
    
    default public void pressBack() {
        ((AndroidDriver<?>) castDriver()).pressKey(new KeyEvent(AndroidKey.BACK));
    }

    /**
     * Pressing "search" key of Android keyboard by coordinates.
     * <p>
     * Tested at Nexus 6P Android 8.0.0 standard keyboard. Coefficients of
     * coordinates for other devices and custom keyboards could be different.
     * <p>
     * Following options are not working: 1.
     * AndroidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_SEARCH); 2.
     * searchEditText.sendKeys("textToSearch" + "\n")
     */
    default public void pressSearchKey() {
        pressBottomRightKey();
    }

    default public void pressNextKey() {
        pressBottomRightKey();
    }

    
    //TODO Update this method using findByImage strategy
    /**
     * Pressing bottom right button on the keyboard by coordinates: "search",
     * "ok", "next", etc. - various keys appear at this position. Tested at
     * Nexus 6P Android 8.0.0 standard keyboard. Coefficients of coordinates for
     * other devices and custom keyboards could be different.
     */
    @SuppressWarnings("rawtypes")
    default public void pressBottomRightKey() {
        WebDriver driver = castDriver();
        Dimension size = helper.performIgnoreException(() -> driver.manage().window().getSize());
        int height =  size.getHeight();
        int width = size.getWidth();

        PointOption<?> option = PointOption.point(Double.valueOf(width * 0.915).intValue(), Double.valueOf(height * 0.945).intValue());
        new TouchAction((AndroidDriver<?>) castDriver()).tap(option).perform();
    }
    
    // Change Device Language section
    
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
     * @return boolean
     */
    default public boolean setDeviceLanguage(String language) {
        boolean status = setDeviceLanguage(language, true, 20);
        return status;
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
     *            params
     * @param waitTime int wait in seconds before device refresh.
     * @return boolean
     */
    default public boolean setDeviceLanguage(String language, boolean changeConfig, int waitTime) {
        boolean status = false;

        String initLanguage = language;

        String currentAndroidVersion = IDriverPool.getDefaultDevice().getOsVersion();

        LOGGER.info("Do not concat language for Android. Keep: " + language);
        language = language.replace("_", "-");
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
            CommonUtils.pause(waitTime);
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
                currentAndroidVersion = IDriverPool.getDefaultDevice().getOsVersion();
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
    default public String getDeviceLanguage() {
        String locale = executeAdbCommand("shell getprop persist.sys.language");
        if (locale.isEmpty()) {
            locale = executeAdbCommand("shell getprop persist.sys.locale");
        }
        return locale;
    }
    
    // End Language Change section
    
    /**
     * install android Apk by path to apk file.
     *
     * @param apkPath String
     */
    default public void installApk(final String apkPath) {
        installApk(apkPath, false);
    }

    /**
     * install android Apk by path to apk or by name in classpath.
     *
     * @param apkPath String
     * @param inClasspath boolean
     */
    default public void installApk(final String apkPath, boolean inClasspath) {

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
    
    default public boolean isChecked(final ExtendedWebElement element) {
        //TODO: SZ migrate to FluentWaits
        return element.isElementPresent(5)
                && (element.getElement().isSelected() || element.getAttribute("checked").equals("true"));
    }

    public enum SelectorType {
        TEXT, TEXT_CONTAINS, TEXT_STARTS_WITH, ID, DESCRIPTION, DESCRIPTION_CONTAINS, CLASS_NAME
    }

    /**
     * Scrolls into view in specified container by text only and return boolean
     *
     * @param container  ExtendedWebElement - defaults to id Selector Type
     * @param scrollToElement String defaults to text Selector Type
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer);
     **/
    default public ExtendedWebElement scroll(String scrollToElement, ExtendedWebElement container) {
        return scroll(scrollToElement, container, SelectorType.ID, SelectorType.TEXT);
    }

    /** Scrolls into view in a container specified by it's instance (index)
     * @param scrollToEle - has to be id, text, contentDesc or className
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param containerInstance - has to an instance number of desired container
     * @param eleSelectorType -  has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer, AndroidUtils.SelectorType.CLASS_NAME, 1,
     *                          AndroidUtils.SelectorType.TEXT);
     **/
    default public ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer, SelectorType containerSelectorType,
                          int containerInstance, SelectorType eleSelectorType) {
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        // TODO: support multi threaded WebDriver's removing DriverPool usage
        WebDriver drv = castDriver();

        //workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","),
                    ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
                By scrollBy = MobileBy.AndroidUIAutomator("new UiScrollable("
                        + getScrollContainerSelector(scrollableContainer, containerSelectorType) + ".instance("
                        + containerInstance + "))" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")"
                        + ".scrollIntoView(" + getScrollToElementSelector(scrollToEle, eleSelectorType) + ")");
                
                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    LOGGER.info("Element found!!!");
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                LOGGER.error(String.format("Element %s:%s was NOT found.", eleSelectorType, scrollToEle), noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                MobileBy.AndroidUIAutomator("new UiScrollable(" +
                        getScrollContainerSelector(scrollableContainer, containerSelectorType)
                        + ".instance("+ containerInstance + ")).scrollForward()");
                LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /** Scrolls into view in specified container
     * @param scrollToEle - has to be id, text, contentDesc or className
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param containerInstance - has to an instance number of desired container
     * @param eleSelectorType -  has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param eleSelectorInstance - has to an instance number of desired container
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer, AndroidUtils.SelectorType.CLASS_NAME, 1,
     *                          AndroidUtils.SelectorType.TEXT, 2);
     **/
    default public ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer, SelectorType containerSelectorType,
                          int containerInstance, SelectorType eleSelectorType, int eleSelectorInstance) {
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        // TODO: support multi threaded WebDriver's removing DriverPool usage
        WebDriver drv = castDriver();

        //workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","),
                    ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
                By scrollBy = MobileBy.AndroidUIAutomator("new UiScrollable("
                        + getScrollContainerSelector(scrollableContainer, containerSelectorType) + ".instance("
                        + containerInstance + "))" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")"
                        + ".scrollIntoView(" + getScrollToElementSelector(scrollToEle, eleSelectorType) + ".instance("
                        + eleSelectorInstance + "))");
                
                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    LOGGER.info("Element found!!!");
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                LOGGER.error(String.format("Element %s:%s was NOT found.", eleSelectorType, scrollToEle), noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                MobileBy.AndroidUIAutomator("new UiScrollable(" +
                        getScrollContainerSelector(scrollableContainer, containerSelectorType)
                        + ".instance("+ containerInstance + ")).scrollForward()");
                LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /** Scrolls into view in specified container
     * @param scrollToEle - has to be id, text, contentDesc or className
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - container Selector type: has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @param eleSelectorType -  scrollToEle Selector type: has to be id, text, textContains, textStartsWith, Description, DescriptionContains
     *                             or className
     * @return ExtendedWebElement
     * <p>
     * example of usage:
     * ExtendedWebElement res = AndroidUtils.scroll("News", newsListContainer, AndroidUtils.SelectorType.CLASS_NAME,
     *                          AndroidUtils.SelectorType.TEXT);
     **/
    default public ExtendedWebElement scroll(String scrollToEle, ExtendedWebElement scrollableContainer, SelectorType containerSelectorType,
                          SelectorType eleSelectorType){
        ExtendedWebElement extendedWebElement = null;
        long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        // TODO: support multi threaded WebDriver's removing DriverPool usage
        WebDriver drv = castDriver();

        //workaorund for appium issue: https://github.com/appium/appium/issues/10159
        if (scrollToEle.contains(",")) {
            scrollToEle = StringUtils.join(StringUtils.split(scrollToEle, ","),
                    ",", 0, 2);
            if (eleSelectorType.equals(SelectorType.TEXT)) {
                eleSelectorType = SelectorType.TEXT_CONTAINS;
            }
        }

        for (int i = 0; i < SCROLL_MAX_SEARCH_SWIPES; i++) {

            try {
                By scrollBy = MobileBy.AndroidUIAutomator(
                        "new UiScrollable(" + getScrollContainerSelector(scrollableContainer, containerSelectorType)
                                + ")" + ".setMaxSearchSwipes(" + SCROLL_MAX_SEARCH_SWIPES + ")" + ".scrollIntoView("
                                + getScrollToElementSelector(scrollToEle, eleSelectorType) + ")");
                
                WebElement ele = drv.findElement(scrollBy);
                if (ele.isDisplayed()) {
                    LOGGER.info("Element found!!!");
                    extendedWebElement = new ExtendedWebElement(scrollBy, scrollToEle, drv);
                    break;
                }
            } catch (NoSuchElementException noSuchElement) {
                LOGGER.error(String.format("Element %s:%s was NOT found.", eleSelectorType, scrollToEle), noSuchElement);
            }

            for (int j = 0; j < i; j++) {
                checkTimeout(startTime);
                MobileBy.AndroidUIAutomator("new UiScrollable(" +
                        getScrollContainerSelector(scrollableContainer, containerSelectorType) + ").scrollForward()");
                LOGGER.info("Scroller got stuck on a page, scrolling forward to next page of elements..");
            }
        }

        return extendedWebElement;
    }

    /** Scrolls into view in specified container
     * @param scrollableContainer - ExtendedWebElement type
     * @param containerSelectorType - Selector type: has to be id, text, contentDesc or className
     * @return scrollViewContainerFinder String	
     *
     **/
    default String getScrollContainerSelector(ExtendedWebElement scrollableContainer, SelectorType containerSelectorType){
        LOGGER.debug(scrollableContainer.getBy().toString());
        String scrollableContainerBy;
        String scrollViewContainerFinder = "";

        switch (containerSelectorType){
            case TEXT:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.text:", "").trim();
                scrollViewContainerFinder = "new UiSelector().text(\"" + scrollableContainerBy + "\")";
                break;
            case TEXT_CONTAINS:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.textContains:", "").trim();
                scrollViewContainerFinder = "new UiSelector().textContains(\"" + scrollableContainerBy + "\")";
                break;
            case TEXT_STARTS_WITH:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.textStartsWith:", "").trim();
                scrollViewContainerFinder = "new UiSelector().textStartsWith(\"" + scrollableContainerBy + "\")";
                break;
            case ID:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.id:", "").trim();
                scrollViewContainerFinder = "new UiSelector().resourceId(\"" + scrollableContainerBy + "\")";
                break;
            case DESCRIPTION:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.description:", "").trim();
                scrollViewContainerFinder = "new UiSelector().description(\"" + scrollableContainerBy + "\")";
                break;
            case DESCRIPTION_CONTAINS:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.descriptionContains:", "").trim();
                scrollViewContainerFinder = "new UiSelector().descriptionContains(\"" + scrollableContainerBy + "\")";
                break;
            case CLASS_NAME:
                scrollableContainerBy = scrollableContainer.getBy().
                        toString().replace("By.className:", "").trim();
                scrollViewContainerFinder = "new UiSelector().className(\"" + scrollableContainerBy + "\")";
                break;
            default:
                LOGGER.info("Please provide valid selectorType for element to be found...");
                break;
        }

        return scrollViewContainerFinder;

    }

    /** Scrolls into view in specified container
     * @param scrollToEle - String type
     * @param eleSelectorType - Selector type: has to be id, text, contentDesc or className
     * @return String
     **/
    default String getScrollToElementSelector(String scrollToEle, SelectorType eleSelectorType){
        String neededElementFinder = "";
        String scrollToEleTrimmed;

        switch (eleSelectorType){
            case TEXT:
                neededElementFinder= "new UiSelector().text(\"" + scrollToEle + "\")";
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
                LOGGER.info("Please provide valid selectorType for element to be found...");
                break;
        }

        return neededElementFinder;
    }

    /** Scroll Timeout check
     * @param startTime - Long initial time for timeout count down
     **/
    default public void checkTimeout(long startTime){
        long elapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())-startTime;

        if (elapsed > SCROLL_TIMEOUT) {
            throw new NoSuchElementException("Scroll timeout has been reached..");
        }
    }
    
    /**
     * getCurrentDeviceFocus - get actual device apk in focus
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
     * @param command String
     * @return String command output in one line
     */
    default public String executeAdbCommand(String command) {
        String deviceName = getDevice().getAdbName();
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
            LOGGER.error(e.getMessage(), e);
        }

        return result;
    }

}