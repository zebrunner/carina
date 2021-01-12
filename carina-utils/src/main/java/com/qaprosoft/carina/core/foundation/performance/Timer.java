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
package com.qaprosoft.carina.core.foundation.performance;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class Timer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        start(operation, "");
    }
    
    /**
     * Start timer to track IPerformanceOperation action using extra key.
     * 
     * @param operation
     *            IPerformanceOperation.
     * @param key
     *            String.
     */
    public static synchronized void start(IPerformanceOperation operation, String key) {
        String operationKey =  operation.getKey() + key;
        Map<String, Long> testTimer = getTimer();
        if (testTimer.containsKey(operationKey)) {
            // do not put new time as the same operation already started.
            LOGGER.error("Operation already started: " + operationKey);
        } else {
            testTimer.put(operationKey, Calendar.getInstance().getTimeInMillis());
        }
    }

    /**
     * Stop timer and calculate summarized execution time for the action 
     * 
     * @param operation
     *            IPerformanceOperation.
     * @return long elapsedTime from last start/stop.
     */
    public static synchronized long stop(IPerformanceOperation operation) {
        return stop(operation, "");
    }
    
    /**
     * Stop timer and calculate summarized execution time for the action using extra key
     * 
     * @param operation
     *            IPerformanceOperation.
     * @param key
     *            String.
     * @return long elapsedTime from last start/stop.
     */
    public static synchronized long stop(IPerformanceOperation operation, String key) {
        String operationKey =  operation.getKey() + key;
        Map<String, Long> testTimer = getTimer();
        if (!testTimer.containsKey(operationKey)) {
			// TODO: current exception could stop tests execution which is
			// inappropriate. Think about error'ing only
//            Disabled due to socket issue
//            throw new RuntimeException("Operation not started: " + operationKey);
            LOGGER.error("Operation not started: " + operationKey);
            testTimer.remove(operationKey);
            return 0;
        }
        
        Map<String, Long> testMertrics = getMetrics();
        long capturedTime = 0;
        if (testMertrics.get(operationKey) != null) {
            	//summarize operation time
            	capturedTime = testMertrics.get(operationKey);
        }
        
        long elapsedTime = testTimer.get(operationKey);
        testMertrics.put(operationKey, capturedTime + Calendar.getInstance().getTimeInMillis() - elapsedTime);
        //remove stopped timer data
        testTimer.remove(operationKey);
        
        return elapsedTime;
    }


    //TODO: investigate if this call from ZafiraConfigurator could remove "ACTION_NAME.RUN_SUITE" data 
    public static synchronized Map<String, Long> readAndClear() {
        Map<String, Long> testTimer = getTimer();
        for (String key : testTimer.keySet()) {
            // timer not stopped
            LOGGER.debug("Timer not stopped for operation: " + key);
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
