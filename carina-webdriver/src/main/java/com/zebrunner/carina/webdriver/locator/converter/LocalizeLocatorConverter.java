package com.zebrunner.carina.webdriver.locator.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.resources.L10N;

public class LocalizeLocatorConverter implements LocatorConverter {
    private static final Pattern L10N_PATTERN = Pattern.compile(SpecialKeywords.L10N_PATTERN);

    @Override
    public String convert(String by) {
        String locator = by;
        // replace by using localization pattern
        Matcher matcher = L10N_PATTERN.matcher(locator);
        while (matcher.find()) {
            int start = locator.indexOf(SpecialKeywords.L10N + ":") + 5;
            int end = locator.indexOf("}");
            String key = locator.substring(start, end);
            locator = StringUtils.replaceOnce(locator, matcher.group(), L10N.getText(key));
        }
        return locator;
    }

    /**
     * Get compiled L10N pattern
     * 
     * @return {@link Pattern}
     */
    public static Pattern getL10nPattern() {
        return L10N_PATTERN;
    }
}
