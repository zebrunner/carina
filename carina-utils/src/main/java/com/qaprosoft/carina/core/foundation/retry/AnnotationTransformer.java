/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
    public void transform(ITestAnnotation testAnnotation, Class clazz, Constructor test, Method method) {
        IRetryAnalyzer retry = testAnnotation.getRetryAnalyzer();
        if (retry == null) {
            testAnnotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
        LOGGER.debug("retry analyzer: " + method.getName() + testAnnotation.getRetryAnalyzer());
    }
}