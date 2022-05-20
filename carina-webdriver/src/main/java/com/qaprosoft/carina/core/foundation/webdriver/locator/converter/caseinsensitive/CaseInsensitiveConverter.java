package com.qaprosoft.carina.core.foundation.webdriver.locator.converter.caseinsensitive;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.locator.LocatorType;
import com.qaprosoft.carina.core.foundation.webdriver.locator.converter.LocatorConverter;

public class CaseInsensitiveConverter implements LocatorConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    // Contains type of locator as key and start part of locator as value
    private static final List<LocatorType> listOfConvertableLocators = new ArrayList<>();
    private final IPlatformDependsConverter platformDependsConverter;
    private ParamsToConvert paramsToConvert;

    {
        listOfConvertableLocators.add(LocatorType.ID);
        listOfConvertableLocators.add(LocatorType.NAME);
        listOfConvertableLocators.add(LocatorType.XPATH);
        listOfConvertableLocators.add(LocatorType.LINKTEXT);
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

        if (locator.startsWith(LocatorType.ID.getStartsWith())) {
            byToConvert = platformDependsConverter.idToXpath(byToConvert);
        }
        if (locator.startsWith(LocatorType.NAME.getStartsWith())) {
            byToConvert = platformDependsConverter.nameToXpath(byToConvert);
        }

        if (locator.startsWith(LocatorType.LINKTEXT.getStartsWith())) {
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
        IPlatformDependsConverter converter;

        switch (platform) {
        case WEB:
            converter = new WebCaseInsensitiveConverter();
            break;
        case MOBILE:
            converter = new MobileCaseInsensitiveConverter();
            break;
        default:
            throw new InvalidArgumentException("Platform " + platform + " is not supported");
        }

        return converter;
    }

    private boolean isConvertibleToXpath(By by) {
        String locator = by.toString();
        return listOfConvertableLocators.stream()
                .anyMatch(locatorType -> locator.startsWith(locatorType.getStartsWith()));
    }

    public void setParamsToConvert(ParamsToConvert paramsToConvert) {
        this.paramsToConvert = paramsToConvert;
    }
}
