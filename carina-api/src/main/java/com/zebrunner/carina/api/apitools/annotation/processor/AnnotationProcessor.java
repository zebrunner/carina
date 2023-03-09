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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface AnnotationProcessor<E extends AnnotatedElement> {

    <A extends Annotation> Optional<AnnotationContext<A, E>> findFirstAnnotationContext(E element, Class<A> annClass);

    <A extends Annotation> Optional<E> findFirstConditionalElement(E element, Predicate<E> condition);

    <A extends Annotation> List<AnnotationContext<A, E>> findAllAnnotationContexts(E element, Class<A> annClass);

    AnnotatedElement covertToNextLevel(E element);

}
