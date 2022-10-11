package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethod;
import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

interface ContextResolver<E extends AnnotatedElement> {

    Optional<RequestStartLine> resolveUrl(E element);

    Optional<String> resolveContentType(E element);

    Optional<String[]> resolveHiddenRequestBodyPartsInLogs(E element);

    Optional<String[]> resolveHiddenResponseBodyPartsInLogs(E element);

    Optional<String[]> resolveHiddenRequestHeadersInLogs(E element);

    Optional<String> resolveRequestTemplatePath(E element);

    Optional<String> resolveResponseTemplatePath(E element);

    Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(E element);

    Optional<Map<String, ?>> resolvePathVariables(E element);

    Optional<Map<String, ?>> resolveQueryParams(E element);

    Optional<Map<String, ?>> resolveProperties(E element);

    Optional<Map<String, ?>> resolveHeaders(E element);

    Optional<Map<String, ?>> resolveCookies(E element);

    boolean isSupportedType(AnnotatedElement element);

    default <A extends Annotation> Optional<A> findAnnotationValue(AnnotatedElement element, Class<A> annClazz) {
        A result = null;
        if (element.isAnnotationPresent(annClazz)) {
            result = element.getAnnotation(annClazz);
        }
        return Optional.ofNullable(result);
    }

    default <A extends Annotation> Optional<A> findClassAnnotationValue(AnnotatedElement element, Class<A> annCLazz) {
        return findParentClass(element, c -> c.isAnnotationPresent(annCLazz))
                .map(c -> c.getAnnotation(annCLazz));
    }

    default Optional<Class<?>> findParentClass(AnnotatedElement element, Predicate<Class<?>> condition) {
        Class<?> clazz = Member.class.isAssignableFrom(element.getClass()) ? ((Member) element).getDeclaringClass() : (Class<?>) element;
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
