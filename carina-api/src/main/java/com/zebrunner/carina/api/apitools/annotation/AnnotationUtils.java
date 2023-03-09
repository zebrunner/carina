package com.zebrunner.carina.api.apitools.annotation;

import com.zebrunner.carina.api.apitools.annotation.processor.AnnotationProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnnotationUtils {

    public static <A extends Annotation> List<AnnotationContext<A, AnnotatedElement>> findAllAnnotationContextsByChain(AnnotatedElement element, Class<A> annClass) {
        return resolveAnnotationProcessors(element).stream()
                .map(annotationProcessor -> annotationProcessor.findAllAnnotationContexts(convertToProcessorElement(element, annotationProcessor), annClass))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static <A extends Annotation> Optional<AnnotationContext<A, AnnotatedElement>> findFirstAnnotationContextByChain(AnnotatedElement element, Class<A> annClass) {
        return resolveAnnotationProcessors(element).stream()
                .map(annotationProcessor -> annotationProcessor.findFirstAnnotationContext(convertToProcessorElement(element, annotationProcessor), annClass))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    public static Optional<AnnotatedElement> findFirstConditionalElementByChain(AnnotatedElement element, Predicate<AnnotatedElement> condition) {
        return resolveAnnotationProcessors(element).stream()
                .map(annotationProcessor -> annotationProcessor.findFirstConditionalElement(convertToProcessorElement(element, annotationProcessor), condition))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    public static boolean isAnnotatedPresent(AnnotatedElement element, Class<? extends Annotation> annClass) {
        return AnnotationProcessorUtils.findAnnotatedElement(element, annClass)
                .isPresent();
    }

    private static List<AnnotationProcessor<AnnotatedElement>> resolveAnnotationProcessors(AnnotatedElement element) {
        List<SupportedType> typesChain = SupportedType.findSupportedTypesFrom(element);
        return resolveAnnotationProcessors(typesChain);
    }

    @SuppressWarnings("unchecked")
    private static List<AnnotationProcessor<AnnotatedElement>> resolveAnnotationProcessors(List<SupportedType> typesChain) {
        return typesChain.stream()
                .map(SupportedType::getAnnotationProcessor)
                .map(annotationProcessor -> (AnnotationProcessor<AnnotatedElement>) annotationProcessor)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static AnnotatedElement convertToProcessorElement(AnnotatedElement element, AnnotationProcessor<?> annotationProcessor) {
        AnnotatedElement toElement = SupportedType.findSupportedAnnotatedElementBy((Class<AnnotationProcessor<?>>) annotationProcessor.getClass());
        return convertToElement(element, toElement);
    }

    @SuppressWarnings("unchecked")
    private static AnnotatedElement convertToElement(AnnotatedElement element, AnnotatedElement toElement) {
        List<SupportedType> typesChain = SupportedType.findSupportedTypesFrom(element, toElement);

        AtomicReference<AnnotatedElement> nextElement = new AtomicReference<>(element);

        typesChain.stream()
                .map(SupportedType::getAnnotationProcessor)
                .forEach(annotationProcessor -> nextElement.set(((AnnotationProcessor<AnnotatedElement>) annotationProcessor)
                        .covertToNextLevel(nextElement.get()))
                );

        return nextElement.get();
    }
}
