/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.filter;

import java.util.Arrays;
import java.util.List;

import org.testng.ITestNGMethod;

import com.zebrunner.carina.utils.commons.SpecialKeywords;

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

    default boolean ruleCheck(List<String> ruleExpression) {
        String expression = ruleExpression.get(0);
        boolean match;

        if (expression.contains(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION)) {
            match = true;
        } else {
            match = false;
        }

        for (int i = 1; i < ruleExpression.size(); i++) {
            expression = ruleExpression.get(i);
            if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
                //if previous expression is true, we don't need to check this because of (true || false) == true
                if (match) {
                    continue;
                }
                if (expression.contains(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION)) {
                    match = true;
                }
            } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
                //if previous expression is false, we don't need to check this because of (false && true) = false
                if (!match) {
                    continue;
                }
                if (!expression.contains(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION)) {
                    match = false;
                }
            }
        }

        return match;
    }
}
