package com.zebrunner.carina.dataprovider;

import com.zebrunner.carina.dataprovider.core.DataProviderFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;

import java.lang.annotation.Annotation;

public interface IAbstractDataProvider {

    @DataProvider(name = "DataProvider", parallel = true)
    default Object[][] createData(final ITestNGMethod testMethod, ITestContext context) {
        Annotation[] annotations = testMethod.getConstructorOrMethod().getMethod().getDeclaredAnnotations();
        Object[][] objects = DataProviderFactory.getDataProvider(annotations, context, testMethod);
        return objects;
    }

    @DataProvider(name = "SingleDataProvider")
    default Object[][] createDataSingleThread(final ITestNGMethod testMethod, ITestContext context) {
        Annotation[] annotations = testMethod.getConstructorOrMethod().getMethod().getDeclaredAnnotations();
        Object[][] objects = DataProviderFactory.getDataProvider(annotations, context, testMethod);
        return objects;
    }
}
