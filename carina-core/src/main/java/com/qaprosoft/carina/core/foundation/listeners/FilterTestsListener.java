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
                List<String> priority = prioritize(ruleStructure[1]);
                IFilter filter = Filter.getRuleByName(ruleStructure[0]).getFilter();
                rules.add(new Rule(ruleStructure[0], filter, priority));
            }
        }
        return rules;
    }

    public List<String> prioritize(String ruleStr) {
        Deque<Integer> openBracketsIndex = new ArrayDeque<>();
        List<String> priority = new ArrayList<>();

        StringBuffer expression = new StringBuffer(ruleStr);
        expression.insert(0,"(");
        expression.insert(expression.length(),")");
        int closedBrackets = 0;
        for (int i = 0; i < expression.length(); i++) {
            char el = expression.charAt(i);
            if (el == '(') {
                openBracketsIndex.add(i);
            } else if (el == (')')) {
                closedBrackets++;
                int exprBeginIndex = openBracketsIndex.pollLast();
                String priorityPart = expression.substring(exprBeginIndex + 1, i);
                if (priorityPart.contains(SpecialKeywords.RULE_FILTER_OR_CONDITION) &&
                        priorityPart.contains(SpecialKeywords.RULE_FILTER_AND_CONDITION)) {
                    List<String> values = new ArrayList<>(Arrays.asList(priorityPart.split("(?=&&)|(?=\\|\\|)")));
                    if (priority.isEmpty()){
                        values.set(1, values.get(0) + values.get(1));
                        values.remove(0);
                    }
                    priority.addAll(values);
                } else {
                    priority.add(priorityPart);
                }
                expression.delete(exprBeginIndex, i + 1);
                i -= (i - exprBeginIndex);
            }
        }

        if (expression.length() != 0) {
            priority.add(expression.substring(1,expression.length()-1));
        }

        return priority;
    }
}
