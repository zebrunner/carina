package com.qaprosoft.carina.core.foundation.filter.impl;

import com.qaprosoft.carina.core.foundation.filter.IFilter;
import com.qaprosoft.carina.core.foundation.utils.ownership.MethodOwner;
import org.apache.log4j.Logger;
import org.testng.ITestNGMethod;

import java.util.List;

public class OwnerFilter implements IFilter {

    protected static final Logger LOGGER = Logger.getLogger(OwnerFilter.class);

    @Override
    public boolean isPerform(ITestNGMethod testMethod, List<String> expectedData) {

        MethodOwner ownerAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.class);
        if (ownerAnnotation != null) {
            String owner = ownerAnnotation.owner();
            String secondOwner = ownerAnnotation.secondaryOwner();
            LOGGER.info(String.format("Test: [%s]. Owner: [%s]. Second owner: [%s] Expected owner: [%s]", testMethod.getMethodName(), owner,
                    secondOwner, expectedData.toString()));
            return expectedData.parallelStream().anyMatch(d -> d.equalsIgnoreCase(owner) || d.equalsIgnoreCase(secondOwner));
        }
        return false;
    }

}
