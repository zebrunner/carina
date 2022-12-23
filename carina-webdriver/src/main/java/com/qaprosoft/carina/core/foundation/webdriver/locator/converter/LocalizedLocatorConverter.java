package com.qaprosoft.carina.core.foundation.webdriver.locator.converter;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.AccessibilityId;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.ClassChain;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Predicate;
import com.qaprosoft.carina.core.foundation.webdriver.locator.LocatorType;
import com.zebrunner.carina.utils.commons.SpecialKeywords;
import com.zebrunner.carina.utils.resources.L10N;

import io.appium.java_client.AppiumBy;

public class LocalizedLocatorConverter implements LocatorConverter {
    public static final Pattern L10N_PATTERN = Pattern.compile(SpecialKeywords.L10N_PATTERN);
    private final Field field;

    public LocalizedLocatorConverter(Field field) {
        this.field = field;
    }

    @Override
    public String convert(String by) {
        String locator = by;
        // replace by using localization pattern
        Matcher matcher = L10N_PATTERN.matcher(locator);
        while (matcher.find()) {
            int start = locator.indexOf(SpecialKeywords.L10N + ":") + 5;
            int end = locator.indexOf("}");
            String key = locator.substring(start, end);
            locator = StringUtils.replace(locator, matcher.group(), L10N.getText(key));
        }

        if (this.field.isAnnotationPresent(Predicate.class)) {
            // TODO: analyze howto determine iOS or Android predicate
            locator = AppiumBy.iOSNsPredicateString(StringUtils.remove(locator, LocatorType.XPATH.getStartsWith()))
                    .toString();
        } else if (this.field.isAnnotationPresent(ClassChain.class)) {
            locator = AppiumBy.iOSClassChain(StringUtils.remove(locator, LocatorType.XPATH.getStartsWith()))
                    .toString();
        } else if (this.field.isAnnotationPresent(AccessibilityId.class)) {
            locator = AppiumBy.accessibilityId(StringUtils.remove(locator, LocatorType.NAME.getStartsWith()))
                    .toString();
        }
        return locator;
    }
}
