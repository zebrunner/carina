package com.qaprosoft.apitools.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;

public class AnnotationContext<A extends Annotation, E extends AnnotatedElement> {

    private final A annotation;
    private final E element;
    private Object value;

    public AnnotationContext(A annotation, E element, Object value) {
        this.annotation = annotation;
        this.element = element;
        this.value = value;
    }

    public AnnotationContext(A annotation, E element) {
        this.annotation = annotation;
        this.element = element;
    }

    @SuppressWarnings("unchecked")
    public <V> V getValue(Function<A, V> valueGetter) {
        return value != null ? (V) value : valueGetter.apply(annotation);
    }

    public A getAnnotation() {
        return annotation;
    }

    public E getElement() {
        return element;
    }

    public Object getValue() {
        return value;
    }
}
