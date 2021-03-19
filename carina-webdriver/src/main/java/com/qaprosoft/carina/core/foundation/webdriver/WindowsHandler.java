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
package com.qaprosoft.carina.core.foundation.webdriver;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Map<Integer, Set<String>> windows = new HashMap<Integer, Set<String>>();

    public static synchronized void saveBeforePopup(WebDriver driver) {
        windows.put(driver.hashCode(), driver.getWindowHandles());
    }

    public static void switchBackAfterPopup(WebDriver driver) {
        try {
            Set<String> beforeHandles = windows.get((driver).hashCode());
            String newWindowHandle = beforeHandles.iterator().next();
            driver.switchTo().window(newWindowHandle);
        } catch (Exception e) {
            LOGGER.warn("Switching to bottom window was not performed!");
        }
    }

    public static boolean switchToPopup(WebDriver driver) {
        try {
            Set<String> beforeHandles = windows.get((driver).hashCode());
            Set<String> afterHandles = driver.getWindowHandles();
            afterHandles.removeAll(beforeHandles);
            String newWindowHandle = afterHandles.iterator().next();
            driver.switchTo().window(newWindowHandle);
            return true;
        } catch (Exception e) {
            LOGGER.warn("Switching to top window was not performed!");
            return false;
        }
    }
}
