package com.qaprosoft.apitools.annotation.processor;

import com.qaprosoft.apitools.annotation.AnnotationContext;
import com.qaprosoft.apitools.annotation.AnnotationProcessorUtils;
import com.qaprosoft.apitools.annotation.AnnotationUtils;
import com.qaprosoft.apitools.annotation.ParameterAnnotationContext;
import com.qaprosoft.carina.core.foundation.api.binding.RuntimeMethod;
import com.zebrunner.carina.utils.exception.NotSupportedOperationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ParameterAnnotationProcessor implements AnnotationProcessor<RuntimeMethod> {

    @Override
    public <A extends Annotation> Optional<AnnotationContext<A, RuntimeMethod>> findFirstAnnotationContext(RuntimeMethod element, Class<A> annClass) {
        Map<A, Optional<Object>> result = findAnnotatedParameters(element.getMethod(), annClass, element.getArgs());

        if (result.size() > 1) {
            throw new RuntimeException("During %s annotation processing. More than one candidate has been found.");
        }

        return result.entrySet().stream()
                .findFirst()
                .map(entry -> new ParameterAnnotationContext<>(entry.getKey(), element, entry.getValue().orElse(null)));
    }

    @Override
    public <A extends Annotation> Optional<RuntimeMethod> findFirstConditionalElement(RuntimeMethod element, Predicate<RuntimeMethod> condition) {
        throw new NotSupportedOperationException("Unable to find the first conditional parameter");
    }

    @Override
    public <A extends Annotation> List<AnnotationContext<A, RuntimeMethod>> findAllAnnotationContexts(RuntimeMethod element, Class<A> annClass) {
        return findAnnotatedParameters(element.getMethod(), annClass, element.getArgs()).entrySet().stream()
                .map(entry -> new ParameterAnnotationContext<>(entry.getKey(), element, entry.getValue().orElse(null)))
                .collect(Collectors.toList());
    }

    private static <A extends Annotation> Map<A, Optional<Object>> findAnnotatedParameters(Method method, Class<A> annClass, Object... values) {
        Parameter[] parameters = method.getParameters();
        return findAnnotatedParameterIndexesStream(method, annClass)
                .collect(Collectors.toMap(index -> AnnotationProcessorUtils.getAnnotation(parameters[index], annClass).orElse(null), index -> Optional.ofNullable(values[index])));
    }

    private static <A extends Annotation> Stream<Integer> findAnnotatedParameterIndexesStream(Method method, Class<A> annotationClass) {
        return findConditionalParameterIndexesStream(method, parameter -> AnnotationUtils.isAnnotatedPresent(parameter, annotationClass));
    }

    private static Stream<Integer> findConditionalParameterIndexesStream(Method method, Predicate<Parameter> condition) {
        Parameter[] parameters = method.getParameters();
        return IntStream.range(0, parameters.length)
                .boxed()
                .filter(index -> condition.test(parameters[index]));
    }

    @Override
    public AnnotatedElement covertToNextLevel(RuntimeMethod element) {
        return element.getMethod();
    }
}
