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

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.qaprosoft.carina.core.foundation.performance.Operation.OPERATIONS;

public class PerformanceTest {

    @Test(priority = 1)
    public void testAlreadyStartedMetric() {
        Timer.start(OPERATIONS.TEST);
        try {
            Timer.start(OPERATIONS.TEST);
            Assert.fail("Operation already started exception is not raised!");
        } catch (Exception e) {
            Assert.assertEquals("Operation already started: " + OPERATIONS.TEST.getKey(), e.getMessage());
        }
        Timer.stop(OPERATIONS.TEST);
    }

//    TODO: enable as soon as SocketException will be fixed
    @Test(priority = 2, enabled = false)
    public void testNotStartedMetric() {
        try {
            Timer.stop(OPERATIONS.TEST2);
            Assert.fail("Operation not started is not raised!");
        } catch (Exception e) {
            Assert.assertEquals("Operation not started: " + OPERATIONS.TEST2.getKey(), e.getMessage());
        }
    }

    @Test(priority = 3)
    public void testClearMetric() {
        Timer.start(OPERATIONS.TEST3);
        pause(0.1);
        Timer.stop(OPERATIONS.TEST3);

        Timer.clear();
        Map<String, Long> testMetrics = Timer.readAndClear();
        Assert.assertEquals(testMetrics.size(), 0);
    }

    @Test(priority = 4)
    public void testTrackMetric() {
        Timer.start(OPERATIONS.TEST4);
        pause(0.1);
        Timer.stop(OPERATIONS.TEST4);

        Map<String, Long> testMetrics = Timer.readAndClear();
        Assert.assertEquals(testMetrics.size(), 1);
        Assert.assertTrue(testMetrics.containsKey(OPERATIONS.TEST4.getKey()));

        // map should be empty for 2nd read attempt
        testMetrics = Timer.readAndClear();
        Assert.assertEquals(testMetrics.size(), 0);

    }

    @Test(priority = 5)
    public void testNotStoppedMetric() {
        Timer.start(OPERATIONS.TEST5);
        Map<String, Long> testMetrics = Timer.readAndClear();
        // do not return non stopped metric
        Assert.assertEquals(testMetrics.size(), 0);
    }
    
    private void pause(Number timeout) {
        try {
            Float timeoutFloat = timeout.floatValue() * 1000;
            long timeoutLong = timeoutFloat.longValue();
            Thread.sleep(timeoutLong);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
