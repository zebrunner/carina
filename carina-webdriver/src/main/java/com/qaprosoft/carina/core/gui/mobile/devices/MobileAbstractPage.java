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
package com.qaprosoft.carina.core.gui.mobile.devices;

import org.openqa.selenium.WebDriver;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.gui.AbstractPage;

public abstract class MobileAbstractPage extends AbstractPage {

    protected static final long DELAY = 10;

    protected static final long SHORT_TIMEOUT = Configuration.getLong(Configuration.Parameter.EXPLICIT_TIMEOUT) / 20;

    protected static final long ONE_SEC_TIMEOUT = 1;

    protected static final long DEFAULT_TRIES = 10;

    public static final long PUSH_NOTIFICATIONS_TIMEOUT = 120;

    public static final int SWIPE_DURATION = 1000;

    public MobileAbstractPage(WebDriver driver) {
        super(driver);
    }

    /**
     * @return true by default. Override it in child classes
     */
    public abstract boolean isOpened();

}
