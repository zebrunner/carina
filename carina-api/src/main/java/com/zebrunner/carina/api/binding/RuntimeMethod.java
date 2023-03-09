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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class RuntimeMethod implements AnnotatedElement, Member {

    private final Object object;
    private final Method method;
    private final Object[] args;

    public RuntimeMethod(Object object, Method method, Object[] args) {
        this.object = object;
        this.method = method;
        this.args = args;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return method.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return method.getDeclaredAnnotations();
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public int getModifiers() {
        return method.getModifiers();
    }

    @Override
    public boolean isSynthetic() {
        return method.isSynthetic();
    }
}
