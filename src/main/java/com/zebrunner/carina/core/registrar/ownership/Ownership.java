/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.registrar.ownership;

import java.lang.reflect.Method;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.agent.core.registrar.maintainer.MaintainerResolver;
import org.apache.commons.lang3.StringUtils;

public class Ownership implements MaintainerResolver {

    @Override
    public String resolve(Class<?> clazz, Method method) {
        String owner = StringUtils.EMPTY;
        // Get a handle to the class and method
        // We can't use getMethod() because we may have parameterized tests
        // for which we don't know the matching signature
        String methodName = method.getName();
        Method testMethod = null;
        Method[] possibleMethods = clazz.getMethods();
        for (Method possibleMethod : possibleMethods) {
            if (possibleMethod.getName().equals(methodName)) {
                testMethod = possibleMethod;
                break;
            }
        }
        
        // do a scan for single Methodowner annotation as well)
        if (testMethod != null && testMethod.isAnnotationPresent(MethodOwner.class)) {
            MethodOwner methodAnnotation = testMethod.getAnnotation(MethodOwner.class);
            owner = methodAnnotation.owner();
        }
        
        // scan all MethodOwner annotations to find default ownership without any platform
        if (testMethod != null && testMethod.isAnnotationPresent(MethodOwner.List.class)) {
            MethodOwner.List methodAnnotation = testMethod.getAnnotation(MethodOwner.List.class);
            for (MethodOwner methodOwner : methodAnnotation.value()) {
                String actualPlatform = methodOwner.platform();
                if (actualPlatform.isEmpty()) {
                    owner = methodOwner.owner();
                    break;
                }            
            }
        }
        
        //do one more scan using platform ownership filter if any to override default owner value
        if (testMethod != null && testMethod.isAnnotationPresent(MethodOwner.List.class)) {
            MethodOwner.List methodAnnotation = testMethod.getAnnotation(MethodOwner.List.class);
            for (MethodOwner methodOwner : methodAnnotation.value()) {

                String actualPlatform = methodOwner.platform();
                String expectedPlatform = Configuration.getPlatform();
                
                if (!actualPlatform.isEmpty() && isValidPlatform(actualPlatform, expectedPlatform)) {
                    owner = methodOwner.owner();
                }               
            }
        }

        return owner;
    }
    
    private static boolean isValidPlatform(String actualPlatform, String expectedPlatform) {
        return actualPlatform.equalsIgnoreCase(expectedPlatform);
    }

}