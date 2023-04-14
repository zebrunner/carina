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
import java.util.Optional;

import com.zebrunner.agent.core.registrar.maintainer.MaintainerResolver;
import com.zebrunner.carina.utils.Configuration;

public class Ownership implements MaintainerResolver {

    @Override
    public String resolve(Class<?> clazz, Method method) {
        // In order not to check for null further
        if (method == null || clazz == null) {
            return null;
        }

        MethodOwner methodAnnotation = null;
        // do a scan for single MethodOwner annotation as well)
        if (method.isAnnotationPresent(MethodOwner.class)) {
            MethodOwner annotation = method.getAnnotation(MethodOwner.class);
            if (isSuitablePlatform(annotation) || annotation.platform().isBlank()) {
                methodAnnotation = annotation;
            }
        } else if (method.isAnnotationPresent(MethodOwner.List.class)) {
            MethodOwner platformSpecificAnnotation = null;
            MethodOwner anyPlatformAnnotation = null;
            for (MethodOwner annotation : method.getAnnotation(MethodOwner.List.class).value()) {
                if (isSuitablePlatform(annotation)) {
                    if (platformSpecificAnnotation != null) {
                        throw new IllegalArgumentException(
                                String.format("There is more than one annotation above the '{%s}' method with the same platform '%s'",
                                        method.getName(), Configuration.getPlatform()));
                    }
                    platformSpecificAnnotation = annotation;
                } else if (annotation.platform().isBlank()) {

                }

            }
        }

        // scan all MethodOwner annotations to find default ownership without any platform
        if (method != null && method.isAnnotationPresent(MethodOwner.List.class)) {
            MethodOwner.List methodListAnnotation = method.getAnnotation(MethodOwner.List.class);
            for (MethodOwner methodOwner : methodListAnnotation.value()) {
                String actualPlatform = methodOwner.platform();
                if (actualPlatform.isEmpty()) {
                    methodAnnotation = methodOwner;
                    break;
                }            
            }
        }
        
        //do one more scan using platform ownership filter if any to override default owner value
        if (method != null && method.isAnnotationPresent(MethodOwner.List.class)) {
            MethodOwner.List methodListAnnotation = method.getAnnotation(MethodOwner.List.class);
            for (MethodOwner methodOwner : methodListAnnotation.value()) {

                String actualPlatform = methodOwner.platform();
                String expectedPlatform = Configuration.getPlatform();
                
                if (!actualPlatform.isEmpty() && isValidPlatform(actualPlatform, expectedPlatform)) {
                    methodAnnotation = methodOwner;
                }               
            }
        }

        return methodAnnotation != null ? methodAnnotation.owner() : null;
    }
    
    private static boolean isValidPlatform(String actualPlatform, String expectedPlatform) {
        return actualPlatform.equalsIgnoreCase(expectedPlatform);
    }

    private static boolean isSuitablePlatform(MethodOwner annotation) {
        return annotation.platform().equalsIgnoreCase(Configuration.getPlatform());
    }

    private static Optional<String> getOwner(MethodOwner[] annotations) {

    }

    private static void assert

}