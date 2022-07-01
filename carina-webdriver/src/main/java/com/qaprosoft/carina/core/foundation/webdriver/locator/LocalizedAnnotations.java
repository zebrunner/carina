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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.resources.L10N;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.AccessibilityId;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.ClassChain;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Predicate;

import io.appium.java_client.MobileBy;

public class LocalizedAnnotations extends Annotations {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Pattern L10N_PATTERN = Pattern.compile(SpecialKeywords.L10N_PATTERN);

    public LocalizedAnnotations(Field field) {
        super(field);
    }

    @Override
    public By buildBy() {

        By by = super.buildBy();
        String param = by.toString();
        
        // replace by using localization pattern
        Matcher matcher = L10N_PATTERN.matcher(param);
        while (matcher.find()) {
            int start = param.indexOf(SpecialKeywords.L10N + ":") + 5;
            int end = param.indexOf("}");
            String key = param.substring(start, end);
            param = StringUtils.replace(param, matcher.group(), L10N.getText(key));
        }
        Field field = getField();

        if (field.isAnnotationPresent(Predicate.class)) {
            // TODO: analyze howto determine iOS or Android predicate
            param = StringUtils.remove(param, "By.xpath: ");
            by = MobileBy.iOSNsPredicateString(param);
            // by = MobileBy.AndroidUIAutomator(param);
        } else if (field.isAnnotationPresent(ClassChain.class)) {
            param = StringUtils.remove(param, "By.xpath: ");
            by = MobileBy.iOSClassChain(param);
        } else if (field.isAnnotationPresent(AccessibilityId.class)) {
            param = StringUtils.remove(param, "By.name: ");
            by = MobileBy.AccessibilityId(param);
        } else if (field.isAnnotationPresent(ExtendedFindBy.class)) {
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
        String resultLocator = locator;
        // Example: id=, name=, By.linkText: , By.className: , cssSelector=
        Pattern patternWithEquals = Pattern.compile("^(?<prefix>(((id|name|xpath|linkText|partialLinkText|cssSelector|css|tagName|className)=)|" +
                "(By\\.(id|name|xpath|linkText|partialLinkText|css|cssSelector|className|tagName): )))");
        Matcher matcherWithEquals = patternWithEquals.matcher(locator);

        if (!matcherWithEquals.find()) {
            throw new RuntimeException(String.format("Unable to generate By using locator: '%s'!", locator));
        }
        String prefix = matcherWithEquals.group("prefix");
        resultLocator = StringUtils.replace(resultLocator, prefix, "");
        By by;
        switch (prefix) {
        case "id=":
        case "By.id: ":
            by = By.id(resultLocator);
            break;
        case "name=":
        case "By.name: ":
            by = By.name(resultLocator);
            break;
        case "xpath=":
        case "By.xpath: ":
            by = By.xpath(resultLocator);
            break;
        case "linkText=":
        case "By.linkText: ":
            by = By.linkText(resultLocator);
            break;
        case "partialLinkText=":
        case "By.partialLinkText: ":
            by = By.partialLinkText(resultLocator);
            break;
        case "cssSelector=":
        case "By.cssSelector: ":
        case "css=":
        case "By.css: ":
            by = By.cssSelector(resultLocator);
            break;
        case "tagName=":
        case "By.tagName: ":
            by = By.tagName(resultLocator);
            break;
        case "className=":
        case "By.className: ":
            by = By.className(resultLocator);
            break;
        default:
            throw new RuntimeException(String.format("Unable to generate By with prefix: '%s'!", prefix));
        }
        return by;
    }

    private By createExtendedBy(String locator) {
        if (locator.startsWith("By.AndroidUIAutomator: ")) {
            return MobileBy.AndroidUIAutomator(StringUtils.remove(locator, "By.AndroidUIAutomator: "));
        } else if (locator.startsWith("By.IosClassChain: ")) {
            return MobileBy.iOSClassChain(StringUtils.remove(locator, "By.IosClassChain: "));
        } else if (locator.startsWith("By.IosNsPredicate: ")) {
            return MobileBy.iOSNsPredicateString(StringUtils.remove(locator, "By.IosNsPredicate: "));
        } else if (locator.startsWith("By.xpath: ")) { // for @ExtendedFindBy 'text' attribute L10N supporting
            return By.xpath(StringUtils.remove(locator, "By.xpath: "));
        }

        return null;
    }
}
