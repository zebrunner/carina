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
package com.zebrunner.carina.webdriver.decorator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openqa.selenium.support.FindBy;

/**
 * Convert specific parts of the locator to case-insensitive
 *
 * Support following types of locators: {@link FindBy#id()}, {@link FindBy#name()}, {@link FindBy#xpath()}, {@link FindBy#linkText()}.
 * For all types of locators (except xpath), before converting to case-insensitive, locator converted to xpath
 *
 * You can choose, which part of the locator will be transformed (id, name, text, class).
 *
 * When you convert linkText, there are text() attribute appear, so if you want to convert linkText, you should set text as true
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface CaseInsensitiveXPath {

    /**
     * web, mobile-web: @id
     * mobile-native: @resource-id
     */
    boolean id() default false;

    /**
     * web, mobile-web: @name
     * mobile-native: @name
     */
    boolean name() default false;

    /**
     * web, mobile-web: text()
     * mobile-native: @text, text(), @content-desc
     */
    boolean text() default true;

    /**
     * web, mobile-web: @class
     * mobile-native: @class
     */
    boolean classAttr() default false;
}
