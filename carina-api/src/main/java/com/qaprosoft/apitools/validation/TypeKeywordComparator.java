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

final class TypeKeywordComparator implements JsonKeywordComparator {

    @Override
    public void compare(String prefix, Object expectedValue, Object actualValue, JsonCompareResultWrapper result) {
        String expType = expectedValue.toString().replace(JsonCompareKeywords.TYPE.getKey(), "");
        if (!expType.equals(actualValue.getClass().getSimpleName())) {
            result.fail(String.format("%s\nValue type '%s' doesn't match to expected type '%s'\n", prefix, actualValue.getClass()
                    .getSimpleName(), expType));
        }
    }

    @Override
    public boolean isMatch(Object expectedValue) {
        return expectedValue.toString().startsWith(JsonCompareKeywords.TYPE.getKey());
    }
}
