package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MobileCaseInsensitiveConverterI extends AbstractPlatformDependsConverter implements IPlatformDependsConverter {

    @Override
    public By idToXpath(By by) {
        String cleanXPath = StringUtils.remove(by.toString(), "By.id: ");
        Matcher matcher = Pattern.compile(ATTRIBUTE_SINGLE_PATTERN)
                .matcher(cleanXPath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String attribute = "@resource-id";
            // part of xpath with id prefix (needed for point search of id
            String value = ":id/" + matcher.group();
            // trying to choose between quotes
            String quote = value.contains("'") ? "\"" : "'";

            //unique case for searching id because in mobile apps id consists from package name and id
            String replacement = "//*[ends-with(" + attribute + ", " + quote + value + quote + ")]";
            // Used to escape special symbol $ to be visible in result xpath
            replacement = replacement.replaceAll("\\$", "\\\\\\$");

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    @Override
    public By nameToXpath(By by) {
        String cleanXPath = StringUtils.remove(by.toString(), "By.name: ");
        Matcher matcher = Pattern.compile(ATTRIBUTE_SINGLE_PATTERN)
                .matcher(cleanXPath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String replacement = createXpathFromAnotherTypeOfLocator("", "*",
                    "@name", "'", matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    @Override
    public By linkTextToXpath(By by) {
        String cleanXPath = StringUtils.remove(by.toString(), "By.linkText: ");
        Matcher matcher = Pattern.compile(ATTRIBUTE_SINGLE_PATTERN)
                .matcher(cleanXPath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String replacement = createXpathFromAnotherTypeOfLocator("", "a",
                    "text()", "'", matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    @Override
    public By xpathIdCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "@resource-id");
    }

    @Override
    public By xpathNameCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "@name");
    }

    @Override
    public By xpathTextCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "text\\(\\)|@text|@content-desc");
    }

    @Override
    public By xpathClassCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "@class");
    }

}
