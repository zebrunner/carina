package com.qaprosoft.carina.core.foundation.utils.android.recorder.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.qaprosoft.carina.core.foundation.http.HttpClient;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.Configuration.Parameter;
import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.android.recorder.exception.ExecutorException;
import com.qaprosoft.carina.core.foundation.webdriver.device.Device;
import com.qaprosoft.carina.core.foundation.webdriver.device.DevicePool;


/**
 * Created by YP.
 * Date: 8/19/2014
 * Time: 12:57 AM
 */

//TODO: rename class to CmdExecutor as we added iOS shell commands as well
public class AdbExecutor {
    private static final Logger LOGGER = Logger.getLogger(AdbExecutor.class);
    
    private static final String REMOTE_ADB_EXECUTION_CMD = "ssh %s@%s %s";
    
    private static String[] cmdInit;
    
    public AdbExecutor(String host, String port) {
    	initDefaultCmd();
    }

    public AdbExecutor() {
    	initDefaultCmd();
    }
    
    private void initDefaultCmd() {
    	String tempCmd = "";
    	String adbPath = "adb";
    	if (!Configuration.get(Parameter.ADB_PATH).isEmpty()) {
    		adbPath = Configuration.get(Parameter.ADB_PATH);
    	}
    	if (DevicePool.isSystemDistributed()) {
    		// check if device server value equals to IP of the PC where tests were launched.
    		// adb can be executed locally in this case.
    		String currentIP = HttpClient.getIpAddress();
    		LOGGER.debug("Local IP: ".concat(currentIP));
    		String remoteServer = DevicePool.getServer();
    		if (!remoteServer.equals(currentIP)){
    			String login = Configuration.get(Parameter.SSH_USERNAME);
        		tempCmd = String.format(REMOTE_ADB_EXECUTION_CMD, login, remoteServer, adbPath);
    		}
    	}
    	
    	// TODO: it can be slightly modified 
    	// when issues with remote adb execution will be resolved: "concat("-H ADB_HOST -P ADB_PORT")"
    	cmdInit = tempCmd.concat("adb").split(" ");
    }
    
    public List<String> getAttachedDevices() {
        ProcessBuilderExecutor executor = null;
        BufferedReader in = null;

        try {
        	String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "devices");
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
        return isDeviceCorrect(DevicePool.getDeviceUdid());
    }

