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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.decorators.Decorated;
import org.openqa.selenium.support.pagefactory.AbstractAnnotations;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.zebrunner.carina.utils.factory.DeviceType;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.internal.CapabilityHelpers;
import io.appium.java_client.pagefactory.DefaultElementByBuilder;
import io.appium.java_client.remote.MobileCapabilityType;

public final class ExtendedElementLocatorFactory implements ElementLocatorFactory, IDriverPool {
    static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SearchContext searchContext;
    private final WebDriver webDriver;
    private final WebDriver castDriver;
    private final String platform;
    private final String automation;

    public ExtendedElementLocatorFactory(WebDriver webDriver, SearchContext searchContext) {
        this.webDriver = webDriver;
        this.searchContext = searchContext;
        if (this.webDriver instanceof HasCapabilities) {
            Capabilities capabilities = ((HasCapabilities) this.webDriver).getCapabilities();
            this.platform = CapabilityHelpers.getCapability(capabilities, CapabilityType.PLATFORM_NAME, String.class);
            this.automation = CapabilityHelpers.getCapability(capabilities, MobileCapabilityType.AUTOMATION_NAME, String.class);
        } else {
            this.platform = null;
            this.automation = null;
        }
        this.castDriver = webDriver instanceof Decorated<?> ? (WebDriver) ((Decorated<?>) webDriver).getOriginal() : webDriver;
    }

    public ElementLocator createLocator(Field field) {
        AbstractAnnotations annotations = null;
        Device currentDevice = getDevice(webDriver);

        if (!DeviceType.Type.DESKTOP.equals(currentDevice.getDeviceType()) ||
                this.castDriver instanceof AppiumDriver) {
            if (field.isAnnotationPresent(ExtendedFindBy.class)) {
                annotations = new ExtendedAnnotations(field, currentDevice);
            } else {
                // todo add check if there are no FindBy or other annotations
                DefaultElementByBuilder builder = new DefaultElementByBuilder(platform, automation);
                builder.setAnnotated(field);
                annotations = builder;
            }
        } else {
            if (field.getAnnotation(FindBy.class) != null ||
                    field.getAnnotation(FindBys.class) != null ||
                    field.getAnnotation(FindAll.class) != null) {
                annotations = new Annotations(field);
            }
        }

        // todo remove if all is ok
        if (annotations == null) {
            if (field.isAnnotationPresent(FindBy.class) || field.isAnnotationPresent(ExtendedFindBy.class)) {
                LOGGER.warn("Cannot find correct logic of locator's creation. Use old logic. "
                        + "Please, inform Carina Support about this problem. Device: {}\n Field: {}", currentDevice,
                        field);
                annotations = new ExtendedAnnotations(field, currentDevice);
            }
        }

        if (annotations == null) {
            return null;
        }
        return new ExtendedElementLocator(webDriver, searchContext, field, annotations, currentDevice);
    }
}