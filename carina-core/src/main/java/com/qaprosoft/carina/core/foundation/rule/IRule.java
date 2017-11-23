package com.qaprosoft.carina.core.foundation.rule;

/**
 * Rule for handling of conditional cases: expectedSkips, etc.
 * Just create implementation of interface based on specific conditions:
 * example: return (env.equals('int'))
 *
 */
public interface IRule {

    public boolean isPerform();
    
}
