package com.qaprosoft.carina.core.foundation.api.resolver;

import com.qaprosoft.carina.core.foundation.api.AbstractApiMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ResolverUtils {

    public static <A1 extends Annotation, A2 extends Annotation, V> Optional<V> resolveAnnotationValueFromParameter(
            Method method,
            Class<A1> paramAnnotationClass,
            Class<A2> annotationClass,
            Function<Object, V> paramValueGetter, Function<A2, V> valueGetter,
            String errorMessageItem,
            Object... values
    ) {
        return resolveAnnotatedItemByChain(
                errorMessageItem,
                () -> resolveAnnotatedParameterValues(method, paramAnnotationClass, paramValueGetter, values),
                () -> createSingleElementList(findAnnotationValue(method, annotationClass).map(valueGetter).orElse(null)),
                () -> createSingleElementList(findClassAnnotationValue(method, annotationClass).map(valueGetter).orElse(null))
        );
    }

    public static <A extends Annotation, V> Optional<V> resolveAnnotationValueFromMethod(
            Method method,
            Class<A> annotationClass,
            Function<A, V> valueGetter,
            String errorMessageItem
    ) {
        return resolveAnnotatedItemByChain(
                errorMessageItem,
                () -> createSingleElementList(findAnnotationValue(method, annotationClass).map(valueGetter).orElse(null)),
                () -> createSingleElementList(findClassAnnotationValue(method, annotationClass).map(valueGetter).orElse(null))
        );
    }

    @SafeVarargs
    private static <V> Optional<V> resolveAnnotatedItemByChain(String errorMessageItem, Supplier<List<V>>... valueSuppliersChain) {
        return Arrays.stream(valueSuppliersChain)
                .map(Supplier::get)
                .peek(results -> {
                    if (results.size() > 1) {
                        throw new RuntimeException(String.format("The %s cannot be recognized. More than one value was found", errorMessageItem));
                    }
                })
                .filter(results -> results.size() == 1)
                .map(results -> results.get(0))
                .filter(Objects::nonNull)
                .findFirst();
    }

    public static <A extends Annotation> Map<String, Object> resolveNamedAnnotatedParameterValues(
            Method method,
            Class<A> annotationClass,
            Function<A, String> nameGetter,
            Object... values
    ) {
        Parameter[] parameters = method.getParameters();
        return resolveAnnotatedParameterIndexesStream(method, annotationClass)
                .collect(Collectors.toMap(index -> nameGetter.apply(parameters[index].getAnnotation(annotationClass)), index -> values[index]));
    }

    public static <A extends Annotation, V> List<V> resolveAnnotatedParameterValues(
            Method method,
            Class<A> annotationClass,
            Function<Object, V> converter,
            Object... values
    ) {
        return resolveAnnotatedParameterIndexesStream(method, annotationClass)
                .map(index -> values[index])
                .map(converter)
                .collect(Collectors.toList());
    }

    public static <A extends Annotation> Stream<Integer> resolveAnnotatedParameterIndexesStream(Method method, Class<A> annotationClass) {
        Parameter[] parameters = method.getParameters();
        return IntStream.range(0, parameters.length)
                .boxed()
                .filter(index -> parameters[index].isAnnotationPresent(annotationClass));
    }

    private static <V> List<V> createSingleElementList(V element) {
        List<V> result = new ArrayList<>();
        result.add(element);
        return result;
    }

    public static <A extends Annotation> Optional<A> findAnnotationValue(AnnotatedElement element, Class<A> annClazz) {
        A result = null;
        if (element.isAnnotationPresent(annClazz)) {
            result = element.getAnnotation(annClazz);
        }
        return Optional.ofNullable(result);
    }

    public static <A extends Annotation> Optional<A> findClassAnnotationValue(AnnotatedElement element, Class<A> annCLazz) {
        return findParentClass(element, c -> c.isAnnotationPresent(annCLazz))
                .map(c -> c.getAnnotation(annCLazz));
    }

    public static Optional<Class<?>> findParentClass(AnnotatedElement element, Predicate<Class<?>> condition) {
        Class<?> clazz = Member.class.isAssignableFrom(element.getClass()) ? ((Member) element).getDeclaringClass() : (Class<?>) element;
        return findParentClass(clazz, AbstractApiMethod.class, condition);
    }

    public static Optional<Class<?>> findParentClass(Class<?> clazz, Class<?> untilClass, Predicate<Class<?>> condition) {
        Optional<Class<?>> result;
        if (clazz != null && !clazz.equals(untilClass)) {
            if (!condition.test(clazz)) {
                return findParentClass(clazz.getSuperclass(), untilClass, condition);
            } else {
                result = Optional.of(clazz);
            }
        } else {
            result = Optional.empty();
        }
        return result;
    }
}
