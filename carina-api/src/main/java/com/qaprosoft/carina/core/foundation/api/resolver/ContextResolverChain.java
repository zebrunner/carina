package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ContextResolverChain {

    private static final List<ContextResolver> CONTEXT_RESOLVERS = List.of(
            new AnnotationContextResolver(),
            new PropertiesContextResolver()
    );

    public static Optional<RequestStartLine> resolveUrl(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveUrl(clazz));
    }

    public static Optional<String> resolveContentType(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveContentType(clazz));
    }

    public static Optional<Set<String>> resolveHiddenRequestBodyPartsInLogs(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveHiddenRequestBodyPartsInLogs(clazz))
                .map(Set::of);
    }

    public static Optional<Set<String>> resolveHiddenResponseBodyPartsInLogs(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveHiddenResponseBodyPartsInLogs(clazz))
                .map(Set::of);
    }

    public static Optional<Set<String>> resolveHiddenRequestHeadersInLogs(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveHiddenRequestHeadersInLogs(clazz))
                .map(Set::of);
    }

    public static Optional<String> resolveRequestTemplatePath(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveRequestTemplatePath(clazz));
    }

    public static Optional<String> resolveResponseTemplatePath(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveResponseTemplatePath(clazz));
    }

    public static Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(Class<?> clazz) {
        return getResolverValue(resolver -> resolver.resolveSuccessfulHttpStatus(clazz));
    }

    private static <T> Optional<T> getResolverValue(Function<ContextResolver, Optional<T>> methodCaller) {
        return CONTEXT_RESOLVERS.stream()
                .map(methodCaller)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
