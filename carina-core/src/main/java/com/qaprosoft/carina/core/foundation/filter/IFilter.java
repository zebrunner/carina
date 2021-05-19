package com.qaprosoft.carina.core.foundation.filter;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import org.testng.ITestNGMethod;

import java.util.*;

/**
 * in config.properties
 * {@code #test_run_rules=PRIORITY=>P1&amp;&amp;P2;;OWNER=>msarychau;;TAGS=>tag1=temp&amp;&amp;feature=reg
 * rules logic: test_run_rules={RULE_NAME_ENUM}=>{RULE_VALUE1}&&{RULE_VALUE2}...}
 */

public interface IFilter {

    boolean isPerform(ITestNGMethod testMethod, List<String> rules);

    default boolean ruleCheck(List<String> ruleExpression, List<String> actualValues) {
        String expression = ruleExpression.get(0);
        boolean match;

        //checking value with the highest priority ([0] element of ruleExpression)
        if (expression.contains(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION)) {
            String finalExpression = expression.substring(expression.indexOf(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION) + 2);
            match = actualValues.stream().allMatch(actualValue -> !actualValue.equalsIgnoreCase(finalExpression));
        } else {
            String finalExpression = expression;
            match = actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(finalExpression));
        }

        for (int i = 1; i < ruleExpression.size(); i++) {
            expression = ruleExpression.get(i);
            if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
                //if previous expression is true, we don't need to check this because of (true || false) == true
                if (match) {
                    continue;
                }
                String value = expression.substring(expression.indexOf(SpecialKeywords.RULE_FILTER_OR_CONDITION) + 2);
                if (value.contains(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION)) {
                    String finalValue = value.substring(value.indexOf(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION) + 2);
                    match = actualValues.stream().allMatch(actualValue -> !actualValue.equalsIgnoreCase(finalValue));
                } else {
                    match = actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(value));
                }
            } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
                //if previous expression is false, we don't need to check this because of (false && true) = false
                if (!match) {
                    continue;
                }
                String value = expression.substring(expression.indexOf(SpecialKeywords.RULE_FILTER_AND_CONDITION) + 2);
                if (value.contains(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION)) {
                    String finalValue = value.substring(value.indexOf(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION) + 2);
                    match = actualValues.stream().allMatch(actualValue -> !actualValue.equalsIgnoreCase(finalValue));
                } else {
                    match = actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(value));
                }
            }
        }

        return match;
    }

    default boolean ruleCheck(List<String> ruleExpression, String actualValue) {
        return ruleCheck(ruleExpression, Arrays.asList(actualValue));
    }
}
