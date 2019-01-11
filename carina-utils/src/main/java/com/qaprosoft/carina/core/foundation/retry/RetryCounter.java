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
package com.qaprosoft.carina.core.foundation.retry;

/**
 * @author Alex Khursevich (hursevch@gmail.com)
 */
public class RetryCounter {
    private static ThreadLocal<Integer> runCount = new ThreadLocal<Integer>();

    public static void initCounter() {
        if (runCount.get() != null) {
            // retryCounter already init for current thread
            return;
        }
        runCount.set(0);
    }

    public static Integer getRunCount() {
        int count = 0;
        if (runCount.get() != null) {
            // retryCounter already init for current thread
            count = runCount.get();
        }

        return count;
    }

    public static void incrementRunCount() {
        int count = 0;
        if (runCount.get() != null) {
            // retryCounter already init for current thread
            count = runCount.get();
        }
        runCount.set(++count);
    }

    public static void resetCounter() {
        // explicitly set runCount to 0 for current thread
        runCount.set(0);
    }

}
