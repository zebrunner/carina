package com.zebrunner.carina.core.registrar.ownership;

import java.lang.reflect.Method;

import org.testng.ISuite;

import com.zebrunner.agent.core.registrar.maintainer.MaintainerResolver;

public class SuiteOwnerResolver implements MaintainerResolver {
    private final ISuite suite;

    public String resolve(Class<?> aClass, Method method) {
        return this.suite.getParameter("suiteOwner");
    }

    public SuiteOwnerResolver(ISuite rootXmlSuite) {
        this.suite = rootXmlSuite;
    }
}
