package com.qaprosoft.apitools.validation;

import ognl.Ognl;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

final class OgnlKeywordsComparator implements JsonKeywordComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            result = Ognl.getValue(expression, value);
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
