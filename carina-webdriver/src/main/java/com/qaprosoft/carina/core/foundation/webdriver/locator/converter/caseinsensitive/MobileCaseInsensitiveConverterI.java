package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import com.qaprosoft.carina.core.foundation.webdriver.locator.LocatorType;

class MobileCaseInsensitiveConverterI extends AbstractPlatformDependsConverter implements IPlatformDependsConverter {

    @Override
    public By idToXpath(By by) {
        String cleanXPath = StringUtils.remove(by.toString(), LocatorType.ID.getStartsWith());
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
        return locatorToXpath(by, LocatorType.NAME,
                (value) -> createXpathFromAnotherTypeOfLocator("", "*", "@name", "'", value));
    }

    @Override
    public By linkTextToXpath(By by) {
        return locatorToXpath(by, LocatorType.LINKTEXT,
                (value) -> createXpathFromAnotherTypeOfLocator("", "a", "text()", "'", value));
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
