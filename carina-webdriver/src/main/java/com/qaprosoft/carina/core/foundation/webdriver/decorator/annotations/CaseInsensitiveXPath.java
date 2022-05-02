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
package com.qaprosoft.carina.core.foundation.webdriver.decorator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows converting specific parts of the xpath locator to case-insensitive.
 * Applies to the following types of locators: id, name, xpath, linkText.
 * For all types of locators, except for xpath, before converting to case-insensitive, there is
 * a conversion to xpath.
 * The logic of work for web and mobile is different (the presence or absence of a link to the mobile application in
 * Carina's configuration file depends on what logic is performed).
 *
 * You can choose, which part of the locator will be transformed (id, name, text, class).
 *
 * When you convert linkText, there are text() attribute appear, so if you want to convert linkText, you should
 * set text as true
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface CaseInsensitiveXPath {

    /**
     * web: @id
     * mobile: @resource-id
     */
    boolean id() default false;

    /**
     * web: @name
     * mobile: @name
     */
    boolean name() default false;

    /**
     * web: text()
     * mobile: @text, text(), @content-desc
     */
    boolean text() default true;

    /**
     * web: @class
     * mobile: @class
     */
    boolean classAttr() default false;
}
