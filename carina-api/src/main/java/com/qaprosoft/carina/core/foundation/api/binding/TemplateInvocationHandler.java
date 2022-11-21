package com.qaprosoft.carina.core.foundation.api.binding;

import com.qaprosoft.apitools.annotation.AnnotationUtils;
import com.qaprosoft.carina.core.foundation.api.AbstractApiMethod;
import com.qaprosoft.carina.core.foundation.api.AbstractApiMethodV2;
import com.qaprosoft.carina.core.foundation.api.MethodBasedApiMethod;
import com.qaprosoft.carina.core.foundation.api.annotation.EndpointTemplateMethod;

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
