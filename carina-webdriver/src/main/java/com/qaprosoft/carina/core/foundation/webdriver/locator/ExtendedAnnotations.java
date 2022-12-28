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

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;

import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.AccessibilityId;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.ClassChain;
import com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations.Predicate;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.zebrunner.carina.utils.factory.DeviceType;

import io.appium.java_client.AppiumBy;

public class ExtendedAnnotations extends Annotations {
    private final boolean isIOS;

    public ExtendedAnnotations(Field field, Device device) {
        super(field);
        isIOS = DeviceType.Type.IOS_PHONE.getFamily()
                .equals(device.getDeviceType().getFamily());
    }

    @Override
    public By buildBy() {
        By by = super.buildBy();
        String param = by.toString();
        if (getField().isAnnotationPresent(Predicate.class)) {
            param = StringUtils.remove(param, LocatorType.XPATH.getStartsWith());
            // TODO: analyze howto determine iOS or Android predicate
            // UPDATED 8.0.5: we can detect using Device class
            if (isIOS) {
                by = AppiumBy.iOSNsPredicateString(param);
            } else {
                by = AppiumBy.androidUIAutomator(param);
            }
        } else if (getField().isAnnotationPresent(ClassChain.class)) {
            param = StringUtils.remove(param, LocatorType.XPATH.getStartsWith());
            by = AppiumBy.iOSClassChain(param);
        } else if (getField().isAnnotationPresent(AccessibilityId.class)) {
            param = StringUtils.remove(param, LocatorType.NAME.getStartsWith());
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
