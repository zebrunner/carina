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
package com.qaprosoft.carina.core.foundation.utils.async;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncOperation {

    private static final Logger LOGGER = Logger.getLogger(AsyncOperation.class);

    private static final ThreadLocal<Set<CompletableFuture<?>>> asyncOperation = ThreadLocal.withInitial(HashSet::new);

    public static void add(CompletableFuture<?>... completableFutures) {
        LOGGER.debug("Async operations adding");
        asyncOperation.get().addAll(Arrays.asList(completableFutures));
    }

    public static void add(CompletableFuture<?> completableFuture) {
        LOGGER.debug("Async operation adding");
        asyncOperation.get().add(completableFuture);
    }

    public static void waitUntilFinish(long timeout) {
        try {
            CompletableFuture.allOf(asyncOperation.get().toArray(new CompletableFuture[0])).get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            asyncOperation.remove();
        }
    }

}
