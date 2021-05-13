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

        boolean match = false;

        if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
            List<String> values = List.of(expression.split("\\|\\|"));
            match = values.stream().anyMatch(ruleValue -> actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
        } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
            List<String> values = List.of(expression.split(SpecialKeywords.RULE_FILTER_AND_CONDITION));
            match = values.stream().allMatch(ruleValue -> actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
        } else if (ruleExpression.size() == 1){
            String finalExpression = expression;
            return actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(finalExpression));
        }

        for (int i = 1; i < ruleExpression.size(); i++) {
            expression = ruleExpression.get(i);
            if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
                if (match) {
                    continue;
                }
                List<String> values = new ArrayList<>(Arrays.asList(expression.split("\\|\\|")));
                values.removeAll(Collections.singleton(""));
                match = values.stream().anyMatch(ruleValue -> ruleValue.isEmpty() ||
                        actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
            } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
                if (!match) {
                    continue;
                }
                List<String> values = new ArrayList<>(Arrays.asList(expression.split(SpecialKeywords.RULE_FILTER_AND_CONDITION)));
                values.removeAll(Collections.singleton(""));
                match = values.stream().allMatch(ruleValue -> ruleValue.isEmpty() ||
                        actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
            }
        }

        return match;
    }

    default boolean ruleCheck(List<String> ruleExpression, String actualValue) {

        return ruleCheck(ruleExpression, Arrays.asList(actualValue));
    }
}
