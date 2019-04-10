/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
    String className() default "com.qaprosoft.carina.core.foundation.dataprovider.core.impl.XlsDataProvider";

    /**
     * Is mutually exclusive with spreadsheetId
     */
    String path() default "";

    /**
     * Is mutually exclusive with path
     */
    String spreadsheetId() default "";

    String sheet() default "";

    String dsArgs() default "";

    String dsUid() default "";

    String executeColumn() default "";

    String executeValue() default "";

    String staticArgs() default "";

    String groupColumn() default "";

    String jiraColumn() default "";

    String testRailColumn() default "";

    String qTestColumn() default "";

    String testMethodColumn() default "";

    String testMethodOwnerColumn() default "";

    String bugColumn() default "";

    String[] doNotRunTestNames() default {};
}
