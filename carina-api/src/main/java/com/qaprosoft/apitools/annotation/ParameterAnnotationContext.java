package com.qaprosoft.apitools.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.function.Function;

public class ParameterAnnotationContext<A extends Annotation, E extends AnnotatedElement> extends AnnotationContext<A, E> {

    private final Object value;

    public ParameterAnnotationContext(A annotation, E element, Object value) {
        super(annotation, element);
        this.value = value;
    }

    @Override
    public <V> V getValue(Function<A, V> valueGetter) {
        return getMaybeValue(valueGetter).orElse(null);
    }

    @Override
    public <V> Optional<V> getMaybeValue(Function<A, V> valueGetter) {
        return Optional.ofNullable((V) value);
    }

    @Override
    public Optional<Object> getMaybeValue() {
        return Optional.ofNullable(value);
    }
}
