package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import com.qaprosoft.carina.core.foundation.webdriver.locator.LocatorType;

class MobileCaseInsensitiveConverter extends AbstractPlatformDependsConverter implements IPlatformDependsConverter {

    @Override
    public String idToXpath(String by) {
        return locatorToXpath(by, LocatorType.ID,
                value -> {
                    String quote = value.contains("'") ? "\"" : "'";
                    return "//*[ends-with(" + "@resource-id" + ", " + quote + ":id/" + value + quote + ")]";
                });
    }

    @Override
    public String nameToXpath(String by) {
        return locatorToXpath(by, LocatorType.NAME,
                value -> createXpathFromAnotherTypeOfLocator("", "*", "@name", "'", value));
    }

    @Override
    public String linkTextToXpath(String by) {
        return locatorToXpath(by, LocatorType.LINKTEXT,
                value -> createXpathFromAnotherTypeOfLocator("", "a", "text()", "'", value));
    }

    @Override
    public String xpathIdCaseInsensitive(String by) {
        return caseInsensitiveXpathByAttribute(by, "@resource-id");
    }

    @Override
    public String xpathNameCaseInsensitive(String by) {
        return caseInsensitiveXpathByAttribute(by, "@name");
    }

    @Override
    public String xpathTextCaseInsensitive(String by) {
        return caseInsensitiveXpathByAttribute(by, "text\\(\\)|@text|@content-desc");
    }

    @Override
    public String xpathClassCaseInsensitive(String by) {
        return caseInsensitiveXpathByAttribute(by, "@class");
    }

}
