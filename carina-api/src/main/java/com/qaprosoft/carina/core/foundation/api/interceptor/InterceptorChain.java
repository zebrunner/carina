package com.qaprosoft.carina.core.foundation.api.interceptor;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.api.annotation.LinkedInterceptors;
import com.qaprosoft.carina.core.foundation.api.resolver.ResolverUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public class InterceptorChain {

    private static final List<ApiMethodInterceptor> globalInterceptors;

    private final List<ApiMethodInterceptor> interceptors;
    private final Set<Class<? extends ApiMethodInterceptor>> distinctInterceptors;

    private AbstractApiMethodV2 apiMethod;

    static {
        globalInterceptors = new ArrayList<>();
        ServiceLoader.load(ApiMethodInterceptor.class)
                .forEach(globalInterceptors::add);
    }

    public InterceptorChain(AnnotatedElement anchorElement) {
        this.interceptors = new ArrayList<>();
        this.distinctInterceptors = Collections.synchronizedSet(new HashSet<>());

        processLinkedInterceptors(anchorElement);
    }

    public void onInstantiation(AbstractApiMethodV2 apiMethod) {
        this.apiMethod = apiMethod;
        this.interceptors.forEach(interceptor -> interceptor.onInstantiation(apiMethod));
    }

    public void onBeforeCall() {
        this.interceptors.forEach(interceptor -> interceptor.onBeforeCall(this.apiMethod));
    }

    private void processLinkedInterceptors(AnnotatedElement element) {
        List<LinkedInterceptors> linkedInterceptorsAnnotations = ResolverUtils.resolveAllAnnotatedItemsByChain(element, LinkedInterceptors.class);
        List<Class<? extends ApiMethodInterceptor>> interceptorClasses = linkedInterceptorsAnnotations.stream()
                .map(LinkedInterceptors::classes)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        for (Class<? extends ApiMethodInterceptor> interceptorClass : interceptorClasses) {
            if (!distinctInterceptors.contains(interceptorClass)) {
                distinctInterceptors.add(interceptorClass);

                ApiMethodInterceptor interceptor = createInstance(interceptorClass);
                interceptors.add(interceptor);
            }
        }

        interceptors.addAll(globalInterceptors);

        Collections.reverse(interceptors);
    }

    private static ApiMethodInterceptor createInstance(Class<? extends ApiMethodInterceptor> interceptorClass) {
        try {
            return interceptorClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate %s class", interceptorClass.getSimpleName()), e);
        }
    }
}
