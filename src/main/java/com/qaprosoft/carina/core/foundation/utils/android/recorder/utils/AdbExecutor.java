package com.qaprosoft.carina.core.foundation.utils.android.recorder.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.utils.android.recorder.exception.ExecutorException;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;

/**
 * Created by YP.
 * Date: 8/19/2014
 * Time: 12:57 AM
 */
public class AdbExecutor {
	private static final Logger LOGGER = Logger.getLogger(AdbExecutor.class);
	private String ADB_HOST;
	private String ADB_PORT;

	public AdbExecutor(String host, String port) {

		ADB_HOST="localhost";
		if (host != null)
			ADB_HOST = host;
		
		ADB_PORT="5037";
		if (host != null)
			ADB_PORT = port;		
	}

    public List<String> getAttachedDevices() {
        ProcessBuilderExecutor executor = null;
        BufferedReader in = null;

        try {
            String[] cmd = CmdLine.createPlatformDependentCommandLine("adb", "-H", ADB_HOST, "-P", ADB_PORT, "devices");
            executor = new ProcessBuilderExecutor(cmd);

            Process process = executor.start();
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;

            Pattern pattern = Pattern.compile("^([a-zA-Z0-9\\-]+)(\\s+)(device|offline)");
            List<String> devices = new ArrayList<String>();

            while ((line = in.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    devices.add(line);
                }
            }
            return devices;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            closeQuietly(in);
            ProcessBuilderExecutor.gcNullSafe(executor);
        }
    }

    public boolean isDeviceCorrect() {
        ProcessBuilderExecutor executor = null;
        BufferedReader in = null;
        boolean correctDevice = false;
        try {
            String[] cmd = CmdLine.createPlatformDependentCommandLine("adb", "-H", ADB_HOST, "-P", ADB_PORT, "-s", DevicePool.getDeviceUdid(), "shell", "getprop", "ro.build.version.sdk");
            executor = new ProcessBuilderExecutor(cmd);

            Process process = executor.start();
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = in.readLine();
            LOGGER.debug("sdkVersion: " + line);
            int sdkVersion = Integer.parseInt(line);
            correctDevice = sdkVersion >= 19;

        } catch (Exception e) {
        	e.printStackTrace();
            return correctDevice;
        } finally {
            closeQuietly(in);
            ProcessBuilderExecutor.gcNullSafe(executor);

        }
        return correctDevice;
    }

    public void execute(String[] cmd){
        ProcessBuilderExecutor executor = null;
        BufferedReader in = null;

        try {
            executor = new ProcessBuilderExecutor(cmd);

            Process process = executor.start();
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;


            while ((line = in.readLine()) != null) {
            	
                LOGGER.debug(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            closeQuietly(in);
            ProcessBuilderExecutor.gcNullSafe(executor);
        }
    }

    public int startRecording(String pathToFile) {
    	if (!isDeviceCorrect())
    		return -1;
    	
    	
    	String[] cmd = CmdLine.createPlatformDependentCommandLine("adb", "-H", ADB_HOST, "-P", ADB_PORT, "-s", DevicePool.getDeviceUdid(), "shell", "screenrecord", "--bit-rate", "6000000", "--verbose", pathToFile);

    	try {
            ProcessBuilderExecutor pb = new ProcessBuilderExecutor(cmd);

            pb.start();
            return pb.getPID();

    	} catch (ExecutorException e) {
    		e.printStackTrace();
            return -1;
        }
    }
    public void stopRecording(int pid) {
    	if (!isDeviceCorrect())
    		return;
    	
    	if (pid != -1) {
    		Platform.killProcesses(Arrays.asList(pid));
    	}
    }

    
    public void pullFile(String pathFrom, String pathTo){
    	if (!isDeviceCorrect())
    		return;
    	
    	String[] cmd = CmdLine.createPlatformDependentCommandLine("adb", "-H", ADB_HOST, "-P", ADB_PORT, "-s", DevicePool.getDeviceUdid(), "pull", pathFrom, pathTo);
    	execute(cmd);
    }
    
    public void dropFile(String pathToFile){
    	if (!isDeviceCorrect())
    		return;
    	
    	String[] cmd = CmdLine.createPlatformDependentCommandLine("adb", "-H", ADB_HOST, "-P", ADB_PORT, "-s", DevicePool.getDeviceUdid(), "shell", "rm", pathToFile);
    	execute(cmd);
    }
    
    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
        }
    }
    
    public void pressKey(int key){
    	if (!isDeviceCorrect())
    		return;
    	
    	String[] cmd = CmdLine.createPlatformDependentCommandLine("adb", "-H", ADB_HOST, "-P", ADB_PORT, "-s", DevicePool.getDeviceUdid(), "shell", "input", "keyevent", String.valueOf(key));
    	execute(cmd);
    }    
}
