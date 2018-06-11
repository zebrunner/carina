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
package com.qaprosoft.carina.core.foundation.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.qaprosoft.carina.core.foundation.utils.Configuration;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.qaprosoft.carina.core.foundation.report.ReportContext;

/*
 * This appender log groups test outputs by test method/test thread so they don't mess up each other even they runs in parallel.
 */
public class ThreadLogAppender extends AppenderSkeleton {
    // single buffer for each thread test.log file
    private final ThreadLocal<BufferedWriter> testLogBuffer = new ThreadLocal<BufferedWriter>();
    private long maxBytes = Configuration.getLong(Configuration.Parameter.MAX_LOG_FILE_SIZE);;
    private long bytesWritten;
    @Override
    public void append(LoggingEvent event) {
        // TODO: [VD] OBLIGATORY double check and create separate unit test for this case
        /*
         * if (!ReportContext.isBaseDireCreated()) {
         * System.out.println(event.getMessage().toString());
         * return;
         * }
         */

        try {

            BufferedWriter fw = testLogBuffer.get();
            if (fw == null) {
                // 1st request to log something for this thread/test
                File testLogFile = new File(ReportContext.getTestDir() + "/test.log");
                if (!testLogFile.exists()){
                    testLogFile.createNewFile();
                    bytesWritten = 0;
                }

                fw = new BufferedWriter(new FileWriter(testLogFile, true));
                testLogBuffer.set(fw);

            }

            if (event != null) {
                // append time, thread, class name and device name if any
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // 2016-05-26 04:39:16
                String time = dateFormat.format(event.getTimeStamp());
                // System.out.println("time: " + time);

                long threadId = Thread.currentThread().getId();
                // System.out.println("thread: " + threadId);
                String fileName = event.getLocationInformation().getFileName();
                // System.out.println("fileName: " + fileName);

                String logLevel = event.getLevel().toString();

                String message = "[%s] [%s] [%s] [%s] %s";
                message = String.format(message, time, fileName, threadId, logLevel, event.getMessage().toString());
                ensureCapacity(message.length());
                fw.write(message);
            } else {
                fw.write("null");
            }
            fw.write("\n");
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() {
        try {
            BufferedWriter fw = testLogBuffer.get();
            if (fw != null) {
                fw.close();
                testLogBuffer.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    private void ensureCapacity(int len) throws IOException {
        long newBytesWritten = this.bytesWritten + len;
        long maxBytes = this.maxBytes *1024 * 1024;
        if (newBytesWritten > maxBytes)
            throw new IOException("File size exceeded: " + newBytesWritten + " > " + this.maxBytes);
        this.bytesWritten = newBytesWritten;
    }
}