    public boolean isDeviceCorrect(String udid) {
        ProcessBuilderExecutor executor = null;
        BufferedReader in = null;
        boolean correctDevice = false;
        try {
        	String[] cmd = CmdLine.insertCommandsAfter(cmdInit , "-s", udid, "shell", "getprop", "ro.build.version.sdk");
        	executor = new ProcessBuilderExecutor(cmd);

            Process process = executor.start();
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = in.readLine();
            LOGGER.debug("sdkVersion: " + line);
            if (line != null) {
                int sdkVersion = Integer.parseInt(line);
                correctDevice = sdkVersion >= 19;
            } else {
                LOGGER.error("SDK version for '" + DevicePool.getDevice(udid).getName() + "' device is not recognized!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return correctDevice;
        } finally {
            closeQuietly(in);
            ProcessBuilderExecutor.gcNullSafe(executor);

        }
        return correctDevice;
    }

    public List<String> execute(String[] cmd) {
        ProcessBuilderExecutor executor = null;
        BufferedReader in = null;
        List<String> output = new ArrayList<String>();

        try {
            executor = new ProcessBuilderExecutor(cmd);

            Process process = executor.start();
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

    public int startRecording(String pathToFile) {
        if (!isDeviceCorrect())
            return -1;
        
        if (!Configuration.getBoolean(Parameter.VIDEO_RECORDING)) {
        	return -1;
        }

        dropFile(pathToFile);
        
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", DevicePool.getDeviceUdid(), "shell", "screenrecord", "--bit-rate", "6000000", "--verbose", pathToFile);

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


    public void pullFile(String pathFrom, String pathTo) {
        if (!isDeviceCorrect())
            return;
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", DevicePool.getDeviceUdid(), "pull", pathFrom, pathTo);
        execute(cmd);
    }

    public void dropFile(String pathToFile) {
        if (!isDeviceCorrect())
            return;
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", DevicePool.getDeviceUdid(), "-s", DevicePool.getDeviceUdid(), "shell", "rm", pathToFile);
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

    public void pressKey(int key) {
        if (!isDeviceCorrect())
            return;

        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", DevicePool.getDeviceUdid(), "shell", "input", "keyevent", String.valueOf(key));
        execute(cmd);
    }

    @Deprecated
    public void clearAppData(Device device, String app) {
        //adb -s UDID shell pm clear com.myfitnesspal.android
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", device.getUdid(), "shell", "pm", "clear", app);
        execute(cmd);
    }
    
    public void clearAppData(Device device) {
    	if (!Configuration.getBoolean(Parameter.MOBILE_APP_CLEAR_CACHE))
    		return;

        if (!isDeviceCorrect())
            return;

        //adb -s UDID shell pm clear com.myfitnesspal.android
        String packageName = getApkPackageName(Configuration.get(Parameter.MOBILE_APP));
        
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", device.getUdid(), "shell", "pm", "clear", packageName);
        execute(cmd);
    }


    public void uninstallApp(Device device, String packageName) {
        if (!isDeviceCorrect())
            return;
        //adb -s UDID uninstall com.myfitnesspal.android
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", device.getUdid(), "uninstall", packageName);
        execute(cmd);
    }
    
    public void installApp(Device device, String packageName) {
        if (!isDeviceCorrect())
            return;
        //adb -s UDID install com.myfitnesspal.android
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", device.getUdid(), "install", packageName);
        execute(cmd);
    }
    
    public synchronized void installAppSync(Device device, String packageName) {
        if (!isDeviceCorrect())
            return;
        //adb -s UDID install com.myfitnesspal.android
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", device.getUdid(), "install", packageName);
        execute(cmd);
    }
    
    public void reinstallApp(Device device, String mobileApp) {

    	if (Configuration.getBoolean(Parameter.MOBILE_APP_UNINSTALL)) {
			// explicit reinstall the apk
			String[] apkVersions = getApkVersion(mobileApp); // Configuration.get(Parameter.MOBILE_APP)
			if (apkVersions != null) {
				String appPackage = apkVersions[0];

				String[] apkInstalledVersions = getInstalledApkVersion(device, appPackage);

				LOGGER.info("installed app: " + apkInstalledVersions[2] + "-" + apkInstalledVersions[1]);
				LOGGER.info("new app: " + apkVersions[2] + "-" + apkVersions[1]);

				if (apkVersions[1].equals(apkInstalledVersions[1]) && apkVersions[2].equals(apkInstalledVersions[2])) {
					LOGGER.info(
							"Skip application uninstall and cache cleanup as exactly the same version is already installed.");
				} else {
					uninstallApp(device, appPackage);
					clearAppData(device, appPackage);
				}
			}
			if (Configuration.getBoolean(Parameter.MOBILE_APP_INSTALL)) {
				// install application in single thread to fix issue with gray Google maps
				installAppSync(device, mobileApp);
			}
		}
    }
    
    public void eraseSimulator(String id) {
    	if (!Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.IOS))
    		return;
    		
        //killall "Simulator"
        //xcrun simctl erase $id || echo 'Erase failed'
    	
    	LOGGER.info("Erasing iOS simulators... ");
    	
    	//TODO: implement solution to find simulatorId by device name aka 'iPhone 6'
		String cmdLine ="killall Simulator";
		String[] cmd = CmdLine.insertCommandsAfter(cmdLine.split(" "));
		List<String> output = execute(cmd);
		for (String line : output) {
			LOGGER.debug(line);
		}
		
		
		cmdLine ="xcrun simctl erase all";
		cmd = CmdLine.insertCommandsAfter(cmdLine.split(" "));
		output = execute(cmd);
		for (String line : output) {
			LOGGER.debug(line);
		}
		
		cmdLine ="rm -rf $HOME/Library/Developer/CoreSimulator/Devices/" + id;
		cmd = CmdLine.insertCommandsAfter(cmdLine.split(" "));
		output = execute(cmd);
		for (String line : output) {
			LOGGER.debug(line);
		}
    }
    
    public String[] getInstalledApkVersion(Device device, String packageName) {
        //adb -s UDID shell dumpsys package PACKAGE | grep versionCode
        
        
        String[] res = new String[3];
        res[0] = packageName;
        
        String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", device.getUdid(), "shell",  "dumpsys",  "package", packageName);
		List<String> output = execute(cmd);
        
		
		for (String line : output) {
			LOGGER.debug(line);
			if (line.contains("versionCode")) {
				// versionCode=17040000 targetSdk=25
				LOGGER.info("Line for parsing installed app: " + line);
				String[] outputs = line.split("=");
				String tmp = outputs[1]; //everything after '=' sign
				res[1] = tmp.split(" ")[0];
			}
			
			if (line.contains("versionName")) {
				// versionName=8.5.0
				LOGGER.info("Line for parsing installed app: " + line);
				String[] outputs = line.split("=");
				res[2] = outputs[1];
			}
		}
		
		return res;
    }
    
	public String[] getApkVersion(String apkFile) {

		// aapt dump badging <apk_file> | grep versionCode
		// aapt dump badging <apk_file> | grep versionName
		// output:
		// package: name='com.myfitnesspal.android' versionCode='9025' versionName='develop-QA' platformBuildVersionName='6.0-2704002'

		String[] cmd = CmdLine.insertCommandsAfter("aapt dump badging".split(" "), apkFile);
		List<String> output = execute(cmd);
		// parse output command and get appropriate data
		String[] res = new String[3];

		for (String line : output) {
			if (line.contains("versionCode") && line.contains("versionName")) {
				LOGGER.debug(line);
				String[] outputs = line.split("'");
				res[0] = outputs[1]; //package
				res[1] = outputs[3]; //versionCode
				res[2] = outputs[5]; //versionName
			}
		}

		return res;
	}
	
	public String getApkPackageName(String apkFile) {
		// aapt dump badging <apk_file> | grep versionCode
		// aapt dump badging <apk_file> | grep versionName
		// output:
		// package: name='com.myfitnesspal.android' versionCode='9025' versionName='develop-QA' platformBuildVersionName='6.0-2704002'

		String[] cmd = CmdLine.insertCommandsAfter("aapt dump badging".split(" "), apkFile);
		List<String> output = execute(cmd);
		// parse output command and get appropriate data
		String packageName = "";

		for (String line : output) {
			if (line.contains("versionCode") && line.contains("versionName")) {
				LOGGER.debug(line);
				String[] outputs = line.split("'");
				packageName = outputs[1]; //package
			}
		}

		return packageName;
	}
    
	public void restartAppium(Device device) {
		if (!Configuration.getBoolean(Parameter.MOBILE_APPIUM_RESTART))
			return;
		
		stopAppium(device);
		startAppium(device);
	}
	
    // TODO: think about moving shutdown/startup scripts into external property and make it cross platform 
	public void stopAppium(Device device) {
		if (!Configuration.getBoolean(Parameter.MOBILE_APPIUM_RESTART))
			return;

		LOGGER.info("Stopping appium...");
		
		String cmdLine = Configuration.get(Parameter.MOBILE_TOOLS_HOME) + "/stopNodeAppium.sh";
		String[] cmd = CmdLine.insertCommandsAfter(cmdLine.split(" "), device.getUdid());
		List<String> output = execute(cmd);
		for (String line : output) {
			LOGGER.debug(line);
		}
	}
	
	public void startAppium(Device device) {
		if (!Configuration.getBoolean(Parameter.MOBILE_APPIUM_RESTART))
			return;

		LOGGER.info("Starting appium...");
		
		String cmdLine = Configuration.get(Parameter.MOBILE_TOOLS_HOME) + "/startNodeAppium.sh";
		String[] cmd = CmdLine.insertCommandsAfter(cmdLine.split(" "), device.getUdid(), "&");
		List<String> output = execute(cmd);
		for (String line : output) {
			LOGGER.debug(line);
		}
		//TODO: implement verification logic for started appium
		pause(8);
	}
	

    private Boolean getScreenState(String udid) {
        // determine current screen status
        // adb -s <udid> shell dumpsys input_method | find "mScreenOn"
    	 String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", udid, "shell", "dumpsys",
                "input_method");
        List<String> output = execute(cmd);

        Boolean screenState = null;
        String line;

        Iterator<String> itr = output.iterator();
        while (itr.hasNext()) {
            // mScreenOn - default value for the most of Android devices
            // mInteractive - for Nexuses
            line = itr.next();
            if (line.contains("mScreenOn=true") || line.contains("mInteractive=true")) {
                screenState = true;
                break;
            }
            if (line.contains("mScreenOn=false") || line.contains("mInteractive=false")) {
                screenState = false;
                break;
            }
        }

        if (screenState == null) {
            LOGGER.error(udid
                    + ": Unable to determine existing device screen state!");
            return screenState; //no actions required if state is not recognized.
        }

        if (screenState) {
            LOGGER.info(udid + ": screen is ON");
        }

        if (!screenState) {
            LOGGER.info(udid + ": screen is OFF");
        }

        return screenState;
    }


    public void screenOff() {
        if (!Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.ANDROID)) {
            return;
        }
        if (!Configuration.getBoolean(Parameter.MOBILE_SCREEN_SWITCHER)) {
            return;
        }
        String udid = DevicePool.getDeviceUdid();

        if (!isDeviceCorrect(udid)) {
            return;
        }

        Boolean screenState = getScreenState(udid);
        if (screenState == null) {
            return;
        }
        if (screenState) {
        	 String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", udid, "shell",
                    "input", "keyevent", "26");
            execute(cmd);

            pause(5);

            screenState = getScreenState(udid);
            if (screenState) {
                LOGGER.error(udid + ": screen is still ON!");
            }

            if (!screenState) {
                LOGGER.info(udid + ": screen turned off.");
            }
        }
    }


    public void screenOn() {
        if (!Configuration.get(Parameter.MOBILE_PLATFORM_NAME).equalsIgnoreCase(SpecialKeywords.ANDROID)) {
            return;
        }

        if (!Configuration.getBoolean(Parameter.MOBILE_SCREEN_SWITCHER)) {
            return;
        }

        String udid = DevicePool.getDeviceUdid();
        if (!isDeviceCorrect(udid)) {
            return;
        }

        Boolean screenState = getScreenState(udid);
        if (screenState == null) {
            return;
        }

        if (!screenState) {
        	 String[] cmd = CmdLine.insertCommandsAfter(cmdInit, "-s", udid, "shell",
                    "input", "keyevent", "26");
            execute(cmd);

            pause(5);
            // verify that screen is Off now
            screenState = getScreenState(udid);
            if (!screenState) {
                LOGGER.error(udid + ": screen is still OFF!");
            }

            if (screenState) {
                LOGGER.info(udid + ": screen turned on.");
            }
        }
    }

    public void pause(long timeout) {
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
