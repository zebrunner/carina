package com.qaprosoft.carina.core.foundation.utils.resources;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;

public class L10NLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ArrayList<ResourceBundle> resBoundles = new ArrayList<ResourceBundle>();

    public static void init() {
        List<Locale> locales = LocaleReader.init(Configuration
                .get(Configuration.Parameter.LOCALE));

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
                        for (Locale locale : locales) {
                            resBoundles.add(ResourceBundle.getBundle(resource, locale));
                        }
                        LOGGER.debug(String
                                .format("Resource '%s' added.", resource));
                    } catch (MissingResourceException e) {
                        LOGGER.debug(e.getMessage(), e);
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

    public static String getText(String key) {

        return getText(key, getLocale());
    }

    public static String getText(String key, Locale locale) {
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

    public static Locale getLocale() {
        List<Locale> locales = LocaleReader.init(Configuration
                .get(Configuration.Parameter.LOCALE));

        if (locales.size() == 0) {
            throw new RuntimeException("Undefined default locale specified! Review 'locale' setting in _config.properties.");
        }

        return locales.get(0);
    }
}
