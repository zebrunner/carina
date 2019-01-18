/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils.resources;

import java.net.URL;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;

/*
 * QUALITY-1076:
 * http://maven.apache.org/surefire/maven-surefire-plugin/examples/class-loading.html
 * Need to set useSystemClassLoader=false for maven surefire plugin to receive access to classloader I18N files on CI
 * <plugin>
 * <groupId>org.apache.maven.plugins</groupId>
 * <artifactId>maven-surefire-plugin</artifactId>
 * <version>2.18.1</version>
 * <configuration>
 * <useSystemClassLoader>false</useSystemClassLoader>
 * </configuration>
 * 
 */
public class I18N {
    protected static final Logger LOGGER = Logger.getLogger(I18N.class);

    private static ArrayList<ResourceBundle> resBoundles = new ArrayList<ResourceBundle>();

    public static void init() {
        if (!Configuration.getBoolean(Parameter.ENABLE_I18N)) {
            return;
        }

        List<String> loadedResources = new ArrayList<String>();

        List<Locale> locales = LocaleReader.init(Configuration
                .get(Parameter.LANGUAGE));

        for (URL u : Resources.getResourceURLs(new ResourceURLFilter() {
            public @Override boolean accept(URL u) {
				String s = u.getPath();
				boolean contains = s.contains(SpecialKeywords.I18N);
				if (contains) {
					LOGGER.debug("I18N: file URL: " + u);
				}
				return contains;
            }
        })) {
            LOGGER.debug(String.format(
                    "Analyzing '%s' I18N resource for loading...", u));

            // workable examples for resource loading are
            // ResourceBundle.getBundle("I18N.messages", locale);
            // ResourceBundle.getBundle("I18N.system.data-access.resources.gwt.datasourceAdminDialog",
            // locale);

            /*
             * 2. Exclude localization resources like such I18N.messages_de,
             * I18N.messages_ptBR etc... Note: we ignore valid resources if 3rd
             * or 5th char from the end is "_". As designed :(
             */
            String fileName = FilenameUtils.getBaseName(u.getPath());
            if (u.getPath().endsWith("I18N.class") ||
                    u.getPath().endsWith("I18N$1.class")) {
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
             * convert "file:
             * E:\pentaho\qa-automation\target\classes\I18N\messages.properties"
             * to "I18N.messages"
             */
            String filePath = FilenameUtils.getPath(u.getPath());
            String resource = filePath.substring(
                    filePath.indexOf(SpecialKeywords.I18N))
                    .replaceAll("/", ".")
                    + fileName;

            if (!loadedResources.contains(resource)) {
                loadedResources.add(resource);
                try {
                    LOGGER.debug(String.format("Adding '%s' resource...",
                            resource));
                    for (Locale locale : locales) {
                        resBoundles.add(ResourceBundle.getBundle(resource, locale));
                    }
                    LOGGER.debug(String
                            .format("Resource '%s' added.", resource));
                } catch (MissingResourceException e) {
                    LOGGER.debug(e);
                }
            } else {
                LOGGER.debug(String
                        .format("Requested resource '%s' is already loaded into the ResourceBundle!",
                                resource));
            }
        }
        LOGGER.debug("init: I18N bundle size: " + resBoundles.size());
    }

    private static Locale getDefaultLanguage() {
        List<Locale> locales = LocaleReader.init(Configuration
                .get(Parameter.LANGUAGE));

        if (locales.size() == 0) {
            throw new RuntimeException("Undefined default language specified! Review 'language' setting in _config.properties.");
        }

        return locales.get(0);
    }

    /**
     * getText by key for default language.
     * 
     * @param key
     *            - String
     *
     * @return String
     */
    public static String getText(String key) {
        return getText(key, getDefaultLanguage());
    }

    /**
     * getText for specified language and key.
     * 
     * @param key
     *            - String
     * @param locale
     *            - Locale
     * @return String
     */
    public static String getText(String key, Locale locale) {
        LOGGER.debug("getText: I18N bundle size: " + resBoundles.size());
        Iterator<ResourceBundle> iter = resBoundles.iterator();
        while (iter.hasNext()) {
            ResourceBundle bundle = iter.next();
            try {
                String value = bundle.getString(key);
                LOGGER.debug("Looking for value for language:'"
                        + locale.getLanguage()
                        + "' current iteration language is: '"
                        + bundle.getLocale().getLanguage() + "'.");
                if (bundle.getLocale().getLanguage().equals(locale.getLanguage())) {
                    LOGGER.debug("Found language:'" + locale.getLanguage()
                            + "' and value is '" + value + "'.");
                    return value;
                }
            } catch (MissingResourceException e) {
                // do nothing
            }
        }
        return key;
    }
}