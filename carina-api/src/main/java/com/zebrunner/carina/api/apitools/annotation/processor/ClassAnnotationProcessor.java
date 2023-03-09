/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.zebrunner.carina.api.apitools.annotation.processor;

import com.zebrunner.carina.api.apitools.annotation.AnnotationContext;
import com.zebrunner.carina.api.apitools.annotation.AnnotationProcessorUtils;
import com.zebrunner.carina.api.apitools.annotation.AnnotationUtils;
import com.zebrunner.carina.api.apitools.annotation.FirstElementDuplicateStrategy;
import com.zebrunner.carina.api.AbstractApiMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ClassAnnotationProcessor implements AnnotationProcessor<Class<?>> {

    private static final Function<Class<?>, Stream<Class<?>>> superClassesGetter =
            clazz -> Stream.concat(Stream.of(clazz.getSuperclass()), Arrays.stream(clazz.getInterfaces()));

    @Override
    public <A extends Annotation> Optional<AnnotationContext<A, Class<?>>> findFirstAnnotationContext(Class<?> element, Class<A> annClass) {
        Optional<AnnotationContext<A, Class<?>>> result;
        try {
            result = findFirstClassInHierarchy(element, annClass)
                    .flatMap(clazz -> AnnotationProcessorUtils.getAnnotation(clazz, annClass))
                    .map(annotation -> new AnnotationContext<>(annotation, element));
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format("During %s annotation processing. %s", annClass.getSimpleName(), e.getMessage()), e);
        }
        return result;
    }

    @Override
    public <A extends Annotation> Optional<Class<?>> findFirstConditionalElement(Class<?> element, Predicate<Class<?>> condition) {
        Optional<Class<?>> result;
        try {
            result = findFirstClassInHierarchy(element, condition);
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format("During %s class processing. %s", element.getSimpleName(), e.getMessage()), e);
        }
        return result;
    }

    private static <A extends Annotation> Optional<Class<?>> findFirstClassInHierarchy(Class<?> element, Class<A> annClass) {
        return findFirstClassInHierarchy(element, c -> AnnotationUtils.isAnnotatedPresent(c, annClass));
    }

    private static <A extends Annotation> Optional<Class<?>> findFirstClassInHierarchy(Class<?> element, Predicate<Class<?>> condition) {
        return AnnotationProcessorUtils.findFirstFoundItemInHierarchy(element, AbstractApiMethod.class, condition, c -> c, superClassesGetter, FirstElementDuplicateStrategy.ON_SAME_LEVEL);
    }

    @Override
    public <A extends Annotation> List<AnnotationContext<A, Class<?>>> findAllAnnotationContexts(Class<?> element, Class<A> annClass) {
        Function<Class<?>, AnnotationContext<A, Class<?>>> preparator = foundClass -> new AnnotationContext<>(AnnotationProcessorUtils.getAnnotation(foundClass, annClass).orElse(null), foundClass);
        return findAllClassesInHierarchy(element, annClass, preparator);
    }

    private static <A extends Annotation> List<AnnotationContext<A, Class<?>>> findAllClassesInHierarchy(Class<?> element, Class<A> annClass, Function<Class<?>, AnnotationContext<A, Class<?>>> preparator) {
        return AnnotationProcessorUtils.findAllItemsInHierarchy(element, AbstractApiMethod.class, c -> AnnotationUtils.isAnnotatedPresent(c, annClass), preparator, superClassesGetter);
    }

    @Override
    public AnnotatedElement covertToNextLevel(Class<?> element) {
        return element;
    }
}
