package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import org.openqa.selenium.By;

import com.qaprosoft.carina.core.foundation.webdriver.locator.LocatorType;

class WebCaseInsensitiveConverter extends AbstractPlatformDependsConverter implements IPlatformDependsConverter {

    @Override
    public By idToXpath(By by) {
        return locatorToXpath(by, LocatorType.ID,
                value -> createXpathFromAnotherTypeOfLocator(".", "*", "@id", "'", value));
    }

    @Override
    public By nameToXpath(By by) {
        return locatorToXpath(by, LocatorType.NAME,
                value -> createXpathFromAnotherTypeOfLocator(".", "*", "@name", "'", value));
    }

    @Override
    public By linkTextToXpath(By by) {
        return locatorToXpath(by, LocatorType.LINKTEXT,
                value -> createXpathFromAnotherTypeOfLocator(".", "a", "text()", "'", value));
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
