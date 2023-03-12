package com.zebrunner.carina.webdriver.locator.converter.caseinsensitive;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import com.zebrunner.carina.webdriver.locator.LocatorType;

public abstract class AbstractPlatformDependsConverter implements IPlatformDependsConverter {

    // Can be used for any type of locator except xpath
    protected static final String ATTRIBUTE_SINGLE_PATTERN = "^.*$";

    protected String caseInsensitiveXpathByAttribute(String by, String attributeRegex) {
        String cleanXPath = StringUtils.remove(by, LocatorType.BY_XPATH.getStartsWith());
        String attributePattern =
                "(?<!(translate\\())((" + attributeRegex + ")\\s*(\\,|\\=)\\s*((['\"])((?:(?!\\6|\\\\).|\\\\.)*)\\6))";

        Matcher matcher = Pattern.compile(attributePattern)
                .matcher(cleanXPath);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String replacement = createTranslateWithParameters(matcher.group(3), matcher.group(7),
                    matcher.group(6), matcher.group(4));
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString()).toString();
    }

    protected String createXpathFromAnotherTypeOfLocator(String context, String tag, String attribute, String quote, String value) {
        return context + "//" + tag + "[" + attribute + "=" + quote + value + quote + "]";
    }

    protected String createTranslateWithParameters(String attribute, String value, String quote, String delimiter) {
        return ("translate(" + attribute + ", " + quote + value.toUpperCase() + quote + ", " + quote
                + value.toLowerCase() + quote + ")" + delimiter
                + "translate(" + quote + value + quote + ", " + quote + value.toUpperCase()
                + quote + ", " + quote + value.toLowerCase() + quote
                + ")")
                // Used to escape special symbol $ to be visible in result xpath
                        .replaceAll("\\$", "\\\\\\$");
    }

    protected String locatorToXpath(String by, LocatorType locatorType, UnaryOperator<String> replacementFunc) {
        String cleanXPath = StringUtils.remove(by, locatorType.getStartsWith());

        Matcher matcher = Pattern.compile(ATTRIBUTE_SINGLE_PATTERN)
                .matcher(cleanXPath);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String replacement = replacementFunc.apply(matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString()).toString();
    }
}
