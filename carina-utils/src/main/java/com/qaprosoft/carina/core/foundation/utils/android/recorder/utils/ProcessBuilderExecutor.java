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

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.utils.android.recorder.exception.ExecutorException;

/**
 * Created by YP.
 * Date: 8/19/2014
 * Time: 12:32 AM
 */
public class ProcessBuilderExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Map<Integer, Process> runPIDs = new HashMap<Integer, Process>();

    private Process process;

    private int pid;

    private boolean alreadyPerformed;

    private String[] cmd;

    private ProcessBuilder pb;

    public ProcessBuilderExecutor(String... cmd) {
        this.cmd = cmd;
        alreadyPerformed = false;
    }

    private ProcessBuilder getProcessBuilder() {
        if (pb == null) {
            pb = new ProcessBuilder(cmd);
        }
        return pb;
    }

    public List<String> getCommand() {
        return getProcessBuilder().command();
    }

    public Map<String, String> getEnvironment() {
        return getProcessBuilder().environment();
    }

    public int getPID() throws ExecutorException {
        if (!alreadyPerformed) {
            throw new ExecutorException("Process not started yet.");
        }
        return pid;
    }

    public Process start() throws ExecutorException {
        if (alreadyPerformed) {
            throw new ExecutorException("Multiple execution attempt.");
        }
        ProcessBuilder pb = getProcessBuilder();
        LOGGER.debug("trying to execute:  " + pb.command());
        try {
            process = pb.start();

            pid = Platform.getPID(process);
            addToGlobalGC(process, pid);
            return process;
        } catch (Exception e) {
            throw new ExecutorException(e.getMessage(), e);
        } finally {
            alreadyPerformed = true;
            LOGGER.debug("Process started. PID = " + pid);
        }
    }

    public void gc() {
        destroyProcess(process);
    }

    @Override
    protected void finalize() throws Throwable {
        // LOGGER.debug("finalize");
        try {
            gc();
        } finally {
            super.finalize();
        }
    }

    public static void gcNullSafe(ProcessBuilderExecutor executor) {
        if (executor != null) {
            executor.gc();
        }
    }

    private static void destroyProcess(Process process) {
        if (process == null) {
            return;
        }

        InputStream is = process.getInputStream();
        InputStream err = process.getErrorStream();
        OutputStream out = process.getOutputStream();

        process.destroy();

        // ensure streams are closed
        closeQuietly(is);
        closeQuietly(err);
        closeQuietly(out);
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static void addToGlobalGC(Process process, int pid) {
        synchronized (ProcessBuilderExecutor.class) {
            runPIDs.put(pid, process);
        }
    }

    public static void gcGlobal() {
        synchronized (ProcessBuilderExecutor.class) {

            Collection<Process> processes = runPIDs.values();
            LOGGER.debug("perform process cleaning ... (" + processes.size() + " processes need to destroy)");
            for (Process p : processes) {
                destroyProcess(p);
            }
            Platform.killProcesses(runPIDs.keySet());
        }
    }

}
