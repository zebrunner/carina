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
public @interface XlsDataSourceParameters {
    /**
     * Define data provider instance
     *
     * @return String
     */
    String className() default "com.qaprosoft.carina.core.foundation.dataprovider.core.impl.XlsDataProvider";

    String spreadsheetId() default "";

    String sheet() default "";

    /**
     * Path to data source file
     *
     * @return String
     */
    String path() default "";

    /**
     * Column names that need to be inserted into test from row. Several arguments should be split by ",".
     * If empty, per row will be created hashMap with all data source columns as keys and row data as values.
     *
     * @return String
     */
    String dsArgs() default "";

    /**
     * Column name with unique test identifiers. Several name arguments should be split by ",".
     *
     * @return String
     */
    String dsUid() default "";

    /**
     * Column name which rows determines whether to execute test or not
     *
     * @return String
     */
    String executeColumn() default "Execute";

    /**
     * Defines to execute test or not.
     * If executeValue equalsIgnoreCase to value from executeColumn -> add test row to run.
     *
     * @return String
     */
    String executeValue() default "y";

    /**
     * Gets value from testNG suite by defined static arguments name.
     * The same argument will present in every test
     *
     * @return String
     */
    String staticArgs() default "";

    String groupColumn() default "";

    /**
     * Column name, which contains values for test name overriding
     *
     * @return String
     */
    String testMethodColumn() default "";

    /**
     * Reason: not implemented
     */
    @Deprecated(forRemoval = true, since = "1.0.0")
    String testRailColumn() default "";

    /**
     * Reason: not implemented
     */
    @Deprecated(forRemoval = true, since = "1.0.0")
    String qTestColumn() default "";

    /**
     * Reason: not implemented
     */
    @Deprecated(forRemoval = true, since = "1.0.0")
    String testMethodOwnerColumn() default "";

    /**
     * Reason: not implemented
     */
    @Deprecated(forRemoval = true, since = "1.0.0")
    String bugColumn() default "";

    /**
     * Reason: not implemented
     */
    @Deprecated(forRemoval = true, since = "1.0.0")
    String[] doNotRunTestNames() default {};
}
