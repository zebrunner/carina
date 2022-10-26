package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.http.HttpResponseStatusType;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;

interface ContextResolver<E extends AnnotatedElement> {

    Optional<RequestStartLine> resolveUrl(E element);

    Optional<String> resolveContentType(E element);

    Optional<String[]> resolveHiddenRequestBodyPartsInLogs(E element);

    Optional<String[]> resolveHiddenResponseBodyPartsInLogs(E element);

    Optional<String[]> resolveHiddenRequestHeadersInLogs(E element);

    Optional<String> resolveRequestTemplatePath(E element);

    Optional<RequestBodyContainer> resolveRequestBody(E element);

    Optional<String> resolveResponseTemplatePath(E element);

    Optional<HttpResponseStatusType> resolveSuccessfulHttpStatus(E element);

    Optional<Map<String, ?>> resolvePathParams(E element);

    Optional<Map<String, ?>> resolveQueryParams(E element);

    Optional<String> resolvePropertiesPath(E element);

    Optional<Map<String, ?>> resolveProperties(E element);

    Optional<Map<String, ?>> resolveHeaders(E element);

    Optional<Map<String, ?>> resolveCookies(E element);

    boolean isSupportedType(AnnotatedElement element);

}
