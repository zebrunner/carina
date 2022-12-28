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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.pagefactory.AbstractAnnotations;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.zebrunner.carina.utils.factory.DeviceType;

import io.appium.java_client.internal.CapabilityHelpers;
import io.appium.java_client.pagefactory.DefaultElementByBuilder;
import io.appium.java_client.remote.MobileCapabilityType;

public final class ExtendedElementLocatorFactory implements ElementLocatorFactory, IDriverPool {
    private final SearchContext searchContext;
    private final WebDriver webDriver;
    private boolean isRootElementUsed;
    private String platform;
    private String automation;

    public ExtendedElementLocatorFactory(WebDriver webDriver, SearchContext searchContext, boolean isRootElementUsed) {
        this.webDriver = webDriver;
        this.searchContext = searchContext;
        this.isRootElementUsed = isRootElementUsed;
        if (this.webDriver instanceof HasCapabilities) {
            Capabilities caps = ((HasCapabilities) this.webDriver).getCapabilities();
            this.platform = CapabilityHelpers.getCapability(caps, CapabilityType.PLATFORM_NAME, String.class);
            this.automation = CapabilityHelpers.getCapability(caps, MobileCapabilityType.AUTOMATION_NAME, String.class);
        } else {
            this.platform = null;
            this.automation = null;
        }
    }

    public boolean isRootElementUsed() {
        return isRootElementUsed;
    }

    public ElementLocator createLocator(Field field) {
        AbstractAnnotations annotations = null;
        Device currentDevice = getDevice(webDriver);
        if (DeviceType.Type.DESKTOP.equals(currentDevice.getDeviceType())) {
            if (field.getAnnotation(FindBy.class) != null ||
                    field.getAnnotation(FindBys.class) != null ||
                    field.getAnnotation(FindAll.class) != null) {
                annotations = new ExtendedAnnotations(field);
            }
        } else if (field.isAnnotationPresent(ExtendedFindBy.class)) {
            annotations = new ExtendedAnnotations(field);
        } else {
            DefaultElementByBuilder builder = new DefaultElementByBuilder(platform, automation);
            builder.setAnnotated(field);
            annotations = builder;
        }
        if (annotations == null) {
            return null;
        }

        return new ExtendedElementLocator(webDriver, searchContext, field, annotations, currentDevice);
    }
}