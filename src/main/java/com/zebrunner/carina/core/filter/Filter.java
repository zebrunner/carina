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
package com.zebrunner.carina.core.filter;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.core.filter.impl.OwnerFilter;
import com.zebrunner.carina.core.filter.impl.PriorityFilter;
import com.zebrunner.carina.core.filter.impl.TagFilter;
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

    public static Filter getRuleByName(String ruleName) {
        try {
            return Filter.valueOf(ruleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.info(String.format("Filter [%s] is not defined. Please, review all available filters", ruleName));
            throw new IncorrectFilterException(String.format("Filter [%s] is not defined. Please, review all available filters", ruleName));
        }
    }

}
