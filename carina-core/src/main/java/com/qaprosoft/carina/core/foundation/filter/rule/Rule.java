package com.qaprosoft.carina.core.foundation.filter.rule;


import com.qaprosoft.carina.core.foundation.filter.IFilter;

import java.util.List;

/**
 * Java bean for the rule that can be used for suite limit 
 *
 */
public class Rule {

    private String ruleName;
    
    private IFilter testFilter;

    private List<String> ruleValues;

    public Rule(String ruleName, IFilter filter, List<String> ruleValues) {
        this.ruleName = ruleName;
        this.testFilter = filter;
        this.ruleValues = ruleValues;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public List<String> getRuleValues() {
        return ruleValues;
    }

    public void setRuleValues(List<String> ruleValues) {
        this.ruleValues = ruleValues;
    }

    public IFilter getTestFilter() {
        return testFilter;
    }

    public void setTestFilter(IFilter testFilter) {
        this.testFilter = testFilter;
    }

    @Override
    public String toString() {
        return "Rule [ruleName=" + ruleName + ", testFilter=" + testFilter + ", ruleValues=" + ruleValues + "]";
    }

}
