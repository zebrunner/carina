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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.Response;

import io.appium.java_client.MobileCommand;
import io.appium.java_client.remote.AppiumCommandExecutor;

/**
 * EventFiringCommandExecutor triggers event listener before/after execution of the command.
 * 
 * @author akhursevich
 */
public class EventFiringAppiumCommandExecutor extends AppiumCommandExecutor {
	
	private List<IDriverCommandListener> listeners = new ArrayList<>();

	public EventFiringAppiumCommandExecutor(URL remoteURL) {
        super(MobileCommand.commandRepository, remoteURL);
    }
	
	public EventFiringAppiumCommandExecutor(URL remoteURL, List<IDriverCommandListener> listeners) {
		super(MobileCommand.commandRepository, remoteURL);
		this.listeners = listeners;
	}

	@Override
	public Response execute(Command command) {
		
		for(IDriverCommandListener listener : listeners) {
			listener.beforeEvent(command);
		}
		
		Response response = super.execute(command);
		
		for(IDriverCommandListener listener : listeners) {
			listener.afterEvent(command);
		}
		
		return response;
	}

    public List<IDriverCommandListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<IDriverCommandListener> listeners) {
        this.listeners = listeners;
    }
}