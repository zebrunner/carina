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

import org.json.JSONObject;

import java.util.Map;
import java.util.function.Predicate;

final class PredicateKeywordComparator implements JsonKeywordComparator {

    private final Map<String, Predicate<Object>> namedPredicates;

    PredicateKeywordComparator(Map<String, Predicate<Object>> namedPredicates) {
        this.namedPredicates = namedPredicates;
    }

    @Override
    public void compare(String prefix, Object expectedValue, Object actualValue, JsonCompareResultWrapper result) {
        if (namedPredicates != null) {
            String expPredicate = expectedValue.toString().replace(JsonCompareKeywords.PREDICATE.getKey(), "");
            Predicate<Object> predicate = namedPredicates.get(expPredicate);
            if (predicate != null) {
                boolean nullValue = JSONObject.NULL.getClass().isAssignableFrom(actualValue.getClass());
                if (nullValue) {
                    actualValue = null;
                }
                if (!predicate.test(actualValue)) {
                    result.fail(String.format("%s\nActual value '%s' doesn't match to expected predicate '%s'\n", prefix, actualValue, expPredicate));
                }
            } else {
                result.compareByDefault(prefix, expectedValue, actualValue);
            }
        } else {
            result.compareByDefault(prefix, expectedValue, actualValue);
        }
    }

    @Override
    public boolean isMatch(Object expectedValue) {
        return expectedValue.toString().startsWith(JsonCompareKeywords.PREDICATE.getKey());
    }
}
