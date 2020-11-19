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

    // boolean property to identify when artifact is ready for registration using valid sessionId
    private boolean inited = false;

    public ZebrunnerArtifactListener(TestArtifactType testArtifact) {
        this.testArtifact = testArtifact;
    }

    @Override
    public void beforeEvent(Command command) {
        // there is no way to register driver artifact after quit because some drivers may belong to different tests
        // i.e. we have to assign existing artifact at run-time to every unique test we've found
        if (inited) {
            registerArtifact(command, testArtifact);
        }
    }

    @Override
    public void afterEvent(Command command) {
        // all supported artifacts used sessionId to finalize valid value so we should wait a command when valid id is available
        if (command.getSessionId() == null) {
            return;
        }

        if (inited) {
            // no sense to update link because the same session already initialized
            return;
        }
        
        String sessionId = command.getSessionId().toString();
        if (sessionId.length() >= 64 ) {
            //use case with GoGridRouter so we have to cut first 32 symbols!
            sessionId = sessionId.substring(32);
        }

        // double %s replacement by session to support sessionId/sessionId.json metadata!
        testArtifact.setLink(String.format(testArtifact.getLink(), sessionId, sessionId));
        inited = true;
    }

}