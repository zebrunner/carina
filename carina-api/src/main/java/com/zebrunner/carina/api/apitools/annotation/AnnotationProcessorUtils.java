package com.zebrunner.carina.api.apitools.annotation;

import com.zebrunner.carina.api.apitools.annotation.processor.RelatedTo;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnnotationProcessorUtils {

    public static <A extends Annotation> Optional<A> getAnnotation(AnnotatedElement element, Class<A> annClass) {
        return findAnnotatedElement(element, annClass)
                .map(o -> doOnType(o, annotation -> proxyAnnotation(annotation, annClass), ae -> ae.getDeclaredAnnotation(annClass)));
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A proxyAnnotation(Annotation foundAnnotation, Class<A> annClass) {
        A annotation = foundAnnotation.annotationType().getDeclaredAnnotation(annClass);
        List<Method> relatedMethods = Arrays.stream(foundAnnotation.annotationType().getDeclaredMethods())
                .filter(AnnotationProcessorUtils::isAnnotationMethod)
                .filter(method -> method.isAnnotationPresent(RelatedTo.class))
                .filter(method -> method.getDeclaredAnnotation(RelatedTo.class).annotationClass().equals(annClass))
                .collect(Collectors.toList());

        return (A) Proxy.newProxyInstance(annClass.getClassLoader(), new Class[]{annClass}, (proxy, method, args) ->
                relatedMethods.stream()
                        .filter(m -> m.getDeclaredAnnotation(RelatedTo.class).field().equals(method.getName()))
                        .findFirst()
                        .map(m -> invokeMethod(m, foundAnnotation, args))
                        .orElseGet(() -> invokeMethod(method, annotation, args))
        );
    }

    @SuppressWarnings("unchecked")
    private static <R> R invokeMethod(Method method, Object instance, Object[] args) {
        try {
            return (R) method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isAnnotationMethod(Method method) {
        return method.getParameterCount() == 0 && method.getReturnType() != void.class;
    }

    public static Optional<Object> findAnnotatedElement(AnnotatedElement element, Class<? extends Annotation> annClass) {
        Set<Class<? extends Annotation>> processedAnnotations = new HashSet<>();
        Function<Object, Stream<Object>> superElementGetter = e -> doOnType(e, annotation -> {
            Stream<Object> es = processedAnnotations.contains(annotation.annotationType())
                    ? Stream.empty()
                    : Arrays.stream(annotation.annotationType().getDeclaredAnnotations());
            processedAnnotations.add(annotation.annotationType());
            return es;
        }, ae -> Arrays.stream(ae.getDeclaredAnnotations()));

        return findItemBeforeFirstItemInHierarchy(element, Object.class, e -> doOnType(e, Annotation::annotationType, AnnotatedElement::getClass).equals(annClass), e -> e, superElementGetter, FirstElementDuplicateStrategy.ANY);
    }

    private static <R> R doOnType(Object e, Function<Annotation, R> onAnnotation, Function<AnnotatedElement, R> onAnnotatedElement) {
        R result = null;
        if (e instanceof Annotation) {
            Annotation ann = (Annotation) e;
            result = onAnnotation.apply(ann);
        } else if (e instanceof AnnotatedElement) {
            AnnotatedElement ae = (AnnotatedElement) e;
            result = onAnnotatedElement.apply(ae);
        }
        return result;
    }

    public static <R, E> Optional<E> findItemBeforeFirstItemInHierarchy(E element, E untilElement, Predicate<E> condition, Function<E, R> preparator, Function<E, Stream<E>> superElementsGetter, FirstElementDuplicateStrategy firstElementDuplicateStrategy) {
        return findFirstItemsGraphNodeInHierarchy(element, untilElement, condition, preparator, superElementsGetter, firstElementDuplicateStrategy)
                .map(entry -> entry.getKey() == null ? element : entry.getKey());
    }

    public static <R, E> Optional<R> findFirstFoundItemInHierarchy(E element, E untilElement, Predicate<E> condition, Function<E, R> preparator, Function<E, Stream<E>> superElementsGetter, FirstElementDuplicateStrategy firstElementDuplicateStrategy) {
        return findFirstItemsGraphNodeInHierarchy(element, untilElement, condition, preparator, superElementsGetter, firstElementDuplicateStrategy)
                .flatMap(entry -> entry.getValue().stream().findFirst());
    }

    private static <R, E> Optional<Map.Entry<E, List<R>>> findFirstItemsGraphNodeInHierarchy(E element, E untilElement, Predicate<E> condition, Function<E, R> preparator, Function<E, Stream<E>> superElementsGetter, FirstElementDuplicateStrategy firstElementDuplicateStrategy) {
        Map<E, List<R>> result = findAllItemsGraphInHierarchy(element, untilElement, condition, preparator, superElementsGetter);

        String[] duplicates = firstElementDuplicateStrategy.getDuplicatesRecognizer().apply(
                        result.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                ).stream()
                .map(Object::toString)
                .toArray(String[]::new);

        if (duplicates.length != 0) {
            throw new RuntimeException(String.format("More than one candidate has been found: %s", Arrays.toString(duplicates)));
        }
        return result.entrySet().stream()
                .filter(entry -> entry.getValue().size() == 1)
                .findFirst();
    }

    public static <R, E> List<R> findAllItemsInHierarchy(E clazz, E untilClass, Predicate<E> condition, Function<E, R> preparator, Function<E, Stream<E>> superElementsGetter) {
        Map<E, List<R>> result = findAllItemsGraphInHierarchy(clazz, untilClass, condition, preparator, superElementsGetter);

        return result.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static <R, E> Map<E, List<R>> findAllItemsGraphInHierarchy(E element, E untilElement, Predicate<E> condition, Function<E, R> preparator, Function<E, Stream<E>> superElementsGetter) {
        Map<E, List<R>> result = new LinkedHashMap<>();

        findAllItemsGraphInHierarchy(null, element, untilElement, condition, preparator, result, superElementsGetter);

        return result;
    }

    private static <R, E> void findAllItemsGraphInHierarchy(
            E previousElement,
            E element,
            E untilElement,
            Predicate<E> condition,
            Function<E, R> preparator,
            Map<E, List<R>> collector,
            Function<E, Stream<E>> superElementsGetter
    ) {
        if (element != null && !element.equals(untilElement)) {
            collector.putIfAbsent(previousElement, new ArrayList<>());

            if (condition.test(element)) {
                collector.get(previousElement).add(preparator.apply(element));
            }

            superElementsGetter.apply(element)
                    .forEach(superElement -> findAllItemsGraphInHierarchy(element, superElement, untilElement, condition, preparator, collector, superElementsGetter));
        }
    }
}
