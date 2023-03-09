/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.api.interceptor;

import com.zebrunner.carina.api.apitools.annotation.AnnotationContext;
import com.zebrunner.carina.api.apitools.annotation.AnnotationUtils;
import com.zebrunner.carina.api.AbstractApiMethod;
import com.zebrunner.carina.api.AbstractApiMethodV2;
import com.zebrunner.carina.api.annotation.LinkedInterceptors;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InterceptorChain {

    private static final List<ApiMethodInterceptor<AbstractApiMethod>> globalInterceptors;

    private final AbstractApiMethod apiMethod;
    private final List<ApiMethodInterceptor<AbstractApiMethod>> interceptors;
    private final Set<Class<? extends ApiMethodInterceptor<? extends AbstractApiMethod>>> distinctInterceptors;

    static {
        globalInterceptors = new ArrayList<>();
        ServiceLoader.load(ApiMethodInterceptor.class)
                .forEach(globalInterceptors::add);
    }

    public InterceptorChain(AbstractApiMethod apiMethod, AnnotatedElement anchorElement) {
        this.apiMethod = apiMethod;
        this.interceptors = new ArrayList<>();
        this.distinctInterceptors = Collections.synchronizedSet(new HashSet<>());

        processLinkedInterceptors(anchorElement);
    }

    public void onInstantiation() {
        doAction(interceptor -> interceptor.onInstantiation(this.apiMethod));
    }

    public void onBeforeCall() {
        doAction(interceptor -> interceptor.onBeforeCall(this.apiMethod));
    }

    public void onAfterCall() {
        doAction(interceptor -> interceptor.onAfterCall(this.apiMethod));
    }

    private void doAction(Consumer<ApiMethodInterceptor<AbstractApiMethod>> action) {
        this.interceptors.forEach(interceptor -> doAction(interceptor, action));
    }

    private void doAction(ApiMethodInterceptor<AbstractApiMethod> interceptor, Consumer<ApiMethodInterceptor<AbstractApiMethod>> action) {
        try {
            action.accept(interceptor);
        } catch (ClassCastException e) {
            throw new RuntimeException(String.format(
                    "Unable to use the %s interceptor. Make sure your interceptor's generic type is %s",
                    interceptor.getClass().getSimpleName(),
                    recognizeApiMethodSuperClass(this.apiMethod.getClass()).getSimpleName()
            ), e);
        }
    }

    private Class<?> recognizeApiMethodSuperClass(Class<?> apiMethodClass) {
        boolean baseApiMethodClass = AbstractApiMethodV2.class.getName().equals(apiMethodClass.getName()) || AbstractApiMethod.class.getName().equals(apiMethodClass.getName());
        if (!baseApiMethodClass) {
            return recognizeApiMethodSuperClass(apiMethodClass.getSuperclass());
        }
        return apiMethodClass;
    }

    @SuppressWarnings("unchecked")
    private void processLinkedInterceptors(AnnotatedElement element) {
        List<LinkedInterceptors> linkedInterceptorsAnnotations = AnnotationUtils.findAllAnnotationContextsByChain(element, LinkedInterceptors.class).stream()
                .map(AnnotationContext::getAnnotation)
                .collect(Collectors.toList());

        Collections.reverse(linkedInterceptorsAnnotations);

        List<Class<? extends ApiMethodInterceptor<? extends AbstractApiMethod>>> interceptorClasses = linkedInterceptorsAnnotations.stream()
                .map(LinkedInterceptors::classes)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        for (Class<? extends ApiMethodInterceptor<? extends AbstractApiMethod>> interceptorClass : interceptorClasses) {
            if (!distinctInterceptors.contains(interceptorClass)) {
                distinctInterceptors.add(interceptorClass);

                ApiMethodInterceptor<? extends AbstractApiMethod> interceptor = createInstance(interceptorClass);
                interceptors.add((ApiMethodInterceptor<AbstractApiMethod>) interceptor);
            }
        }

        interceptors.addAll(0, globalInterceptors);
    }

    private static ApiMethodInterceptor<? extends AbstractApiMethod> createInstance(Class<? extends ApiMethodInterceptor<? extends AbstractApiMethod>> interceptorClass) {
        try {
            return interceptorClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate %s class", interceptorClass.getSimpleName()), e);
        }
    }
}
