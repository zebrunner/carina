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

public class ResolverUtils {

    public static <A1 extends Annotation, A2 extends Annotation, V> Optional<V> resolveFirstAnnotationValueStartingFromParameter(
            Method method,
            Class<A1> paramAnnotationClass,
            Class<A2> annotationClass,
            Function<Object, V> paramValueGetter, Function<A2, V> valueGetter,
            String errorMessageItem,
            Object... values
    ) {
        return resolveFirstSingleAnnotatedItemByChain(
                errorMessageItem,
                () -> resolveAnnotatedParameterValues(method, paramAnnotationClass, paramValueGetter, values),
                () -> createSingleElementList(findAnnotation(method, annotationClass).map(valueGetter).orElse(null)),
                () -> createSingleElementList(findFirstClassAnnotation(method, annotationClass).map(valueGetter).orElse(null))
        );
    }

    public static <A extends Annotation, V> Optional<V> resolveFirstAnnotationValueStartingFromMethod(
            Method method,
            Class<A> annotationClass,
            Function<A, V> valueGetter,
            String errorMessageItem
    ) {
        return resolveFirstSingleAnnotatedItemByChain(
                errorMessageItem,
                () -> createSingleElementList(findAnnotation(method, annotationClass).map(valueGetter).orElse(null)),
                () -> createSingleElementList(findFirstClassAnnotation(method, annotationClass).map(valueGetter).orElse(null))
        );
    }

    private static <V> List<V> createSingleElementList(V element) {
        List<V> result = new ArrayList<>();
        result.add(element);
        return result;
    }

    @SafeVarargs
    private static <V> Optional<V> resolveFirstSingleAnnotatedItemByChain(String errorMessageItem, Supplier<List<V>>... valueSuppliersChain) {
        return Arrays.stream(valueSuppliersChain)
                .map(Supplier::get)
                .peek(results -> {
                    if (results.size() > 1) {
                        throw new RuntimeException(String.format("The %s cannot be recognized. More than one candidate has been found", errorMessageItem));
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

    public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annClazz) {
        A result = null;
        if (element.isAnnotationPresent(annClazz)) {
            result = element.getAnnotation(annClazz);
        }
        return Optional.ofNullable(result);
    }

    public static <A extends Annotation> Optional<A> findFirstClassAnnotation(AnnotatedElement element, Class<A> annCLazz) {
        try {
            return findFirstParentClass(element, c -> c.isAnnotationPresent(annCLazz))
                    .map(c -> c.getAnnotation(annCLazz));
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format("During %s annotation processing. %s", annCLazz.getSimpleName(), e.getMessage()), e);
        }
    }

    public static Optional<Class<?>> findFirstParentClass(AnnotatedElement element, Predicate<Class<?>> condition) {
        Class<?> clazz = Member.class.isAssignableFrom(element.getClass()) ? ((Member) element).getDeclaringClass() : (Class<?>) element;
        return findFirstParentClass(clazz, AbstractApiMethod.class, condition);
    }

    private static Optional<Class<?>> findFirstParentClass(Class<?> clazz, Class<?> untilClass, Predicate<Class<?>> condition) {
        Optional<Class<?>> result;
        if (clazz != null && !clazz.equals(untilClass)) {
            if (!condition.test(clazz)) {
                List<Optional<Class<?>>> maybeClasses = Stream.concat(Stream.of(clazz.getSuperclass()), Arrays.stream(clazz.getInterfaces()))
                        .map(superClass -> findFirstParentClass(superClass, untilClass, condition))
                        .filter(Optional::isPresent)
                        .collect(Collectors.toList());

                if (maybeClasses.size() > 1) {
                    throw new RuntimeException("More than one candidate has been found: " + Arrays.toString(maybeClasses.stream()
                            .filter(Optional::isPresent)
                            .map(maybeClazz -> maybeClazz.get().getSimpleName())
                            .toArray(String[]::new))
                    );
                }
                return maybeClasses.stream()
                        .findFirst()
                        .orElse(Optional.empty());
            } else {
                result = Optional.of(clazz);
            }
        } else {
            result = Optional.empty();
        }
        return result;
    }

    public static <A extends Annotation> List<A> resolveAllAnnotatedItemsByChain(AnnotatedElement element, Class<A> annotationClass) {
        List<A> result = new ArrayList<>();

        Class<?> annotatedClass = null;
        if (Member.class.isAssignableFrom(element.getClass())) {
            findAnnotation(element, annotationClass)
                    .ifPresent(result::add);
            annotatedClass = ((Member) element).getDeclaringClass();
        } else if (Class.class.isAssignableFrom(element.getClass())) {
            annotatedClass = (Class<?>) element;
        }
        findAllParentClasses(annotatedClass, AbstractApiMethod.class, clazz -> clazz.isAnnotationPresent(annotationClass))
                .forEach(clazz -> result.add(clazz.getAnnotation(annotationClass)));

        return result;
    }

    private static List<Class<?>> findAllParentClasses(Class<?> clazz, Class<?> untilClass, Predicate<Class<?>> condition) {
        return findAllParentClasses(clazz, untilClass, condition, new ArrayList<>());
    }

    private static List<Class<?>> findAllParentClasses(Class<?> clazz, Class<?> untilClass, Predicate<Class<?>> condition, List<Class<?>> foundClasses) {
        if (clazz != null && !clazz.equals(untilClass)) {
            if (condition.test(clazz)) {
                foundClasses.add(clazz);
            }
            Stream.concat(Stream.of(clazz.getSuperclass()), Arrays.stream(clazz.getInterfaces()))
                    .forEach(superClass -> findAllParentClasses(superClass, untilClass, condition, foundClasses));
        }
        return foundClasses;
    }
}
