package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import org.openqa.selenium.By;

interface IPlatformDependsConverter {

    By idToXpath(By by);

    By nameToXpath(By by);

    By linkTextToXpath(By by);

    By xpathIdCaseInsensitive(By by);

    By xpathNameCaseInsensitive(By by);

    By xpathTextCaseInsensitive(By by);

    By xpathClassCaseInsensitive(By by);

}
