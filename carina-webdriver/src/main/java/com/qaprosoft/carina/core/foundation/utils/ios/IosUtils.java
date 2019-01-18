/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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
package com.qaprosoft.carina.core.foundation.utils.ios;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils;

import io.appium.java_client.ios.IOSDriver;

/**
 * Useful iOS utilities. For usage: import
 * com.qaprosoft.carina.core.foundation.utils.ios.IosUtils;
 *
 */
public class IosUtils extends MobileUtils {

    private static final Logger LOGGER = Logger.getLogger(IosUtils.class);

    /**
     * Hide Keyboard
     * Use com.qaprosoft.carina.core.foundation.utils.mobile.MobileUtils.hideKeyboard()
     */
    @Deprecated
    public static void hideKeyboard() {
        try {
            ((IOSDriver<?>) getDriver()).hideKeyboard();
        } catch (Exception e) {
            LOGGER.info("Keyboard was already hided");
        }
    }

}