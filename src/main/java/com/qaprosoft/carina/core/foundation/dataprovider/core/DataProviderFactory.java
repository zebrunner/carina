package com.qaprosoft.carina.core.foundation.dataprovider.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.testng.ITestContext;

import com.qaprosoft.carina.core.foundation.dataprovider.core.impl.BaseDataProvider;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class DataProviderFactory {

	protected static final Logger LOGGER = Logger.getLogger(DataProviderFactory.class);
	
    private DataProviderFactory() {
    }


// TODO: WTF?! remove hard code from map

    public static Object[][] getDataProvider(Annotation[] annotations, ITestContext context) {
    	
        Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> jiraArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
        

        Object[][] provider = new Object[][]{};
        for (Annotation annotation : annotations) {
        	try {
        		Class<? extends Annotation> type = annotation.annotationType();

                String providerClass = "";
                
                for (Method method : type.getDeclaredMethods()) {
                    if (method.getName().equalsIgnoreCase("classname")) {
                    	providerClass = (String) method.invoke(annotation, null);
                    	break;
                    }
                }
                
                if (providerClass.isEmpty())
                	continue;
                
        		
    			Class<?> clazz;
    			Object object = null;
    			try {
    				clazz = Class.forName(providerClass);
    				Constructor<?> ctor = clazz.getConstructor();
    				object = ctor.newInstance();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}	

    			BaseDataProvider activeProvider = (BaseDataProvider) object;
    			if (object instanceof com.qaprosoft.carina.core.foundation.dataprovider.core.impl.BaseDataProvider) {
                    provider = ArrayUtils.addAll(provider, activeProvider.getDataProvider(annotation, context));
                    testNameArgsMap.putAll(activeProvider.getTestNameArgsMap());
                    jiraArgsMap.putAll(activeProvider.getJiraArgsMap());
    			}

        	}
        	catch (Exception e){
        		e.printStackTrace();
        		//do nothing
        	}
        }
        context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameArgsMap);
        context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraArgsMap);
        return provider;
    }

}
