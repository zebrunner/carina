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

package com.zebrunner.carina.api.log;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.zebrunner.carina.api.http.ContentTypeEnum;

import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class CarinaRequestBodyLoggingFilter extends RequestLoggingFilter {

    private final PrintStream stream;
    private final boolean shouldPrettyPrint;
    private final Set<String> hiddenPaths;
    private final ContentTypeEnum contentType;

    public CarinaRequestBodyLoggingFilter(boolean shouldPrettyPrint, PrintStream stream, Set<String> hiddenPaths, ContentTypeEnum contentType) {
        Validate.notNull(stream, "Print stream cannot be null");
        this.stream = stream;
        this.shouldPrettyPrint = shouldPrettyPrint;
        this.hiddenPaths = new HashSet<>(hiddenPaths);
        this.contentType = contentType;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        CarinaBodyPrinter.printRequestBody(requestSpec, stream, shouldPrettyPrint, hiddenPaths, contentType);
        return ctx.next(requestSpec, responseSpec);
    }

}
