package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WebCaseInsensitiveConverterI extends AbstractPlatformDependsConverter implements IPlatformDependsConverter {

    @Override
    public By idToXpath(By by) {
        String cleanXPath = StringUtils.remove(by.toString(), LocatorType.ID.getStartsWith());

        Matcher matcher = Pattern.compile(ATTRIBUTE_SINGLE_PATTERN)
                .matcher(cleanXPath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String replacement = createXpathFromAnotherTypeOfLocator(".", "*", "@id", "'", matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    @Override
    public By nameToXpath(By by) {
        String cleanXpath = StringUtils.remove(by.toString(), LocatorType.NAME.getStartsWith());

        Matcher matcher = Pattern.compile(ATTRIBUTE_SINGLE_PATTERN)
                .matcher(cleanXpath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String replacement = createXpathFromAnotherTypeOfLocator(".", "*", "@name", "'", matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    @Override
    public By linkTextToXpath(By by) {
        String cleanXPath = StringUtils.remove(by.toString(), LocatorType.LINKTEXT.getStartsWith());

        Matcher matcher = Pattern.compile(ATTRIBUTE_SINGLE_PATTERN)
                .matcher(cleanXPath);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String replacement = createXpathFromAnotherTypeOfLocator(".", "a", "text()", "'", matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return By.xpath(sb.toString());
    }

    @Override
    public By xpathIdCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "@id");
    }

    @Override
    public By xpathNameCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "@name");
    }

    @Override
    public By xpathTextCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "text\\(\\)");
    }

    @Override
    public By xpathClassCaseInsensitive(By by) {
        return caseInsensitiveXpathByAttribute(by, "@class");
    }

}
