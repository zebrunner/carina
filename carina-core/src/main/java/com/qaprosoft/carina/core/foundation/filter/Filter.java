package com.qaprosoft.carina.core.foundation.filter;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.filter.impl.OwnerFilter;
import com.qaprosoft.carina.core.foundation.filter.impl.PriorityFilter;
import com.qaprosoft.carina.core.foundation.filter.impl.TagFilter;
/**
 * Enum to store rules (name and implementation of the rule)
 *
 */
public enum Filter {

    PRIORITY("PRIORITY", new PriorityFilter()),
    OWNER("OWNER", new OwnerFilter()),
    TAGS("TAGS", new TagFilter());

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String ruleName;

    private IFilter filter;

    Filter(String ruleName, IFilter filter) {
        this.ruleName = ruleName;
        this.filter = filter;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public IFilter getFilter() {
        return filter;
    }

    public void setFilter(IFilter filter) {
        this.filter = filter;
    }

    public Filter getRuleByName(String ruleName) {
        try {
            Filter rule = Filter.valueOf(ruleName.toUpperCase());
            return rule;
        } catch (IllegalArgumentException e) {
            LOGGER.info(String.format("Filter [%s] is not defined. Please, review all available filters", ruleName));
            throw new IncorrectFilterException(String.format("Filter [%s] is not defined. Please, review all available filters", ruleName));
        }
    }

}
