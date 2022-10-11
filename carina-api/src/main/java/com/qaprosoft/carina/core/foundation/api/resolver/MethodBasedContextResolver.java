package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.annotation.ContentType;
import com.qaprosoft.carina.core.foundation.api.annotation.Cookie;
import com.qaprosoft.carina.core.foundation.api.annotation.Endpoint;
import com.qaprosoft.carina.core.foundation.api.annotation.EndpointTemplate;
import com.qaprosoft.carina.core.foundation.api.annotation.Header;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        return findClassAnnotationValue(method, EndpointTemplate.class)
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
        return findAnnotationValue(element.getMethod(), ContentType.class)
                .map(ContentType::type);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(RuntimeMethod element) {
        return findAnnotationValue(element.getMethod(), HideRequestBodyPartsInLogs.class)
                .map(HideRequestBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(RuntimeMethod element) {
        return findAnnotationValue(element.getMethod(), HideResponseBodyPartsInLogs.class)
                .map(HideResponseBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(RuntimeMethod element) {
        return findAnnotationValue(element.getMethod(), HideRequestHeadersInLogs.class)
                .map(HideRequestHeadersInLogs::headers);
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(RuntimeMethod element) {
        return resolveAnnotatedItem(
                "request template path",
                () -> resolveAnnotatedParameterValues(element.getMethod(), RequestTemplatePathParam.class, element.getArgs()),
                () -> createSingleElementList(findAnnotationValue(element.getMethod(), RequestTemplatePath.class)
                        .map(RequestTemplatePath::path)
                        .orElse(null))
        ).flatMap(result -> Optional.of(result.toString()));
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(RuntimeMethod element) {
        return resolveAnnotatedItem(
                "response template path",
                () -> resolveAnnotatedParameterValues(element.getMethod(), ResponseTemplatePathParam.class, element.getArgs()),
                () -> createSingleElementList(findAnnotationValue(element.getMethod(), ResponseTemplatePath.class)
                        .map(ResponseTemplatePath::path)
                        .orElse(null))
        ).flatMap(result -> Optional.of(result.toString()));
    }

    @Override
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(RuntimeMethod element) {
        return resolveAnnotatedItem(
                "response template path",
                () -> resolveAnnotatedParameterValues(element.getMethod(), SuccessfulHttpStatusParam.class, element.getArgs()),
                () -> createSingleElementList(findAnnotationValue(element.getMethod(), SuccessfulHttpStatus.class)
                        .map(SuccessfulHttpStatus::status)
                        .orElse(null))
        ).flatMap(result -> Optional.of(HttpResponseStatusType.valueOf(result.toString())));
    }

    @Override
    public Optional<Map<String, ?>> resolvePathVariables(RuntimeMethod element) {
        Map<String, ?> result = resolveNamedParameters(element.getMethod(), PathVariable.class, PathVariable::value, element.getArgs());
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveQueryParams(RuntimeMethod element) {
        Map<String, ?> result = resolveNamedParameters(element.getMethod(), QueryParam.class, QueryParam::value, element.getArgs());
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveProperties(RuntimeMethod element) {
        Map<String, ?> result = resolveNamedParameters(element.getMethod(), Property.class, Property::value, element.getArgs());
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveHeaders(RuntimeMethod element) {
        Map<String, ?> result = resolveNamedParameters(element.getMethod(), Header.class, Header::key, element.getArgs());
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveCookies(RuntimeMethod element) {
        Map<String, ?> result = resolveNamedParameters(element.getMethod(), Cookie.class, Cookie::key, element.getArgs());
        return Optional.of(result);
    }

    private static <A extends Annotation> Map<String, Object> resolveNamedParameters(Method method, Class<A> annotationClass, Function<A, String> nameGetter, Object... values) {
        Parameter[] parameters = method.getParameters();
        return resolveAnnotatedParameterIndexesStream(method, annotationClass)
                .collect(Collectors.toMap(index -> nameGetter.apply(parameters[index].getAnnotation(annotationClass)), index -> values[index]));
    }

    private static <A extends Annotation> List<?> resolveAnnotatedParameterValues(Method method, Class<A> annotationClass, Object... values) {
        return resolveAnnotatedParameterIndexesStream(method, annotationClass)
                .map(index -> values[index])
                .collect(Collectors.toList());
    }

    private static <A extends Annotation> Stream<Integer> resolveAnnotatedParameterIndexesStream(Method method, Class<A> annotationClass) {
        Parameter[] parameters = method.getParameters();
        return IntStream.range(0, parameters.length)
                .boxed()
                .filter(index -> parameters[index].isAnnotationPresent(annotationClass));
    }

    @SafeVarargs
    private Optional<?> resolveAnnotatedItem(String errorMessageItem, Supplier<List<?>>... valueSuppliers) {
        return Arrays.stream(valueSuppliers)
                .map(Supplier::get)
                .peek(results -> {
                    if (results.size() > 1) {
                        throw new RuntimeException(String.format("The %s cannot be recognized. More than one value was found", errorMessageItem));
                    }
                })
                .filter(results -> results.size() == 1)
                .map(results -> results.get(0))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private static <V> List<V> createSingleElementList(V element) {
        List<V> result = new ArrayList<>();
        result.add(element);
        return result;
    }

    @Override
    public boolean isSupportedType(AnnotatedElement element) {
        return element instanceof RuntimeMethod;
    }
}
