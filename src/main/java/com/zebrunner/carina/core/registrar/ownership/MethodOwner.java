/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.registrar.ownership;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to specify the owner of the test method/test class.<br>
 * The order of priorities when choosing the owner of the test method: <br>
 * 1. The annotation on a test method containing a suitable plaftorm.<br>
 * 2. The annotation on a test class containing a suitable platform.<br>
 * 3. The annotation on a test method without the specified platform.<br>
 * 4 The annotation on a test class withot the specified platform.<br>
 * <br>
 *
 * The expected platform is determined based on the "capabilities.platformName" in the {@link com.zebrunner.carina.utils.R#CONFIG} (case-insensitive).
 */
@Repeatable(MethodOwner.List.class)
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface MethodOwner {
    String owner() default "";

    String platform() default "";
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface List {
    	MethodOwner[] value();
    }
    
}