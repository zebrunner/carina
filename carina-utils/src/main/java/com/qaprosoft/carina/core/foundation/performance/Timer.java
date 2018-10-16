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

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class Timer {
    private static final Logger LOGGER = Logger.getLogger(Timer.class);

    //data structure to collect summarized/combined datetime  
    private static ThreadLocal<ConcurrentHashMap<String, Long>> metrics = new ThreadLocal<ConcurrentHashMap<String, Long>>();
    
    //data structure for current timer only
    private static ThreadLocal<ConcurrentHashMap<String, Long>> timer = new ThreadLocal<ConcurrentHashMap<String, Long>>();

    /**
     * Start timer to track IPerformanceOperation action.
     * 
     * @param operation
     *            IPerformanceOperation.
     */
    public static synchronized void start(IPerformanceOperation operation) {
        Map<String, Long> testTimer = getTimer();
        if (testTimer.containsKey(operation.getKey())) {
            throw new RuntimeException("Operation already started: " + operation.getKey());
        }
        testTimer.put(operation.getKey(), Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Stop timer and calculate summarized execution time for the action 
     * 
     * @param operation
     *            IPerformanceOperation.
     * @return long elapsedTime from last start/stop.
     */
    public static synchronized long stop(IPerformanceOperation operation) {
        Map<String, Long> testTimer = getTimer();
        if (!testTimer.containsKey(operation.getKey())) {
			// TODO: current exception could stop tests execution which is
			// inappropriate. Think about error'ing only
//            Disabled due to socket issue
//            throw new RuntimeException("Operation not started: " + operation.getKey());
            LOGGER.error("Operation not started: " + operation.getKey());
            testTimer.remove(operation.getKey());
            return 0;
        }
        
        Map<String, Long> testMertrics = getMetrics();
        long capturedTime = 0;
        if (testMertrics.get(operation.getKey()) != null) {
            	//summarize operation time
            	capturedTime = testMertrics.get(operation.getKey());
        }
        
        long elapsedTime = testTimer.get(operation.getKey());
        testMertrics.put(operation.getKey(), capturedTime + Calendar.getInstance().getTimeInMillis() - elapsedTime);
        //remove stopped timer data
        testTimer.remove(operation.getKey());
        
        return elapsedTime;
    }


    //TODO: investigate if this caal from ZafiraConfigurator could remove "ACTION_NAME.RUN_SUITE" data 
    public static synchronized Map<String, Long> readAndClear() {
        Map<String, Long> testTimer = getTimer();
        for (String key : testTimer.keySet()) {
            // timer not stopped
            LOGGER.error("Timer not stopped for operation: " + key);
        }

        Map<String, Long> testMertrics = getMetrics();
        Map<String, Long> returnMetrics = new ConcurrentHashMap<>(testMertrics);
        // clear
        testTimer.clear();
        testMertrics.clear();
        return returnMetrics;
    }

    private static Map<String, Long> getTimer() {
        ConcurrentHashMap<String, Long> testTimer = timer.get();
        if (testTimer == null) {
        	testTimer = new ConcurrentHashMap<>();
            timer.set(testTimer);
        }
        return testTimer;
    }
    
    public static synchronized void clear() {
        getMetrics().clear();
    }

    private static Map<String, Long> getMetrics() {
        ConcurrentHashMap<String, Long> testMetrics = metrics.get();
        if (testMetrics == null) {
            testMetrics = new ConcurrentHashMap<>();
            metrics.set(testMetrics);
        }
        return testMetrics;
    }

    public interface IPerformanceOperation {
        String getKey();
    }
}
