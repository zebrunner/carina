package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethod;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.util.Optional;
import java.util.function.Predicate;

interface ContextResolver {

    Optional<RequestStartLine> resolveUrl(Class<?> clazz);

    Optional<String> resolveContentType(Class<?> clazz);

    Optional<String[]> resolveHiddenRequestBodyPartsInLogs(Class<?> clazz);

    Optional<String[]> resolveHiddenResponseBodyPartsInLogs(Class<?> clazz);

    Optional<String[]> resolveHiddenRequestHeadersInLogs(Class<?> clazz);

    Optional<String> resolveRequestTemplatePath(Class<?> clazz);

    Optional<String> resolveResponseTemplatePath(Class<?> clazz);

    Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(Class<?> clazz);

    default Optional<Class<?>> findParentClass(Class<?> clazz, Predicate<Class<?>> condition) {
        return findParentClass(clazz, AbstractApiMethod.class, condition);
    }

    default Optional<Class<?>> findParentClass(Class<?> clazz, Class<?> untilClass, Predicate<Class<?>> condition) {
        Optional<Class<?>> result;
        if (clazz != null && !clazz.equals(untilClass)) {
            if (!condition.test(clazz)) {
                return findParentClass(clazz.getSuperclass(), untilClass, condition);
            } else {
                result = Optional.of(clazz);
            }
        } else {
            result = Optional.empty();
        }
        return result;
    }
}
