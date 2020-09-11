package com.qaprosoft.carina.core.foundation.filter.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;

public class OwnerFilter implements IFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> expectedData) {

        MethodOwner.List ownerAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.List.class);
        if (ownerAnnotation != null) {
        	List<String> owners = new ArrayList<String>();
        	for (MethodOwner methodOwner : ownerAnnotation.value()) {
        		owners.add(methodOwner.owner().toLowerCase());
            }
            LOGGER.info(String.format("Test: [%s]. Owners: %s. Expected owner: [%s]", testMethod.getMethodName(), owners.toString(),
                    expectedData.toString()));
            return expectedData.parallelStream().anyMatch(d -> owners.contains(d.toLowerCase()));
        }
        return false;
    }

}
