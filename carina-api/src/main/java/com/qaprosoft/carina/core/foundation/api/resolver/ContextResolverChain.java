package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ContextResolverChain {

    private static final List<ContextResolver<? extends AnnotatedElement>> CONTEXT_RESOLVERS = List.of(
            new AnnotationContextResolver(),
            new PropertiesContextResolver(),
            new MethodBasedContextResolver()
    );

    public static Optional<RequestStartLine> resolveUrl(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveUrl(element), element);
    }

    public static Optional<String> resolveContentType(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveContentType(element), element);
    }

    public static Optional<Set<String>> resolveHiddenRequestBodyPartsInLogs(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHiddenRequestBodyPartsInLogs(element), element)
                .map(Set::of);
    }

    public static Optional<Set<String>> resolveHiddenResponseBodyPartsInLogs(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHiddenResponseBodyPartsInLogs(element), element)
                .map(Set::of);
    }

    public static Optional<Set<String>> resolveHiddenRequestHeadersInLogs(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHiddenRequestHeadersInLogs(element), element)
                .map(Set::of);
    }

    public static Optional<String> resolveRequestTemplatePath(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveRequestTemplatePath(element), element);
    }

    public static Optional<RequestBodyContainer> resolveRequestBody(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveRequestBody(element), element);
    }

    public static Optional<String> resolveResponseTemplatePath(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveResponseTemplatePath(element), element);
    }

    public static Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveSuccessfulHttpStatus(element), element);
    }

    public static Optional<Map<String, ?>> resolvePathVariables(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolvePathVariables(element), element);
    }

    public static Optional<Map<String, ?>> resolveQueryParams(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveQueryParams(element), element);
    }

    public static Optional<Map<String, ?>> resolveProperties(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveProperties(element), element);
    }

    public static Optional<String> resolvePropertiesPath(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolvePropertiesPath(element), element);
    }

    public static Optional<Map<String, ?>> resolveHeaders(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveHeaders(element), element);
    }

    public static Optional<Map<String, ?>> resolveCookies(AnnotatedElement element) {
        return getResolverValue(resolver -> resolver.resolveCookies(element), element);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> getResolverValue(Function<ContextResolver<AnnotatedElement>, Optional<T>> methodCaller, AnnotatedElement element) {
        return CONTEXT_RESOLVERS.stream()
                .filter(contextResolver -> contextResolver.isSupportedType(element))
                .map(contextResolver -> methodCaller.apply((ContextResolver<AnnotatedElement>) contextResolver))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
