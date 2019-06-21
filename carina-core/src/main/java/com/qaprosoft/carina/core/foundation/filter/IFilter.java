package com.qaprosoft.carina.core.foundation.filter;

import org.testng.ITestNGMethod;

import java.util.List;

/**
 * in config.properties
 * {@code #test_run_rules=PRIORITY=>P1&amp;&amp;P2;;OWNER=>msarychau;;TAGS=>tag1=temp&amp;&amp;feature=reg
 * rules logic: test_run_rules={RULE_NAME_ENUM}=>{RULE_VALUE1}&&{RULE_VALUE2}...}
 * 
 */

public interface IFilter {

    public boolean isPerform(ITestNGMethod testMethod, List<String> expectedData);

}
