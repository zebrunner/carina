/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.resources;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.resources.L10NpropsReader;
import com.qaprosoft.carina.core.foundation.utils.resources.LocaleReader;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.ExtendedWebElement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;

/*
 * http://maven.apache.org/surefire/maven-surefire-plugin/examples/class-loading.html
 * Need to set useSystemClassLoader=false for maven surefire plugin to receive access to classloader L10N files on CI
 * <plugin>
 * <groupId>org.apache.maven.plugins</groupId>
 * <artifactId>maven-surefire-plugin</artifactId>
 * <version>3.0.0-M4</version>
 * <configuration>
 * <useSystemClassLoader>false</useSystemClassLoader>
 * </configuration>
 */

public class L10Nnew {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Locale actualLocale;

    public static String assertErrorMsg = "";

    public static LinkedList<String> newLocList = new LinkedList<String>();

    public static Properties prop = new Properties();

    public static String propFileName = "";

    private static String encoding = "UTF-8";

    protected static final int BASIC_WAIT_SHORT_TIMEOUT = 5;

    public static void init() {
        L10NpropsReader.init();
        L10Nnew.setActualLocale(Configuration.get(Configuration.Parameter.LOCALE));
    }

    public static void resourcesToProperties(Map <String, String> resources) {
        resourcesToProperties(resources, actualLocale.toString());
    }

