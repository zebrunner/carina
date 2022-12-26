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
package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.apitools.annotation.AnnotationContext;
import com.qaprosoft.apitools.annotation.AnnotationUtils;
import com.qaprosoft.carina.core.foundation.api.annotation.ContentType;
import com.qaprosoft.carina.core.foundation.api.annotation.Cookie;
import com.qaprosoft.carina.core.foundation.api.annotation.Endpoint;
import com.qaprosoft.carina.core.foundation.api.annotation.Header;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestHeadersInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideResponseBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.PropertiesPath;
import com.qaprosoft.carina.core.foundation.api.annotation.RequestTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.ResponseTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.SuccessfulHttpStatus;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AnnotationContextResolver implements ContextResolver<Class<?>> {

    @Override
    public Optional<RequestStartLine> resolveUrl(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, Endpoint.class)
                .map(AnnotationContext::getAnnotation)
                .map(endpoint -> new RequestStartLine(endpoint.url(), endpoint.methodType()));
    }

    @Override
    public Optional<String> resolveContentType(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, ContentType.class)
                .map(AnnotationContext::getAnnotation)
                .map(ContentType::type);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, HideRequestBodyPartsInLogs.class)
                .map(AnnotationContext::getAnnotation)
                .map(HideRequestBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, HideResponseBodyPartsInLogs.class)
                .map(AnnotationContext::getAnnotation)
                .map(HideResponseBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, HideRequestHeadersInLogs.class)
                .map(AnnotationContext::getAnnotation)
                .map(HideRequestHeadersInLogs::headers);
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, RequestTemplatePath.class)
                .map(AnnotationContext::getAnnotation)
                .map(RequestTemplatePath::path);
    }

    @Override
    public Optional<RequestBodyContainer> resolveRequestBody(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, ResponseTemplatePath.class)
                .map(AnnotationContext::getAnnotation)
                .map(ResponseTemplatePath::path);
    }

    @Override
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(Class<?> element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, SuccessfulHttpStatus.class)
                .map(AnnotationContext::getAnnotation)
                .map(SuccessfulHttpStatus::status);
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
        return AnnotationUtils.findFirstAnnotationContextByChain(element, PropertiesPath.class)
                .map(AnnotationContext::getAnnotation)
                .map(PropertiesPath::path);
    }

    @Override
    public Optional<Map<String, ?>> resolveProperties(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolveHeaders(Class<?> element) {
        List<Header> headers = AnnotationUtils.findAllAnnotationContextsByChain(element, Header.class).stream()
                .map(AnnotationContext::getAnnotation)
                .collect(Collectors.toList());

        List<Header> headerLists = AnnotationUtils.findAllAnnotationContextsByChain(element, Header.List.class).stream()
                .map(AnnotationContext::getAnnotation)
                .flatMap(list -> Arrays.stream(list.value()))
                .collect(Collectors.toList());

        Map<String, ?> result = Stream.of(headers, headerLists)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Header::key, Header::value, (o, o2) -> o));

        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveCookies(Class<?> element) {
        List<Cookie> headers = AnnotationUtils.findAllAnnotationContextsByChain(element, Cookie.class).stream()
                .map(AnnotationContext::getAnnotation)
                .collect(Collectors.toList());

        List<Cookie> headerLists = AnnotationUtils.findAllAnnotationContextsByChain(element, Cookie.List.class).stream()
                .map(AnnotationContext::getAnnotation)
                .flatMap(list -> Arrays.stream(list.value()))
                .collect(Collectors.toList());

        Map<String, ?> result = Stream.of(headers, headerLists)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Cookie::key, Cookie::value, (o, o2) -> o));

        return Optional.of(result);
    }

    @Override
    public boolean isSupportedType(AnnotatedElement element) {
        return element instanceof Class;
    }
}
