package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.annotation.ContentType;
import com.qaprosoft.carina.core.foundation.api.annotation.Endpoint;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideRequestHeadersInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.HideResponseBodyPartsInLogs;
import com.qaprosoft.carina.core.foundation.api.annotation.RequestTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.ResponseTemplatePath;
import com.qaprosoft.carina.core.foundation.api.annotation.SuccessfulHttpStatus;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.lang.annotation.Annotation;
import java.util.Optional;

class AnnotationContextResolver implements ContextResolver {

    @Override
    public Optional<RequestStartLine> resolveUrl(Class<?> clazz) {
        return findAnnotationValue(clazz, Endpoint.class)
                .map(endpoint -> new RequestStartLine(endpoint.url(), endpoint.methodType()));
    }

    @Override
    public Optional<String> resolveContentType(Class<?> clazz) {
        return findAnnotationValue(clazz, ContentType.class)
                .map(ContentType::type);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestBodyPartsInLogs(Class<?> clazz) {
        return findAnnotationValue(clazz, HideRequestBodyPartsInLogs.class)
                .map(HideRequestBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenResponseBodyPartsInLogs(Class<?> clazz) {
        return findAnnotationValue(clazz, HideResponseBodyPartsInLogs.class)
                .map(HideResponseBodyPartsInLogs::paths);
    }

    @Override
    public Optional<String[]> resolveHiddenRequestHeadersInLogs(Class<?> clazz) {
        return findAnnotationValue(clazz, HideRequestHeadersInLogs.class)
                .map(HideRequestHeadersInLogs::headers);
    }

    @Override
    public Optional<String> resolveRequestTemplatePath(Class<?> clazz) {
        return findAnnotationValue(clazz, RequestTemplatePath.class)
                .map(RequestTemplatePath::path);
    }

    @Override
    public Optional<String> resolveResponseTemplatePath(Class<?> clazz) {
        return findAnnotationValue(clazz, ResponseTemplatePath.class)
                .map(ResponseTemplatePath::path);
    }

    @Override
    public Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(Class<?> clazz) {
        return findAnnotationValue(clazz, SuccessfulHttpStatus.class)
                .map(SuccessfulHttpStatus::status);
    }

    private <A extends Annotation> Optional<A> findAnnotationValue(Class<?> inClass, Class<A> annCLazz) {
        return findParentClass(inClass, c -> c.isAnnotationPresent(annCLazz))
                .map(c -> c.getAnnotation(annCLazz));
    }
}
