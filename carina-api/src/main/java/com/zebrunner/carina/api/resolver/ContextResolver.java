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
package com.zebrunner.carina.api.resolver;

import com.zebrunner.carina.api.http.HttpResponseStatus;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;

interface ContextResolver<E extends AnnotatedElement> {

    Optional<RequestStartLine> resolveUrl(E element);

    Optional<String> resolveContentType(E element);

    Optional<String[]> resolveHiddenRequestBodyPartsInLogs(E element);

    Optional<String[]> resolveHiddenResponseBodyPartsInLogs(E element);

    Optional<String[]> resolveHiddenRequestHeadersInLogs(E element);

    Optional<String> resolveRequestTemplatePath(E element);

    Optional<RequestBodyContainer> resolveRequestBody(E element);

    Optional<String> resolveResponseTemplatePath(E element);

    Optional<HttpResponseStatus> resolveSuccessfulHttpStatus(E element);

    Optional<Map<String, ?>> resolvePathParams(E element);

    Optional<Map<String, ?>> resolveQueryParams(E element);

    Optional<String> resolvePropertiesPath(E element);

    Optional<Map<String, ?>> resolveProperties(E element);

    Optional<Map<String, ?>> resolveHeaders(E element);

    Optional<Map<String, ?>> resolveCookies(E element);

    boolean isSupportedType(AnnotatedElement element);

}
