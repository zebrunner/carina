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

//        if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
//            List<String> values = List.of(expression.split("\\|\\|"));
//            if (expression.contains("!!")) {
//                List<Boolean> booleanList = new ArrayList<>();
//                for (int i = 0; i<values.size();i++){
//                    String value = values.get(i);
//                    if (values.get(i).contains("!!")){
//                        booleanList.add(actualValues.stream().anyMatch(actualValue -> !actualValue.equalsIgnoreCase(value)));
//                    } else {
//                        booleanList.add(actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(value)));
//                    }
//                    match
//                }
//
//            } else{
//                match = values.stream().anyMatch(ruleValue -> actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
//            }
//        } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
//            List<String> values = List.of(expression.split(SpecialKeywords.RULE_FILTER_AND_CONDITION));
//            if (expression.contains("!!")){
//
//            } else {
//                match = values.stream().allMatch(ruleValue -> actualValues.stream().anyMatch(ruleValue::equalsIgnoreCase));
//            }
//        } else if (ruleExpression.size() == 1){
//            String finalExpression = expression;
//            boolean tmp = actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(finalExpression));
//            if (finalExpression.contains("!!")){
//                return !tmp;
//            }
//            return tmp;
//        }

        if (expression.contains("!!")){
            String finalExpression = expression.substring(expression.indexOf("!!")+2);
            match = actualValues.stream().allMatch(actualValue -> !actualValue.equalsIgnoreCase(finalExpression));
        } else {
            String finalExpression = expression;
            match = actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(finalExpression));
        }

        if (ruleExpression.size()==1){
            return match;
        }

        for (int i = 1; i < ruleExpression.size(); i++) {
            expression = ruleExpression.get(i);
            if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
                if (match) {
                    continue;
                }
                String value = expression.substring(expression.indexOf(SpecialKeywords.RULE_FILTER_OR_CONDITION)+2);
                if (value.contains("!!")){
                    String finalValue = value.substring(value.indexOf("!!")+2);
                    match = actualValues.stream().allMatch(actualValue -> !actualValue.equalsIgnoreCase(finalValue));
                } else  {
                    match = actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(value));
                }
            } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
                if (!match) {
                    continue;
                }
                String value = expression.substring(expression.indexOf(SpecialKeywords.RULE_FILTER_AND_CONDITION)+2);
                if (value.contains("!!")){
                    String finalValue = value.substring(value.indexOf("!!")+2);
                    match = actualValues.stream().allMatch(actualValue -> !actualValue.equalsIgnoreCase(finalValue));
                } else  {
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
