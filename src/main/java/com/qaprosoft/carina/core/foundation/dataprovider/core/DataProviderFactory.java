package com.qaprosoft.carina.core.foundation.dataprovider.core;

import com.qaprosoft.carina.core.foundation.dataprovider.core.impl.CSVDataProvider;
import com.qaprosoft.carina.core.foundation.dataprovider.core.impl.XlsDataProvider;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.ITestContext;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class DataProviderFactory {

    private DataProviderFactory() {
    }


// TODO: WTF?! remove hard code from map

    public static Object[][] getDataProvider(Annotation[] annotations, ITestContext context) {

        Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> jiraArgsMap = Collections.synchronizedMap(new HashMap<String, String>());

        Object[][] provider = new Object[][]{};
        for (Annotation annotation : annotations) {
            DataProviderType dataProviderType = DataProviderType.fromString(getAnnotationName(annotation));
            switch (dataProviderType) {
                case XLS_DATA_SOURCE:
                    XlsDataProvider xmlDataProvider = new XlsDataProvider();
                    provider = ArrayUtils.addAll(provider, xmlDataProvider.getDataProvider(annotation, context));
                    testNameArgsMap.putAll(xmlDataProvider.getTestNameArgsMap());
                    jiraArgsMap.putAll(xmlDataProvider.getJiraArgsMap());
                    break;
                case CSV_DATA_SOURCE:
                    CSVDataProvider csvDataProvider = new CSVDataProvider();
                    provider = ArrayUtils.addAll(provider, csvDataProvider.getDataProvider(annotation, context));
                    testNameArgsMap.putAll(csvDataProvider.getTestNameArgsMap());
                    jiraArgsMap.putAll(csvDataProvider.getJiraArgsMap());
                    break;
                case UNKNOWN:
                    break;
            }
        }
        context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameArgsMap);
        context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraArgsMap);
        return provider;
    }


    private static String getAnnotationName(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return annotationType.getSimpleName();
    }
}
