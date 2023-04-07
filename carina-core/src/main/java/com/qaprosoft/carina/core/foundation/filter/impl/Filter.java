package com.qaprosoft.carina.core.foundation.filter.impl;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class Filter implements IFilter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        return false;
    }

}
