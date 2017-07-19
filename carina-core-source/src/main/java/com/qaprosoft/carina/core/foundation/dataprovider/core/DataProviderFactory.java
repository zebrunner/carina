package com.qaprosoft.carina.core.foundation.dataprovider.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByImpl;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.exceptions.GroupByException;
import com.qaprosoft.carina.core.foundation.dataprovider.core.impl.BaseDataProvider;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class DataProviderFactory {

	protected static final Logger LOGGER = Logger.getLogger(DataProviderFactory.class);
	
    private DataProviderFactory() {
    }


	public static Object[][] getDataProvider(Annotation[] annotations, ITestContext context, ITestNGMethod m)
	{
    	
        Map<String, String> testNameArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
		Map<String, String> canonicalTestNameArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> testMethodNameArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> testMethodOwnerArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> jiraArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> testRailsArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
		Map<String, String> bugArgsMap = Collections.synchronizedMap(new HashMap<String, String>());
		List<String> doNotRunTests = Collections.synchronizedList(new ArrayList<>());
        

        Object[][] provider = new Object[][]{};

        for (Annotation annotation : annotations) {
        	try {
        		Class<? extends Annotation> type = annotation.annotationType();

                String providerClass = "";
                
                for (Method method : type.getDeclaredMethods()) {
                    if (method.getName().equalsIgnoreCase("classname")) {
                    	providerClass = (String) method.invoke(annotation);
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
					provider = ArrayUtils.addAll(provider, activeProvider.getDataProvider(annotation, context, m));
                    testNameArgsMap.putAll(activeProvider.getTestNameArgsMap());
					canonicalTestNameArgsMap.putAll(activeProvider.getCanonicalTestNameArgsMap());
                    testMethodNameArgsMap.putAll(activeProvider.getTestMethodNameArgsMap());
                    testMethodOwnerArgsMap.putAll(activeProvider.getTestMethodOwnerArgsMap());
                    jiraArgsMap.putAll(activeProvider.getJiraArgsMap());
                    testRailsArgsMap.putAll(activeProvider.getTestRailsArgsMap());
					bugArgsMap.putAll(activeProvider.getBugArgsMap());
					doNotRunTests.addAll(activeProvider.getDoNotRunRowsIDs());
    			}

        	}
        	catch (Exception e){
        		e.printStackTrace();
        		//do nothing
        	}
        }

		if (!GroupByMapper.getInstanceInt().isEmpty() || !GroupByMapper.getInstanceStrings().isEmpty()){
			provider = getGroupedList(provider);
		}

        context.setAttribute(SpecialKeywords.TEST_NAME_ARGS_MAP, testNameArgsMap);
		context.setAttribute(SpecialKeywords.CANONICAL_TEST_NAME_ARGS_MAP, canonicalTestNameArgsMap);
        context.setAttribute(SpecialKeywords.TEST_METHOD_NAME_ARGS_MAP, testMethodNameArgsMap);
        context.setAttribute(SpecialKeywords.TEST_METHOD_OWNER_ARGS_MAP, testMethodOwnerArgsMap);
        context.setAttribute(SpecialKeywords.JIRA_ARGS_MAP, jiraArgsMap);
        context.setAttribute(SpecialKeywords.TESTRAIL_ARGS_MAP, testRailsArgsMap);
		context.setAttribute(SpecialKeywords.BUG_ARGS_MAP, bugArgsMap);
		context.setAttribute(SpecialKeywords.DO_NOT_RUN_TESTS, doNotRunTests);

        //clear group by settings
        GroupByMapper.getInstanceInt().clear();
        GroupByMapper.getInstanceStrings().clear();

        
        return provider;
    }


	private static Object[][] getGroupedList(Object[][] provider){
		Object[][] finalProvider;
		if (GroupByMapper.isHashMapped()){
			if (GroupByMapper.getInstanceStrings().size()==1){
				finalProvider = GroupByImpl.getGroupedDataProviderMap(provider, GroupByMapper.getInstanceStrings().iterator().next());
			}
			else{
				throw new GroupByException("Incorrect groupColumn annotation parameter!");
			}
		}
		else{
			if (GroupByMapper.getInstanceInt().size()==1 && !GroupByMapper.getInstanceInt().contains(-1)){

				finalProvider = GroupByImpl.getGroupedDataProviderArgs(provider, GroupByMapper.getInstanceInt().iterator().next());
			}
			else {
				throw new GroupByException("Incorrect groupColumn annotation  parameter!");
			}
		}

	return finalProvider;
	}

	@SuppressWarnings("unchecked")
	public static Object[][] getNeedRerunDataProvider(Annotation[] annotations, ITestContext context, ITestNGMethod m)
	{
		Object[][] dp = getDataProvider(annotations, context, m);
		List<String> doNotRunRowIDs = (List<String>) context.getAttribute(SpecialKeywords.DO_NOT_RUN_TESTS);
		Map<String, String> testNameArgsMap = (Map<String, String>) context.getAttribute(SpecialKeywords.CANONICAL_TEST_NAME_ARGS_MAP);
		if (!doNotRunRowIDs.isEmpty())
		{
			for (int i = dp.length - 1; i >= 0; i--)
			{
				String testUniqueName = testNameArgsMap.get(testNameArgsMap.keySet().toArray()[i]);
				if (testUniqueName != null)
				{
					if (doNotRunRowIDs.contains(testUniqueName))
					{
						dp = ArrayUtils.remove(dp, i);
					}
				}
			}
		}
		return dp;
	}

}
