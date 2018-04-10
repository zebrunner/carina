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
package com.qaprosoft.carina.core.foundation.performance;

import com.qaprosoft.carina.core.foundation.performance.Timer.IPerformanceOperation;

public class CoreOperation {

    public enum CORE_OPERATIONS implements IPerformanceOperation {
        PAUSE("pause"),
        WAIT("wait"),
        CAPTURE_SCREENSHOT("capture_screenshot"),
        CLICK("click"),
        TYPE("type");

        private String key;

        private CORE_OPERATIONS(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return this.key;
        }

    }

}
