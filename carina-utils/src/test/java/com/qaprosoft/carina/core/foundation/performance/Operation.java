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
package com.qaprosoft.carina.core.foundation.performance;

import com.qaprosoft.carina.core.foundation.performance.Timer.IPerformanceOperation;

public class Operation {

    public enum OPERATIONS implements IPerformanceOperation {
        TEST("test"),
        TEST2("test2"),
        TEST3("test3"),
        TEST4("test4"),
        TEST5("test5");

        private String key;

        private OPERATIONS(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return this.key;
        }

    }

}
