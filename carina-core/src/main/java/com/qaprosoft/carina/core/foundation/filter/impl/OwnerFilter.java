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
    public boolean isPerform(ITestNGMethod testMethod, List<String> rules) {
        if (testMethod != null) {
            //if test was described only by one OwnerFilter
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(MethodOwner.class)) {
                MethodOwner ownerAnnotation = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.class);
                if (ownerAnnotation != null) {
                    String owner = ownerAnnotation.owner().toLowerCase();
                    LOGGER.info(String.format("Test: [%s]. Owners: %s. Expected ownerAnnotation: [%s]", testMethod.getMethodName(), owner,
                            rules.toString()));
                    return ruleCheck(rules, owner);
                }
            }

            //if test was described by several OwnerFilters
            if (testMethod.getConstructorOrMethod().getMethod().isAnnotationPresent(MethodOwner.List.class)) {
                MethodOwner.List ownerAnnotations = testMethod.getConstructorOrMethod().getMethod().getAnnotation(MethodOwner.List.class);
                if (ownerAnnotations != null) {
                    List<String> owners = new ArrayList<String>();
                    for (MethodOwner methodOwner : ownerAnnotations.value()) {
                        owners.add(methodOwner.owner().toLowerCase());
                    }
                    LOGGER.info(String.format("Test: [%s]. Owners: %s. Expected owner: [%s]", testMethod.getMethodName(), owners.toString(),
                            rules.toString()));
                    return ruleCheck(rules, owners);
                }
            }

            //if test was not described by OwnerFilter annotation
            return ruleCheckWithoutAnnotation(rules);
        }
        return false;
    }
}
