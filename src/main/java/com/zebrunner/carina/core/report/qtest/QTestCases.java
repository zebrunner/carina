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
package com.zebrunner.carina.core.report.qtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This defines the 'QTestCases' annotation used to specify the
 * qTest test case id values that the TestNG test maps to
 */

@Repeatable(QTestCases.List.class)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QTestCases {
    String id();

    String platform() default "";
    String locale() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @interface List {
        QTestCases[] value();
    }
}
