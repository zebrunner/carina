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

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoggingOutputStreamTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testLoggingOutputStream() {
        LoggingOutputStream loggingOutputStream = new LoggingOutputStream(LOGGER, Level.INFO);
        try {
            String str = "Hello World!";
            for (byte charByte: str.getBytes()) {
                loggingOutputStream.write(charByte);
            }
            loggingOutputStream.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test
    public void testLoggingOutputStreamWithClosedStream() {
        LoggingOutputStream loggingOutputStream = new LoggingOutputStream(LOGGER, Level.INFO);
        try {
            String str = "Hello World!";
            loggingOutputStream.close();
            for (byte charByte: str.getBytes()) {
                loggingOutputStream.write(charByte);
            }
        } catch (IOException e) {
            Assert.assertEquals(e.getMessage(), "The stream has been closed.");
        }
    }
}
