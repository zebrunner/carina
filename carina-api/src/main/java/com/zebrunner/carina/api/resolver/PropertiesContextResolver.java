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

import com.zebrunner.carina.api.apitools.annotation.AnnotationUtils;
import com.zebrunner.carina.api.http.HttpMethodType;
import com.zebrunner.carina.api.http.HttpResponseStatus;
import com.zebrunner.carina.utils.R;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;

class PropertiesContextResolver implements ContextResolver<Class<?>> {

    @Override
    public Optional<RequestStartLine> resolveUrl(Class<?> element) {
        try {
            return AnnotationUtils.findFirstConditionalElementByChain(element, el -> R.API.containsKey(((Class<?>) el).getSimpleName()))
                    .map(c -> (Class<?>) c)
                    .map(c -> R.API.get(c.getSimpleName()))
                    .map(PropertiesContextResolver::resolveStartLine);
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format("While searching for an url and type for a %s class in a properties file. %s", element.getSimpleName(), e.getMessage()), e);
        }
    }

    private static RequestStartLine resolveStartLine(String typePath) {
        RequestStartLine startLine;
        if (typePath.contains(":")) {
            HttpMethodType methodType = HttpMethodType.valueOf(typePath.split(":")[0]);
            String methodPath = StringUtils.substringAfter(typePath, methodType + ":");
            startLine = new RequestStartLine(methodPath, methodType);
        } else {
            startLine = new RequestStartLine(HttpMethodType.valueOf(typePath));
        }
        return startLine;
    }

    @Override
    public Optional<String> resolveContentType(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<RequestBodyContainer> resolveRequestBody(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpResponseStatus> resolveSuccessfulHttpStatus(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolvePathParams(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolveQueryParams(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String> resolvePropertiesPath(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolveProperties(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolveHeaders(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolveCookies(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public boolean isSupportedType(AnnotatedElement element) {
        return element instanceof Class;
    }
}
