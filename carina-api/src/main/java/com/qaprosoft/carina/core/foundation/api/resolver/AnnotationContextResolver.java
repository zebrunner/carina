package com.qaprosoft.carina.core.foundation.api.resolver;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AnnotationContextResolver implements ContextResolver<Class<?>> {

    @Override
    public Optional<RequestStartLine> resolveUrl(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, Endpoint.class)
                .map(endpoint -> new RequestStartLine(endpoint.url(), endpoint.methodType()));
    }

    @Override
    public Optional<String> resolveContentType(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, ContentType.class)
                .map(ContentType::type);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, HideRequestBodyPartsInLogs.class)
                .map(HideRequestBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, HideResponseBodyPartsInLogs.class)
                .map(HideResponseBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, HideRequestHeadersInLogs.class)
                .map(HideRequestHeadersInLogs::headers);
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, RequestTemplatePath.class)
                .map(RequestTemplatePath::path);
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, ResponseTemplatePath.class)
                .map(ResponseTemplatePath::path);
    }

    @Override
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, SuccessfulHttpStatus.class)
                .map(SuccessfulHttpStatus::status);
    }

    @Override
    public Optional<Map<String, ?>> resolvePathVariables(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolveQueryParams(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<String> resolvePropertiesPath(Class<?> element) {
        return ResolverUtils.findFirstClassAnnotation(element, PropertiesPath.class)
                .map(PropertiesPath::path);
    }

    @Override
    public Optional<Map<String, ?>> resolveProperties(Class<?> element) {
        return Optional.empty();
    }

    @Override
    public Optional<Map<String, ?>> resolveHeaders(Class<?> element) {
        Map<String, ?> headers = ResolverUtils.resolveAllAnnotatedItemsByChain(element, Header.class).stream()
                .collect(Collectors.toMap(Header::key, Header::value));
        Map<String, ?> headerLists = ResolverUtils.resolveAllAnnotatedItemsByChain(element, Header.List.class).stream()
                .flatMap(list -> Arrays.stream(list.value()))
                .collect(Collectors.toMap(Header::key, Header::value));

        Map<String, ?> result = Stream.of(headers.entrySet(), headerLists.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveCookies(Class<?> element) {
        Map<String, ?> cookies = ResolverUtils.resolveAllAnnotatedItemsByChain(element, Cookie.class).stream()
                .collect(Collectors.toMap(Cookie::key, Cookie::value));
        Map<String, ?> cookieList = ResolverUtils.resolveAllAnnotatedItemsByChain(element, Cookie.List.class).stream()
                .flatMap(list -> Arrays.stream(list.value()))
                .collect(Collectors.toMap(Cookie::key, Cookie::value));

        Map<String, ?> result = Stream.of(cookies.entrySet(), cookieList.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return Optional.of(result);
    }

    @Override
    public boolean isSupportedType(AnnotatedElement element) {
        return element instanceof Class;
    }
}
