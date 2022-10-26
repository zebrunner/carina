package com.qaprosoft.apitools.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.function.Function;

public class AnnotationContext<A extends Annotation, E extends AnnotatedElement> {

    private final A annotation;
    private final E element;

    public AnnotationContext(A annotation, E element) {
        this.annotation = annotation;
        this.element = element;
    }

    public <V> V getValue(Function<A, V> valueGetter) {
        return getMaybeValue(valueGetter).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> getMaybeValue(Function<A, V> valueGetter) {
        return Optional.ofNullable(valueGetter.apply(annotation));
    }

    public A getAnnotation() {
        return annotation;
    }

    public E getElement() {
        return element;
    }
}
