package com.qaprosoft.apitools.validation;

import com.qaprosoft.carina.core.foundation.utils.JsonUtils;
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
