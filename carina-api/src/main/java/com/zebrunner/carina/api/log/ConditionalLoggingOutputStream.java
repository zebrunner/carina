/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.api.log;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.function.Predicate;

public class ConditionalLoggingOutputStream extends LoggingOutputStream {

    private Predicate<Response> logCondition;

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param log   the Logger to write to
     * @param level the log level
     * @throws IllegalArgumentException in case if one of arguments is null.
     */
    public ConditionalLoggingOutputStream(Logger log, Level level) throws IllegalArgumentException {
        super(log, level);
    }

    @Override
    public void close() {
        // No operation
    }

    public void conditionLogging(Response response) {
        if (logCondition.test(response)) {
            super.flush();
        }
    }

    public void setLogCondition(Predicate<Response> logCondition) {
        this.logCondition = logCondition;
    }
}
