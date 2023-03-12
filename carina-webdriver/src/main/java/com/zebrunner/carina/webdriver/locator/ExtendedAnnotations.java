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
package com.zebrunner.carina.webdriver.locator;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;

import com.zebrunner.carina.webdriver.decorator.annotations.AccessibilityId;
import com.zebrunner.carina.webdriver.decorator.annotations.ClassChain;
import com.zebrunner.carina.webdriver.decorator.annotations.Predicate;

import io.appium.java_client.AppiumBy;

public class ExtendedAnnotations extends Annotations {

    public ExtendedAnnotations(Field field) {
        super(field);
    }

    @Override
    public By buildBy() {
        By by = super.buildBy();
        String param = by.toString();
        if (getField().isAnnotationPresent(Predicate.class)) {
            param = StringUtils.remove(param, LocatorType.BY_XPATH.getStartsWith());
            // TODO: analyze howto determine iOS or Android predicate
                by = AppiumBy.iOSNsPredicateString(param);
                // AppiumBy.androidUIAutomator(param);
        } else if (getField().isAnnotationPresent(ClassChain.class)) {
            param = StringUtils.remove(param, LocatorType.BY_XPATH.getStartsWith());
            by = AppiumBy.iOSClassChain(param);
        } else if (getField().isAnnotationPresent(AccessibilityId.class)) {
            param = StringUtils.remove(param, LocatorType.BY_NAME.getStartsWith());
            by = AppiumBy.accessibilityId(param);
        } else if (getField().isAnnotationPresent(ExtendedFindBy.class) || getField().isAnnotationPresent(FindBy.class)) {
            by = buildBy(param);
        } else {
            throw new RuntimeException("Something went wrong when try to build locator: " + by);
        }
        return by;
    }

    private By buildBy(String locator) {
        return Arrays.stream(LocatorType.values())
                .filter(type -> type.is(locator))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Unable to generate By using locator: '%s'!", locator)))
                .buildLocatorFromString(locator);
    }
}
