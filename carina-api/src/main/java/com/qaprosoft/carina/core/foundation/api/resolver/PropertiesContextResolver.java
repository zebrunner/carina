package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.apitools.annotation.AnnotationUtils;
import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.qaprosoft.carina.core.foundation.utils.R;
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
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(Class<?> element) {
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
