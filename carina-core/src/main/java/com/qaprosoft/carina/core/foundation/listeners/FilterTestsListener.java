package com.qaprosoft.carina.core.foundation.listeners;

import java.lang.invoke.MethodHandles;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.filter.Filter;
import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.filter.rule.Rule;
import com.qaprosoft.carina.core.foundation.utils.Configuration;

public class FilterTestsListener implements ISuiteListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private List<Rule> rules = new ArrayList<>();

    @Override
    public void onStart(ISuite suite) {
        rules = parseRules(Configuration.get(Configuration.Parameter.TEST_RUN_RULES));

        // rules are absent
        if (rules.isEmpty()) {
            LOGGER.info("There are no any rules and limitations");
            return;
        }

        boolean isPerform;
        LOGGER.info("Extracted rules: ".concat(rules.toString()));
        for (ITestNGMethod testMethod : suite.getAllMethods()) {
            isPerform = true;
            // multiple conditions
            for (Rule rule : rules) {
                // condition when test doesn't satisfy at least one filter
                if (!isPerform) {
                    break;
                }
                isPerform = rule.getTestFilter().isPerform(testMethod, rule.getRuleExpression());
            }
            // condition when test should be disabled
            if (!isPerform) {
                disableTest(testMethod);
            }
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        // TODO Auto-generated method stub
    }

    /**
     * Method to disable test
     * 
     * @param testMethod
     */
    private void disableTest(ITestNGMethod testMethod) {
        LOGGER.info(String.format("Disable test: [%s]", testMethod.getMethodName()));
        testMethod.setInvocationCount(0);
    }

    /**
     * Method that is responsible for rules and filters parsing
     * 
     * @param ruleStr String
     * @return list of rules
     */
    private List<Rule> parseRules(String ruleStr) {
        List<Rule> rules = new ArrayList<>();
        String[] ruleStructure;
        if (!ruleStr.isEmpty()) {
            LOGGER.info("Rules for suite limitation have been defined.");
            if (ruleStr.contains("&amp;&amp;")) {
                ruleStr = ruleStr.replaceAll("&amp;&amp;", SpecialKeywords.RULE_FILTER_AND_CONDITION);
            }

            for (String ruleItem : ruleStr.split(SpecialKeywords.RULE_FILTER_SPLITTER)) {
                ruleStructure = ruleItem.split(SpecialKeywords.RULE_FILTER_VALUE_SPLITTER);
//                if (ruleStructure[1].contains("(") && ruleStructure[1].contains(")")) {
                    List<String> priority = prioritize(ruleStructure[1]);
                    rules = splitRuleExpression(priority, ruleStructure[0]);
//                }
            }
        }
        return rules;
    }

    public List<String> prioritize(String expression) {
        Deque<Integer> openBracketsIndex = new ArrayDeque<>();
        List<String> priority = new ArrayList<>();

        for (int i = 0; i < expression.length(); i++) {
            char el = expression.charAt(i);
            if (el == '(') {
                openBracketsIndex.add(i);
            } else if (el == (')')) {
                int exprBeginIndex = openBracketsIndex.pollLast();
                priority.add(expression.substring(exprBeginIndex + 1, i));
                expression = expression.substring(0, exprBeginIndex) + expression.substring(i + 1);
                i -= (i - exprBeginIndex);
            }
        }
        return priority;
    }

    private List<Rule> splitRuleExpression(List<String> priority, String ruleName) {
        List<String> ruleValues = new ArrayList<>();
        IFilter filter = Filter.getRuleByName(ruleName).getFilter();
        String expression = priority.get(0);

        if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
            ruleValues.addAll(Arrays.asList(expression.split("\\|\\|")));
        } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
            this.createRules(ruleName, filter, ruleValues, expression, rules);
        }

        for (int i = 1; i < priority.size(); i++) {
            expression = priority.get(i);
            if (expression.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION)) {
                ruleValues.add(expression.replace(SpecialKeywords.RULE_FILTER_OR_CONDITION, ""));
            } else if (expression.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
                this.createRules(ruleName, filter, ruleValues, expression, rules);
                ruleValues = new ArrayList<>();
            }
        }

        if (!ruleValues.isEmpty()) {
            Rule rule = new Rule(ruleName, filter, ruleValues);
            LOGGER.info("Following rule will be added: ".concat(rule.toString()));
            rules.add(rule);
        }
        return rules;
    }

    private void createRules(String ruleName, IFilter filter, List<String> ruleValues, String expression, List<Rule> rules) {
        String[] values = expression.split(SpecialKeywords.RULE_FILTER_AND_CONDITION);

        if (!values[0].isEmpty()) {
            ruleValues.add(values[1]);
            Rule rule = new Rule(ruleName, filter, ruleValues);
            rules.add(rule);
        } else if (!values[1].isEmpty()) {
            ruleValues.add(values[0]);
            Rule rule = new Rule(ruleName, filter, ruleValues);
            rules.add(rule);
        } else {
            ruleValues.add(values[0]);
            Rule rule1 = new Rule(ruleName, filter, ruleValues);

            ruleValues.remove(ruleValues.size() - 1);
            ruleValues.add(values[1]);
            Rule rule2 = new Rule(ruleName, filter, ruleValues);

            LOGGER.info("Following rule will be added: ".concat(rule1.toString()));
            LOGGER.info("Following rule will be added: ".concat(rule2.toString()));
            rules.add(rule1);
            rules.add(rule2);
        }
    }

}
