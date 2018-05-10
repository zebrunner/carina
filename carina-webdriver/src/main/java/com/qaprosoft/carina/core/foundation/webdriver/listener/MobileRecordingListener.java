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
    
    private TestArtifactType artifact;
    
    public MobileRecordingListener(CommandExecutor commandExecutor, O1 startRecordingOpt, O2 stopRecordingOpt, TestArtifactType artifact) {
        this.commandExecutor = commandExecutor;
        this.startRecordingOpt = startRecordingOpt;
        this.stopRecordingOpt = stopRecordingOpt;
        this.artifact = artifact;
    }

    @Override
    public void beforeEvent(Command command) {
        if (recording && DriverCommand.QUIT.equals(command.getName())) {
            try {
                commandExecutor.execute(new Command(command.getSessionId(), 
                        MobileCommand.STOP_RECORDING_SCREEN, 
                        MobileCommand.stopRecordingScreenCommand((BaseStopScreenRecordingOptions) stopRecordingOpt).getValue()));

                if (ZafiraSingleton.INSTANCE.isRunning()) {
                    ZafiraSingleton.INSTANCE.getClient().addTestArtifact(artifact);
                }
            } catch (Exception e) {
                LOGGER.error("Unable to stop screen recording: " + e.getMessage(), e);
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
}
