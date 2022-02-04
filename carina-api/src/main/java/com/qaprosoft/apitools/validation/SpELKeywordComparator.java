package com.qaprosoft.apitools.validation;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

final class SpELKeywordComparator implements JsonKeywordComparator {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Override
    public void compare(String prefix, Object expectedValue, Object actualValue, JsonCompareResultWrapper result) {
        String expectedExpression = expectedValue.toString().replace(JsonCompareKeywords.SPEL.getKey(), "");
        if (!expectedExpression.isBlank()) {
            EvaluationContext context = new StandardEvaluationContext(actualValue);

            Expression expression = PARSER.parseExpression(expectedExpression);
            Object expressionResult = expression.getValue(context);
            if (expressionResult instanceof Boolean) {
                boolean valid = (Boolean) expressionResult;
                if (!valid) {
                    result.fail(String.format("%s\nActual value '%s' doesn't match to expected SpEL expression '%s'\n", prefix, actualValue, expectedExpression));
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
        return expectedValue.toString().startsWith(JsonCompareKeywords.SPEL.getKey());
    }
}
