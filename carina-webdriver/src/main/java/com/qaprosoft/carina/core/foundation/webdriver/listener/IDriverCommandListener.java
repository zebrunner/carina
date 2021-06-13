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

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * IDriverEventListener - listens to {@link RemoteWebDriver} commands and injects additional steps.
 * 
 * @author akhursevich
 */
public interface IDriverCommandListener {
    static final Logger LISTENER_LOGGER = Logger.getLogger(IDriverCommandListener.class);

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
	
}
