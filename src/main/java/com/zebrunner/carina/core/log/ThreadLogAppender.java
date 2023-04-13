/*******************************************************************************
 * Copyright 2020-2023 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.core.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;
import org.slf4j.MDC;

import com.zebrunner.carina.utils.report.ReportContext;

/*
 * This appender log groups test outputs by test method/test thread so they don't mess up each other even they runs in parallel.
 */
@Plugin(
        name = "ThreadLogAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE
)
public class ThreadLogAppender extends AbstractAppender {

    private static final long MAX_LOG_FILE_SIZE_IN_MEGABYTES = (long) 1024 * 1024 * 1024;
    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    private static final ThreadLocal<File> currentTestDirectory = new ThreadLocal<>();
    // single buffer for each thread test.log file
    private static final ThreadLocal<BufferedWriter> testLogBuffer = new ThreadLocal<>();

    private static final Map<String, Long> fileNameToWrittenBytes = new ConcurrentHashMap<>();

    private ThreadLogAppender(String name,
                              Filter filter,
                              Layout<? extends Serializable> layout,
                              boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static ThreadLogAppender create(@PluginAttribute("name") String name,
                                           @PluginElement("Layout") Layout<? extends Serializable> layout,
                                           @PluginElement("Filter") Filter filter) {

        if (name == null) {
            LOGGER.error("No name provided for ThreadLogAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new ThreadLogAppender(name, filter, layout, true);
    }

    @Override
    public void append(LogEvent event) {
        // TODO: [VD] OBLIGATORY double check and create separate unit test for this case
        /*
         * if (!ReportContext.isBaseDireCreated()) {
         * System.out.println(event.getMessage().toString());
         * return;
         * }
         */

        try {
            BufferedWriter logFileWriter = testLogBuffer.get();

            // check does writer log to the correct test directory, if not - reinit it
            if (currentTestDirectory.get() != ReportContext.getTestDir()) {
                logFileWriter = null;
            }

            String logFilePath = ReportContext.getTestDir() + "/test.log";
            if (logFileWriter == null) {
                // 1st request to log something for this thread/test
                File testLogFile = new File(logFilePath);
                currentTestDirectory.set(ReportContext.getTestDir());

                if (!testLogFile.exists()) {
                    testLogFile.createNewFile();
                }

                logFileWriter = new BufferedWriter(new FileWriter(testLogFile, true));
                testLogBuffer.set(logFileWriter);

                fileNameToWrittenBytes.putIfAbsent(logFilePath, 0L);
            }

            String logLine = this.toLogLine(event);
            long newWrittenBytes = fileNameToWrittenBytes.get(logFilePath) + logLine.length();
            if (newWrittenBytes > MAX_LOG_FILE_SIZE_IN_MEGABYTES) {
                throw new IOException("test Log file size exceeded core limit: " + newWrittenBytes + " > " + MAX_LOG_FILE_SIZE_IN_MEGABYTES);
            }

            logFileWriter.write(logLine);
            logFileWriter.flush();

            fileNameToWrittenBytes.computeIfPresent(logFilePath, ($, bytesWritten) -> bytesWritten + logLine.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLogLine(LogEvent event) {
        String logTime = LocalDateTime.ofEpochSecond(event.getInstant().getEpochSecond(),
                                                     event.getInstant().getNanoOfSecond(),
                                                     ZoneOffset.UTC)
                                      .format(LOG_TIME_FORMATTER);

        long threadId = Thread.currentThread().getId();
        MDC.put("threadId", "-" + threadId);
        String logLevel = event.getLevel().toString();

        Message eventMessage = event.getMessage();
        String logMessage = eventMessage != null
                ? eventMessage.getFormattedMessage()
                : "";

        return "[" + logTime + "] " + "[" + threadId + "] " + "[" + logLevel + "] " + logMessage + "\n";
    }

    @Override
    public void stop() {
        try {
            BufferedWriter fw = testLogBuffer.get();
            if (fw != null) {
                fw.close();
                testLogBuffer.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.setStopped();
        }
    }

}
