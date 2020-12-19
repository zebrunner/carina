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
package com.qaprosoft.carina.core.foundation.webdriver.listener;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.ReportContext;

import io.appium.java_client.MobileCommand;
import io.appium.java_client.screenrecording.BaseStartScreenRecordingOptions;
import io.appium.java_client.screenrecording.BaseStopScreenRecordingOptions;

/**
 * ScreenRecordingListener - starts/stops video recording for Android and IOS
 * drivers.
 * 
 * @author akhursevich
 */
@SuppressWarnings({ "rawtypes" })
public class MobileRecordingListener<O1 extends BaseStartScreenRecordingOptions, O2 extends BaseStopScreenRecordingOptions>
		implements IDriverCommandListener {

    private static final Logger LOGGER = Logger.getLogger(MobileRecordingListener.class);

	private CommandExecutor commandExecutor;

	private O1 startRecordingOpt;

	private O2 stopRecordingOpt;

	private boolean recording = false;

	public MobileRecordingListener(CommandExecutor commandExecutor, O1 startRecordingOpt, O2 stopRecordingOpt) {
		this.commandExecutor = commandExecutor;
		this.startRecordingOpt = startRecordingOpt;
		this.stopRecordingOpt = stopRecordingOpt;
	}

	@Override
	public void beforeEvent(Command command) {
		if (recording) {
			if (DriverCommand.QUIT.equals(command.getName())) {
			    // stop video recording and publish it to local artifacts
			    String data = "";
                try {
                    LOGGER.debug("Stopping mobile video recording and upload data locally for " + command.getSessionId());
                    data = commandExecutor
                            .execute(new Command(command.getSessionId(), MobileCommand.STOP_RECORDING_SCREEN,
                                    MobileCommand.stopRecordingScreenCommand(
                                            (BaseStopScreenRecordingOptions) stopRecordingOpt).getValue()))
                            .getValue().toString();
                    
                    LOGGER.debug("Stopped mobile video recording and uploaded data locally for " + command.getSessionId());
                } catch (Throwable e) {
                    LOGGER.error("Unable to stop screen recording!", e);
                }
                
                if (data == null || data.isEmpty()) {
                    // do nothing
                    return;
                }
                
                // create file in artifacts using driver session id
                //IMPORTANT! DON'T MODIFY FILENAME WITHOUT UPDATING DRIVER FACTORIES AND LISTENERS!
                String fileName = String.format(SpecialKeywords.DEFAULT_VIDEO_FILENAME, command.getSessionId());
                String filePath = ReportContext.getArtifactsFolder().getAbsolutePath() + File.separator + fileName;
                File file = null;
                
                try {
                    LOGGER.debug("Saving video artifact: " + fileName);
                    file = new File(filePath);
                    FileUtils.writeByteArrayToFile(file, Base64.getDecoder().decode(data));
                    LOGGER.debug("Saved video artifact: " + fileName);
                } catch (IOException e) {
                    LOGGER.warn("Error has been occurred during video artifact generation: " + fileName, e);
                }
			}
		}
	}

    @Override
    public void afterEvent(Command command) {
        if (!recording && command.getSessionId() != null) {
            try {
                recording = true;
                
                commandExecutor.execute(new Command(command.getSessionId(), MobileCommand.START_RECORDING_SCREEN,
                        MobileCommand.startRecordingScreenCommand((BaseStartScreenRecordingOptions) startRecordingOpt)
                                .getValue()));
            } catch (Exception e) {
                LOGGER.error("Unable to start screen recording: " + e.getMessage(), e);
            }
        }
    }
    
}
