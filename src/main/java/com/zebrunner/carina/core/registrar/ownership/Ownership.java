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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;

import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.agent.core.registrar.maintainer.MaintainerResolver;
import com.zebrunner.carina.utils.R;

public class Ownership implements MaintainerResolver {
    // todo think about adding validation for the platform duplicates in method/class annotations

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    // todo move to AbstractCapabilities
    private static final String CAPABILITIES_CONFIG_PREFIX = "capabilities.";

    @Override
    public String resolve(Class<?> clazz, Method method) {
        // In order not to check for null further
        if (method == null || clazz == null) {
            return null;
        }

        String expectedPlatform = R.CONFIG.get(CAPABILITIES_CONFIG_PREFIX + CapabilityType.PLATFORM_NAME);
        Optional<MethodOwner> possibleMethodOwner = getMethodOwner(method, expectedPlatform);
        Optional<MethodOwner> possibleClassOwner = getClassOwner(clazz, expectedPlatform);

        // resolve platform-specific method owner
        if (possibleMethodOwner.isPresent()) {
            MethodOwner methodOwner = possibleMethodOwner.get();
            if (!methodOwner.platform().isEmpty()) {
                return methodOwner.owner();
            }
        }

        // resolve platform-specific class owner
        if (possibleClassOwner.isPresent()) {
            MethodOwner classOwner = possibleClassOwner.get();
            if (!classOwner.platform().isEmpty()) {
                return classOwner.owner();
            }
        }

        String suitableAnyPlatformOwner = null;
        // resolve all-other-platforms method/class owner
        if (possibleMethodOwner.isPresent()) {
            suitableAnyPlatformOwner = possibleMethodOwner.get().owner();
        } else if (possibleClassOwner.isPresent()) {
            suitableAnyPlatformOwner = possibleClassOwner.get().owner();
        }

        return suitableAnyPlatformOwner;
    }

    /**
     * Get suitable method owner
     * 
     * @param method test method
     * @param expectedPlatform expected platform
     * @return {@link Optional} of {@link MethodOwner}
     */
    private static Optional<MethodOwner> getMethodOwner(Method method, String expectedPlatform) {
        Optional<MethodOwner> suitableMethodOwner = Optional.empty();
        if (method.isAnnotationPresent(MethodOwner.class)) {
            MethodOwner[] owners = new MethodOwner[1];
            owners[0] = method.getAnnotation(MethodOwner.class);
            suitableMethodOwner = getOwner(owners, expectedPlatform);
        } else if (method.isAnnotationPresent(MethodOwner.List.class)) {
            suitableMethodOwner = getOwner(method.getAnnotation(MethodOwner.List.class).value(), expectedPlatform);
        }
        return suitableMethodOwner;
    }

    /**
     * Get suitable class owner
     *
     * @param clazz test class
     * @param expectedPlatform expected platform
     * @return {@link Optional} of {@link MethodOwner}
     */
    private static Optional<MethodOwner> getClassOwner(Class<?> clazz, String expectedPlatform) {
        Optional<MethodOwner> suitableMethodOwner = Optional.empty();
        if (clazz.isAnnotationPresent(MethodOwner.class)) {
            MethodOwner[] owners = new MethodOwner[1];
            owners[0] = clazz.getAnnotation(MethodOwner.class);
            suitableMethodOwner = getOwner(owners, expectedPlatform);
        } else if (clazz.isAnnotationPresent(MethodOwner.List.class)) {
            suitableMethodOwner = getOwner(clazz.getAnnotation(MethodOwner.List.class).value(), expectedPlatform);
        }
        return suitableMethodOwner;
    }

    private static Optional<MethodOwner> getOwner(MethodOwner[] owners, String expectedPlatform) {
        MethodOwner suitableOwner = null;
        for (MethodOwner owner : owners) {
            if (owner.platform().isEmpty()) {
                suitableOwner = owner;
            } else if (owner.platform().equalsIgnoreCase(expectedPlatform)) {
                suitableOwner = owner;
                // If an annotation was found suitable for a specific platform, there is no point in continuing to search further.
                break;
            }
        }
        return Optional.ofNullable(suitableOwner);
    }
}
