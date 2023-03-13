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
package com.qaprosoft.carina.core.foundation.dataprovider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvDataSourceParameters {
    /**
     * Define data provider instance
     *
     * @return String
     */
    String className() default "com.qaprosoft.carina.core.foundation.dataprovider.core.impl.CsvDataProvider";

    /**
     * Define column separator for parsing
     *
     * @return char
     */
    char separator() default ',';

    /**
     * The character to use for quoted elements
     *
     * @return char
     */
    char quote() default '"';

    /**
     * Path to source data file
     *
     * @return String
     */
    String path() default "";

    /**
     * Column names with data that need to be inserted into test.
     * If empty, all columns as keys and data as values will be put into hashMap
     * and can be accessed by column names in test
     *
     * @return String
     */
    String dsArgs() default "";

    /**
     * Column name with unique test identifiers
     *
     * @return String
     */
    String dsUid() default "";

    /**
     * Column name that determines whether to execute test or not
     *
     * @return String
     */
    String executeColumn() default "Execute";

    /**
     * executeColumn value from record which defines to execute test or not
     * (if executeValue equalsIgnoreCase to value from executeColumn -> add test to run)
     *
     * @return String
     */
    String executeValue() default "y";

    /**
     * Static arguments names, which values will be present in every test.
     * Gets value from testNG suite by defined static arguments name
     *
     * @return String
     */
    String staticArgs() default "";

    String groupColumn() default "";

    String testRailColumn() default "";

    String qTestColumn() default "";

    /**
     * Column name, which contains values for test name overriding
     *
     * @return String
     */
    String testMethodColumn() default "";

    String testMethodOwnerColumn() default "";

    String bugColumn() default "";
}