    public static void resourcesToProperties(Map<String, String> resources, String locale) {

        String localePath = String.format("./src/main/resources/%s/locale_%s.properties", SpecialKeywords.L10N, locale);
        boolean fileExists = new File(localePath).exists();

        Properties properties = new Properties();

        InputStreamReader reader = null;
        if (fileExists) {
            try {
                reader = new InputStreamReader(
                        new FileInputStream(localePath), encoding);

                properties.load(reader);
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (String key : resources.keySet()) {
            if (!properties.containsKey(key)) {
                properties.put(key, resources.get(key));
            }
        }

        if (properties.size() < 1) {
            LOGGER.debug("No key-value pairs were passed, returning...");
            return;
        } else {
            try {
                properties.store(
                        new OutputStreamWriter(
                                new FileOutputStream(localePath), encoding), null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * getText by key for default locale.
     *
     * @param key
     *            - String
     *
     * @return String
     */
    public static String getText(String key) {
        return L10NpropsReader.getText(key);
    }

    /**
     * getText for specified locale and key.
     *
     * @param key
     *            - String
     * @param locale
     *            - Locale
     * @return String
     */
    public static String getText(String key, Locale locale) {
        return L10NpropsReader.getText(key, locale);
    }

    /*
     * This method helps when translating strings that have single quote or other special characters that get omitted.
     */
    public static String formatString(String resource, String... parameters) {
        for (int i = 0; i < parameters.length; i++) {
            resource = resource.replace("{" + i + "}", parameters[i]);
            LOGGER.debug("Localized string value is: " + resource);
        }
        return resource;
    }

    /*
     * Make sure you remove the single quotes around %s in xpath as string
     * returned will either have it added for you or single quote won't be
     * added as concat() doesn't need them.
     */
    public static String generateConcatForXPath(String xpathString) {
        String returnString = "";
        String searchString = xpathString;
        char[] quoteChars = new char[] { '\'', '"' };

        int quotePos = StringUtils.indexOfAny(searchString, quoteChars);
        if (quotePos == -1) {
            returnString = "'" + searchString + "'";
        } else {
            returnString = "concat(";
            LOGGER.debug("Current concatenation: " + returnString);
            while (quotePos != -1) {
                String subString = searchString.substring(0, quotePos);
                returnString += "'" + subString + "', ";
                LOGGER.debug("Current concatenation: " + returnString);
                if (searchString.substring(quotePos, quotePos + 1).equals("'")) {
                    returnString += "\"'\", ";
                    LOGGER.debug("Current concatenation: " + returnString);
                } else {
                    returnString += "'\"', ";
                    LOGGER.debug("Current concatenation: " + returnString);
                }
                searchString = searchString.substring(quotePos + 1,
                        searchString.length());
                quotePos = StringUtils.indexOfAny(searchString, quoteChars);
            }
            returnString += "'" + searchString + "')";
            LOGGER.debug("Concatenation result: " + returnString);
        }
        return returnString;
    }

    //Parser part
    /**
     * get Actual Locale
     *
     * @return Locale
     */
    public static Locale getActualLocale() {
        return actualLocale;
    }

    /**
     * get AssertErrorMsg
     *
     * @return String
     */
    public static String getAssertErrorMsg() {
        return assertErrorMsg;
    }

    public static void assertAll(){
        boolean isCorrect = true;
        String tmp = assertErrorMsg;

        if (assertErrorMsg.length() > 0){
            assertErrorMsg = "";
            isCorrect = false;
        }

        Assert.assertTrue(isCorrect, tmp);
    }

    /**
     * set Actual Locale
     *
     * @param countryCode String
     */
    public static void setActualLocale(String countryCode) {
        List<Locale> locales = LocaleReader.init(Configuration.get(Parameter.LOCALE));
        Locale locale = locales.get(0);
        try {
            String[] localeSetttings = countryCode.split("_");
            String lang, country = "";
            lang = localeSetttings[0];
            if (localeSetttings.length > 1) {
                country = localeSetttings[1];
            }
            locale = new Locale(lang, country);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        setActualLocale(locale);
    }

    /**
     * set Actual Locale
     *
     * @param locale - Locale
     */
    public static void setActualLocale(Locale locale) {
        LOGGER.info("Set actual Locale to " + locale);
        actualLocale = locale;

        propFileName = getPropertyFileName(actualLocale.toString());
        LOGGER.info("propFileName:=" + propFileName);

        boolean exists = new File(propFileName).exists();

        if (exists) {
            InputStreamReader reader = null;
            try {
                 reader = new InputStreamReader(
                    new FileInputStream(propFileName), encoding);
                prop.load(reader);
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * get Property FileName
     *
     * @param localName - String
     * @return String with path + PropertyFileName
     */
    private static String getPropertyFileName(String localName) {
        final String L10N_NEW_LOCALE_PATH = String.format("./src/main/resources/%s/", SpecialKeywords.L10N);

        File file = new File(L10N_NEW_LOCALE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }

        String ret = L10N_NEW_LOCALE_PATH + "new_locale_" + localName + ".properties";

        return ret;
    }

    /**
     * check Localization Text. Will work ONLY if locKey is equal to element
     * Name and element is Public
     *
     * @param elem ExtendedWebElement
     * @return boolean
     */
    public static boolean checkLocalizationText(ExtendedWebElement elem) {
        return checkLocalizationText(elem, true, BASIC_WAIT_SHORT_TIMEOUT , false);
    }

    /**
     * check Localization Text. Will work ONLY if locKey is equal to element
     * Name and element is Public
     *
     * @param elem                      ExtendedWebElement
     * @param skipMissed                - boolean - if true - will ignore missed elements.
     * @param timeout                   - timeout for element presence waiting.
     * @param skipPunctuationAndNumbers - if true - there will be no numbers and tricky punctuation in l10n values
     * @return boolean
     */
    public static boolean checkLocalizationText(ExtendedWebElement elem, boolean skipMissed, int timeout, boolean skipPunctuationAndNumbers) {
        if (elem.isElementPresent(timeout)) {
            String elemText = elem.getText();
            String locKey = elem.getName();
            return checkLocalizationText(elemText, locKey, skipPunctuationAndNumbers);
        } else {
            LOGGER.info("Expected element not present. Please check: " + elem);
            if (skipMissed) {
                LOGGER.info("Skip missed element: " + elem);
                return true;
            }
        }
        return false;
    }

    private static boolean checkLocalizationText(String expectedText, String locKey, boolean skipPunctuationAndNumbers) {
        String l10n_default = L10Nnew.getText(locKey, actualLocale);
        boolean ret;

        if (skipPunctuationAndNumbers) {
            ret = removeNumbersAndPunctuation(expectedText).toLowerCase().contains(removeNumbersAndPunctuation(l10n_default).toLowerCase());
        } else {
            ret = expectedText.contains(l10n_default);
        }

        if (!ret) {
            LOGGER.error("Actual text should be localized and be equal to: '" + l10n_default + "'." +
                            " But currently it is '" + expectedText + "'.");

            String error = "Expected: '" + l10n_default + "', length=" + l10n_default.length() +
                    ". Actually: '" + expectedText + "', length=" + expectedText.length() + ".";

            if (!assertErrorMsg.contains(error)) {
                assertErrorMsg = assertErrorMsg + '\n' + error;
            }

            if (skipPunctuationAndNumbers) {
                expectedText = removeNumbersAndPunctuation(expectedText);
            }
            String newItem = locKey + "=" + expectedText;
            LOGGER.info("Making new localization string: " + newItem);
            newLocList.add(newItem);
            prop.setProperty(locKey, expectedText);
            return false;
        } else {
            LOGGER.debug("Found localization text '" + expectedText + "' in ISO-8859-1 encoding : " + l10n_default);
            return true;
        }
    }

    /**
     * removeNumbersAndPunctuation from L10n string
     *
     * @param str String
     * @return String
     */
    private static String removeNumbersAndPunctuation(String str) {
        try {
            str = str.replaceAll("[0-9]", "");
            str = str.replace("!", "").replace("\u0085", "").replace("…", "");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return str;
    }

    /**
     * check MultipleLocalization
     *
     * @param localizationCheckList - ExtendedWebElement[] should be set on required page with all
     *                              needed public elements
     * @return boolean
     */
    public static boolean checkMultipleLocalization(ExtendedWebElement[] localizationCheckList) {
        return checkMultipleLocalization(localizationCheckList, BASIC_WAIT_SHORT_TIMEOUT, false);
    }

    /**
     * check MultipleLocalization
     *
     * @param localizationCheckList     - ExtendedWebElement[] should be set on required page with all
     *                                  needed public elements
     * @param timeout                   - timeout for element presence waiting.
     * @param skipPunctuationAndNumbers - if true - there will be no numbers and tricky punctuation in l10n values
     * @return boolean
     */
    public static boolean checkMultipleLocalization(ExtendedWebElement[] localizationCheckList, int timeout, boolean skipPunctuationAndNumbers) {
        boolean ret = true;
        for (ExtendedWebElement elem : localizationCheckList) {
            if (!checkLocalizationText(elem, true, timeout, skipPunctuationAndNumbers)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Save Localization to property file
     */
    public static void saveLocalization() {
        try {
            if (prop.size() == 0) {
                LOGGER.info("There are no new localization properties.");
                return;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.info("New localization for '" + actualLocale + "'");
        LOGGER.info(newLocList.toString());
        LOGGER.info("Properties: " + prop.toString());
        newLocList.clear();
        try {
            if (propFileName.isEmpty()) {
                propFileName = getPropertyFileName(actualLocale.toString());
                LOGGER.info("propFileName:=" + propFileName);
            }

            String encoding = getLocalizationSaveEncoding();
            if (encoding.contains("UTF")) {
                prop.store(new OutputStreamWriter(
                        new FileOutputStream(propFileName), "UTF-8"), null);
            } else {
                OutputStream output = new FileOutputStream(propFileName);
                prop.store(output, null);
                output.close();
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        prop.clear();
    }

    /**
     * get Localization Save Encoding
     *
     * @return String
     */
    private static String getLocalizationSaveEncoding() {
        try {
            encoding = Configuration.get(Parameter.ADD_NEW_LOCALIZATION_ENCODING);
        } catch (Exception e) {
            LOGGER.error("There is no localization encoding parameter in config property.");
        }
        LOGGER.info("Will use encoding: " + encoding);
        return encoding.toUpperCase();
    }

}
