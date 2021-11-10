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
     * @param testMethod ITestNGMethod
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

            //parsing each rule
            for (String ruleItem : ruleStr.split(SpecialKeywords.RULE_FILTER_SPLITTER)) {
                //ruleStructure[0] contains type of the rule, ruleStructure[1] contains the rule description
                ruleStructure = ruleItem.split(SpecialKeywords.RULE_FILTER_VALUE_SPLITTER);
                if (ruleStructure.length == 2) {
                    List<String> priority = prioritize(ruleStructure[1]);
                    IFilter filter = Filter.getRuleByName(ruleStructure[0]).getFilter();
                    rules.add(new Rule(ruleStructure[0], filter, priority));
                }
            }
        }
        return rules;
    }

    /**
     * Method that is responsible for parsing rule String into prioritized sequence.
     *
     * @param ruleStr String
     * @return prioritized sequence
     */
    private List<String> prioritize(String ruleStr) {
        List<String> values = new ArrayList<>(Arrays.asList(ruleStr.split("(?=&&)|(?=\\|\\|)")));
        return values;
    }
}
