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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateFactory {

    private static final Map<Class<?>, Object> templates = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <M> M prepareTemplate(Class<M> mClass) {
        if (!isClassValid(mClass)) {
            throw new RuntimeException(String.format("Unable to prepare class %s which is not an interface", mClass.getName()));
        }
        return (M) templates.computeIfAbsent(mClass, TemplateFactory::proxyTemplate);
    }

    private static boolean isClassValid(Class<?> mClass) {
        return mClass.isInterface();
    }

    static <M> M proxyTemplate(Class<M> mClass) {
        InvocationHandler handler = new TemplateInvocationHandler(mClass);
        return proxyTemplate(mClass, handler);
    }

    @SuppressWarnings("unchecked")
    private static <M> M proxyTemplate(Class<M> mClass, InvocationHandler handler) {
        return (M) Proxy.newProxyInstance(mClass.getClassLoader(), new Class[] {mClass}, handler);
    }
}
