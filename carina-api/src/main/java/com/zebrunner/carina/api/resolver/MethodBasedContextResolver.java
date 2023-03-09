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

import com.zebrunner.carina.api.apitools.annotation.AnnotationContext;
import com.zebrunner.carina.api.apitools.annotation.AnnotationUtils;
import com.zebrunner.carina.api.annotation.ContentType;
import com.zebrunner.carina.api.annotation.Cookie;
import com.zebrunner.carina.api.annotation.EndpointTemplate;
import com.zebrunner.carina.api.annotation.EndpointTemplateMethod;
import com.zebrunner.carina.api.annotation.Header;
import com.zebrunner.carina.api.annotation.HideRequestBodyPartsInLogs;
import com.zebrunner.carina.api.annotation.HideRequestHeadersInLogs;
import com.zebrunner.carina.api.annotation.HideResponseBodyPartsInLogs;
import com.zebrunner.carina.api.annotation.PathParam;
import com.zebrunner.carina.api.annotation.PropertiesPath;
import com.zebrunner.carina.api.annotation.Property;
import com.zebrunner.carina.api.annotation.QueryParam;
import com.zebrunner.carina.api.annotation.RequestBody;
import com.zebrunner.carina.api.annotation.RequestTemplatePath;
import com.zebrunner.carina.api.annotation.ResponseTemplatePath;
import com.zebrunner.carina.api.annotation.SuccessfulHttpStatus;
import com.zebrunner.carina.api.binding.RuntimeMethod;
import com.zebrunner.carina.api.http.HttpMethodType;
import com.zebrunner.carina.api.http.HttpResponseStatus;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodBasedContextResolver implements ContextResolver<RuntimeMethod> {

    @Override
    public Optional<RequestStartLine> resolveUrl(RuntimeMethod element) {
        EndpointTemplateMethod endpointAnnotation = resolveEndpointTemplateMethod(element);
        HttpMethodType methodType = endpointAnnotation.methodType();

        String methodPath = resolveGlobalPath(element.getMethod())
                .map(globalPath -> buildPath(globalPath, endpointAnnotation.url()))
                .orElse(endpointAnnotation.url());

        return Optional.of(new RequestStartLine(methodPath, methodType));
    }

    private EndpointTemplateMethod resolveEndpointTemplateMethod(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element.getMethod(), EndpointTemplateMethod.class)
                .map(AnnotationContext::getAnnotation)
                .orElseThrow(() -> new RuntimeException("Unable to recognize " + EndpointTemplateMethod.class.getName() + " annotation"));
    }

    private Optional<String> resolveGlobalPath(Method method) {
        return AnnotationUtils.findFirstAnnotationContextByChain(method, EndpointTemplate.class)
                .map(AnnotationContext::getAnnotation)
                .map(EndpointTemplate::url);
    }

    private static String buildPath(String... pathSlices) {
        String[] preparedSlices = Arrays.stream(pathSlices)
                .map(MethodBasedContextResolver::preparePathSlice)
                .toArray(String[]::new);
        return String.join("/", preparedSlices);
    }

    private static String preparePathSlice(String pathSlice) {
        if (pathSlice.startsWith("/")) {
            pathSlice = pathSlice.substring(1);
        } else if (pathSlice.endsWith("/")) {
            pathSlice = pathSlice.substring(0, pathSlice.lastIndexOf("/"));
        } else {
            return pathSlice;
        }
        return preparePathSlice(pathSlice);
    }

    @Override
    public Optional<String> resolveContentType(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, ContentType.class)
                .map(context -> context.getValue(ContentType::type, Object::toString, null));
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, HideRequestBodyPartsInLogs.class)
                .map(context -> context.getValue(HideRequestBodyPartsInLogs::paths, o -> (String[]) o, new String[] {}));
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, HideResponseBodyPartsInLogs.class)
                .map(context -> context.getValue(HideResponseBodyPartsInLogs::paths, o -> (String[]) o, new String[] {}));
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, HideRequestHeadersInLogs.class)
                .map(context -> context.getValue(HideRequestHeadersInLogs::headers, o -> (String[]) o, new String[] {}));
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, RequestTemplatePath.class)
                .map(context -> context.getValue(RequestTemplatePath::path, Object::toString, null));
    }

    @Override
    public Optional<RequestBodyContainer> resolveRequestBody(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, RequestBody.class)
                .map(context -> new RequestBodyContainer(context.getValue(requestBody -> null), context.getAnnotation().json()));
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, ResponseTemplatePath.class)
                .map(context -> context.getValue(ResponseTemplatePath::path, Object::toString, null));
    }

    @Override
    public Optional<HttpResponseStatus> resolveSuccessfulHttpStatus(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, SuccessfulHttpStatus.class)
                .map(context -> context.getValue(annotation -> annotation.status().getResponseStatus().withMessageOverride(annotation.messageOverride()), HttpResponseStatus.class::cast, null));
    }

    @Override
    public Optional<Map<String, ?>> resolvePathParams(RuntimeMethod element) {
        Map<String, ?> result = AnnotationUtils.findAllAnnotationContextsByChain(element, PathParam.class).stream()
                .filter(context -> context.isValueExist(null))
                .collect(Collectors.toMap(context -> context.getAnnotation().key(), context -> context.getValue(null), (o, o2) -> o));
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveQueryParams(RuntimeMethod element) {
        Map<String, ?> queryParams = AnnotationUtils.findAllAnnotationContextsByChain(element, QueryParam.class).stream()
                .filter(context -> context.isValueExist(QueryParam::value))
                .collect(Collectors.toMap(context -> context.getAnnotation().key(), context -> context.getValue(QueryParam::value, Object::toString, null), (o, o2) -> o));
        Map<String, ?> queryParamsLists = AnnotationUtils.findAllAnnotationContextsByChain(element, QueryParam.List.class).stream()
                .flatMap(list -> Arrays.stream(list.getAnnotation().value()))
                .filter(annotation -> annotation.value() != null)
                .collect(Collectors.toMap(QueryParam::key, QueryParam::value, (o, o2) -> o));

        Map<String, ?> result = Stream.of(queryParams.entrySet(), queryParamsLists.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, o2) -> o));

        return Optional.of(result);
    }

    @Override
    public Optional<String> resolvePropertiesPath(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, PropertiesPath.class)
                .map(context -> context.getValue(PropertiesPath::path, Object::toString, null));
    }

    @Override
    public Optional<Map<String, ?>> resolveProperties(RuntimeMethod element) {
        Map<String, ?> result = AnnotationUtils.findAllAnnotationContextsByChain(element, Property.class).stream()
                .filter(context -> context.getMaybeValue(Property::value).isPresent())
                .collect(Collectors.toMap(context -> context.getAnnotation().value(), context -> context.getValue(Property::value), (o, o2) -> o));
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveHeaders(RuntimeMethod element) {
        Map<String, ?> headers = AnnotationUtils.findAllAnnotationContextsByChain(element, Header.class).stream()
                .filter(context -> context.isValueExist(Header::value))
                .collect(Collectors.toMap(context -> context.getAnnotation().key(), context -> context.getValue(Header::value), (o, o2) -> o));
        Map<String, ?> headerLists = AnnotationUtils.findAllAnnotationContextsByChain(element, Header.List.class).stream()
                .flatMap(list -> Arrays.stream(list.getAnnotation().value()))
                .filter(annotation -> annotation.value() != null)
                .collect(Collectors.toMap(Header::key, Header::value, (o, o2) -> o));

        Map<String, ?> result = Stream.of(headers.entrySet(), headerLists.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, o2) -> o));

        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveCookies(RuntimeMethod element) {
        Map<String, ?> cookies = AnnotationUtils.findAllAnnotationContextsByChain(element, Cookie.class).stream()
                .filter(context -> context.isValueExist(Cookie::value))
                .collect(Collectors.toMap(context -> context.getAnnotation().key(), context -> context.getValue(Cookie::value), (o, o2) -> o));
        Map<String, ?> cookieList = AnnotationUtils.findAllAnnotationContextsByChain(element, Cookie.List.class).stream()
                .flatMap(list -> Arrays.stream(list.getAnnotation().value()))
                .filter(annotation -> annotation.value() != null)
                .collect(Collectors.toMap(Cookie::key, Cookie::value, (o, o2) -> o));

        Map<String, ?> result = Stream.of(cookies.entrySet(), cookieList.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, o2) -> o));

        return Optional.of(result);
    }

    @Override
    public boolean isSupportedType(AnnotatedElement element) {
        return element instanceof RuntimeMethod;
    }
}
