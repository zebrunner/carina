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
package com.zebrunner.carina.api.binding;

import com.zebrunner.carina.api.apitools.annotation.AnnotationUtils;
import com.zebrunner.carina.api.AbstractApiMethod;
import com.zebrunner.carina.api.AbstractApiMethodV2;
import com.zebrunner.carina.api.MethodBasedApiMethod;
import com.zebrunner.carina.api.annotation.EndpointTemplateMethod;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TemplateInvocationHandler implements InvocationHandler {

    private final Class<?> originalClass;

    public TemplateInvocationHandler(Class<?> originalClass) {
        this.originalClass = originalClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return buildMethod(proxy, method, args);
    }

    private Object buildMethod(Object proxy, Method method, Object[] args) {
        Object result;
        if (isMethodValid(method)) {
            if (!(AbstractApiMethod.class.isAssignableFrom(method.getReturnType()))) {
                throw new RuntimeException(String.format("Method %s should return instance of %s class", method.getName(), AbstractApiMethod.class.getName()));
            }
            AnnotatedElement anchorElement = new RuntimeMethod(proxy, method, args);
            result = createAnchorElementBasedInstance(method.getReturnType(), anchorElement);
        } else {
            result = invokeNotSuitableMethod(originalClass, proxy, method, args);
        }
        return result;
    }

    private static boolean isMethodValid(Method method) {
        return !Modifier.isStatic(method.getModifiers())
                && !method.isDefault()
                && Modifier.isPublic(method.getModifiers())
                && AnnotationUtils.isAnnotatedPresent(method, EndpointTemplateMethod.class);
    }

    private static Object createAnchorElementBasedInstance(Class<?> targetClass, AnnotatedElement anchorElement) {
        try {
            return AbstractApiMethod.class.equals(targetClass) || AbstractApiMethodV2.class.equals(targetClass)
                    ? new MethodBasedApiMethod(anchorElement)
                    : targetClass.getDeclaredConstructor(AnnotatedElement.class).newInstance(anchorElement);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Object invokeNotSuitableMethod(Class<?> originalClass, Object proxy, Method method, Object[] args) {
        try {
            return MethodHandles.lookup()
                    .findSpecial(
                            originalClass,
                            method.getName(),
                            MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                            originalClass
                    )
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
