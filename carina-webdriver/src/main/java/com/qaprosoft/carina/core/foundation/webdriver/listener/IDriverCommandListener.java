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
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.qaprosoft.zafira.client.ZafiraSingleton;
import com.qaprosoft.zafira.models.dto.TestArtifactType;

/**
 * IDriverEventListener - listens to {@link RemoteWebDriver} commands and injects additional steps.
 * 
 * @author akhursevich
 */
public interface IDriverCommandListener {
    static final Logger LOGGER = Logger.getLogger(IDriverCommandListener.class);

	/**
	 * Triggered before command execution.
	 * 
	 * @param command {@link Command}
	 */
	void beforeEvent(Command command);

	/**
	 * Triggered after command execution.
	 * 
	 * @param command {@link Command}
	 */
	void afterEvent(Command command);
	
    default public void registerVideoArtifact(Command command, TestArtifactType videoArtifact) {
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
                if (ZafiraSingleton.INSTANCE.isRunning()) {
                    ZafiraSingleton.INSTANCE.getClient().addTestArtifact(videoArtifact);
                }
            }
        }
    }
}
