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
package com.zebrunner.carina.api.apitools.validation;

import com.zebrunner.carina.utils.JsonUtils;
import ognl.Ognl;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;

final class OgnlKeywordsComparator implements JsonKeywordComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String actualStr;
    private Object root;

    public OgnlKeywordsComparator(String actualStr) {
        this.actualStr = actualStr;
    }

    @Override
    public void compare(String prefix, Object expectedValue, Object actualValue, JsonCompareResultWrapper result) {
        String expectedExpression = expectedValue.toString().replace(JsonCompareKeywords.OGNL.getKey(), "");
        if (!expectedExpression.isBlank()) {
            Object expressionResult = parseExpression(expectedExpression, actualValue);
            if (expressionResult instanceof Boolean) {
                boolean valid = (Boolean) expressionResult;
                if (!valid) {
                    result.fail(String.format("%s\nActual value '%s' doesn't match to expected OGNL expression '%s'\n", prefix, actualValue, expectedExpression));
                }
            } else {
                result.compareByDefault(prefix, expectedValue, actualValue);
            }
        } else {
            result.compareByDefault(prefix, expectedValue, actualValue);
        }
    }

    private Object parseExpression(String expression, Object value) {
        Object result = null;
        try {
            if (this.root == null) {
                this.root = JsonUtils.fromJson(actualStr, Object.class);
            }
            result = Ognl.getValue(expression, Map.of("val", value), root);
        } catch (OgnlException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean isMatch(Object expectedValue) {
        return expectedValue.toString().startsWith(JsonCompareKeywords.OGNL.getKey());
    }
}
