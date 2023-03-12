package com.zebrunner.carina.webdriver.locator.converter.caseinsensitive;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.webdriver.decorator.annotations.CaseInsensitiveXPath;
import com.zebrunner.carina.webdriver.locator.LocatorType;
import com.zebrunner.carina.webdriver.locator.converter.LocatorConverter;

public class CaseInsensitiveConverter implements LocatorConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    // Contains type of locator as key and start part of locator as value
    private static final List<LocatorType> LIST_OF_CONVERTIBLE_LOCATORS = List.of(LocatorType.BY_ID, LocatorType.APPIUM_BY_ID,
            LocatorType.BY_NAME, LocatorType.APPIUM_BY_NAME, LocatorType.BY_XPATH, LocatorType.BY_LINKTEXT);

    private boolean isId = false;
    private boolean isName = false;
    private boolean isText = false;
    private boolean isClassAttr = false;

    private final IPlatformDependsConverter platformDependsConverter;

    CaseInsensitiveConverter(boolean isNativeMobile) {
        if (isNativeMobile) {
            platformDependsConverter = new NativeMobileCaseInsensitiveConverter();
        } else {
            platformDependsConverter = new WebCaseInsensitiveConverter();
        }
    }

    public CaseInsensitiveConverter(CaseInsensitiveXPath annotation, boolean isNativeMobile) {
        this(isNativeMobile);
        isId = annotation.id();
        isName = annotation.name();
        isText = annotation.text();
        isClassAttr = annotation.classAttr();
    }

    @Override
    public String convert(String by) {
        LOGGER.debug("Locator before converting to be case-insensitive: {}", by);

        if (!isConvertibleToXpath(by)) {
            LOGGER.error("The locator '{}' is not supported for case-insensitive conversion, so will be returned as is", by);
            return by;
        }

        String xpath = convertToXpath(by);
        xpath = convertXPathToCaseInsensitive(xpath);

        LOGGER.debug("Locator after converting to be case-insensitive: {}", xpath);
        return xpath;
    }

    private String convertToXpath(String by) {
        String resultBy = by;
        if (LocatorType.BY_ID.is(by) || LocatorType.APPIUM_BY_ID.is(by)) {
            resultBy = platformDependsConverter.idToXpath(by);
        } else if (LocatorType.BY_NAME.is(by) || LocatorType.APPIUM_BY_NAME.is(by)) {
            resultBy = platformDependsConverter.nameToXpath(by);
        } else if (LocatorType.BY_LINKTEXT.is(by)) {
            resultBy = platformDependsConverter.linkTextToXpath(by);
        }
        return resultBy;
    }

    private String convertXPathToCaseInsensitive(String by) {
        String byToConvert = by;
        if (isId) {
            byToConvert = platformDependsConverter.xpathIdCaseInsensitive(byToConvert);
        }
        if (isName) {
            byToConvert = platformDependsConverter.xpathNameCaseInsensitive(byToConvert);
        }
        if (isText) {
            byToConvert = platformDependsConverter.xpathTextCaseInsensitive(byToConvert);
        }
        if (isClassAttr) {
            byToConvert = platformDependsConverter.xpathClassCaseInsensitive(byToConvert);
        }
        return byToConvert;
    }

    private boolean isConvertibleToXpath(String by) {
        return LIST_OF_CONVERTIBLE_LOCATORS.stream()
                .anyMatch(locatorType -> locatorType.is(by));
    }

    public boolean isId() {
        return isId;
    }

    CaseInsensitiveConverter setId(boolean id) {
        isId = id;
        return this;
    }

    public boolean isName() {
        return isName;
    }

    CaseInsensitiveConverter setName(boolean name) {
        isName = name;
        return this;
    }

    public boolean isText() {
        return isText;
    }

    CaseInsensitiveConverter setText(boolean text) {
        isText = text;
        return this;
    }

    public boolean isClassAttr() {
        return isClassAttr;
    }

    CaseInsensitiveConverter setClassAttr(boolean classAttr) {
        isClassAttr = classAttr;
        return this;
    }
}
