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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MethodAnnotationProcessor implements AnnotationProcessor<Method> {

    @Override
    public <A extends Annotation> Optional<AnnotationContext<A, Method>> findFirstAnnotationContext(Method element, Class<A> annClass) {
        return AnnotationProcessorUtils.getAnnotation(element, annClass)
                .map((A annotation) -> new AnnotationContext<>(annotation, element));
    }

    @Override
    public <A extends Annotation> Optional<Method> findFirstConditionalElement(Method element, Predicate<Method> condition) {
        return condition.test(element) ? Optional.of(element) : Optional.empty();
    }

    @Override
    public <A extends Annotation> List<AnnotationContext<A, Method>> findAllAnnotationContexts(Method element, Class<A> annClass) {
        return AnnotationProcessorUtils.getAnnotation(element, annClass)
                .map(annotation -> new AnnotationContext<>(annotation, element))
                .map(List::of)
                .orElse(new ArrayList<>());
    }

    @Override
    public AnnotatedElement covertToNextLevel(Method element) {
        return element.getDeclaringClass();
    }
}
