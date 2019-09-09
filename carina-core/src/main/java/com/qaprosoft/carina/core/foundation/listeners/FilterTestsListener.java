package com.qaprosoft.carina.core.foundation.listeners;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.filter.Filter;
import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.filter.rule.Rule;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.apache.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterTestsListener implements ISuiteListener {

    private static final Logger LOGGER = Logger.getLogger(FilterTestsListener.class);

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
                isPerform = rule.getTestFilter().isPerform(testMethod, rule.getRuleValues());
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
     * 
     * Method that is responsible for rules and filters parsing
     * 
     * @param ruleStr String
     * @return list of rules
     */
    private List<Rule> parseRules(String ruleStr) {
        List<Rule> rules = new ArrayList<>();
        String[] ruleStructure;
        String[] ruleValues;
        IFilter filter;
        Rule rule;
        if (!ruleStr.isEmpty()) {
            LOGGER.info("Rules for suite limitation have been defined.");
            if(ruleStr.contains("&amp;&amp;")) {
                ruleStr = ruleStr.replaceAll("&amp;&amp;",SpecialKeywords.RULE_FILTER_AND_CONDITION);
            }

            for (String ruleItem : ruleStr.split(SpecialKeywords.RULE_FILTER_SPLITTER)) {
                ruleStructure = ruleItem.split(SpecialKeywords.RULE_FILTER_VALUE_SPLITTER);
                ruleValues = ruleStructure[1].split(SpecialKeywords.RULE_FILTER_AND_CONDITION);
                filter = Filter.PRIORITY.getRuleByName(ruleStructure[0]).getFilter();
                rule = new Rule(ruleStructure[0], filter, Arrays.asList(ruleValues));
                LOGGER.info("Following rule will be added: ".concat(rule.toString()));
                rules.add(rule);
            }
        }
        return rules;
    }

}
