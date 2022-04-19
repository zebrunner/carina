package com.qaprosoft.carina.core.foundation.webdriver.locator;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocatorConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Can be used to convert: xpath (@text, text()), id, name to be case-insensitive
     */
    public static By toCaseInsensitive(By by) {
        LOGGER.debug("Locator before converting to be case-insensitive: {}", by);
        String locator = by.toString();

        if (locator.startsWith("By.xpath:")) {
            by = xpathToCaseInsensitive(locator);
        }

        if (locator.startsWith("By.id:")) {
            by = idToXpathCaseInsensitive(locator);
        }

        if (locator.startsWith("By.name:")) {
            by = nameToXpathCaseInsensitive(locator);
        }

        if (locator.startsWith("By.linkText:")) {
            by = linkTextToXpathCaseInsensitive(locator);
        }
        LOGGER.debug("Locator after converting to be case-insensitive: {}", by);
        return by;
    }

    /**
     * Transform XPath locator to case-insensitive
     *
     * @param locator - locator as a String
     * @return By
     */
    // todo make it private
    public static By xpathToCaseInsensitive(String locator) {
        String xpath = StringUtils.remove(locator, "By.xpath: ");
        String attributePattern = "(?<!(translate\\())((@text|text\\(\\)|@content-desc)\\s*(\\,|\\=)\\s*((['\"])((?:(?!\\6|\\\\).|\\\\.)*)\\6))";
        //TODO: test when xpath globally are declared inside single quota

        // double translate is needed to make xpath and value case insensitive.
        // For example on UI we have "Inscription", so with a single translate we must convert in page object all those values to lowercase
        // double translate allow to use as is and convert everywhere

        // Expected xpath for both side translate
        // *[translate(@text, '$U', '$l')=translate("Inscription", "inscription".UPPER, "inscription".LOWER)]

        Matcher matcher = Pattern.compile(attributePattern).matcher(xpath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String attribute = matcher.group(3);            // @text or text()
            String value = matcher.group(7);                //  'some text' or "some text"
            String quote = matcher.group(6);                // ' or "
            String delimiter = matcher.group(4);            // , or =

            String replacement =
                    "translate(" + attribute + ", " + quote + value.toUpperCase() + quote + ", " + quote
                            + value.toLowerCase() + quote + ")" + delimiter
                            + "translate(" + quote + value + quote + ", " + quote + value.toUpperCase()
                            + quote + ", " + quote + value.toLowerCase() + quote
                            + ")";

            replacement = replacement.replaceAll("\\$", "\\\\\\$");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    private static By idToXpathCaseInsensitive(String locator) {
        String xpath = StringUtils.remove(locator, "By.id: ");
        String attributePattern = "^.*$";

        Matcher matcher = Pattern.compile(attributePattern).matcher(xpath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String attribute = "@id";
            String value = matcher.group();
            String quote = "'";
            String delimiter = "=";

            String replacement =
                    ".//*[translate(" + attribute + ", " + quote + value.toUpperCase() + quote + ", " + quote
                            + value.toLowerCase() + quote + ")" + delimiter
                            + "translate(" + quote + value + quote + ", " + quote + value.toUpperCase()
                            + quote + ", " + quote + value.toLowerCase() + quote
                            + ")]";

            replacement = replacement.replaceAll("\\$", "\\\\\\$");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    private static By nameToXpathCaseInsensitive(String locator) {
        String xpath = StringUtils.remove(locator, "By.name: ");
        String attributePattern = "^.*$";

        Matcher matcher = Pattern.compile(attributePattern).matcher(xpath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String attribute = "@name";
            String value = matcher.group();
            String quote = "'";
            String delimiter = "=";

            String replacement =
                    ".//*[translate(" + attribute + ", " + quote + value.toUpperCase() + quote + ", " + quote
                            + value.toLowerCase() + quote + ")" + delimiter
                            + "translate(" + quote + value + quote + ", " + quote + value.toUpperCase()
                            + quote + ", " + quote + value.toLowerCase() + quote
                            + ")]";

            replacement = replacement.replaceAll("\\$", "\\\\\\$");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    private static By linkTextToXpathCaseInsensitive(String locator) {
        String xpath = StringUtils.remove(locator, "By.linkText: ");
        String attributePattern = "^.*$";

        Matcher matcher = Pattern.compile(attributePattern).matcher(xpath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String attribute = "text()";
            String value = matcher.group();
            String quote = "'";
            String delimiter = "=";

            String replacement =
                    ".//a[translate(" + attribute + ", " + quote + value.toUpperCase() + quote + ", " + quote
                            + value.toLowerCase() + quote + ")" + delimiter
                            + "translate(" + quote + value + quote + ", " + quote + value.toUpperCase()
                            + quote + ", " + quote + value.toLowerCase() + quote
                            + ")]";

            replacement = replacement.replaceAll("\\$", "\\\\\\$");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }
}
