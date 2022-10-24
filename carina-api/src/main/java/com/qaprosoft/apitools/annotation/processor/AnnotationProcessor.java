package com.qaprosoft.apitools.annotation.processor;

import com.qaprosoft.apitools.annotation.AnnotationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface AnnotationProcessor<E extends AnnotatedElement> {

    <A extends Annotation> Optional<AnnotationContext<A, E>> findFirstAnnotationContext(E element, Class<A> annClass);

    <A extends Annotation> Optional<E> findFirstConditionalElement(E element, Predicate<E> condition);

    <A extends Annotation> List<AnnotationContext<A, E>> findAllAnnotationContexts(E element, Class<A> annClass);

    AnnotatedElement covertToNextLevel(E element);

}
