/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.registrar.tag;

import com.zebrunner.agent.core.registrar.domain.LabelDTO;
import com.zebrunner.agent.core.registrar.label.LabelResolver;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagManager implements LabelResolver {

    @Override
    public List<LabelDTO> resolve(Class<?> clazz, Method method) {
        Map<String, List<String>> labels = getAnnotations(clazz);
        labels.putAll(getAnnotations(method));

        return labels.entrySet()
                     .stream()
                     .flatMap(keyToValues -> keyToValues.getValue().stream()
                                                        .map(value -> new LabelDTO(keyToValues.getKey(), value)))
                     .collect(Collectors.toList());
    }

    private Map<String, List<String>> getAnnotations(AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(TestTag.List.class))
                       .map(TestTag.List::value)
                       .map(Arrays::stream)
                       .orElseGet(() -> Stream.of(annotatedElement.getAnnotation(TestTag.class)))
                       .filter(Objects::nonNull)
                       .collect(Collectors.toMap(
                               TestTag::name,
                               tagLabel -> new ArrayList<>(Collections.singletonList(tagLabel.value())),
                               this::union
                       ));
    }

    private List<String> union(List<String> values1, List<String> values2) {
        ArrayList<String> values = new ArrayList<>(values1);
        values.addAll(values2);
        return values;
    }

}