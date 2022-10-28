package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.apitools.annotation.AnnotationContext;
import com.qaprosoft.apitools.annotation.AnnotationUtils;
import com.qaprosoft.carina.core.foundation.api.annotation.ContentType;
import com.qaprosoft.carina.core.foundation.api.annotation.Cookie;
import com.qaprosoft.carina.core.foundation.api.annotation.EndpointTemplate;
import com.qaprosoft.carina.core.foundation.api.annotation.EndpointTemplateMethod;
import com.qaprosoft.carina.core.foundation.api.annotation.Header;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestHeadersInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideResponseBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.PathParam;
import com.qaprosoft.carina.core.foundation.api.annotation.PropertiesPath;
import com.qaprosoft.carina.core.foundation.api.annotation.Property;
import com.qaprosoft.carina.core.foundation.api.annotation.QueryParam;
import com.qaprosoft.carina.core.foundation.api.annotation.RequestBody;
import com.qaprosoft.carina.core.foundation.api.annotation.RequestTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.ResponseTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.SuccessfulHttpStatus;
import com.qaprosoft.carina.core.foundation.api.binding.RuntimeMethod;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

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
                .map(context -> new RequestBodyContainer(context.getValue(requestBody -> null, Object::toString, null), context.getAnnotation().json()));
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, ResponseTemplatePath.class)
                .map(context -> context.getValue(ResponseTemplatePath::path, Object::toString, null));
    }

    @Override
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(RuntimeMethod element) {
        return AnnotationUtils.findFirstAnnotationContextByChain(element, SuccessfulHttpStatus.class)
                .map(context -> context.getValue(SuccessfulHttpStatus::status, o -> HttpResponseStatusType.valueOf(o.toString()), null));
    }

    @Override
    public Optional<Map<String, ?>> resolvePathParams(RuntimeMethod element) {
        Map<String, ?> result = AnnotationUtils.findAllAnnotationContextsByChain(element, PathParam.class).stream()
                .filter(context -> context.isValueExist(null))
                .collect(Collectors.toMap(context -> context.getAnnotation().value(), context -> context.getValue(null)));
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveQueryParams(RuntimeMethod element) {
        Map<String, ?> queryParams = AnnotationUtils.findAllAnnotationContextsByChain(element, QueryParam.class).stream()
                .filter(context -> context.isValueExist(QueryParam::value))
                .collect(Collectors.toMap(context -> context.getAnnotation().key(), context -> context.getValue(QueryParam::value, Object::toString, null)));
        Map<String, ?> queryParamsLists = AnnotationUtils.findAllAnnotationContextsByChain(element, QueryParam.List.class).stream()
                .flatMap(list -> Arrays.stream(list.getAnnotation().value()))
                .filter(annotation -> annotation.value() != null)
                .collect(Collectors.toMap(QueryParam::key, QueryParam::value));

        Map<String, ?> result = Stream.of(queryParams.entrySet(), queryParamsLists.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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
                .collect(Collectors.toMap(context -> context.getAnnotation().value(), context -> context.getValue(Property::value)));
        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveHeaders(RuntimeMethod element) {
        Map<String, ?> headers = AnnotationUtils.findAllAnnotationContextsByChain(element, Header.class).stream()
                .filter(context -> context.isValueExist(Header::value))
                .collect(Collectors.toMap(context -> context.getAnnotation().key(), context -> context.getValue(Header::value)));
        Map<String, ?> headerLists = AnnotationUtils.findAllAnnotationContextsByChain(element, Header.List.class).stream()
                .flatMap(list -> Arrays.stream(list.getAnnotation().value()))
                .filter(annotation -> annotation.value() != null)
                .collect(Collectors.toMap(Header::key, Header::value));

        Map<String, ?> result = Stream.of(headers.entrySet(), headerLists.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, ?>> resolveCookies(RuntimeMethod element) {
        Map<String, ?> cookies = AnnotationUtils.findAllAnnotationContextsByChain(element, Cookie.class).stream()
                .filter(context -> context.isValueExist(Cookie::value))
                .collect(Collectors.toMap(context -> context.getAnnotation().key(), context -> context.getValue(Cookie::value), (c1, c2) -> c1));
        Map<String, ?> cookieList = AnnotationUtils.findAllAnnotationContextsByChain(element, Cookie.List.class).stream()
                .flatMap(list -> Arrays.stream(list.getAnnotation().value()))
                .filter(annotation -> annotation.value() != null)
                .collect(Collectors.toMap(Cookie::key, Cookie::value, (c1, c2) -> c1));

        Map<String, ?> result = Stream.of(cookies.entrySet(), cookieList.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Optional.of(result);
    }

    @Override
    public boolean isSupportedType(AnnotatedElement element) {
        return element instanceof RuntimeMethod;
    }
}
