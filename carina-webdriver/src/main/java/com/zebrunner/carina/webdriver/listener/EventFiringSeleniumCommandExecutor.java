/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
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
package com.zebrunner.carina.webdriver.listener;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.http.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.utils.Configuration;
import com.zebrunner.carina.utils.Configuration.Parameter;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.utils.common.CommonUtils;
import com.zebrunner.carina.utils.commons.SpecialKeywords;

/**
 * EventFiringSeleniumCommandExecutor triggers event listener before/after execution of the command.
 */
public class EventFiringSeleniumCommandExecutor extends HttpCommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public EventFiringSeleniumCommandExecutor(URL addressOfRemoteServer) {
        super(ClientConfig.defaultConfig()
                .baseUrl(addressOfRemoteServer)
                //todo reuse parameter from Configuration.Parameter class
                .readTimeout(Duration.ofSeconds(R.CONFIG.getLong("read_timeout"))));
    }

    @Override
    public Response execute(Command command) throws IOException {
        Response response = null;
        int retry = 2; // extra retries to execute command
        Number pause = Configuration.getInt(Parameter.EXPLICIT_TIMEOUT) / retry;
        while (retry >= 0) {
            response = super.execute(command);
            if (response.getValue() instanceof WebDriverException) {
                LOGGER.debug("CarinaCommandExecutor catched: " + response.getValue().toString());
                
                if (DriverCommand.QUIT.equals(command.getName())) {
                    // do not retry on quit command (grid will close it forcibly anyway)
                    break;
                }

                String msg = response.getValue().toString();
                if (msg.contains(SpecialKeywords.DRIVER_CONNECTION_REFUSED)
                        || msg.contains(SpecialKeywords.DRIVER_CONNECTION_REFUSED2)
                        || msg.contains(SpecialKeywords.DRIVER_TARGET_FRAME_DETACHED)) {
                    LOGGER.warn("Enabled command executor retries: " + msg);
                    CommonUtils.pause(pause);
                } else {
                    // do not retry for non "driver connection refused" errors!
                    break;
                }
            } else {
                // do nothing as response already contains all the information we need
                break;
            }
            retry--;
        }

        return response;
    }

}