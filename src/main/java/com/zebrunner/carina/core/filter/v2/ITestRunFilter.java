package com.zebrunner.carina.core.filter.v2;

import org.apiguardian.api.API;
import org.testng.ITestNGMethod;

@API(status = API.Status.STABLE)
public interface ITestRunFilter {

    boolean perform(ITestNGMethod testMethod);

}
