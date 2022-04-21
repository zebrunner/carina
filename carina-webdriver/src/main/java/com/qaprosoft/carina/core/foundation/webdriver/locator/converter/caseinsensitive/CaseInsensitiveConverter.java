package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.LocatorConverter;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class CaseInsensitiveConverter implements LocatorConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    // Contains type of locator as key and start part of locator as value
    private static final List<String> listOfConvertableLocators = new ArrayList<>();
    private final IPlatformDependsConverter platformDependsConverter;
    private ParamsToConvert paramsToConvert;

    {
        listOfConvertableLocators.add("By.xpath: ");
        listOfConvertableLocators.add("By.id: ");
        listOfConvertableLocators.add("By.name: ");
        listOfConvertableLocators.add("By.linkText: ");
    }

    public CaseInsensitiveConverter(ParamsToConvert paramsToConvert, Platform platform) {
        this.paramsToConvert = paramsToConvert;
        platformDependsConverter = getConverterDependsOnPlatform(platform);
    }

    @Override
    public By convert(By by) {
        LOGGER.debug("Locator before converting to be case-insensitive: {}", by);

        if (!isConvertibleToXpath(by)) {
            throw new IllegalArgumentException("Cannot convert locator: " + by + " to case-insensitive because it doesn't supported");
        }

        By xpath = convertToXpath(by);
        xpath = convertXPathToCaseInsensitive(xpath);

        LOGGER.debug("Locator after converting to be case-insensitive: {}", xpath);
        return xpath;
    }

    private By convertToXpath(By by) {
        By byToConvert = by;
        String locator = by.toString();

        if (locator.startsWith("By.id: ")) {
            byToConvert = platformDependsConverter.idToXpath(byToConvert);
        }
        if (locator.startsWith("By.name: ")) {
            byToConvert = platformDependsConverter.nameToXpath(byToConvert);
        }

        if (locator.startsWith("By.linkText: ")) {
            byToConvert = platformDependsConverter.linkTextToXpath(byToConvert);
        }

        return byToConvert;
    }

    private By convertXPathToCaseInsensitive(By by) {
        By byToConvert = by;

        if (paramsToConvert.isId()) {
            byToConvert = platformDependsConverter.xpathIdCaseInsensitive(byToConvert);
        }

        if (paramsToConvert.isName()) {
            byToConvert = platformDependsConverter.xpathNameCaseInsensitive(byToConvert);
        }

        if (paramsToConvert.isText()) {
            byToConvert = platformDependsConverter.xpathTextCaseInsensitive(byToConvert);
        }

        if (paramsToConvert.isClassAttr()) {
            byToConvert = platformDependsConverter.xpathClassCaseInsensitive(byToConvert);
        }

        return byToConvert;
    }

    private IPlatformDependsConverter getConverterDependsOnPlatform(Platform platform) {
        IPlatformDependsConverter platformDependsConverter;

        switch (platform) {
        case WEB:
            platformDependsConverter = new WebCaseInsensitiveConverterI();
            break;
        case MOBILE:
            platformDependsConverter = new MobileCaseInsensitiveConverterI();
            break;
        default:
            throw new InvalidArgumentException("Platform " + platform + " is not supported");
        }

        return platformDependsConverter;
    }

    private boolean isConvertibleToXpath(By by) {
        String locator = by.toString();
        return listOfConvertableLocators.stream()
                .anyMatch(locator::startsWith);
    }

    public void setParamsToConvert(ParamsToConvert paramsToConvert) {
        this.paramsToConvert = paramsToConvert;
    }
}
