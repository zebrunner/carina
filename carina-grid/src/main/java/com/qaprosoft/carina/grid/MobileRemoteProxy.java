/*
 * Copyright 2013-2017 QAPROSOFT (http://qaprosoft.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qaprosoft.carina.grid;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

import com.qaprosoft.carina.grid.integration.STF;

/**
 * Mobile proxy that connects/disconnects STF devices.
 * 
 * @author Alex Khursevich (alex@qaprosoft.com)
 */
public class MobileRemoteProxy extends DefaultRemoteProxy
{
	public MobileRemoteProxy(RegistrationRequest request, Registry registry) 
	{
		super(request, registry);
	}

	@Override
	public void beforeSession(TestSession session) 
	{
		super.beforeSession(session);
		if(STF.isRunning() && session.getSlot().getCapabilities().containsKey("udid") && !Platform.IOS.equals(Platform.fromCapabilities(session.getRequestedCapabilities())))
		{
			STF.reserveDevice(String.valueOf(session.getSlot().getCapabilities().get("udid")));
		}
	}

	@Override
	public void afterSession(TestSession session) 
	{
		super.afterSession(session);
		if(STF.isRunning() && session.getSlot().getCapabilities().containsKey("udid") && !Platform.IOS.equals(Platform.fromCapabilities(session.getRequestedCapabilities())))
		{
			STF.returnDevice(String.valueOf(session.getSlot().getCapabilities().get("udid")));
		}
	}
}