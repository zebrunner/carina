/*******************************************************************************
 * Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.utils.ftp.FtpUtils;
import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

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

	protected static final Logger LOGGER = Logger.getLogger(MobileRecordingListener.class);

	private CommandExecutor commandExecutor;

	private O1 startRecordingOpt;

	private O2 stopRecordingOpt;

	private boolean recording = false;

	private TestArtifactType videoArtifact;

	public MobileRecordingListener(CommandExecutor commandExecutor, O1 startRecordingOpt, O2 stopRecordingOpt,
			TestArtifactType artifact) {
		this.commandExecutor = commandExecutor;
		this.startRecordingOpt = startRecordingOpt;
		this.stopRecordingOpt = stopRecordingOpt;
		this.videoArtifact = artifact;
	}

	@Override
	public void beforeEvent(Command command) {
		if (recording) {
			if (DriverCommand.QUIT.equals(command.getName())) {
				onBeforeEvent();
				try {
					String data = commandExecutor
							.execute(new Command(command.getSessionId(), MobileCommand.STOP_RECORDING_SCREEN,
									MobileCommand.stopRecordingScreenCommand(
											(BaseStopScreenRecordingOptions) stopRecordingOpt).getValue()))
							.getValue().toString();
					LOGGER.debug("Video will be uploaded to ftp. Test thread ID : " + Thread.currentThread().getId());
					CompletableFuture.runAsync(() -> {uploadToFTP(data);});
					if (ZafiraSingleton.INSTANCE.isRunning()) {
						ZafiraSingleton.INSTANCE.getClient().addTestArtifact(videoArtifact);
					}
				} catch (Throwable e) {
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
				commandExecutor.execute(new Command(command.getSessionId(), MobileCommand.START_RECORDING_SCREEN,
						MobileCommand.startRecordingScreenCommand((BaseStartScreenRecordingOptions) startRecordingOpt)
								.getValue()));
			} catch (Exception e) {
				LOGGER.error("Unable to start screen recording: " + e.getMessage(), e);
			}
		}
	}

	private void onBeforeEvent() {
		// 4a. if "tzid" not exist inside videoArtifact and exists in Reporter ->
		// register new videoArtifact in Zafira.
		// 4b. if "tzid" already exists in current artifact but in Reporter there is
		// another value. Then this is use case for class/suite mode when we share the
		// same
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

	// To get host address for video uploading we have to use screen_record_ftp parameter. 
	// To generate file name we have to extract it from video artifact link.
	private void uploadToFTP(String data) {
	    LOGGER.debug("Uploading in async mode started in thread ID : " + Thread.currentThread().getId());
	    LOGGER.debug("Link to video artifact : " + videoArtifact.getLink());
	    LOGGER.debug("Screen record ftp : " + R.CONFIG.get("screen_record_ftp"));
	    LOGGER.debug("Screen record host : " + R.CONFIG.get("screen_record_host"));
		String videoUrl = videoArtifact.getLink();
		String ftpUrl = R.CONFIG.get("screen_record_ftp").replace("%","");
		URI ftpUri = null;
		URI videoUri = null;
		try {
			ftpUri = new URI(ftpUrl);
			videoUri = new URI(videoUrl);
		} catch (URISyntaxException e1) {
			LOGGER.error("Incorrect URL format for screen record ftp parameter");
		}
		if (null != ftpUri && null != videoUri) {
			String ftpHost = ftpUri.getHost();
			String[] segments = videoUri.getPath().split("/");
			String destinationFileName = segments[segments.length-1];
			FtpUtils.uploadData(ftpHost, R.CONFIG.get("screen_record_user"), R.CONFIG.get("screen_record_pass"), data,
					destinationFileName);
		} else {
			LOGGER.error("The video won't be uploaded due to incorrect ftp or video recording parameters");
		}
	}
}
