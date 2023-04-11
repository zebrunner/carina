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
package com.zebrunner.carina.core.filter.rule;

import java.util.List;

import com.zebrunner.carina.core.filter.IFilter;

/**
 * Java bean for the rule that can be used for suite limit 
 *
 */
public class Rule {

    private String ruleName;
    
    private IFilter testFilter;

    private List<String> ruleExpression;

    public Rule(String ruleName, IFilter filter, List<String> ruleExpression) {
        this.ruleName = ruleName;
        this.testFilter = filter;
        this.ruleExpression = ruleExpression;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public IFilter getTestFilter() {
        return testFilter;
    }

    public void setTestFilter(IFilter testFilter) {
        this.testFilter = testFilter;
    }

    public List<String> getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(List<String> ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    @Override
    public String toString() {
        return "Rule [ruleName=" + ruleName + ", testFilter=" + testFilter + ", ruleExpression=" + ruleExpression + "]";
    }

}
