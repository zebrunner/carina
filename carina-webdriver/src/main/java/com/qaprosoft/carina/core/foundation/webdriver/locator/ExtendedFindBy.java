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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactoryFinder;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
@PageFactoryFinder(ExtendedFindBy.FindByBuilder.class)
public @interface ExtendedFindBy {

    /**
     * The NSPredicate class is used to define logical conditions used to constrain
     * a search either for a fetch or for in-memory filtering.
     *
     * @return iOS NSPredicate
     */
    String iosPredicate() default "";

    /**
     * The Class Chain locator is similar to xpath, but it's faster and can only
     * search direct children elements. For more info see {@link iOSXCUITFindBy#iOSClassChain()}
     *
     * @return iOS class chain
     */
    String iosClassChain() default "";

    /**
     * A String that can build an Android UiSelector or UiScrollable object.
     * Refer to https://developer.android.com/training/testing/ui-automator
     *
     * @return an Android UIAutomator string
     */
    String androidUIAutomator() default "";

    /**
     * It an UI automation accessibility Id which is a convenient to Android and IOS.<br>
     * About iOS accessibility see {@link iOSXCUITFindBy#accessibility()}<br>
     * About Android accessibility see {@link AndroidFindBy#accessibility()}
     *
     * @return an UI automation accessibility Id
     */
    String accessibilityId() default "";

    /**
     * It is a desired data matcher expression.
     *
     * @return a desired data matcher expression
     */
    String androidDataMatcher() default "";

    /**
     * It is a desired view matcher expression.
     *
     * @return a desired view matcher expression
     */
    String androidViewMatcher() default "";
    
    // Images in automation:
    // https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/image-comparison.md
    // How to adjust parameters: 
    // http://appium.io/docs/en/advanced-concepts/image-elements/
    // Real screen sizes for iOS:
    // https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/Displays/Displays.html
    String image() default "";
    
    String text() default "";

    public static class FindByBuilder extends ExtendedFindByBuilder {
        public By buildIt(Object annotation, Field field) {
            ExtendedFindBy findBy = (ExtendedFindBy) annotation;
            return buildByFromShortFindBy(findBy);
        }
    }
}
