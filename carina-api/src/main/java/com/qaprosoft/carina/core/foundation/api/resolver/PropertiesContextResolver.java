package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.http.HttpMethodType;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;
import com.zebrunner.carina.utils.R;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

class PropertiesContextResolver implements ContextResolver {

    @Override
    public Optional<RequestStartLine> resolveUrl(Class<?> clazz) {
        return findParentClass(clazz, c -> R.API.containsKey(c.getSimpleName()))
                .map(c -> R.API.get(c.getSimpleName()))
                .map(PropertiesContextResolver::resolveStartLine);
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
    public Optional<String> resolveContentType(Class<?> clazz) {
        return Optional.empty();
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(Class<?> clazz) {
        return Optional.empty();
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(Class<?> clazz) {
        return Optional.empty();
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(Class<?> clazz) {
        return Optional.empty();
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(Class<?> clazz) {
        return Optional.empty();
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(Class<?> clazz) {
        return Optional.empty();
    }

    @Override
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(Class<?> clazz) {
        return Optional.empty();
    }
}
