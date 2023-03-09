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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ContextResolverChain {

    private static final List<ContextResolver<? extends AnnotatedElement>> CONTEXT_RESOLVERS = List.of(
            new AnnotationContextResolver(),
            new PropertiesContextResolver(),
            new MethodBasedContextResolver()
    );

    public static Optional<RequestStartLine> resolveUrl(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveUrl(element), element);
    }

    public static Optional<String> resolveContentType(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveContentType(element), element);
    }

    public static Optional<Set<String>> resolveHiddenRequestBodyPartsInLogs(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHiddenRequestBodyPartsInLogs(element), element)
                .map(Set::of);
    }

    public static Optional<Set<String>> resolveHiddenResponseBodyPartsInLogs(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHiddenResponseBodyPartsInLogs(element), element)
                .map(Set::of);
    }

    public static Optional<Set<String>> resolveHiddenRequestHeadersInLogs(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHiddenRequestHeadersInLogs(element), element)
                .map(Set::of);
    }

    public static Optional<String> resolveRequestTemplatePath(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveRequestTemplatePath(element), element);
    }

    public static Optional<RequestBodyContainer> resolveRequestBody(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveRequestBody(element), element);
    }

    public static Optional<String> resolveResponseTemplatePath(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveResponseTemplatePath(element), element);
    }

    public static Optional<HttpResponseStatus> resolveSuccessfulHttpStatus(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveSuccessfulHttpStatus(element), element);
    }

    public static Optional<Map<String, ?>> resolvePathParams(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolvePathParams(element), element);
    }

    public static Optional<Map<String, ?>> resolveQueryParams(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveQueryParams(element), element);
    }

    public static Optional<Map<String, ?>> resolveProperties(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveProperties(element), element);
    }

    public static Optional<String> resolvePropertiesPath(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolvePropertiesPath(element), element);
    }

    public static Optional<Map<String, ?>> resolveHeaders(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHeaders(element), element);
    }

    public static Optional<Map<String, ?>> resolveCookies(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveCookies(element), element);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> getResolverValue(Function<ContextResolver<AnnotatedElement>, Optional<T>> methodCaller, AnnotatedElement element) {
        return CONTEXT_RESOLVERS.stream()
                .filter(contextResolver -> contextResolver.isSupportedType(element))
                .map(contextResolver -> methodCaller.apply((ContextResolver<AnnotatedElement>) contextResolver))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
