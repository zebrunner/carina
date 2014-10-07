package com.qaprosoft.carina.core.foundation.retry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import com.qaprosoft.carina.core.foundation.retry.RetryAnalyzer;

public class AnnotationTransformer implements IAnnotationTransformer {

	@SuppressWarnings("rawtypes")
	@Override
	public void transform(ITestAnnotation testAnnotation,Class clazz,Constructor test,Method method){
		IRetryAnalyzer retry=testAnnotation.getRetryAnalyzer();
		if(retry==null){
			testAnnotation.setRetryAnalyzer(RetryAnalyzer.class);
		}
	}
}