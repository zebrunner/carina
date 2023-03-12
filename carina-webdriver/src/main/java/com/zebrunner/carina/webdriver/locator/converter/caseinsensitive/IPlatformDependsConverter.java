package com.zebrunner.carina.webdriver.locator.converter.caseinsensitive;

interface IPlatformDependsConverter {

    String idToXpath(String by);

    String nameToXpath(String by);

    String linkTextToXpath(String by);

    String xpathIdCaseInsensitive(String by);

    String xpathNameCaseInsensitive(String by);

    String xpathTextCaseInsensitive(String by);

    String xpathClassCaseInsensitive(String by);
}
