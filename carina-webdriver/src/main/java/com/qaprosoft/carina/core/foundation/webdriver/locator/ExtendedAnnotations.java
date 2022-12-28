/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver.locator;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.AccessibilityId;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.ClassChain;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Predicate;
import com.zebrunner.carina.utils.commons.SpecialKeywords;

import io.appium.java_client.AppiumBy;

public class ExtendedAnnotations extends Annotations {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ExtendedAnnotations(Field field) {
        super(field);
    }

    @Override
    public By buildBy() {
        By by = super.buildBy();
        String param = by.toString();
        if (getField().isAnnotationPresent(Predicate.class)) {
            // TODO: analyze howto determine iOS or Android predicate
            param = StringUtils.remove(param, LocatorType.XPATH.getStartsWith());
            by = AppiumBy.iOSNsPredicateString(param);
            // by = AppiumBy.androidUIAutomator(param);
        } else if (getField().isAnnotationPresent(ClassChain.class)) {
            param = StringUtils.remove(param, LocatorType.XPATH.getStartsWith());
            by = AppiumBy.iOSClassChain(param);
        } else if (getField().isAnnotationPresent(AccessibilityId.class)) {
            param = StringUtils.remove(param, LocatorType.NAME.getStartsWith());
            by = AppiumBy.accessibilityId(param);
        } else if (getField().isAnnotationPresent(ExtendedFindBy.class)) {
            By extendedBy = createExtendedBy(param);
            if (extendedBy != null) {
                by = extendedBy;
            }
            LOGGER.debug("Annotation ExtendedFindBy has been detected. Returning locator : {}", by);
        } else {
            by = createBy(param);
        }
        return by;
    }

    private By createBy(String locator) {
        if (LocatorType.ID.is(locator)) {
            return LocatorType.ID.buildLocatorFromString(locator);
        } else if (LocatorType.NAME.is(locator)) {
            return LocatorType.NAME.buildLocatorFromString(locator);
        } else if (LocatorType.XPATH.is(locator)) {
            return LocatorType.XPATH.buildLocatorFromString(locator);
        } else if (LocatorType.LINKTEXT.is(locator)) {
            return LocatorType.LINKTEXT.buildLocatorFromString(locator);
        } else if (LocatorType.PARTIAL_LINK_TEXT.is(locator)) {
            return LocatorType.PARTIAL_LINK_TEXT.buildLocatorFromString(locator);
        } else if (LocatorType.CSS.is(locator)) {
            return LocatorType.CSS.buildLocatorFromString(locator);
        } else if (LocatorType.CLASSNAME.is(locator)) {
            return LocatorType.CLASSNAME.buildLocatorFromString(locator);
        } else if (LocatorType.TAG_NAME.is(locator)) {
            return LocatorType.TAG_NAME.buildLocatorFromString(locator);
        }
        throw new RuntimeException(String.format("Unable to generate By using locator: '%s'!", locator));
    }

    protected By createAppiumBy(FindBy findBy) {
        if (!"".equals(findBy.id())) {
            return AppiumBy.id(findBy.id());
        }

        if (!"".equals(findBy.name())) {
            return AppiumBy.name(findBy.name());
        }

        if (!"".equals(findBy.className())) {
            return AppiumBy.className(findBy.className());
        }

        if (!"".equals(findBy.css())) {
            return AppiumBy.cssSelector(findBy.css());
        }

        if (!"".equals(findBy.tagName())) {
            return AppiumBy.tagName(findBy.tagName());
        }

        if (!"".equals(findBy.linkText())) {
            return AppiumBy.linkText(findBy.linkText());
        }

        if (!"".equals(findBy.partialLinkText())) {
            return AppiumBy.partialLinkText(findBy.partialLinkText());
        }

        if (!"".equals(findBy.xpath())) {
            return AppiumBy.xpath(findBy.xpath());
        }
        return null;
    }

    private By createExtendedBy(String locator) {
        if (LocatorType.ANDROID_UI_AUTOMATOR.is(locator)) {
            return LocatorType.ANDROID_UI_AUTOMATOR.buildLocatorFromString(locator);
        } else if (LocatorType.IOS_CLASS_CHAIN.is(locator)) {
            return LocatorType.IOS_CLASS_CHAIN.buildLocatorFromString(locator);
        } else if (LocatorType.IOS_NS_PREDICATE.is(locator)) {
            return LocatorType.IOS_NS_PREDICATE.buildLocatorFromString(locator);
        } else if (LocatorType.ACCESSIBILITY_ID.is(locator)) {
            return LocatorType.ACCESSIBILITY_ID.buildLocatorFromString(locator);
        } else if (LocatorType.IMAGE.is(locator)) {
            return LocatorType.IMAGE.buildLocatorFromString(locator);
        } else if (LocatorType.XPATH.is(locator)) {
            // for @ExtendedFindBy 'text' attribute L10N supporting
            return LocatorType.XPATH.buildLocatorFromString(locator);
        }
        return null;
    }
}
