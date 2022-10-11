package com.qaprosoft.carina.core.foundation.api.binding;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethod;
import com.qaprosoft.carina.core.foundation.api.MethodBasedApiMethod;
import com.qaprosoft.carina.core.foundation.api.annotation.Endpoint;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TemplateInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return buildMethod(proxy, method, args);
    }

    private static Object buildMethod(Object proxy, Method method, Object[] args) {
        Object result = null;
        if (isMethodValid(method)) {
            if (!(AbstractApiMethod.class.isAssignableFrom(method.getReturnType()))) {
                throw new RuntimeException(String.format("Method %s should return instance of %s class", method.getName(), AbstractApiMethod.class.getName()));
            }
            AnnotatedElement anchorElement = new RuntimeMethod(proxy, method, args);
            result = new MethodBasedApiMethod(anchorElement);
        }
        return result;
    }

    private static boolean isMethodValid(Method method) {
        return !Modifier.isStatic(method.getModifiers())
                && Modifier.isPublic(method.getModifiers())
                && method.isAnnotationPresent(Endpoint.class);
    }
}
