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

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DriverCommand;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

/**
 * ScreenRecordingListener - starts/stops video recording for desktop drivers.
 * 
 * @author akhursevich
 */
public class DesktopRecordingListener implements IDriverCommandListener {

	private static final Logger LOGGER = Logger.getLogger(DesktopRecordingListener.class);
	
    private boolean recording = false;
    
    private TestArtifactType videoArtifact;
    
    public DesktopRecordingListener(TestArtifactType artifact) {
        this.videoArtifact = artifact;
    }

    @Override
    public void beforeEvent(Command command) {
    	if (recording) {
    		onBeforeEvent(); 
    		
            if (DriverCommand.QUIT.equals(command.getName())) {
                if (ZafiraSingleton.INSTANCE.isRunning()) {
                    ZafiraSingleton.INSTANCE.getClient().addTestArtifact(videoArtifact);
                }
            }
    	}
    }

    @Override
    public void afterEvent(Command command) {
        if (!recording && command.getSessionId() != null) {
            recording = true;
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
