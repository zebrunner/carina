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
package com.qaprosoft.carina.core.foundation.webdriver.augmenter;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmentable;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverAugmenter extends Augmenter {

    public DriverAugmenter() {
        super();
    }

    @Override
    protected RemoteWebDriver extractRemoteWebDriver(WebDriver driver) {
        if (driver.getClass().isAnnotationPresent(Augmentable.class)
                || driver
                        .getClass()
                        .getName()
                        .startsWith(
                                "org.openqa.selenium.remote.RemoteWebDriver$$EnhancerByCGLIB")
                || driver
                        .getClass()
                        .getName()
                        .startsWith(
                                "com.qaprosoft.carina.core.foundation.webdriver")) {
            return (RemoteWebDriver) driver;
        } else {
            return null;
        }
    }
}
