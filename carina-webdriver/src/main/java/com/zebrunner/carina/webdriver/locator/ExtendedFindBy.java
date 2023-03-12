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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactoryFinder;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
@PageFactoryFinder(ExtendedFindBy.FindByBuilder.class)
public @interface ExtendedFindBy {

    String iosPredicate() default "";

    String iosClassChain() default "";

    String androidUIAutomator() default "";

    String accessibilityId() default "";
    
    // Images in automation:
    // https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/image-comparison.md
    // How to adjust parameters: 
    // http://appium.io/docs/en/advanced-concepts/image-elements/
    // Real screen sizes for iOS:
    // https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/Displays/Displays.html
    String image() default "";
    
    String text() default "";

    /**
     * see {@link io.appium.java_client.AppiumBy#androidDataMatcher(String)}
     */
    String androidDataMatcher() default "";

    /**
     * see {@link io.appium.java_client.AppiumBy#androidViewMatcher(String)}}
     */
    String androidViewMatcher() default "";

    /**
     * see {@link io.appium.java_client.AppiumBy#androidViewTag(String)}
     */
    String androidViewTag() default "";

    /**
     * see {@link io.appium.java_client.AppiumBy#custom(String)}
     */
    String custom() default "";

    public static class FindByBuilder extends ExtendedFindByBuilder {
        public By buildIt(Object annotation, Field field) {
            ExtendedFindBy findBy = (ExtendedFindBy) annotation;
            return buildByFromShortFindBy(findBy);
        }
    }
}
