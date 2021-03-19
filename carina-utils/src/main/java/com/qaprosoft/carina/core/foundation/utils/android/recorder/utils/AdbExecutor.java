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
package com.qaprosoft.carina.core.foundation.utils.android.recorder.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.Configuration;

/**
 * Created by YP.
 * Date: 8/19/2014
 * Time: 12:57 AM
 */

public class AdbExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // private static final String REMOTE_ADB_EXECUTION_CMD = "ssh %s@%s %s";
    private static String[] cmdInit;

    public AdbExecutor() {
        cmdInit = "adb".split(" ");
    }

    /**
     * getDefaultCmd from init Cmd
     * 
     * @return String[]
     */
    public String[] getDefaultCmd() {
        return cmdInit;
    }

    public List<String> execute(String[] cmd) {
        ProcessBuilderExecutor executor = null;
        BufferedReader in = null;
        List<String> output = new ArrayList<String>();

        try {
            executor = new ProcessBuilderExecutor(cmd);

            Process process = executor.start();
            if (!process.waitFor(Configuration.getAdbExecTimeout(), TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("Waiting time elapsed before the adb execution command has exited");
            }
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;

            while ((line = in.readLine()) != null) {
                output.add(line);
                LOGGER.debug(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            closeQuietly(in);
            ProcessBuilderExecutor.gcNullSafe(executor);
        }

        return output;
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
        }
    }

}
