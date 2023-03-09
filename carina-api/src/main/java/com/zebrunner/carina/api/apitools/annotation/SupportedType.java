package com.zebrunner.carina.api.apitools.annotation;

import com.zebrunner.carina.api.apitools.annotation.processor.AnnotationProcessor;
import com.zebrunner.carina.api.apitools.annotation.processor.ClassAnnotationProcessor;
import com.zebrunner.carina.api.apitools.annotation.processor.MethodAnnotationProcessor;
import com.zebrunner.carina.api.apitools.annotation.processor.ParameterAnnotationProcessor;
import com.zebrunner.carina.api.binding.RuntimeMethod;
import com.zebrunner.carina.utils.exception.NotSupportedOperationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SupportedType {

    PARAMETER(RuntimeMethod.class, new ParameterAnnotationProcessor()),
    METHOD(Method.class, new MethodAnnotationProcessor()),
    TYPE(Class.class, new ClassAnnotationProcessor());

    private final Class<? extends AnnotatedElement> elementClass;
    private final AnnotationProcessor<? extends AnnotatedElement> annotationProcessor;

    SupportedType(Class<? extends AnnotatedElement> elementClass, AnnotationProcessor<? extends AnnotatedElement> annotationProcessor) {
        this.elementClass = elementClass;
        this.annotationProcessor = annotationProcessor;
    }

    public static List<SupportedType> findSupportedTypesFrom(AnnotatedElement element) {
        return Arrays.stream(SupportedType.values())
                .dropWhile(supportedType -> !supportedType.getElementClass().equals(element.getClass()))
                .collect(Collectors.toList());
    }

    public static List<SupportedType> findSupportedTypesFrom(AnnotatedElement element, AnnotatedElement toElement) {
        return findSupportedTypesFrom(element).stream()
                .takeWhile(supportedType -> !supportedType.getElementClass().equals(toElement))
                .collect(Collectors.toList());
    }

    public static AnnotatedElement findSupportedAnnotatedElementBy(Class<? extends AnnotationProcessor<?>> annProcessorClass) {
        return Arrays.stream(SupportedType.values())
                .filter(supportedType -> supportedType.getAnnotationProcessor().getClass().isAssignableFrom(annProcessorClass))
                .findFirst()
                .map(SupportedType::getElementClass)
                .orElseThrow(() -> new NotSupportedOperationException(String.format("Unable to process annotation processor of type %s", annProcessorClass.getSimpleName())));
    }

    public Class<? extends AnnotatedElement> getElementClass() {
        return elementClass;
    }

    public AnnotationProcessor<? extends AnnotatedElement> getAnnotationProcessor() {
        return annotationProcessor;
    }
}
