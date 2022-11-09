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
package com.zebrunner.carina.utils.resources;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.IWebElement;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import com.zebrunner.carina.utils.commons.SpecialKeywords;

/*
 * http://maven.apache.org/surefire/maven-surefire-plugin/examples/class-loading.html
 * Need to set useSystemClassLoader=false for maven surefire plugin to receive access to L10N files on CI by ClassLoader
 * <plugin>
 * <groupId>org.apache.maven.plugins</groupId>
 * <artifactId>maven-surefire-plugin</artifactId>
 * <version>3.0.0-M4</version>
 * <configuration>
 * <useSystemClassLoader>false</useSystemClassLoader>
 * </configuration>
 */
public class L10N {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static Locale locale = getLocale(Configuration.get(Configuration.Parameter.LOCALE));
    private static ArrayList<ResourceBundle> resBoundles = new ArrayList<ResourceBundle>();
    private static Properties missedResources = new Properties();
    
    private static SoftAssert mistakes;


    /**
     * Load L10N resource bundle corresponding to a specific locale.
     * If called setLocale function in the test, must be called to reload resources
     */    
    public static void load() {
        // #1679: L10N: made assertion threads dependent
        mistakes = new SoftAssert();
        List<String> loadedResources = new ArrayList<String>();
        try {

            for (URL u : Resources.getResourceURLs(new ResourceURLFilter() {
                public @Override
                boolean accept(URL u) {
                    String s = u.getPath();
                    boolean contains = s.contains(SpecialKeywords.L10N);
                    if (contains) {
                        LOGGER.debug("L10N: file URL: " + u);
                    }
                    return contains;
                }
            })) {
                LOGGER.debug(String.format(
                        "Analyzing '%s' L10N resource for loading...", u));
                /*
                 * 2. Exclude localization resources like such L10N.messages_de, L10N.messages_ptBR etc...
                 * Note: we ignore valid resources if 3rd or 5th char from the end is "_". As designed :(
                 */
                String fileName = FilenameUtils.getBaseName(u.getPath());

                if (u.getPath().endsWith("L10N.class")
                        || u.getPath().endsWith("L10N$1.class")) {
                    // separate conditions to support core JUnit tests
                    continue;
                }

                if (fileName.lastIndexOf('_') == fileName.length() - 3
                        || fileName.lastIndexOf('_') == fileName.length() - 5) {
                    LOGGER.debug(String
                            .format("'%s' resource IGNORED as it looks like localized resource!",
                                    fileName));
                    continue;
                }
                /*
                 * convert "file: <REPO>\target\classes\L10N\messages.properties" to "L10N.messages"
                 */
                String filePath = FilenameUtils.getPath(u.getPath());
                int index = filePath.indexOf(SpecialKeywords.L10N);

                if (index == -1) {
                    LOGGER.warn("Unable to find L10N pattern for " + u.getPath() + " resource!");
                    continue;
                }

                String resource = filePath.substring(
                        filePath.indexOf(SpecialKeywords.L10N))
                        .replaceAll("/", ".")
                        + fileName;

                if (!loadedResources.contains(resource)) {
                    loadedResources.add(resource);
                    try {
                        LOGGER.debug(String.format("Adding '%s' resource...",
                                resource));
                        
                        resBoundles.add(ResourceBundle.getBundle(resource, locale));
                        LOGGER.debug(String
                                .format("Resource '%s' added.", resource));
                    } catch (MissingResourceException e) {
                        LOGGER.debug("No resource bundle for the " + resource + " can be found", e);
                    }
                } else {
                    LOGGER.debug(String
                            .format("Requested resource '%s' is already loaded into the ResourceBundle!",
                                    resource));
                }
            }
            LOGGER.debug("init: L10N bundle size: " + resBoundles.size());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("L10N folder with resources is missing!");
        }
    }

    /**
     * Replace default L10N resource bundle.
     *
     * @param resources ArrayList
     *
     */    
    public static void load(ArrayList<ResourceBundle> resources) {
        // #1679: L10N: made assertion threads dependent
        mistakes = new SoftAssert();
        resBoundles = resources;
    }
    
    /**
     * Return translated value by key for default locale.
     *
     * @param key String
     *
     * @return String
     */
    static public String getText(String key) {
        LOGGER.debug("getText: L10N bundle size: " + resBoundles.size());
        Iterator<ResourceBundle> iter = resBoundles.iterator();
        while (iter.hasNext()) {
            ResourceBundle bundle = iter.next();
            try {
                String value = bundle.getString(key);
                if (bundle.getLocale().toString().equals(locale.toString())) {
                    return value;
                }
            } catch (MissingResourceException e) {
                // do nothing
            }
        }
        return key;
    }
    
    /**
     * Verify that ExtendedWebElement text is correctly localized.
     * Called automatically when an action is performed on an element
     * marked with the Localized annotation (getText, hover, etc.)
     * 
     * @param element IWebElement
     * @return boolean
     */
    public static boolean verify(IWebElement element) {
        if (!Configuration.getBoolean(Configuration.Parameter.LOCALIZATION_TESTING)) {
            return true;
        }
        
        String actualText = element.getText();
        String key = element.getName();

        String expectedText = getText(key);
        boolean isValid = actualText.contains(expectedText) && !expectedText.isEmpty();

        if (!isValid) {
            String error = "Expected: '" + expectedText + "', length=" + expectedText.length() +
                    ". Actual: '" + actualText + "', length=" + actualText.length() + ".";

            LOGGER.error(error);
            mistakes.fail(error);

            String newItem = key + "=" + actualText;
            LOGGER.info("Making new localization string: " + newItem);
            missedResources.setProperty(key, actualText);
        } else {
            LOGGER.debug("Found localization text '" + actualText + " in +" + getEncoding() + " encoding: " + expectedText);
        }

        return isValid;
    }

    /**
     * Raise summarized asserts for mistakes in localization  
     */       
    public static void assertAll() {
        mistakes.assertAll();
    }
    
    /**
     * Override default locale.
     *
     * @param loc String
     *
     */       
    public static void setLocale(String loc) {
        LOGGER.warn("Default locale: " + locale + " was overriden by " + loc);
        locale = getLocale(loc);
    }    
    
    /**
     * Flush missed localization resources to property file.
     */
    public static void flush() {
        if (missedResources.size() == 0) {
            LOGGER.info("There are no new localization properties.");
            return;
        }

        LOGGER.info("New localization for '" + locale + "'");
        LOGGER.info("Properties: " + missedResources.toString());
        

        String missedResorceFile = "missed_" + locale + ".properties";
        try {
            missedResources.store(new OutputStreamWriter(
                    new FileOutputStream(missedResorceFile), getEncoding()), null);            
        } catch (Exception e) {
            LOGGER.error("Unable to store missed resources: " + missedResorceFile + "!", e);
        }
        missedResources.clear();
    }
    
    private static String getEncoding() {
        return Configuration.get(Configuration.Parameter.LOCALIZATION_ENCODING).toUpperCase();
    }

    static private Locale getLocale(String locale) {
        String[] localeSetttings = locale.trim().split("_");
        String lang, country = "";
        lang = localeSetttings[0];
        if (localeSetttings.length > 1) {
            country = localeSetttings[1];
        }
        
        return new Locale(lang, country);
    }    

}
