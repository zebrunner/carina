package com.zebrunner.carina.api.apitools.annotation;

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

    public <V> V getValue(Function<A, ?> valueGetter, Function<Object, V> valueConverter, V other) {
        return getMaybeValue(valueGetter)
                .map(valueConverter)
                .orElse(other);
    }

    public Object getValue(Function<A, ?> valueGetter, Object other) {
        return getValue(valueGetter, o -> o, other);
    }

    public Object getValue(Function<A, ?> valueGetter) {
        return getValue(valueGetter, null);
    }

    public Optional<Object> getMaybeValue(Function<A, ?> valueGetter) {
        return Optional.ofNullable(valueGetter.apply(annotation));
    }

    public boolean isValueExist(Function<A, ?> valueGetter) {
        return getMaybeValue(valueGetter).isPresent();
    }

    public A getAnnotation() {
        return annotation;
    }

    public E getElement() {
        return element;
    }
}
