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

import org.openqa.selenium.remote.Command;

import com.qaprosoft.zafira.models.dto.TestArtifactType;

/**
 * ZebrunnerArtifactListener - saves artifact link in Zebrunner Reporting.
 * 
 * @author akhursevich
 */
public class ZebrunnerArtifactListener implements IDriverCommandListener {

    private TestArtifactType testArtifact;

    private boolean inited = false; //boolean property to wait until valid driver sessionId appear
    private boolean registered = false; //boolean property to minimize number of calls to reporting

    public ZebrunnerArtifactListener(TestArtifactType testArtifact) {
        this.testArtifact = testArtifact;
    }

    @Override
    public void beforeEvent(Command command) {
        if (inited && !registered) {
            registerArtifact(command, testArtifact);
            registered = true;
        }
    }

    @Override
    public void afterEvent(Command command) {
        if (!inited && command.getSessionId() != null) {
            // double %s replacement by session to support sessionId/sessionId.json metadata!
            testArtifact.setLink(String.format(testArtifact.getLink(), command.getSessionId().toString(), command.getSessionId().toString()));
            inited = true;
        }
    }

}