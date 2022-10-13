package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.annotation.ContentType;
import com.qaprosoft.carina.core.foundation.api.annotation.Cookie;
import com.qaprosoft.carina.core.foundation.api.annotation.CookieParam;
import com.qaprosoft.carina.core.foundation.api.annotation.Endpoint;
import com.qaprosoft.carina.core.foundation.api.annotation.EndpointTemplate;
import com.qaprosoft.carina.core.foundation.api.annotation.Header;
import com.qaprosoft.carina.core.foundation.api.annotation.HeaderParam;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestHeadersInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideResponseBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.PathVariable;
import com.qaprosoft.carina.core.foundation.api.annotation.Property;
import com.qaprosoft.carina.core.foundation.api.annotation.QueryParam;
import com.qaprosoft.carina.core.foundation.api.annotation.RequestTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.RequestTemplatePathParam;
import com.qaprosoft.carina.core.foundation.api.annotation.ResponseTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.ResponseTemplatePathParam;
import com.qaprosoft.carina.core.foundation.api.annotation.SuccessfulHttpStatus;
import com.qaprosoft.carina.core.foundation.api.annotation.SuccessfulHttpStatusParam;
import com.qaprosoft.carina.core.foundation.api.binding.RuntimeMethod;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodBasedContextResolver implements ContextResolver<RuntimeMethod> {

    @Override
    public Optional<RequestStartLine> resolveUrl(RuntimeMethod element) {
        Endpoint endpointAnnotation = element.getAnnotation(Endpoint.class);
        HttpMethodType methodType = endpointAnnotation.methodType();

        String globalPath = resolveGlobalPath(element.getMethod());
        String methodPath = globalPath != null
                ? buildPath(globalPath, endpointAnnotation.url())
                : endpointAnnotation.url();

        return Optional.of(new RequestStartLine(methodPath, methodType));
    }

    private String resolveGlobalPath(AnnotatedElement method) {
        return ResolverUtils.findFirstClassAnnotation(method, EndpointTemplate.class)
                .map(EndpointTemplate::url)
                .orElse(null);
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
        return ResolverUtils.resolveFirstAnnotationValueStartingFromMethod(
                element.getMethod(),
                ContentType.class,
                ContentType::type,
                "content type"
        );
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(RuntimeMethod element) {
        return ResolverUtils.resolveFirstAnnotationValueStartingFromMethod(
                element.getMethod(),
                HideRequestBodyPartsInLogs.class,
                HideRequestBodyPartsInLogs::paths,
                "hidden request body parts"
        );
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(RuntimeMethod element) {
        return ResolverUtils.resolveFirstAnnotationValueStartingFromMethod(
                element.getMethod(),
                HideResponseBodyPartsInLogs.class,
                HideResponseBodyPartsInLogs::paths,
                "hidden response body parts"
        );
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(RuntimeMethod element) {
        return ResolverUtils.resolveFirstAnnotationValueStartingFromMethod(
                element.getMethod(),
                HideRequestHeadersInLogs.class,
                HideRequestHeadersInLogs::headers,
                "hidden request headers"
        );
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(RuntimeMethod element) {
        return ResolverUtils.resolveFirstAnnotationValueStartingFromParameter(
                element.getMethod(),
                RequestTemplatePathParam.class,
                RequestTemplatePath.class,
                Object::toString,
                RequestTemplatePath::path,
                "request template path",
                element.getArgs()
        );
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(RuntimeMethod element) {
        return ResolverUtils.resolveFirstAnnotationValueStartingFromParameter(
                element.getMethod(),
                ResponseTemplatePathParam.class,
                ResponseTemplatePath.class,
                Object::toString,
                ResponseTemplatePath::path,
                "response template path",
                element.getArgs()
        );
    }

    @Override
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(RuntimeMethod element) {
        return ResolverUtils.resolveFirstAnnotationValueStartingFromParameter(
                element.getMethod(),
                SuccessfulHttpStatusParam.class,
                SuccessfulHttpStatus.class,
                obj -> HttpResponseStatusType.valueOf(obj.toString()),
                SuccessfulHttpStatus::status,
                "successful http status",
                element.getArgs()
        );
    }

    @Override
    public Optional<Map<String, ?>> resolvePathVariables(RuntimeMethod element) {
        Map<String, ?> result = ResolverUtils.resolveNamedAnnotatedParameterValues(element.getMethod(), PathVariable.class, PathVariable::value, element.getArgs());
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveQueryParams(RuntimeMethod element) {
        Map<String, ?> result = ResolverUtils.resolveNamedAnnotatedParameterValues(element.getMethod(), QueryParam.class, QueryParam::value, element.getArgs());
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveProperties(RuntimeMethod element) {
        Map<String, ?> result = ResolverUtils.resolveNamedAnnotatedParameterValues(element.getMethod(), Property.class, Property::value, element.getArgs());
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveHeaders(RuntimeMethod element) {
        Map<String, ?> paramHeader = ResolverUtils.resolveNamedAnnotatedParameterValues(element.getMethod(), HeaderParam.class, HeaderParam::key, element.getArgs());
        Map<String, ?> headers = ResolverUtils.resolveAllAnnotatedItemsByChain(element.getMethod(), Header.class).stream()
                .collect(Collectors.toMap(Header::key, Header::value));
        Map<String, ?> headerLists = ResolverUtils.resolveAllAnnotatedItemsByChain(element.getMethod(), Header.List.class).stream()
                .flatMap(list -> Arrays.stream(list.value()))
                .collect(Collectors.toMap(Header::key, Header::value));

        Map<String, ?> result = Stream.of(paramHeader.entrySet(), headers.entrySet(), headerLists.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveCookies(RuntimeMethod element) {
        Map<String, ?> paramCookie = ResolverUtils.resolveNamedAnnotatedParameterValues(element.getMethod(), CookieParam.class, CookieParam::key, element.getArgs());
        Map<String, ?> cookies = ResolverUtils.resolveAllAnnotatedItemsByChain(element.getMethod(), Cookie.class).stream()
                .collect(Collectors.toMap(Cookie::key, Cookie::value));
        Map<String, ?> cookieList = ResolverUtils.resolveAllAnnotatedItemsByChain(element.getMethod(), Cookie.List.class).stream()
                .flatMap(list -> Arrays.stream(list.value()))
                .collect(Collectors.toMap(Cookie::key, Cookie::value));

        Map<String, ?> result = Stream.of(paramCookie.entrySet(), cookies.entrySet(), cookieList.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return Optional.of(result);
    }

    @Override
    public boolean isSupportedType(AnnotatedElement element) {
        return element instanceof RuntimeMethod;
    }
}
