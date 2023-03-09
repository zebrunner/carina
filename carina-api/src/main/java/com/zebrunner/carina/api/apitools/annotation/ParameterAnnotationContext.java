package com.zebrunner.carina.api.apitools.annotation;

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
    public Object getValue(Function<A, ?> valueGetter) {
        return getMaybeValue(valueGetter).orElse(null);
    }

    @Override
    public Optional<Object> getMaybeValue(Function<A, ?> valueGetter) {
        return Optional.ofNullable(value);
    }

    @Override
    public boolean isValueExist(Function<A, ?> valueGetter) {
        return value != null;
    }
}
