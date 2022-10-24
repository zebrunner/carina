package com.qaprosoft.apitools.annotation.processor;

import com.qaprosoft.apitools.annotation.AnnotationContext;
import com.qaprosoft.apitools.annotation.AnnotationProcessorUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MethodAnnotationProcessor implements AnnotationProcessor<Method> {

    @Override
    public <A extends Annotation> Optional<AnnotationContext<A, Method>> findFirstAnnotationContext(Method element, Class<A> annClass) {
        return AnnotationProcessorUtils.getAnnotation(element, annClass)
                .map((A annotation) -> new AnnotationContext<>(annotation, element));
    }

    @Override
    public <A extends Annotation> Optional<Method> findFirstConditionalElement(Method element, Predicate<Method> condition) {
        return condition.test(element) ? Optional.of(element) : Optional.empty();
    }

    @Override
    public <A extends Annotation> List<AnnotationContext<A, Method>> findAllAnnotationContexts(Method element, Class<A> annClass) {
        return AnnotationProcessorUtils.getAnnotation(element, annClass)
                .map(annotation -> new AnnotationContext<>(annotation, element))
                .map(List::of)
                .orElse(new ArrayList<>());
    }

    @Override
    public AnnotatedElement covertToNextLevel(Method element) {
        return element.getDeclaringClass();
    }
}
