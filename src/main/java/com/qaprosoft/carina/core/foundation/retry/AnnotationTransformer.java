package com.qaprosoft.carina.core.foundation.retry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

public class AnnotationTransformer implements IAnnotationTransformer {

	public static final Logger LOGGER = Logger.getLogger(AnnotationTransformer.class);
	
	@SuppressWarnings("rawtypes")
	@Override
	public void transform(ITestAnnotation testAnnotation, Class clazz, Constructor test, Method method){
		IRetryAnalyzer retry=testAnnotation.getRetryAnalyzer();
		if(retry==null){
			testAnnotation.setRetryAnalyzer(RetryAnalyzer.class);
		}
		LOGGER.debug("retry analyzer: " + method.getName() + testAnnotation.getRetryAnalyzer());
	}
}