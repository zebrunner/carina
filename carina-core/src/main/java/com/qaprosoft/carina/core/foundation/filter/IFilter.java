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
package com.qaprosoft.carina.core.foundation.filter;

import com.zebrunner.carina.utils.commons.SpecialKeywords;
import org.testng.ITestNGMethod;

import java.util.*;

/**
 * in config.properties
 * {@code #test_run_rules=PRIORITY=>P1&amp;&amp;P2;;OWNER=>msarychau;;TAGS=>tag1=temp&amp;&amp;feature=reg
 * rules logic: test_run_rules={RULE_NAME_ENUM}=>{RULE_VALUE1}&&{RULE_VALUE2}...}
 */

public interface IFilter {

    boolean isPerform(ITestNGMethod testMethod, List<String> rules);

    default RuleExpressionParser getRuleExpressionParser(String expression) {
        if (expression.contains(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION)) {
            String finalExpression = expression.substring(expression.indexOf(SpecialKeywords.RULE_FILTER_EXCLUDE_CONDITION) + 2);
            return new ExcludeRuleExpressionParser(finalExpression);
        } else {
            String finalExpression = expression;
            return new IncludeRuleExpressionParser(finalExpression);
        }
    }

    interface RuleExpressionParser {
        boolean evaluate(List<String> actualValues);
    }

    class IncludeRuleExpressionParser implements RuleExpressionParser {
        private final String includeValue;

        public IncludeRuleExpressionParser(String includeValue) {
            this.includeValue = includeValue;
        }

        @Override
        public boolean evaluate(List<String> actualValues) {
            return actualValues.stream().anyMatch(actualValue -> actualValue.equalsIgnoreCase(includeValue));
        }
    }

    class ExcludeRuleExpressionParser implements RuleExpressionParser {
        private final String excludeValue;

        public ExcludeRuleExpressionParser(String excludeValue) {
            this.excludeValue = excludeValue;
        }

        @Override
        public boolean evaluate(List<String> actualValues) {
            return actualValues.stream().allMatch(actualValue -> !actualValue.equalsIgnoreCase(excludeValue));
        }
    }

    default boolean ruleCheck(List<String> ruleExpression, List<String> actualValues) {
        RuleExpressionParser ruleExpressionParser = getRuleExpressionParser(ruleExpression.get(0));
        boolean match = ruleExpressionParser.evaluate(actualValues);
        for (int i = 1; i < ruleExpression.size(); i++) {
            RuleExpressionParser currentRuleExpressionParser = getRuleExpressionParser(ruleExpression.get(i));
            match = currentRuleExpressionParser.evaluate(actualValues);
        }
        return match;
    }

    default boolean ruleCheck(List<String> ruleExpression, String actualValue) {
        return ruleCheck(ruleExpression, Arrays.asList(actualValue));
    }

    default boolean ruleCheck(List<String> ruleExpression) {
        RuleExpressionParser ruleExpressionParser = getRuleExpressionParser(ruleExpression.get(0));
        boolean match = ruleExpressionParser.evaluate(null);
        for (int i = 1; i < ruleExpression.size(); i++) {
            RuleExpressionParser currentRuleExpressionParser = getRuleExpressionParser(ruleExpression.get(i));
            match = currentRuleExpressionParser.evaluate(null);
        }
        return match;
    }
}
