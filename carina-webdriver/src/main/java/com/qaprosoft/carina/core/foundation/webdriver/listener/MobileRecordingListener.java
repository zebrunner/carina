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
package com.qaprosoft.carina.core.foundation.webdriver.listener;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

import io.appium.java_client.MobileCommand;
import io.appium.java_client.screenrecording.BaseStartScreenRecordingOptions;
import io.appium.java_client.screenrecording.BaseStopScreenRecordingOptions;

/**
 * ScreenRecordingListener - starts/stops video recording for Android and IOS drivers.
 * 
 * @author akhursevich
 */
@SuppressWarnings({ "rawtypes"})
public class MobileRecordingListener<O1 extends BaseStartScreenRecordingOptions, O2 extends BaseStopScreenRecordingOptions> implements IDriverCommandListener {

    protected static final Logger LOGGER = Logger.getLogger(MobileRecordingListener.class);
    
    private CommandExecutor commandExecutor;

    private O1 startRecordingOpt;

    private O2 stopRecordingOpt;
    
    private boolean recording = false;
    
    private TestArtifactType videoArtifact;
    
    public MobileRecordingListener(CommandExecutor commandExecutor, O1 startRecordingOpt, O2 stopRecordingOpt, TestArtifactType artifact) {
        this.commandExecutor = commandExecutor;
        this.startRecordingOpt = startRecordingOpt;
        this.stopRecordingOpt = stopRecordingOpt;
        this.videoArtifact = artifact;
    }

    @Override
    public void beforeEvent(Command command) {
    	if (recording) {
    		onBeforeEvent();
    		
            if (DriverCommand.QUIT.equals(command.getName())) {
                try {
                    commandExecutor.execute(new Command(command.getSessionId(), 
                            MobileCommand.STOP_RECORDING_SCREEN, 
                            MobileCommand.stopRecordingScreenCommand((BaseStopScreenRecordingOptions) stopRecordingOpt).getValue()));

                    if (ZafiraSingleton.INSTANCE.isRunning()) {
                        ZafiraSingleton.INSTANCE.getClient().addTestArtifact(videoArtifact);
                    }
                } catch (Exception e) {
                    LOGGER.error("Unable to stop screen recording: " + e.getMessage(), e);
                }
            }
    	}
    }

    @Override
    public void afterEvent(Command command) {
        if (!recording && command.getSessionId() != null) {
            try {
                recording = true;
                commandExecutor.execute(new Command(command.getSessionId(), 
                        MobileCommand.START_RECORDING_SCREEN, 
                        MobileCommand.startRecordingScreenCommand((BaseStartScreenRecordingOptions) startRecordingOpt).getValue()));
            } catch (Exception e) {
                LOGGER.error("Unable to start screen recording: " + e.getMessage(), e);
            }
        }
    }
    
	private void onBeforeEvent() {
		// 4a. if "tzid" not exist inside videoArtifact and exists in Reporter -> register new videoArtifact in Zafira.
		// 4b. if "tzid" already exists in current artifact but in Reporter there is another value. Then this is use case for class/suite mode when we share the same
		// driver across different tests
		
		ITestResult res = Reporter.getCurrentTestResult();
		if (res != null && res.getAttribute("ztid") != null) {
			Long ztid = (Long) res.getAttribute("ztid");
			if (ztid != videoArtifact.getTestId()) {
				videoArtifact.setTestId(ztid);
				LOGGER.debug("Registered recorded video artifact " + videoArtifact.getName() + " into zafira");
				ZafiraSingleton.INSTANCE.getClient().addTestArtifact(videoArtifact);
			}
		}
	}
}
