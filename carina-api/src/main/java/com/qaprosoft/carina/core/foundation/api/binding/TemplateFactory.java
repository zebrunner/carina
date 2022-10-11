package com.qaprosoft.carina.core.foundation.api.binding;

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
        InvocationHandler handler = new TemplateInvocationHandler();
        return proxyTemplate(mClass, handler);
    }

    @SuppressWarnings("unchecked")
    private static <M> M proxyTemplate(Class<M> mClass, InvocationHandler handler) {
        return (M) Proxy.newProxyInstance(mClass.getClassLoader(), new Class[] {mClass}, handler);
    }
}
