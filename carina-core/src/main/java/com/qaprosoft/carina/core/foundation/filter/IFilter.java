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

    boolean isPerform(ITestNGMethod testMethod, List<String> ruleExpression);

    default boolean ruleCheck(List<String> ruleExpression, List<String> actualValues) {
        boolean match = false;

        String expression = ruleExpression.get(0);
        if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
            List<String> values = List.of(expression.split("\\|\\|"));
            match = values.stream().anyMatch(ruleValue -> actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
        } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
            List<String> values = List.of(expression.split(SpecialKeywords.RULE_FILTER_AND_CONDITION));
            match = values.stream().allMatch(ruleValue -> actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
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

    default boolean ruleCheck(String ruleExpression, List<String> actualValue) {

        Deque<Integer> openBracketsIndex = new ArrayDeque<>();
        StringBuffer expression = new StringBuffer(ruleExpression);
        expression.insert(0,"(");
        expression.insert(expression.length(),")");

        openBracketsIndex.add(0);

        for (int i = 1; i < expression.length(); i++) {
            char el = expression.charAt(i);
            if (el == '(') {
                openBracketsIndex.add(i);
            } else  if ( i +4 > expression.length() && expression.substring()){

            } else if (el == (')')) {
                int exprBeginIndex = openBracketsIndex.pollLast();
                String priorityPart = expression.substring(exprBeginIndex + 1, i);
                if (priorityPart.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION) &&
                        priorityPart.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
//                    List<String> values = new ArrayList<>(Arrays.asList(priorityPart.split("(?=&&)|(?=\\|\\|)")));
//                    if (priority.isEmpty()){
//                        values.set(1, values.get(0) + values.get(1));
//                        values.remove(0);
//                    }
//                    priority.addAll(values);
                    return false;
                } else {
                    priority.add(priorityPart);
                }
                expression.delete(exprBeginIndex, i + 1);
                i -= (i - exprBeginIndex);
            } else if (operandSearch.equals(SpecialKeywords.RULE_FILTER_OR_CONDITION)){

            } else if (operandSearch.equals(SpecialKeywords.RULE_FILTER_AND_CONDITION)){

            }
        }

        if (expression.length() != 0) {
            priority.add(expression.substring(1,expression.length()-1));
        }
        return false;
    }
}
