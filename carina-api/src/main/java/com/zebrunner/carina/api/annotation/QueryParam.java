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
package com.zebrunner.carina.api.annotation;

import com.zebrunner.carina.api.apitools.annotation.processor.RelatedTo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(QueryParam.List.class)
@Target(value = { ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface QueryParam {

    String key();

    String value();

    @Target(value = { ElementType.TYPE, ElementType.METHOD })
    @Retention(value = RetentionPolicy.RUNTIME)
    @interface List {

        QueryParam[] value();

    }

    @Target(value = { ElementType.PARAMETER })
    @Retention(value = RetentionPolicy.RUNTIME)
    @QueryParam(key = "", value = "")
    @interface Value {

        @RelatedTo(annotationClass = QueryParam.class, field = "key")
        String key();

    }
}
