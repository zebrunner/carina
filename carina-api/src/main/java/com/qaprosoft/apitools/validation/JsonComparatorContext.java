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
package com.qaprosoft.apitools.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class JsonComparatorContext {

    private final Map<String, Predicate<Object>> namedPredicates;
    private final List<JsonKeywordComparator> comparators;

    public JsonComparatorContext() {
        this.namedPredicates = new ConcurrentHashMap<>();
        this.comparators = new ArrayList<>();
    }

    public <T> JsonComparatorContext withPredicate(String name, Predicate<T> predicate) {
        if (predicate != null) {
            this.namedPredicates.put(name, (Predicate<Object>) predicate);
        }
        return this;
    }

    public <T> JsonComparatorContext withComparator(JsonKeywordComparator comparator) {
        if (comparator != null) {
            this.comparators.add(comparator);
        }
        return this;
    }

    Map<String, Predicate<Object>> getNamedPredicates() {
        return namedPredicates;
    }

    List<JsonKeywordComparator> getComparators() {
        return comparators;
    }

    public static JsonComparatorContext context() {
        return new JsonComparatorContext();
    }
}
