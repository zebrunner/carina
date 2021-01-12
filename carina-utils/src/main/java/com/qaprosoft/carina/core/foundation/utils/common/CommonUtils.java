/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils.common;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private CommonUtils() {
    	//hide public constructor
    }

    /**
     * pause
     *
     * @param timeout Number
     */
    public static void pause(Number timeout) {
        LOGGER.debug(String.format("Will wait for %s seconds", timeout));
        try {
            Float timeoutFloat = timeout.floatValue() * 1000;
            long timeoutLong = timeoutFloat.longValue();
            Thread.sleep(timeoutLong);
        } catch (InterruptedException e) {
            // do nothing
        }
        LOGGER.debug("Pause is overed. Keep going..");
    }
}
